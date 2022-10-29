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

package de.mnl.mtc.conlets.senderinfo;

import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.Subject;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Event;
import org.jgrapes.core.Manager;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.io.events.Close;
import org.jgrapes.mail.InternetAddressPrincipal;
import org.jgrapes.mail.MailChannel;
import org.jgrapes.mail.events.FoldersUpdated;
import org.jgrapes.mail.events.MailMonitorOpened;
import org.jgrapes.mail.events.OpenMailMonitor;
import org.jgrapes.mail.events.UpdateFolders;
import org.jgrapes.util.Password;
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
 * A conlet for obtaining info related to mail senders.
 */
@SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "PMD.ExcessiveImports" })
public class SenderInfoConlet
        extends FreeMarkerConlet<SenderInfoConlet.SenderInfoModel> {

    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final Logger logger
        = LoggerFactory.getLogger(SenderInfoConlet.class);

    private static final String MAIL_CON_REQUESTED
        = SenderInfoConlet.class.getName() + "_Requested";
    private static final Set<RenderMode> MODES
        = RenderMode.asSet(RenderMode.Preview);

    /**
     * Creates a new component with its channel set to the given channel.
     * 
     * @param componentChannel the channel that the component's handlers listen
     *            on by default and that {@link Manager#fire(Event, Channel...)}
     *            sends the event to
     */
    public SenderInfoConlet(Channel componentChannel) {
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
                    type(), "Conlet-functions.js"))
                .setScriptType("module"))
            .addCss(event.renderSupport(),
                WebConsoleUtils.uriFromPath("Conlet-style.css")));
    }

    @Override
    protected String generateInstanceId(AddConletRequest event,
            ConsoleConnection channel) {
        if (event.renderAs().contains(RenderMode.View)) {
            @SuppressWarnings("PMD.AvoidDuplicateLiterals")
            String address = (String) event.properties().get("address");
            if (address != null) {
                return address;
            }
        }
        return "Singleton";
    }

    @Override
    protected Optional<SenderInfoModel> createNewState(AddConletRequest event,
            ConsoleConnection channel, String conletId) throws Exception {
        var state = stateFromSession(channel.session(), conletId);
        if (state.isPresent()) {
            return state;
        }
        if (event.renderAs().contains(RenderMode.View)
            && event.properties().containsKey("address")) {
            return Optional.of(new SenderInfoModel(conletId,
                (String) event.properties().get("address")));
        }
        return Optional.of(new SenderInfoModel(conletId));
    }

    @Override
    protected Optional<SenderInfoModel> createStateRepresentation(
            RenderConletRequestBase<?> event,
            ConsoleConnection channel, String conletId) throws IOException {
        return Optional.of(new SenderInfoModel(conletId));
    }

    @Override
    protected Set<RenderMode> doRenderConlet(RenderConletRequestBase<?> event,
            ConsoleConnection channel, String conletId, SenderInfoModel model)
            throws Exception {
        var bundle = resourceBundle(channel.locale());
        Set<RenderMode> renderedAs = new HashSet<>(event.renderAs());
        if (event.renderAs().contains(RenderMode.Preview)) {
            Template tpl
                = freemarkerConfig().getTemplate("Conlet-preview.ftl.html");
            channel.respond(new RenderConlet(type(), conletId,
                processTemplate(event, tpl,
                    fmModel(event, channel, conletId, model))).setRenderAs(
                        RenderMode.Preview.addModifiers(event.renderAs()))
                        .setSupportedModes(MODES));
            renderedAs.add(RenderMode.Preview);
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessage",
                bundle.getString("LoadingMails")));
            refreshMailConnection(channel);
        }
        if (event.renderAs().contains(RenderMode.View)) {
            if (model.getEmailAddress() == null) {
                // Left over, remove
                removeState(channel.session(), conletId);
                return renderedAs;
            }
            Template tpl
                = freemarkerConfig().getTemplate("Conlet-view.ftl.html");
            var fmModel = fmModel(event, channel, conletId, model);
            fmModel.put("emailAddress", model.getEmailAddress());
            channel.respond(new RenderConlet(type(), conletId,
                processTemplate(event, tpl, fmModel)).setRenderAs(
                    RenderMode.View.addModifiers(event.renderAs()))
                    .setSupportedModes(MODES));
            renderedAs.add(RenderMode.View);
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessage",
                bundle.getString("LoadingInfos")));
            sendUserInfo(channel, model);
        }
        return renderedAs;
    }

    @SuppressWarnings({ "unchecked" })
    private void refreshMailConnection(ConsoleConnection channel) {
        var data = channel.session().transientData();
        if ((boolean) data.getOrDefault(MAIL_CON_REQUESTED, false)) {
            // Get fresh data if connection to mail monitor exists
            Optional.of(data.get(MailChannel.class))
                .ifPresent(c -> fire(new UpdateFolders("INBOX"),
                    ((CloseableWrapper<MailChannel>) c).get()));
        } else {
            // Open connection to mail monitor
            var subject = Optional
                .ofNullable((Subject) channel.session().get(Subject.class));
            var internetAddress = subject
                .flatMap(s -> s.getPrincipals(InternetAddressPrincipal.class)
                    .stream().findFirst());
            var password = subject
                .flatMap(s -> s.getPrivateCredentials(Password.class)
                    .stream().findFirst());
            if (internetAddress.isPresent() && password.isPresent()) {
                fire(new OpenMailMonitor().setMailProperty("mail.user",
                    internetAddress.get().getName()).setPassword(password.get())
                    .setAssociated(this, channel));
                channel.session().transientData().put(MAIL_CON_REQUESTED, true);
            }
        }
    }

    @Override
    protected boolean doSetLocale(SetLocale event, ConsoleConnection channel,
            String conletId) throws Exception {
        return true;
    }

    /**
     * On mail monitor opened.
     *
     * @param event the event
     * @param channel the mail channel
     */
    @Handler
    public void onMailMonitorOpened(MailMonitorOpened event,
            MailChannel mailChannel) {
        event.openEvent().associated(this, ConsoleConnection.class)
            .ifPresent(cc -> {
                cc.session().transientData().put(MailChannel.class,
                    new CloseableWrapper<>(mailChannel));
                mailChannel.setAssociated(this, cc);
            });
    }

    /**
     * On messages retrieved.
     *
     * @param event the event
     * @param channel the channel
     * @throws MessagingException 
     */
    @Handler
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void onFoldersUpdated(FoldersUpdated event, MailChannel channel)
            throws MessagingException {
        var data = new LinkedList<Map<String, Object>>();
        Optional<Folder> inbox = event.folders().stream()
            .filter(f -> "INBOX".equals(f.getFullName())).findFirst();
        if (inbox.isPresent()) {
            var msgs = FoldersUpdated.messages(inbox.get(), 10);
            for (var msg : msgs) {
                if (!(msg.getFrom()[0] instanceof InternetAddress)) {
                    continue;
                }
                var from = (InternetAddress) msg.getFrom()[0];
                data.add(Map.of("from", from.toUnicodeString(),
                    "address", from.getAddress()));
            }
        }
        channel.associated(this, ConsoleConnection.class).ifPresent(cc -> {
            for (var conletId : conletIds(cc)) {
                cc.respond(new NotifyConletView(type(),
                    conletId, "setItems", data));
            }
        });
    }

    private void sendUserInfo(ConsoleConnection channel,
            SenderInfoModel model) {
        MoodleClient client = (MoodleClient) channel.session()
            .transientData().get(MoodleClient.class);
        if (client == null) {
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
        var bundle = resourceBundle(channel.locale());
        try {
            var user = client.userByEmail(model.getEmailAddress());
            if (user.isEmpty()) {
                channel.respond(new NotifyConletView(type(),
                    model.getConletId(), "setMessage",
                    bundle.getString("NotMoodleUser")));
                return;
            }
            @SuppressWarnings("PMD.UseConcurrentHashMap")
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("lastName", user.get().getLastname());
            userInfo.put("firstName", user.get().getFirstname());
            var courses = client.courses(user.get());
            var courseInfos = courseInfos(client, courses);
            userInfo.put("courses", courseInfos);
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setUserInfo", userInfo));
        } catch (IOException e) {
            logger.debug(() -> e.getMessage(), e);
            channel.respond(new ResourceNotAvailable(MoodleClient.class));
            return;
        }
    }

    @SuppressWarnings("PMD.UseVarargs")
    private List<Map<String, String>> courseInfos(MoodleClient client,
            MoodleCourse[] courses) {
        return Stream.of(courses).sorted(new Comparator<MoodleCourse>() {
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
            return Map.of("name", course.getDisplayname(),
                "url", client.courseUri(course).toString());
        }).collect(Collectors.toList());
    }

    /**
     * The Class CloseableWrapper.
     *
     * @param <C> the generic type
     */
    private final class CloseableWrapper<C extends Channel>
            implements AutoCloseable {
        private final C channel;

        private CloseableWrapper(C channel) {
            this.channel = channel;
        }

        /**
         * Returns the.
         *
         * @return the c
         */
        public C get() {
            return channel;
        }

        @Override
        public void close() throws Exception {
            fire(new Close(), channel);
        }
    }

    /**
     * Model with no additional info.
     */
    public static class SenderInfoModel extends ConletBaseModel {

        private String emailAddress;

        /**
         * Creates a new model with the given type and id.
         * 
         * @param conletId the web console component id
         */
        @ConstructorProperties({ "conletId" })
        public SenderInfoModel(String conletId) {
            super(conletId);
        }

        /**
         * Instantiates a new sender info model. Version with 
         * address is only used by view.
         *
         * @param conletId the conlet id
         * @param address the address
         */
        public SenderInfoModel(String conletId, String address) {
            super(conletId);
            emailAddress = address;
        }

        /**
         * Gets the email address.
         *
         * @return the email address
         */
        public String getEmailAddress() {
            return emailAddress;
        }
    }

}
