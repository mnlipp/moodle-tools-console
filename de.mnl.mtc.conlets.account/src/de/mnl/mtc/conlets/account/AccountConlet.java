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

package de.mnl.mtc.conlets.account;

import de.mnl.mtc.credentialsmgr.Credentials;
import de.mnl.mtc.credentialsmgr.events.UpdateCredentials;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jdrupes.json.JsonBeanDecoder;
import org.jdrupes.json.JsonBeanEncoder;
import org.jdrupes.json.JsonDecodeException;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Event;
import org.jgrapes.core.Manager;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.http.Session;
import org.jgrapes.util.events.KeyValueStoreData;
import org.jgrapes.util.events.KeyValueStoreQuery;
import org.jgrapes.util.events.KeyValueStoreUpdate;
import org.jgrapes.webconsole.base.Conlet.RenderMode;
import org.jgrapes.webconsole.base.ConletBaseModel;
import org.jgrapes.webconsole.base.ConsoleConnection;
import org.jgrapes.webconsole.base.ConsoleUser;
import org.jgrapes.webconsole.base.WebConsoleUtils;
import org.jgrapes.webconsole.base.events.AddConletRequest;
import org.jgrapes.webconsole.base.events.AddConletType;
import org.jgrapes.webconsole.base.events.AddPageResources.ScriptResource;
import org.jgrapes.webconsole.base.events.ConsoleReady;
import org.jgrapes.webconsole.base.events.NotifyConletModel;
import org.jgrapes.webconsole.base.events.NotifyConletView;
import org.jgrapes.webconsole.base.events.RenderConlet;
import org.jgrapes.webconsole.base.events.RenderConletRequestBase;
import org.jgrapes.webconsole.base.events.SetLocale;
import org.jgrapes.webconsole.base.freemarker.FreeMarkerConlet;

/**
 * A conlet for poll administration.
 */
public class AccountConlet
        extends FreeMarkerConlet<AccountConlet.AccountModel> {

    private static final Set<RenderMode> MODES
        = RenderMode.asSet(RenderMode.Preview);

    private String storagePath(Session session) {
        return "/" + WebConsoleUtils.userFromSession(session)
            .map(ConsoleUser::toString).orElse("")
            + "/conlets/" + AccountConlet.class.getName() + "/";
    }

    /**
     * Creates a new component with its channel set to the given channel.
     * 
     * @param componentChannel the channel that the component's handlers listen
     *            on by default and that {@link Manager#fire(Event, Channel...)}
     *            sends the event to
     */
    public AccountConlet(Channel componentChannel) {
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
                    type(), "Account-functions.js"))
                .setScriptType("module"))
            .addCss(event.renderSupport(),
                WebConsoleUtils.uriFromPath("Account-style.css")));
    }

    @Override
    protected String generateInstanceId(AddConletRequest event,
            ConsoleConnection channel) {
        return "Singleton";
    }

    @Override
    protected Optional<AccountModel> createNewState(AddConletRequest event,
            ConsoleConnection channel, String conletId) throws Exception {
        return Optional
            .ofNullable(stateFromSession(channel.session(), conletId)
                .orElse(super.createNewState(event, channel, conletId).get()));
    }

    @Override
    protected Optional<AccountModel> createStateRepresentation(Event<?> event,
            ConsoleConnection channel, String conletId) throws IOException {
        return Optional.of(new AccountModel(conletId));
    }

    @Override
    protected Set<RenderMode> doRenderConlet(RenderConletRequestBase<?> event,
            ConsoleConnection channel, String conletId,
            AccountModel conletState) throws Exception {
        Set<RenderMode> renderedAs = new HashSet<>(event.renderAs());
        if (event.renderAs().contains(RenderMode.Preview)) {
            Template tpl
                = freemarkerConfig().getTemplate("Account-preview.ftl.html");
            channel.respond(new RenderConlet(type(), conletId,
                processTemplate(event, tpl,
                    fmModel(event, channel, conletId, conletState)))
                        .setRenderAs(
                            RenderMode.Preview.addModifiers(event.renderAs()))
                        .setSupportedModes(MODES));
            renderedAs.add(RenderMode.Preview);
            KeyValueStoreQuery query = new KeyValueStoreQuery(
                storagePath(channel.session()), channel);
            fire(query, channel);
        }
        return renderedAs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jgrapes.portal.AbstractPortlet#doNotifyPortletModel
     */
    @Override
    protected void doUpdateConletState(NotifyConletModel event,
            ConsoleConnection channel, AccountModel conletModel)
            throws Exception {
        event.stop();

        if ("accountData".equals(event.method())) {
            Credentials credentials
                = new Credentials(event.params().asString(0),
                    event.params().asString(1), event.params().asString(2));
            channel.respond(new UpdateCredentials(credentials));
            conletModel.setResource(credentials.getResource());
            conletModel.setUsername(credentials.getUsername());
            String jsonState = JsonBeanEncoder.create()
                .writeObject(conletModel).toJson();
            channel.respond(new KeyValueStoreUpdate().update(
                storagePath(channel.session())
                    + conletModel.getConletId(),
                jsonState));
            return;
        }
    }

    /**
     * Invoked when the key/value store provides data.
     *
     * @param event the event
     * @param channel the channel
     * @throws JsonDecodeException the json decode exception
     */
    @Handler
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void onKeyValueStoreData(KeyValueStoreData event,
            ConsoleConnection channel) throws JsonDecodeException {
        Session session = channel.session();
        if (!event.event().query()
            .equals(storagePath(session))) {
            return;
        }

        for (String json : event.data().values()) {
            AccountModel model = JsonBeanDecoder.create(json)
                .readObject(AccountModel.class);
            putInSession(channel.session(), model.getConletId(), model);
            channel.respond(new NotifyConletView(type(),
                model.getConletId(), "accountData",
                model.getResource(), model.getUsername()));
        }
    }

    @Override
    protected boolean doSetLocale(SetLocale event, ConsoleConnection channel,
            String conletId) throws Exception {
        return true;
    }

    /**
     * Model with account info.
     */
    @SuppressWarnings("PMD.DataClass")
    public static class AccountModel extends ConletBaseModel {

        private String resource;
        private String username;

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
         * @return the resource
         */
        public String getResource() {
            return resource;
        }

        /**
         * @param resource the resource to set
         */
        public void setResource(String resource) {
            this.resource = resource;
        }

        /**
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * @param username the username to set
         */
        public void setUsername(String username) {
            this.username = username;
        }

    }

}
