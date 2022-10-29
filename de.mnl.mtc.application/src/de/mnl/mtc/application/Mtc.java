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

import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Component;
import org.jgrapes.core.Components;
import org.jgrapes.core.NamedChannel;
import org.jgrapes.core.events.Stop;
import org.jgrapes.http.HttpServer;
import org.jgrapes.http.InMemorySessionManager;
import org.jgrapes.http.LanguageSelector;
import org.jgrapes.http.events.Request;
import org.jgrapes.io.FileStorage;
import org.jgrapes.io.NioDispatcher;
import org.jgrapes.io.util.PermitsPool;
import org.jgrapes.mail.MailStoreMonitor;
import org.jgrapes.net.TcpServer;
import org.jgrapes.osgi.core.ComponentCollector;
import org.jgrapes.util.TomlConfigurationStore;
import org.jgrapes.webconsole.base.BrowserLocalBackedKVStore;
import org.jgrapes.webconsole.base.ConletComponentFactory;
import org.jgrapes.webconsole.base.ConsoleWeblet;
import org.jgrapes.webconsole.base.KVStoreBasedConsolePolicy;
import org.jgrapes.webconsole.base.PageResourceProviderFactory;
import org.jgrapes.webconsole.base.WebConsole;
import org.jgrapes.webconsole.rbac.RoleConfigurator;
import org.jgrapes.webconsole.rbac.RoleConletFilter;
import org.jgrapes.webconsole.vuejs.VueJsConsoleWeblet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 */
@SuppressWarnings("PMD.ShortClassName")
public class Mtc extends Component implements BundleActivator {

    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final Logger logger
        = LoggerFactory.getLogger(Mtc.class);
    private Mtc app;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    @Override
    @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
    public void start(BundleContext context) throws Exception {
        app = new Mtc();
        String configFile
            = Optional.ofNullable(System.getProperty("mtc.config.file"))
                .orElse("mtc-config.json");
        var config = new TomlConfigurationStore(app, new File(configFile));
        app.attach(config);
        // Attach a general nio dispatcher
        app.attach(new NioDispatcher());

        // Create a TCP server
        Channel tcpChannel = new NamedChannel("TCP");
        app.attach(new TcpServer(tcpChannel)
            .setConnectionLimiter(new PermitsPool(300))
            .setMinimalPurgeableTime(1000));

        // Create an HTTP server as converter between transport and application
        // layer.
        Channel httpChannel = new NamedChannel("HTTP");
        HttpServer httpServer = app.attach(new HttpServer(httpChannel,
            tcpChannel, Request.In.Get.class, Request.In.Post.class));

        // Build HTTP application layer
        httpServer.attach(new InMemorySessionManager(httpChannel));
        httpServer.attach(new LanguageSelector(httpChannel));
        httpServer.attach(new FileStorage(httpChannel, 65_536));
        String prefix = config.values(app.componentPath())
            .map(v -> v.get("prefix")).orElse("");
        ConsoleWeblet consoleWeblet
            = app.attach(new VueJsConsoleWeblet(httpChannel,
                Channel.SELF, new URI(prefix + "/")))
                .prependClassTemplateLoader(getClass())
                .prependResourceBundleProvider(getClass());
        WebConsole console = consoleWeblet.console();
        console.attach(new MailStoreMonitor(console.channel()));
        console.attach(new BrowserLocalBackedKVStore(
            console.channel(), consoleWeblet.prefix().getPath()));
        console.attach(new KVStoreBasedConsolePolicy(console.channel()));
        console.attach(new NewConsoleSessionPolicy(console.channel()));
        console.attach(new RoleConfigurator(console.channel()));
        console.attach(new RoleConletFilter(console.channel()));
        console.attach(new ActionFilter(console.channel()));
        console.attach(new ComponentCollector<>(
            console.channel(), context, PageResourceProviderFactory.class,
            type -> {
                switch (type) {
                case "org.jgrapes.webconsole.provider.gridstack.GridstackProvider":
                    return Arrays.asList(
                        Map.of("configuration", "CoreWithJQUiPlugin"));
                default:
                    return Arrays.asList(Collections.emptyMap());
                }
            }));
        console.attach(new ComponentCollector<>(
            console.channel(), context, ConletComponentFactory.class));
        Components.start(app);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        app.fire(new Stop(), Channel.BROADCAST);
        Components.awaitExhaustion();
    }
}
