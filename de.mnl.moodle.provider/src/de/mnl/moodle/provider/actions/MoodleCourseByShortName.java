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
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.Map;

/**
 * Searches course by short name.
 */
public class MoodleCourseByShortName extends RestAction {

    /**
     * Creates the action.
     *
     * @param client the client
     */
    public MoodleCourseByShortName(RestClient client) {
        super(client);
    }

    /**
     * Must be public in order for the JSON decoder to work. For
     * internal use only. 
     */
    public static class ResultWrapper {
        private final MoodleCourse[] courses;

        /**
         * Instantiates a new result wrapper.
         *
         * @param courses the courses
         */
        @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
        @ConstructorProperties({ "courses" })
        public ResultWrapper(MoodleCourse[] courses) {
            super();
            this.courses = courses;
        }

        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public MoodleCourse[] getCourses() {
            return courses;
        }

    }

    /**
     * Invoke the action.
     *
     * @param shortName the short name
     * @return the moodle course
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public MoodleCourse invoke(String shortName) throws IOException {
        ResultWrapper result = client.invoke(ResultWrapper.class, Map.of(
            "wsfunction", "core_course_get_courses_by_field",
            "field", "shortname",
            "value", shortName));
        var courses = result.getCourses();
        if (courses.length != 1) {
            throw new IllegalArgumentException(
                "Course \"" + shortName + "\"not found.");
        }
        return courses[0];
    }
}
