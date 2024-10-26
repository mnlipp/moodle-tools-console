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

package de.mnl.mtc.conlets.login;

import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.MoodleService;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.security.auth.Subject;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Event;
import org.jgrapes.core.Manager;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.http.events.DiscardSession;
import org.jgrapes.io.events.Close;
import org.jgrapes.mail.InternetAddressPrincipal;
import org.jgrapes.util.Password;
import org.jgrapes.util.events.ConfigurationUpdate;
import org.jgrapes.webconsole.base.Conlet.RenderMode;
import org.jgrapes.webconsole.base.ConletBaseModel;
import org.jgrapes.webconsole.base.ConsoleConnection;
import org.jgrapes.webconsole.base.ConsoleUser;
import org.jgrapes.webconsole.base.WebConsoleUtils;
import org.jgrapes.webconsole.base.events.AddConletRequest;
import org.jgrapes.webconsole.base.events.AddConletType;
import org.jgrapes.webconsole.base.events.AddPageResources.ScriptResource;
import org.jgrapes.webconsole.base.events.CloseModalDialog;
import org.jgrapes.webconsole.base.events.ConsolePrepared;
import org.jgrapes.webconsole.base.events.ConsoleReady;
import org.jgrapes.webconsole.base.events.DisplayNotification;
import org.jgrapes.webconsole.base.events.NotifyConletModel;
import org.jgrapes.webconsole.base.events.NotifyConletView;
import org.jgrapes.webconsole.base.events.OpenModalDialog;
import org.jgrapes.webconsole.base.events.RenderConlet;
import org.jgrapes.webconsole.base.events.RenderConletRequestBase;
import org.jgrapes.webconsole.base.events.ResourceNotAvailable;
import org.jgrapes.webconsole.base.events.SetLocale;
import org.jgrapes.webconsole.base.events.SimpleConsoleCommand;
import org.jgrapes.webconsole.base.events.UserAuthenticated;
import org.jgrapes.webconsole.base.freemarker.FreeMarkerConlet;

/**
 * A conlet for poll administration.
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class LoginConlet extends FreeMarkerConlet<LoginConlet.AccountModel> {

    private static final String PENDING_CONSOLE_PREPARED
        = "pendingConsolePrepared";
    private String moodleServer;
    private final MoodleService moodleService;

    /**
     * Creates a new component with its channel set to the given channel.
     * 
     * @param componentChannel the channel that the component's handlers listen
     *            on by default and that {@link Manager#fire(Event, Channel...)}
     *            sends the event to
     * @param moodleService used to create connections to moodle
     */
    public LoginConlet(Channel componentChannel, MoodleService moodleService) {
        super(componentChannel);
        this.moodleService = moodleService;
    }

    /**
     * Returns the preconfigured moodle server.
     *
     * @return the moodleServer
     */
    public String moodleServer() {
        return moodleServer;
    }

    /**
     * Preconfigures a moodle server. The respective input field
     * is omitted from the login dialog and the preconfigured
     * value is used.
     *
     * @param moodleServer the moodle server to configure
     */
    public void setMoodleServer(String moodleServer) {
        this.moodleServer = moodleServer;
    }

    /**
     * The component can be configured with events that include a path 
     * (see @link {@link ConfigurationUpdate#paths()}) that matches 
     * this components path (see {@link Manager#componentPath()}).
     * 
     * The following properties are recognized:
     * 
     * `moodleServer`
     * : Invokes {@link #setMoodleServer(String)} with the given values.
     * 
     * @param event the event
     */
    @Handler
    public void onConfigUpdate(ConfigurationUpdate event) {
        event.value(componentPath(), "moodleServer")
            .ifPresent(this::setMoodleServer);
    }

    @Override
    protected String generateInstanceId(AddConletRequest event,
            ConsoleConnection channel) {
        return "Singleton";
    }

    @Override
    protected Optional<AccountModel> createStateRepresentation(Event<?> event,
            ConsoleConnection channel, String conletId) throws IOException {
        return Optional.of(new AccountModel(conletId));
    }

    /**
     * As a model has already been created in {@link #doUpdateConletState},
     * the "new" model may already exist in the session.
     */
    @Override
    protected Optional<AccountModel> createNewState(AddConletRequest event,
            ConsoleConnection channel, String conletId) throws Exception {
        Optional<AccountModel> model
            = stateFromSession(channel.session(), conletId);
        if (model.isPresent()) {
            return model;
        }
        return super.createNewState(event, channel, conletId);
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
            .addScript(new ScriptResource()
                .setScriptUri(event.renderSupport().conletResource(
                    type(), "Login-functions.js"))
                .setScriptType("module"))
            .addCss(event.renderSupport(), WebConsoleUtils.uriFromPath(
                "Login-style.css"))
            .addPageContent("headerIcons", Map.of("priority", "1000"))
            .addRenderMode(RenderMode.Content));
    }

    /**
     * Handle web console page loaded.
     *
     * @param event the event
     * @param channel the channel
     * @throws IOException 
     * @throws ParseException 
     * @throws MalformedTemplateNameException 
     * @throws TemplateNotFoundException 
     */
    @Handler(priority = 1000)
    public void onConsolePrepared(ConsolePrepared event,
            ConsoleConnection channel)
            throws TemplateNotFoundException, MalformedTemplateNameException,
            ParseException, IOException {
        // Check if client connection to moodle service exists
        if (channel.session().transientData()
            .get(MoodleClient.class) != null) {
            return;
        }

        // Force login
        event.suspendHandling();
        channel.setAssociated(PENDING_CONSOLE_PREPARED, event);

        // Create model and save in session.
        String conletId = type() + TYPE_INSTANCE_SEPARATOR + "Singleton";
        AccountModel accountModel = new AccountModel(conletId);
        if (moodleServer != null) {
            accountModel.setTaggedInstance(moodleServer);
        }
        accountModel.setDialogOpen(true);
        putInSession(channel.session(), conletId, accountModel);

        // Render login dialog
        Template tpl = freemarkerConfig().getTemplate("Login-dialog.ftl.html");
        final Map<String, Object> renderModel
            = fmSessionModel(channel.session());
        renderModel.put("locale", channel.locale());
        renderModel.put("taggedInstance",
            Optional.ofNullable(accountModel.getTaggedInstance()).orElse(""));
        var bundle = resourceBundle(channel.locale());
        channel.respond(new OpenModalDialog(type(), conletId,
            processTemplate(event, tpl,
                renderModel)).addOption("title", bundle.getString("title"))
                    .addOption("cancelable", false).addOption("okayLabel", "")
                    .addOption("applyLabel", bundle.getString("Submit"))
                    .addOption("useSubmit", true));
    }

    @Override
    protected Set<RenderMode> doRenderConlet(RenderConletRequestBase<?> event,
            ConsoleConnection channel, String conletId,
            AccountModel model) throws Exception {
        Set<RenderMode> renderedAs = new HashSet<>();
        if (event.renderAs().contains(RenderMode.Content)) {
            Template tpl
                = freemarkerConfig().getTemplate("Login-status.ftl.html");
            channel.respond(new RenderConlet(type(), conletId,
                processTemplate(event, tpl,
                    fmModel(event, channel, conletId, model)))
                        .setRenderAs(RenderMode.Content));
            channel.respond(new NotifyConletView(type(), conletId, "updateUser",
                WebConsoleUtils.userFromSession(channel.session())
                    .map(ConsoleUser::getDisplayName).orElse(null)));
            renderedAs.add(RenderMode.Content);
        }
        return renderedAs;
    }

    @Override
    protected void doUpdateConletState(NotifyConletModel event,
            ConsoleConnection channel, AccountModel model)
            throws InterruptedException, UnsupportedEncodingException {
        // Let's give it a try
        if ("loginData".equals(event.method())) {
            MoodleClient client = attemptLogin(event, channel, model);
            if (client == null) {
                return;
            }

            // Update model, put client in session and track
            model.setFullName(client.siteInfo().getFullname());
            channel.session().transientData().put(MoodleClient.class, client);
            trackConlet(channel, model.getConletId(), null);

            // Save login data for use by framework
            Subject user = new Subject();
            user.getPrincipals()
                .add(new ConsoleUser(model.userName, model.fullName));

            // Get and save internet address principal for general use
            var moodleUser = client.moodleUser();
            user.getPrincipals().add(new InternetAddressPrincipal(
                moodleUser.getEmail(), model.fullName));
            user.getPrivateCredentials().add(
                new Password(((String) event.param(2)).toCharArray()));
            channel.session().put(Subject.class, user);
            fire(new UserAuthenticated(event.setAssociated(this,
                new LoginContext(channel, model)), user).by("Moodle Login"));
            return;
        }
        if ("logout".equals(event.method())) {
            channel.responsePipeline()
                .fire(new Close(), channel.upstreamChannel()).get();
            channel.close();
            channel.respond(new DiscardSession(channel.session(),
                channel.webletChannel()));
            // Alternative to sending Close (see above):
            // channel.respond(new SimpleConsoleCommand("reload"));
        }
    }

    /**
     * Invoked when a user has been authenticated.
     *
     * @param event the event
     * @param channel the channel
     */
    @Handler
    public void onUserAuthenticated(UserAuthenticated event, Channel channel) {
        var ctx = event.forLogin().associated(this, LoginContext.class)
            .filter(c -> c.conlet() == this).orElse(null);
        if (ctx == null) {
            return;
        }
        var model = ctx.model;
        model.setDialogOpen(false);
        var connection = ctx.connection;
        connection.session().put(Subject.class, event.subject());
        connection.respond(new CloseModalDialog(type(), model.getConletId()));
        connection.associated(PENDING_CONSOLE_PREPARED, ConsolePrepared.class)
            .ifPresentOrElse(ConsolePrepared::resumeHandling,
                () -> connection
                    .respond(new SimpleConsoleCommand("reload")));
    }

    @SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
        "PMD.AvoidCatchingGenericException" })
    private MoodleClient attemptLogin(NotifyConletModel event,
            ConsoleConnection channel, AccountModel model) {
        var bundle = resourceBundle(channel.locale());
        try {
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessages",
                bundle.getString("Connecting"), null));
            String site = model.getTaggedInstance();
            if (site == null) {
                site = event.param(0);
            }
            model.setUserName(event.param(1));
            return moodleService
                .connect(site, model.getUserName(),
                    new Password(((String) event.param(2)).toCharArray()));
        } catch (IOException e) {
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessages",
                null, bundle.getString("IOException")));
        } catch (Exception e) {
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessages", null, e.getMessage()));
        }
        return null;
    }

    /**
     * Central handling of connection lost. Display notification.
     *
     * @param event the event
     * @param channel the channel
     */
    @Handler
    public void onResourceNotAvailable(ResourceNotAvailable event,
            ConsoleConnection channel) {
        if (!MoodleClient.class.equals(event.itemSpecification())) {
            return;
        }
        channel.session().transientData()
            .computeIfPresent(MoodleClient.class, (key, value) -> {
                ((MoodleClient) value).close();
                var bundle = resourceBundle(channel.locale());
                channel.respond(new DisplayNotification(
                    "<span>" + bundle.getString("connectionLost") + "</span>",
                    new HashMap<>()));
                return null;
            });
    }

    @Override
    protected boolean doSetLocale(SetLocale event, ConsoleConnection channel,
            String conletId) throws Exception {
        return stateFromSession(channel.session(),
            type() + TYPE_INSTANCE_SEPARATOR + "Singleton")
                .map(model -> !model.isDialogOpen()).orElse(true);
    }

    /**
     * The context to preserve during the authentication process.
     */
    private class LoginContext {
        public final ConsoleConnection connection;
        public final AccountModel model;

        /**
         * Instantiates a new oidc context.
         *
         * @param connection the connection
         * @param model the model
         */
        public LoginContext(ConsoleConnection connection, AccountModel model) {
            this.connection = connection;
            this.model = model;
        }

        /**
         * Returns the conlet (the outer class).
         *
         * @return the login conlet
         */
        public LoginConlet conlet() {
            return LoginConlet.this;
        }
    }

    /**
     * Model with account info.
     */
    @SuppressWarnings("PMD.DataClass")
    public static class AccountModel extends ConletBaseModel {

        private boolean dialogOpen;
        private String userName;
        private String fullName;
        private String taggedInstance;

        /**
         * Creates a new model with the given type and id.
         * 
         * @param conletId the web console component id
         */
        @ConstructorProperties({ "conletId" })
        public AccountModel(String conletId) {
            super(conletId);
        }

        /**
         * Checks if is dialog open.
         *
         * @return true, if is dialog open
         */
        public boolean isDialogOpen() {
            return dialogOpen;
        }

        /**
         * Sets the dialog open.
         *
         * @param dialogOpen the new dialog open
         */
        public void setDialogOpen(boolean dialogOpen) {
            this.dialogOpen = dialogOpen;
        }

        /**
         * @return the userName
         */
        public String getUserName() {
            return userName;
        }

        /**
         * @param userName the userName to set
         */
        public void setUserName(String userName) {
            this.userName = userName;
        }

        /**
         * @return the fullName
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * @param fullName the fullName to set
         */
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        /**
         * @return the taggedInstance
         */
        public String getTaggedInstance() {
            return taggedInstance;
        }

        /**
         * @param taggedInstance the taggedInstance to set
         */
        public void setTaggedInstance(String taggedInstance) {
            this.taggedInstance = taggedInstance;
        }

    }

}
