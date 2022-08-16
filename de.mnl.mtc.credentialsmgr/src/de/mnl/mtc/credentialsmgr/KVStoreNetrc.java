/*
 * Moodle Console Application
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

package de.mnl.mtc.credentialsmgr;

import de.mnl.mtc.credentialsmgr.events.UpdateCredentials;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import org.jdrupes.json.JsonBeanEncoder;
import org.jdrupes.json.JsonDecodeException;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Component;
import org.jgrapes.core.Event;
import org.jgrapes.core.Manager;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.http.Session;
import org.jgrapes.util.events.KeyValueStoreData;
import org.jgrapes.util.events.KeyValueStoreUpdate;
import org.jgrapes.webconsole.base.ConsoleConnection;
import org.jgrapes.webconsole.base.ConsoleUser;
import org.jgrapes.webconsole.base.WebConsoleUtils;

/**
 * Provides a credentials store based on a key/value store. The
 * credentials follow the same pattern as used by the popular
 * "Netrc" credentials store. However, "machine" has been
 * generalized to "resource".
 */
public class KVStoreNetrc extends Component {

    /**
     * Creates a new component with its channel set to the given channel.
     * 
     * @param componentChannel the channel that the component's handlers listen
     * on by default and that {@link Manager#fire(Event, Channel...)}
     * sends the event to
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public KVStoreNetrc(Channel componentChannel, Map<?, ?> properties) {
        super(componentChannel);
    }

    private String storagePath(Session session) {
        return "/" + WebConsoleUtils.userFromSession(session)
            .map(ConsoleUser::toString).orElse("") + "/credentials/";
    }

    /**
     * Adds or updates credentials.
     *
     * @param event the event
     * @param channel the channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Handler
    public void onUpdateCredentials(UpdateCredentials event,
            ConsoleConnection channel) throws IOException {
        Credentials credentials = event.credentials();
        String jsonState = JsonBeanEncoder.create()
            .writeObject(credentials).toJson();
        channel.respond(new KeyValueStoreUpdate().update(
            storagePath(channel.session())
                + URLEncoder.encode(credentials.getResource(),
                    Charset.forName("utf-8")),
            jsonState));
    }

    /**
     * Invoked when the key/value store provides data.
     *
     * @param event the event
     * @param channel the channel
     * @throws JsonDecodeException the json decode exception
     */
    @Handler
    public void onKeyValueStoreData(KeyValueStoreData event,
            ConsoleConnection channel) throws JsonDecodeException {
        Session session = channel.session();
        if (!event.event().query().equals(storagePath(session))) {
            return;
        }
    }
}
