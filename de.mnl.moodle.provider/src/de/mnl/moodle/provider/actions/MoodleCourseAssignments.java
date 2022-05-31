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
import de.mnl.moodle.service.model.MoodleAssignment;
import de.mnl.moodle.service.model.MoodleCourse;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.Map;

/**
 * Retrieves all assignments from a course.
 */
public class MoodleCourseAssignments extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleCourseAssignments(RestClient client) {
        super(client);
    }

    /**
     * Must be public in order for the JSON decoder to work. For
     * internal use only. 
     */
    public static class ResultWrapper {
        private final CourseWrapper[] courses;

        /**
         * Instantiates a new result wrapper.
         *
         * @param courses the courses
         */
        @ConstructorProperties({ "courses" })
        @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
        public ResultWrapper(CourseWrapper[] courses) {
            super();
            this.courses = courses;
        }

        /**
         * Gets the courses.
         *
         * @return the courses
         */
        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public CourseWrapper[] getCourses() {
            return courses;
        }
    }

    /**
     * Must be public in order for the JSON decoder to work. For
     * internal use only. 
     */
    public static class CourseWrapper {
        private final MoodleAssignment[] assignments;

        /**
         * Instantiates a new course wrapper.
         *
         * @param assignments the assignments
         */
        @ConstructorProperties({ "assignments" })
        @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
        public CourseWrapper(MoodleAssignment[] assignments) {
            super();
            this.assignments = assignments;
        }

        /**
         * Gets the assignments.
         *
         * @return the assignments
         */
        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public MoodleAssignment[] getAssignments() {
            return assignments;
        }
    }

    /**
     * Invokes the action.
     *
     * @param course the course
     * @return the moodle assignment[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MoodleAssignment[] invoke(MoodleCourse course)
            throws IOException {
        var result = client.invoke(ResultWrapper.class, Map.of(
            "wsfunction", "mod_assign_get_assignments",
            "courseids[0]", course.getId()));
        return result.getCourses()[0].getAssignments();
    }
}
