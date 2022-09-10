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

package de.mnl.moodle.provider;

import de.mnl.moodle.provider.actions.MoodleGetSiteInfo;
import de.mnl.moodle.provider.actions.MoodleUserByName;
import de.mnl.moodle.service.MoodleAuthFailedException;
import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.MoodleService;
import de.mnl.moodle.service.model.MoodleSiteInfo;
import de.mnl.moodle.service.model.MoodleTokens;
import de.mnl.moodle.service.model.MoodleUser;
import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Represents an open connection to a moodle instance.
 */
@Component(scope = ServiceScope.SINGLETON)
public class MoodleServiceProvider implements MoodleService {

    @SuppressWarnings({ "PMD.FieldNamingConventions",
        "PMD.UnusedPrivateField", "unused" })
    private static final Logger logger
        = LoggerFactory.getLogger(MoodleServiceProvider.class);

    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingGenericException",
        "PMD.EmptyCatchBlock" })
    public MoodleClient connect(String website, String username,
            char[] password) throws IOException, MoodleAuthFailedException {
        // Request token
        try {
            String site = website;
            if (!site.contains("://")) {
                site = "https://" + site;
            }
            URI siteUri = new URI("https", "localhost", null, null, null)
                .resolve(site);
            if ("".equals(siteUri.getPath())) {
                siteUri = siteUri.resolve(new URI(null, null, "/", null, null));
            }
            URI tokenUri = siteUri
                .resolve(new URI(null, null, "login/token.php", null, null));
            var restClient = new RestClient(tokenUri);
            var tokens = restClient.invoke(MoodleTokens.class,
                Map.of("username", username,
                    "password", new String(password),
                    "service", "moodle_mobile_app"),
                Collections.emptyMap());
            if (tokens.getErrorcode() != null) {
                try {
                    restClient.close();
                } catch (Exception e) {
                    // Was just trying to be nice
                }
                throw new MoodleAuthFailedException(tokens.getError());
            }
            URI serviceUri = siteUri.resolve(
                new URI(null, null, "webservice/rest/server.php", null, null));
            restClient.setUri(serviceUri);
            restClient.setDefaultParams(Map.of("wstoken", tokens.getToken(),
                "moodlewsrestformat", "json"));
            MoodleUser muser
                = new MoodleUserByName(restClient).invoke(username);
            MoodleSiteInfo siteInfo
                = new MoodleGetSiteInfo(restClient).invoke();
            return new MoodleClientConnection(siteUri, restClient, muser,
                siteInfo);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
