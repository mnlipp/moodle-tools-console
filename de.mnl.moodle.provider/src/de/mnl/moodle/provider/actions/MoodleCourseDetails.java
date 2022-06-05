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
import de.mnl.moodle.service.model.MoodleErrorValues;
import de.mnl.moodle.service.model.MoodleUser;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Searches course by short name.
 */
public class MoodleCourseDetails extends RestAction {

    /**
     * Creates the action.
     *
     * @param client the client
     */
    public MoodleCourseDetails(RestClient client) {
        super(client);
    }

    /**
     * Invoke the action.
     *
     * @param shortName the short name
     * @return the moodle course
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public MoodleCourse[] invoke(MoodleCourse... courses) throws IOException {
        return client.invoke(MoodleCourse[].class, Map.of(
            "wsfunction", "core_course_get_courses_by_field"),
            Map.of("field", "ids",
                "value", Stream.of(courses).map(MoodleCourse::getId)
                    .map(id -> id.toString())
                    .collect(Collectors.joining(","))));
    }
}
