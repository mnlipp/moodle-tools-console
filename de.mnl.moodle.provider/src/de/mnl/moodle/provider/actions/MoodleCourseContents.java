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

package de.mnl.moodle.provider.actions;

import de.mnl.moodle.provider.RestAction;
import de.mnl.moodle.provider.RestClient;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleCourseSection;
import java.io.IOException;
import java.util.Map;

/**
 * Obtains the sections of a course.
 */
public class MoodleCourseContents extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleCourseContents(RestClient client) {
        super(client);
    }

    /**
     * Invokes the action.
     *
     * @param course the course
     * @return the moodle course section[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MoodleCourseSection[] invoke(MoodleCourse course)
            throws IOException {
        return client.invoke(MoodleCourseSection[].class, Map.of(
            "wsfunction", "core_course_get_contents",
            "courseid", course.getId()));
    }
}
