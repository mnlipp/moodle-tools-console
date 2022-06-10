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
import de.mnl.moodle.service.model.MoodleCourseSection;
import de.mnl.moodle.service.model.MoodleGroup;
import de.mnl.moodle.service.model.MoodleGrouping;
import de.mnl.moodle.service.model.MoodleModule;
import de.mnl.moodle.service.model.MoodleSubmission;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
            ConsoleSession channel, String conletId,
            ConletBaseModel model) throws Exception {
        Set<RenderMode> renderedAs = new HashSet<>(event.renderAs());
        if (event.renderAs().contains(RenderMode.Preview)) {
            Template tpl
                = freemarkerConfig().getTemplate("ToBeGraded-preview.ftl.html");
            channel.respond(new RenderConlet(type(), conletId,
                processTemplate(event, tpl,
                    fmModel(event, channel, conletId, model)))
                        .setRenderAs(
                            RenderMode.Preview.addModifiers(event.renderAs()))
                        .setSupportedModes(MODES));
            renderedAs.add(RenderMode.Preview);
            var bundle = resourceBundle(channel.locale());
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessage",
                bundle.getString("Loading")));
            activeEventPipeline().executorService().execute(new Runnable() {
                @Override
                public void run() {
                    sendPreviewData(channel, model);
                }
            });
        }
        return renderedAs;
    }

    @SuppressWarnings({ "PMD.GuardLogStatement",
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveMethodLength",
        "PMD.CognitiveComplexity", "PMD.NcssCount",
        "PMD.AvoidLiteralsInIfCondition", "PMD.NPathComplexity" })
    private void sendPreviewData(ConsoleSession channel,
            ConletBaseModel model) {
        Optional<MoodleClient> moodleClient
            = channel.associated(MoodleClient.class);
        if (moodleClient.isEmpty()) {
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
        var bundle = resourceBundle(channel.locale());
        try {
            @SuppressWarnings({ "PMD.UseConcurrentHashMap",
                "PMD.AvoidDuplicateLiterals" })
            Map<Long, Map<String, Object>> data = new HashMap<>();
            MoodleClient client = moodleClient.get();
            MoodleCourse[] courses = client.enrolledIn();
            String processingFormat = bundle.getString("ProcessingCourse");
            for (var course : client.withAssignments(courses,
                "mod/assign:grade")) {
                // The rest is going to take some time
                channel.respond(new NotifyConletView(type(),
                    model.getConletId(), "setMessage",
                    String.format(processingFormat, course.getShortName())));

                // Filter ungraded assignments
                var assignmentsToCheck = Stream.of(course.getAssignments())
                    .filter(a -> a.getGrade() != 0)
                    .toArray(s -> new MoodleAssignment[s]);

                // Check if the assignments have submissions within the
                // last 2 months
                assignmentsToCheck = client.withSubmissions(
                    assignmentsToCheck, "submitted",
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
                @SuppressWarnings("PMD.UseConcurrentHashMap")
                Map<MoodleUser, MoodleGroup[]> groupsOfUser = new HashMap<>();
                for (var user : client.enrolled(course)) {
                    // Handle users in my grouping
                    var groups = client.usersGroupsInGrouping(course, user,
                        myGrouping);
                    if (groups.length == 0) {
                        continue;
                    }
                    groupsOfUser.put(user, groups);
                    myUsers.put(user.getId(), user);
                }
                if (myUsers.isEmpty()) {
                    continue;
                }
                for (var assignment : assignmentsToCheck) {
                    for (var submission : assignment.getSubmissions()) {
                        if (!myUsers.containsKey(submission.getUserid())) {
                            continue;
                        }
                        if (!"graded".equals(submission.getGradingstatus())) {
                            if (course.getContents() == null) {
                                client.withContents(course, true, "assign");
                            }
                            MoodleUser user
                                = myUsers.get(submission.getUserid());
                            addUngraded(client, data, course, assignment,
                                submission, user, groupsOfUser.get(user));
                        }
                    }
                }
            }
            var displayData = compact(data);
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setPreviewData", displayData));
        } catch (IOException e) {
            logger.debug(e.getMessage(), e);
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
    }

    @SuppressWarnings({ "unchecked", "PMD.UnusedFormalParameter",
        "PMD.UseVarargs" })
    private void addUngraded(MoodleClient client,
            Map<Long, Map<String, Object>> data, MoodleCourse course,
            MoodleAssignment assignment, MoodleSubmission submission,
            MoodleUser user, MoodleGroup[] groups) {
        // Course data is collected by id
        @SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
            "PMD.UseConcurrentHashMap" })
        Map<String, Object> courseData = data.computeIfAbsent(
            course.getId(), k -> new HashMap<String, Object>(
                Map.of("url", client.courseUri(course).toString(),
                    "shortname", course.getShortName(),
                    "fullname", course.getFullname(),
                    "assignments", new HashMap<Long, Object>())));
        var assignmentData
            = ((Map<Long, Map<String, Object>>) courseData.get("assignments"))
                .computeIfAbsent(assignment.getId(),
                    k -> {
                        @SuppressWarnings("PMD.UseConcurrentHashMap")
                        Map<String, Object> res = new HashMap<>(Map.of(
                            "id", assignment.getId(),
                            "name", assignment.getName(),
                            "users", new ArrayList<Map<String, Object>>()));
                        Stream.of(course.getContents())
                            .map(MoodleCourseSection::getModules)
                            .flatMap(m -> Stream.of(m))
                            .filter(m -> m.getInstance() == assignment.getId())
                            .findFirst().map(MoodleModule::getUrl)
                            .ifPresent(url -> res.put("url", url));
                        return res;
                    });
        ((List<Map<String, Object>>) assignmentData.get("users"))
            .add(Map.of("id", user.getId(),
                "email", user.getEmail(),
                "fullname", user.getFullname(),
                "groups", Stream.of(groups).map(g -> Map.of("name", g.getName(),
                    "id", g.getId())).collect(Collectors.toList())));
    }

    @SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "unchecked" })
    private List<Map<String, Object>>
            compact(Map<Long, Map<String, Object>> data) {
        List<Map<String, Object>> displayData = new ArrayList<>();
        for (var course : data.values()) {
            displayData.add(course);
            course.put("assignments",
                ((Map<String, Object>) course.get("assignments")).values());
        }
        return displayData;
    }

    @Override
    protected boolean doSetLocale(SetLocale event, ConsoleSession channel,
            String conletId) throws Exception {
        return true;
    }

}
