/*
 * Moodle-Tools
 * Copyright (C) 2021  Michael N. Lipp
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

package de.mnl.moodle.provider;

import de.mnl.osgi.lf4osgi.Logger;
import de.mnl.osgi.lf4osgi.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jdrupes.json.JsonBeanDecoder;
import org.jdrupes.json.JsonDecodeException;

/**
 * A class for invoking REST services.
 */
public class RestClient {

    private static final Logger logger
        = LoggerFactory.getLogger(RestClient.class);

    private HttpClient httpClient;
    private final Map<String, Object> defaultParams;
    private final URI uri;

    /**
     * Instantiates a new rest client.
     *
     * @param uri the uri
     * @param defaultParams the default params
     */
    public RestClient(URI uri, Map<String, Object> defaultParams) {
        createHttpClient();
        this.uri = uri;
        this.defaultParams = defaultParams;
    }

    /**
     * Instantiates a new rest client.
     *
     * @param uri the uri
     * @param defaultParams the default params
     */
    public RestClient(URI uri) {
        this(uri, Collections.emptyMap());
    }

    private void createHttpClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .authenticator(new NetrcAuthenticator()).build();
    }

    /**
     * Invoke a request with the parameters specified.
     *
     * @param <T> the generic type
     * @param resultType the result type
     * @param params the params
     * @return the result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("PMD.GuardLogStatement")
    public <T> T invoke(Class<T> resultType, Map<String, Object> params)
            throws IOException, InterruptedException {
        Map<String, Object> allParams = new HashMap<>(defaultParams);
        allParams.putAll(params);
        var query = allParams.entrySet().stream()
            .map(e -> URLEncoder.encode(e.getKey(), Charset.forName("utf-8"))
                + "=" + URLEncoder.encode(e.getValue().toString(),
                    Charset.forName("utf-8")))
            .collect(Collectors.joining("&"));
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                return doInvoke(resultType, query);
            } catch (IOException | InterruptedException e) {
                logger.debug("Reconnecting due to: " + e.getMessage(), e);
            }
            createHttpClient();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignored
            }
        }
        // Final attempt
        return doInvoke(resultType, query);
    }

    private <T> T doInvoke(Class<T> resultType, String query)
            throws IOException, InterruptedException {
        URI fullUri;
        try {
            fullUri = new URI(uri.getScheme(), uri.getAuthority(),
                uri.getPath(), query, uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        HttpRequest request = HttpRequest.newBuilder().uri(fullUri)
            .POST(HttpRequest.BodyPublishers.noBody()).build();

        // Execute and get the response.
        HttpResponse<InputStream> response
            = httpClient.send(request, BodyHandlers.ofInputStream());
        if (response.body() == null) {
            return null;
        }
        try (InputStream resultData = response.body()) {
            return JsonBeanDecoder
                .create(new InputStreamReader(response.body(), "utf-8"))
                .skipUnknown()
                .readObject(resultType);
        } catch (JsonDecodeException e) {
            throw new IOException("Unparsable result: " + e.getMessage(), e);
        }
    }
}