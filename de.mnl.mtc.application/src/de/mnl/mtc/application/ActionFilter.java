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

package de.mnl.mtc.application;

import java.util.ResourceBundle;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Component;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.webconsole.base.ConsoleConnection;
import org.jgrapes.webconsole.base.events.DisplayNotification;
import org.jgrapes.webconsole.base.events.NotifyConletModel;

/**
 * Prevents some actions from being invoked such as disabling a bundle.
 */
public class ActionFilter extends Component {

    /**
     * Instantiates a new action filter.
     *
     * @param componentChannel the component channel
     */
    public ActionFilter(Channel componentChannel) {
        super(componentChannel);
    }

    /**
     * Intercepts the commands from the web console.
     *
     * @param event the event
     * @param channel the channel
     */
    @Handler(priority = 1000)
    public void onNotifyPortletModel(NotifyConletModel event,
            ConsoleConnection channel) {
        if (event.conletId()
            .startsWith("org.jgrapes.osgi.webconlet.bundles.BundleListConlet~")
            && !"sendDetails".equals(event.method())) {
            event.stop();
            ResourceBundle resources = ResourceBundle.getBundle(
                ActionFilter.class.getPackage().getName() + ".app-l10n");
            channel.respond(new DisplayNotification("<span>"
                + resources.getString("actionDisabled")
                + "</span>")
                    .addOption("autoClose", 5000));
        }
    }
}
