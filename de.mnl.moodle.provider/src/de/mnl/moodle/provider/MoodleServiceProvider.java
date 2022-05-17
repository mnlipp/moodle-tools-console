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

package de.mnl.moodle.provider;

import de.mnl.moodle.service.MoodleService;
import de.mnl.moodle.service.model.MoodleTokens;
import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true)
public class MoodleServiceProvider implements MoodleService {

    private static final Logger logger
        = LoggerFactory.getLogger(MoodleServiceProvider.class);

    public MoodleServiceProvider() {
        println("=== Constructor ===");
    }

    private static String retrieveToken(Map<String, Object> configuration)
            throws ConfigurationException, IOException, InterruptedException {
        // Shortcut to server configuration
        @SuppressWarnings("unchecked")
        var moodleConfig
            = (Map<String, Object>) configuration.get("moodle");

        String moodle = "";

        // Get password for server and user
        PasswordAuthentication credentials = Authenticator
            .requestPasswordAuthentication(new NetrcAuthenticator(),
                moodle, null, 0, null, null, null, null, null);
        if (credentials == null) {
            throw new ConfigurationException("Cannot find credentials.");
        }

        // Request token
        try {
            URI tokenUri = new URI("https", moodle, "/login/token.php", null);
            var restClient = new RestClient(tokenUri);
            var tokens = restClient.invoke(MoodleTokens.class,
                Map.of("username", credentials.getUserName(),
                    "password", new String(credentials.getPassword()),
                    "service", "moodle_mobile_app"));
            logger.info("Obtained access token.");
            return tokens.getToken();
        } catch (URISyntaxException e) {
            throw new ConfigurationException(e);
        }
    }

    @Activate
    public void activate(Map properties) {
        println("=== Activate " + properties.get("number") + " ===");
        println(properties);
        try {
            String token = retrieveToken((Map<String, Object>) properties);
        } catch (ConfigurationException | IOException
                | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Modified
    public void modified(Map properties) {
        println("=== Modified " + properties.get("number") + " ===");
        println(properties);
    }

    @Deactivate
    public void deactivate(Map properties) {
        println("=== Deactivate " + properties.get("number") + " ===");
        println(properties);
    }

    private void println(Map properties) {
        if (properties != null) {
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                println(it.next().toString());
            }
        }
    }

    private void println(String message) {
        System.out.println(message);
    }

}
