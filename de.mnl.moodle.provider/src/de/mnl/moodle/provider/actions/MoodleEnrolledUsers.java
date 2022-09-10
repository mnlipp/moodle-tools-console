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

package de.mnl.moodle.provider.actions;

import de.mnl.moodle.provider.RestAction;
import de.mnl.moodle.provider.RestClient;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleUser;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Obtains the users enrolled in a given course.
 */
public class MoodleEnrolledUsers extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleEnrolledUsers(RestClient client) {
        super(client);
    }

    /**
     * Invokes the action.
     *
     * @param course the course
     * @return the sets the
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Set<MoodleUser> invoke(MoodleCourse course) throws IOException {
        var result = client.invoke(MoodleUser[].class,
            Map.of("wsfunction", "core_enrol_get_enrolled_users"),
            Map.of("courseid", course.getId()));
        return new HashSet<>(Arrays.asList(result));
    }
}
