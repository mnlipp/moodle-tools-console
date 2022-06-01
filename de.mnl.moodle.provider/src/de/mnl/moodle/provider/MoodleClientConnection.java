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

import de.mnl.moodle.provider.actions.MoodleCoursesOfUser;
import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleUser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.osgi.service.component.annotations.Component;

/**
 * Represents an open connection to a moodle instance.
 */
@Component(immediate = true)
public class MoodleClientConnection implements MoodleClient {

    private final URI siteUri;
    private final RestClient restClient;
    private final MoodleUser moodleUser;

    /**
     * Instantiates a new moodle client connection.
     *
     * @param restClient the rest client
     */
    public MoodleClientConnection(URI siteUri, RestClient restClient,
            MoodleUser moodleUser) {
        this.siteUri = siteUri;
        this.restClient = restClient;
        this.moodleUser = moodleUser;
    }

    @Override
    public MoodleUser moodleUser() {
        return moodleUser;
    }

    @Override
    public MoodleCourse[] enrolledIn() throws IOException {
        return new MoodleCoursesOfUser(restClient).invoke(moodleUser);
    }

    // course/view.php?id=17045

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public URI courseUri(MoodleCourse course) {
        try {
            return siteUri.resolve(new URI(null, null,
                "course/view.php", "id=" + course.getId(), null));
        } catch (URISyntaxException e) {
            // Cannot happen.
        }
        // TODO Auto-generated method stub
        return siteUri;
    }

    @Override
    public void close() {
        // TODO restClient.close();
    }

}
