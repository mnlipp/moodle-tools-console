/*
 * JGrapes Event Driven Framework
 * Copyright (C) 2022 Michael N. Lipp
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.mnl.mtc.conlets.login;

import de.mnl.moodle.service.MoodleAuthFailedException;
import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.MoodleService;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Components;
import org.jgrapes.core.Event;
import org.jgrapes.core.Manager;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.io.events.Close;
import org.jgrapes.webconsole.base.Conlet.RenderMode;
import org.jgrapes.webconsole.base.ConletBaseModel;
import org.jgrapes.webconsole.base.ConsoleSession;
import org.jgrapes.webconsole.base.WebConsoleUtils;
import org.jgrapes.webconsole.base.events.AddConletRequest;
import org.jgrapes.webconsole.base.events.AddConletType;
import org.jgrapes.webconsole.base.events.AddPageResources.ScriptResource;
import org.jgrapes.webconsole.base.events.CloseModalDialog;
import org.jgrapes.webconsole.base.events.ConsolePrepared;
import org.jgrapes.webconsole.base.events.ConsoleReady;
import org.jgrapes.webconsole.base.events.NotifyConletModel;
import org.jgrapes.webconsole.base.events.NotifyConletView;
import org.jgrapes.webconsole.base.events.OpenModalDialog;
import org.jgrapes.webconsole.base.events.RenderConletRequestBase;
import org.jgrapes.webconsole.base.events.SetLocale;
import org.jgrapes.webconsole.base.freemarker.FreeMarkerConlet;

/**
 * A conlet for poll administration.
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class LoginConlet extends FreeMarkerConlet<LoginConlet.AccountModel> {

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
            .addScript(new ScriptResource()
                .setScriptUri(event.renderSupport().conletResource(
                    type(), "Login-functions.js"))
                .setScriptType("module"))
            .addCss(event.renderSupport(), WebConsoleUtils.uriFromPath(
                "Login-style.css")));
    }

    @Override
    protected Optional<AccountModel> createNewState(AddConletRequest event,
            ConsoleSession session, String conletId) throws Exception {
        return Optional
            .ofNullable(stateFromSession(session.browserSession(), conletId)
                .orElse(super.createNewState(event, session, conletId).get()));
    }

    @Override
    protected Optional<AccountModel> createStateRepresentation(
            RenderConletRequestBase<?> event,
            ConsoleSession channel, String conletId) throws IOException {
        return Optional.of(new AccountModel(conletId));
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
    public void onConsolePrepared(ConsolePrepared event, ConsoleSession channel)
            throws TemplateNotFoundException, MalformedTemplateNameException,
            ParseException, IOException {
        event.suspendHandling();
        channel.setAssociated(this, event);

        // Clear left over client
        onClose(null, channel);

        // Create model and save in session.
        String conletId = type() + TYPE_INSTANCE_SEPARATOR + "Singleton";
        AccountModel accountModel = new AccountModel(conletId);
        accountModel.setDialogOpen(true);
        putInSession(channel.browserSession(), conletId, accountModel);

        // Render login dialog
        Template tpl = freemarkerConfig().getTemplate("Login-dialog.ftl.html");
        final Map<String, Object> renderModel
            = fmSessionModel(channel.browserSession());
        renderModel.put("locale", channel.locale());
        var bundle = resourceBundle(channel.locale());
        channel.respond(new OpenModalDialog(type(), conletId,
            processTemplate(event, tpl,
                renderModel)).addOption("title", bundle.getString("title"))
                    .addOption("cancelable", false).addOption("okayLabel", "")
                    .addOption("applyLabel", bundle.getString("Submit"))
                    .addOption("useSubmit", true));
    }

    private Future<String> processTemplate(
            Event<?> request, Template template,
            Object dataModel) {
        return request.processedBy().map(procBy -> procBy.executorService())
            .orElse(Components.defaultExecutorService()).submit(() -> {
                StringWriter out = new StringWriter();
                try {
                    template.process(dataModel, out);
                } catch (TemplateException | IOException e) {
                    throw new IllegalArgumentException(e);
                }
                return out.toString();

            });
    }

    @Override
    protected Set<RenderMode> doRenderConlet(RenderConletRequestBase<?> event,
            ConsoleSession channel, String conletId,
            AccountModel conletState) throws Exception {
        return Collections.emptySet();
    }

    @Override
    protected void doUpdateConletState(NotifyConletModel event,
            ConsoleSession channel, AccountModel model) {
        // Let's give it a try
        MoodleClient client;
        var bundle = resourceBundle(channel.locale());
        try {
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessages",
                bundle.getString("Connecting"), null));
            client = moodleService
                .connect(event.params().asString(0), event.params().asString(1),
                    event.params().asString(2).toCharArray());
        } catch (MoodleAuthFailedException e) {
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessages", null, e.getMessage()));
            return;
        } catch (IOException e) {
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "setMessage",
                bundle.getString("IOException")));
            return;
        }
        if (client == null) {
            return;
        }

        // Associate with channel
        channel.setAssociated(MoodleClient.class, client);

        // Okay, close and resume
        channel.respond(new CloseModalDialog(type(), event.conletId()));
        channel.associated(this, ConsolePrepared.class)
            .ifPresent(Event::resumeHandling);
    }

    /**
     * On close, close moodle client.
     *
     * @param event the event
     * @param channel the channel
     */
    @Handler
    public void onClose(Close event, ConsoleSession channel) {
        channel.associated(MoodleClient.class).ifPresent(client -> {
            client.close();
            channel.setAssociated(MoodleClient.class, null);
        });
    }

    @Override
    protected boolean doSetLocale(SetLocale event, ConsoleSession channel,
            String conletId) throws Exception {
        return stateFromSession(channel.browserSession(),
            type() + TYPE_INSTANCE_SEPARATOR + "Singleton")
                .map(model -> !model.isDialogOpen()).orElse(true);
    }

    /**
     * Model with account info.
     */
    @SuppressWarnings({ "serial", "PMD.DataClass" })
    public static class AccountModel extends ConletBaseModel {

        private boolean dialogOpen;

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

    }

}
