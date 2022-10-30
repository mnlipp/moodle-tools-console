/*
 * JGrapes Event Driven Framework
 * Copyright (C) 2017-2018 Michael N. Lipp
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

package de.mnl.mtc.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.stream.Collectors;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Component;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.webconlet.markdowndisplay.MarkdownDisplayConlet;
import org.jgrapes.webconsole.base.Conlet;
import org.jgrapes.webconsole.base.ConsoleConnection;
import org.jgrapes.webconsole.base.events.AddConletRequest;
import org.jgrapes.webconsole.base.events.ConsoleConfigured;
import org.jgrapes.webconsole.base.events.ConsoleReady;
import org.jgrapes.webconsole.base.events.RenderConlet;

/**
 * 
 */
public class AvoidEmptyPolicy extends Component {

    private final String renderedFlagName = getClass().getName() + ".rendered";

    /**
     * Creates a new component with its channel set to the given channel.
     * 
     * @param componentChannel
     */
    public AvoidEmptyPolicy(Channel componentChannel) {
        super(componentChannel);
    }

    /**
     * On console ready.
     *
     * @param event the event
     * @param connection the connection
     */
    @Handler
    public void onConsoleReady(ConsoleReady event,
            ConsoleConnection connection) {
        connection.session().put(renderedFlagName, false);
    }

    /**
     * On render conlet.
     *
     * @param event the event
     * @param connection the connection
     */
    @Handler
    public void onRenderConlet(RenderConlet event,
            ConsoleConnection connection) {
        connection.session().put(renderedFlagName, true);
    }

    /**
     * On console configured.
     *
     * @param event the event
     * @param connection the console connection
     * @throws InterruptedException the interrupted exception
     */
    @Handler
    public void onConsoleConfigured(ConsoleConfigured event,
            ConsoleConnection connection) throws InterruptedException,
            IOException {
        if ((Boolean) connection.session().getOrDefault(
            renderedFlagName, false)) {
            return;
        }
        String shortDesc;
        try (BufferedReader shortDescReader
            = new BufferedReader(new InputStreamReader(
                AvoidEmptyPolicy.class.getResourceAsStream(
                    "ConsoleIntro-Preview.md"),
                "utf-8"))) {
            shortDesc
                = shortDescReader.lines().collect(Collectors.joining("\n"));
        }
//        String longDesc;
//        try (BufferedReader longDescReader
//            = new BufferedReader(new InputStreamReader(
//                AvoidEmptyPolicy.class
//                    .getResourceAsStream("ConsoleIntro-View.md"),
//                "utf-8"))) {
//            longDesc
//                = longDescReader.lines().collect(Collectors.joining("\n"));
//        }
        fire(new AddConletRequest(event.event().event().renderSupport(),
            MarkdownDisplayConlet.class.getName(),
            Conlet.RenderMode.asSet(Conlet.RenderMode.Preview))
                .addProperty(MarkdownDisplayConlet.TITLE, "MTC WebConsole")
                .addProperty(MarkdownDisplayConlet.PREVIEW_SOURCE, shortDesc)
                .addProperty(MarkdownDisplayConlet.EDITABLE_BY,
                    Collections.EMPTY_SET),
            connection);
    }

}
