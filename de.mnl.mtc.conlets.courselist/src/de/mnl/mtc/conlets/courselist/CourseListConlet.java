/*
 * Moodle Tools Console
 * Copyright (C) 2022 Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.mnl.mtc.conlets.courselist;

import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Event;
import org.jgrapes.core.Manager;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.webconsole.base.Conlet.RenderMode;
import org.jgrapes.webconsole.base.ConletBaseModel;
import org.jgrapes.webconsole.base.ConsoleConnection;
import org.jgrapes.webconsole.base.WebConsoleUtils;
import org.jgrapes.webconsole.base.events.AddConletRequest;
import org.jgrapes.webconsole.base.events.AddConletType;
import org.jgrapes.webconsole.base.events.AddPageResources.ScriptResource;
import org.jgrapes.webconsole.base.events.ConsoleReady;
import org.jgrapes.webconsole.base.events.NotifyConletView;
import org.jgrapes.webconsole.base.events.RenderConlet;
import org.jgrapes.webconsole.base.events.RenderConletRequestBase;
import org.jgrapes.webconsole.base.events.ResourceNotAvailable;
import org.jgrapes.webconsole.base.events.SetLocale;
import org.jgrapes.webconsole.base.freemarker.FreeMarkerConlet;

/**
 * A conlet for listing courses.
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class CourseListConlet extends FreeMarkerConlet<ConletBaseModel> {

    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final Logger logger
        = LoggerFactory.getLogger(CourseListConlet.class);

    private static final Set<RenderMode> MODES
        = RenderMode.asSet(RenderMode.Preview);

    /**
     * Creates a new component with its channel set to the given channel.
     * 
     * @param componentChannel the channel that the component's handlers listen
     *            on by default and that {@link Manager#fire(Event, Channel...)}
     *            sends the event to
     */
    public CourseListConlet(Channel componentChannel) {
        super(componentChannel);
    }

    /**
     * Register conlet.
     *
     * @param event the event
     * @param channel the channel
     * @throws TemplateNotFoundException the template not found exception
     * @throws MalformedTemplateNameException the malformed template name exception
     * @throws ParseException the parse exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Handler
    public void onConsoleReady(ConsoleReady event, ConsoleConnection channel)
            throws TemplateNotFoundException, MalformedTemplateNameException,
            ParseException, IOException {
        // Add conlet resources to page
        channel.respond(new AddConletType(type())
            .addRenderMode(RenderMode.Preview).setDisplayNames(
                localizations(channel.supportedLocales(), "conletName"))
            .addScript(new ScriptResource()
                .setScriptUri(event.renderSupport().conletResource(
                    type(), "CourseList-functions.js"))
                .setScriptType("module"))
            .addCss(event.renderSupport(),
                WebConsoleUtils.uriFromPath("CourseList-style.css")));
    }

    @Override
    protected String generateInstanceId(AddConletRequest event,
            ConsoleConnection channel) {
        return "Singleton";
    }

    @Override
    protected Optional<ConletBaseModel> createNewState(AddConletRequest event,
            ConsoleConnection channel, String conletId) throws Exception {
        return Optional
            .ofNullable(stateFromSession(channel.session(), conletId)
                .orElse(super.createNewState(event, channel, conletId).get()));
    }

    @Override
    protected Optional<ConletBaseModel> createStateRepresentation(
            Event<?> event, ConsoleConnection channel, String conletId)
            throws IOException {
        return Optional.of(new ConletBaseModel(conletId));
    }

    @Override
    protected Set<RenderMode> doRenderConlet(RenderConletRequestBase<?> event,
            ConsoleConnection channel, String conletId,
            ConletBaseModel conletState) throws Exception {
        Set<RenderMode> renderedAs = new HashSet<>(event.renderAs());
        if (event.renderAs().contains(RenderMode.Preview)) {
            Template tpl
                = freemarkerConfig().getTemplate("CourseList-preview.ftl.html");
            channel.respond(new RenderConlet(type(), conletId,
                processTemplate(event, tpl,
                    fmModel(event, channel, conletId, conletState)))
                        .setRenderAs(
                            RenderMode.Preview.addModifiers(event.renderAs()))
                        .setSupportedModes(MODES));
            renderedAs.add(RenderMode.Preview);
            sendCourseList(channel, conletState);
        }
        return renderedAs;
    }

    @SuppressWarnings({ "PMD.CognitiveComplexity", "PMD.NcssCount",
        "PMD.NPathComplexity", "PMD.GuardLogStatement" })
    private void sendCourseList(ConsoleConnection channel,
            ConletBaseModel model) {
        var bundle = resourceBundle(channel.locale());
        channel.respond(new NotifyConletView(type(),
            model.getConletId(), "setMessage", bundle.getString("Loading")));
        MoodleClient client = (MoodleClient) channel.session()
            .transientData().get(MoodleClient.class);
        if (client == null) {
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
        try {
            MoodleCourse[] courses = client.enrolledIn();
            var data
                = Stream.of(courses).sorted(new Comparator<MoodleCourse>() {
                    @Override
                    public int compare(MoodleCourse crs1, MoodleCourse crs2) {
                        if (crs1.startDate().isPresent()
                            && crs2.startDate().isEmpty()) {
                            return -1;
                        }
                        if (crs1.startDate().isEmpty()
                            && crs2.startDate().isPresent()) {
                            return 1;
                        }
                        if (crs1.startDate().isEmpty()
                            && crs2.startDate().isEmpty()
                            || Duration.between(crs1.startDate().get(),
                                crs2.startDate().get()).abs().toDays() <= 10) {
                            return crs1.getShortName()
                                .compareTo(crs2.getShortName());
                        }
                        return crs2.startDate().get()
                            .compareTo(crs1.startDate().get());
                    }
                }).map(course -> {
                    var mapped = new HashMap<String, Object>();
                    mapped.put("shortName", course.getShortName());
                    mapped.put("url", client.courseUri(course).toString());
                    mapped.put("startDate", course.startDate());
                    return mapped;
                }).collect(Collectors.toList());
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setCourses", data));
        } catch (IOException e) {
            logger.debug(e.getMessage(), e);
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
    }

    @Override
    protected boolean doSetLocale(SetLocale event, ConsoleConnection channel,
            String conletId) throws Exception {
        return true;
    }

}
