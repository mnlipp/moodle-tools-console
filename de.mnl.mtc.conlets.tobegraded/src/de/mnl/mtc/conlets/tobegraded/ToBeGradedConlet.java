/*
 * Moodle Tools Console
 * Copyright (C) 2022 Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.mnl.mtc.conlets.tobegraded;

import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.model.MoodleAssignment;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleGrouping;
import de.mnl.moodle.service.model.MoodleUser;
import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.jgrapes.webconsole.base.ConsoleSession;
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
@SuppressWarnings({ "PMD.DataflowAnomalyAnalysis",
    "PMD.DataflowAnomalyAnalysis" })
public class ToBeGradedConlet
        extends FreeMarkerConlet<ConletBaseModel> {

    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final Logger logger
        = LoggerFactory.getLogger(ToBeGradedConlet.class);

    private static final Set<RenderMode> MODES
        = RenderMode.asSet(RenderMode.Preview);

    /**
     * Creates a new component with its channel set to the given channel.
     * 
     * @param componentChannel the channel that the component's handlers listen
     *            on by default and that {@link Manager#fire(Event, Channel...)}
     *            sends the event to
     */
    public ToBeGradedConlet(Channel componentChannel) {
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
    public void onConsoleReady(ConsoleReady event, ConsoleSession channel)
            throws TemplateNotFoundException, MalformedTemplateNameException,
            ParseException, IOException {
        // Add conlet resources to page
        channel.respond(new AddConletType(type())
            .addRenderMode(RenderMode.Preview).setDisplayNames(
                localizations(channel.supportedLocales(), "conletName"))
            .addScript(new ScriptResource()
                .setScriptUri(event.renderSupport().conletResource(
                    type(), "ToBeGraded-functions.js"))
                .setScriptType("module"))
            .addCss(event.renderSupport(),
                WebConsoleUtils.uriFromPath("ToBeGraded-style.css")));
    }

    @Override
    protected String generateInstanceId(AddConletRequest event,
            ConsoleSession session) {
        return "Singleton";
    }

    @Override
    protected Optional<ConletBaseModel> createNewState(AddConletRequest event,
            ConsoleSession session, String conletId) throws Exception {
        return Optional
            .ofNullable(stateFromSession(session.browserSession(), conletId)
                .orElse(super.createNewState(event, session, conletId).get()));
    }

    @Override
    protected Optional<ConletBaseModel> createStateRepresentation(
            RenderConletRequestBase<?> event,
            ConsoleSession channel, String conletId) throws IOException {
        return Optional.of(new ConletBaseModel(conletId));
    }

    @Override
    protected Set<RenderMode> doRenderConlet(RenderConletRequestBase<?> event,
            ConsoleSession consoleSession, String conletId,
            ConletBaseModel conletState) throws Exception {
        Set<RenderMode> renderedAs = new HashSet<>(event.renderAs());
        if (event.renderAs().contains(RenderMode.Preview)) {
            Template tpl
                = freemarkerConfig().getTemplate("ToBeGraded-preview.ftl.html");
            consoleSession.respond(new RenderConlet(type(), conletId,
                processTemplate(event, tpl,
                    fmModel(event, consoleSession, conletId, conletState)))
                        .setRenderAs(
                            RenderMode.Preview.addModifiers(event.renderAs()))
                        .setSupportedModes(MODES));
            renderedAs.add(RenderMode.Preview);
            sendPreviewData(consoleSession, conletState);
        }
        return renderedAs;
    }

    @SuppressWarnings({ "PMD.GuardLogStatement",
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveMethodLength",
        "PMD.CognitiveComplexity", "PMD.NcssCount" })
    private void sendPreviewData(ConsoleSession channel,
            ConletBaseModel model) {
        var bundle = resourceBundle(channel.locale());
        channel.respond(new NotifyConletView(type(),
            model.getConletId(), "setMessage", bundle.getString("Loading")));
        Optional<MoodleClient> moodleClient
            = channel.associated(MoodleClient.class);
        if (moodleClient.isEmpty()) {
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
        try {
            MoodleClient client = moodleClient.get();
            MoodleCourse[] courses = client.enrolledIn();

//            try {
//                var grpgs = client.invoke("mod_assign_get_assignments",
//                    Map.of("courseids",
//                        Stream.of(courses).map(MoodleCourse::getId)
//                            .collect(Collectors.toList())));
//                grpgs = null;
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }

            String processingFormat = bundle.getString("ProcessingCourse");
            for (var course : client.withAssignments(courses,
                "mod/assign:grade")) {
                // The rest is going to take some time
                channel.respond(new NotifyConletView(type(),
                    model.getConletId(), "setMessage",
                    String.format(processingFormat, course.getShortName())));

                // We have a course with assignments gradable by current user.
                // Check if the assignments have submissions within the
                // last 2 months
                var assignmentsToCheck = client.withSubmissions(
                    course.getAssignments(), "submitted",
                    Instant.now().minus(Duration.of(60, ChronoUnit.DAYS)),
                    null);
                if (assignmentsToCheck.length == 0) {
                    continue;
                }

                // Get current user's (grading) grouping
                MoodleGrouping myGrouping = Stream.of(client.groupings(course))
                    .filter(g -> g.getName()
                        .endsWith(client.siteInfo().getLastname()))
                    .findFirst().orElse(null);
                if (myGrouping == null) {
                    continue;
                }

                // Get the users in groups in my grading grouping
                var myUsers = new HashMap<Long, MoodleUser>();
                for (var user : client.enrolled(course)) {
                    // Handle users in my grouping
                    if (client.usersGroupsInGrouping(course, user,
                        myGrouping).length == 0) {
                        continue;
                    }
                    myUsers.put(user.getId(), user);
                }
                if (myUsers.isEmpty()) {
                    continue;
                }

                Map<MoodleCourse,
                        Map<MoodleAssignment,
                                Set<MoodleUser>>> toBeGradedAssignments
                                    = new HashMap<>();
                for (var assignment : assignmentsToCheck) {
                    for (var submission : assignment.getSubmissions()) {
                        if (!myUsers.containsKey(submission.getUserid())) {
                            continue;
                        }
                        if (!"graded".equals(submission.getGradingstatus())) {
                            toBeGradedAssignments
                                .computeIfAbsent(course, c -> new HashMap<>())
                                .computeIfAbsent(assignment,
                                    a -> new HashSet<>())
                                .add(myUsers.get(submission.getUserid()));
                        }
                    }
                }
                System.out.println(toBeGradedAssignments);
            }
//            channel.respond(new NotifyConletView(type(),
//                model.getConletId(), "setPreviewData", data));
        } catch (

        IOException e) {
            logger.debug(e.getMessage(), e);
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
    }

    @Override
    protected boolean doSetLocale(SetLocale event, ConsoleSession channel,
            String conletId) throws Exception {
        return true;
    }

}
