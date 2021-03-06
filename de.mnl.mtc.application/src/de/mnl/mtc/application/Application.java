/*
 * Ad Hoc Polling Application
 * Copyright (C) 2018  Michael N. Lipp
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

package de.mnl.mtc.application;

import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
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
import org.jgrapes.net.SslCodec;
import org.jgrapes.net.TcpServer;
import org.jgrapes.osgi.core.ComponentCollector;
import org.jgrapes.webconsole.base.BrowserLocalBackedKVStore;
import org.jgrapes.webconsole.base.ConletComponentFactory;
import org.jgrapes.webconsole.base.ConsoleWeblet;
import org.jgrapes.webconsole.base.KVStoreBasedConsolePolicy;
import org.jgrapes.webconsole.base.PageResourceProviderFactory;
import org.jgrapes.webconsole.base.WebConsole;
import org.jgrapes.webconsole.vuejs.VueJsConsoleWeblet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class Application extends Component implements BundleActivator {

    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final Logger logger
        = LoggerFactory.getLogger(Application.class);
    private Application app;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    @Override
    @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
    public void start(BundleContext context) throws Exception {
        app = new Application();
        // Attach a general nio dispatcher
        app.attach(new NioDispatcher());

        // Create a TCP server
        Channel tcpChannel = new NamedChannel("TCP");
        Optional.ofNullable(System.getenv("PORT")).ifPresent(port -> {
            String[] parts = port.split(":");
            InetSocketAddress addr = parts.length > 1
                ? new InetSocketAddress(parts[0], Integer.parseInt(parts[1]))
                : new InetSocketAddress(Integer.parseInt(parts[0]));
            app.attach(new TcpServer(tcpChannel)
                .setServerAddress(addr)
                .setConnectionLimiter(new PermitsPool(300))
                .setMinimalPurgeableTime(1000));
        });

        // Create TLS server
        Optional.ofNullable(System.getenv("TLS_PORT")).map(Integer::parseInt)
            .ifPresent(port -> {
                try {
                    // Create TLS "converter"
                    KeyStore serverStore = KeyStore.getInstance("JKS");
                    try (InputStream keyFile
                        = Files.newInputStream(Paths.get("localhost.jks"))) {
                        serverStore.load(keyFile, "nopass".toCharArray());
                    }
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(serverStore, "nopass".toCharArray());
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(kmf.getKeyManagers(), null,
                        new SecureRandom());
                    // Create a TCP server for SSL
                    Channel securedNetwork = app.attach(
                        new TcpServer()
                            .setServerAddress(new InetSocketAddress(port))
                            .setBacklog(3000)
                            .setConnectionLimiter(new PermitsPool(50)));
                    app.attach(
                        new SslCodec(tcpChannel, securedNetwork, sslContext));
                } catch (IOException | KeyStoreException
                        | NoSuchAlgorithmException | UnrecoverableKeyException
                        | KeyManagementException | CertificateException e) {
                    logger.error(() -> e.getMessage(), e);
                }
            });

        // Create an HTTP server as converter between transport and application
        // layer.
        Channel httpChannel = new NamedChannel("HTTP");
        HttpServer httpServer = app.attach(new HttpServer(httpChannel,
            tcpChannel, Request.In.Get.class, Request.In.Post.class));

        // Build HTTP application layer
        httpServer.attach(new InMemorySessionManager(httpChannel));
        httpServer.attach(new LanguageSelector(httpChannel));
        httpServer.attach(new FileStorage(httpChannel, 65_536));
        ConsoleWeblet consoleWeblet
            = httpServer.attach(new VueJsConsoleWeblet(httpChannel,
                Channel.SELF,
                new URI(Optional.ofNullable(
                    System.getenv("PATH_PREFIX")).orElse("") + "/")))
                .prependClassTemplateLoader(getClass())
                .prependResourceBundleProvider(getClass());
        WebConsole console = consoleWeblet.console();
        console.attach(new BrowserLocalBackedKVStore(
            console, consoleWeblet.prefix().getPath()));
        console.attach(new KVStoreBasedConsolePolicy(console));
        console.attach(new NewConsoleSessionPolicy(console));
        console.attach(new ActionFilter(console));
        console.attach(new ComponentCollector<>(
            console, context, PageResourceProviderFactory.class,
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
            console, context, ConletComponentFactory.class));
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
