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

import de.mnl.moodle.provider.MoodleException;
import de.mnl.moodle.provider.RestAction;
import de.mnl.moodle.provider.RestClient;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleGrouping;
import java.io.IOException;
import java.util.Map;

/**
 * Get a course's groupings.
 */
public class MoodleGetGroupings extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleGetGroupings(RestClient client) {
        super(client);
    }

    /**
     * Invokes the action.
     *
     * @param assignment the assignment to be graded
     * @param user the user
     * @param grade the grade
     * @param text the text
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MoodleGrouping[] invoke(MoodleCourse course)
            throws IOException {
        try {
            return client.invoke(MoodleGrouping[].class,
                Map.of("wsfunction", "core_group_get_course_groupings"),
                Map.of("courseid", course.getId()));
        } catch (MoodleException e) {
            if ("nopermissions".equals(e.errorCode())) {
                // Permission may be missing in some courses
                return new MoodleGrouping[0];
            }
            throw e;
        }
    }
}
