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

package de.mnl.moodle.service;

import de.mnl.moodle.service.model.MoodleAssignment;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleGroup;
import de.mnl.moodle.service.model.MoodleGrouping;
import de.mnl.moodle.service.model.MoodleSiteInfo;
import de.mnl.moodle.service.model.MoodleUser;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents the client side of a connection to a moodle server.
 */
public interface MoodleClient extends AutoCloseable {

    @Override
    void close();

    /**
     * Invoke some function. Allows the invocation of functions that
     * haven't been typed yet.
     *
     * @param wsfunction the function
     * @param params the parameters
     * @return the result
     */
    Object invoke(String wsfunction, Map<String, Object> params)
            throws IOException;

    /**
     * The authenticated moodle user.
     *
     * @return the moodle user
     */
    MoodleUser moodleUser();

    /**
     * The site info (includes information about the user)
     *
     * @return the moodle site info
     */
    MoodleSiteInfo siteInfo();

    /**
     * The courses that the user is enrolled in.
     *
     * @return the moodle courses
     */
    MoodleCourse[] enrolledIn() throws IOException;

    /**
     * The users enrolled in the course.
     *
     * @param course the course
     * @return the moodle user[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Set<MoodleUser> enrolled(MoodleCourse course) throws IOException;

    /**
     * Generate the URI for the given courses main page.
     *
     * @param course the course
     * @return the uri
     */
    URI courseUri(MoodleCourse course);

    /**
     * User course grades uri.
     *
     * @param user the user
     * @param course the course
     * @return the uri
     */
    URI userCourseGradesUri(MoodleUser user, MoodleCourse course);

    /**
     * Retrieves new course objects with all details filled in.
     *
     * @param courses the courses
     * @return the moodle course[]
     * @throws IOException 
     */
    MoodleCourse[] courseDetails(MoodleCourse... courses) throws IOException;

    /**
     * Retrieves a course's grouping.
     *
     * @param course the course
     * @return the moodle grouping[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MoodleGrouping[] groupings(MoodleCourse course) throws IOException;

    /**
     * Adds the assignments to the given courses.
     *
     * @param courses the courses
     * @param capabilities the capabilities used for filtering
     * @return the moodle assignment[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MoodleCourse[] withAssignments(MoodleCourse[] courses,
            String... capabilities) throws IOException;

    /**
     * Get a user's groups filtered by grouping.
     *
     * @param course the course
     * @param user the user
     * @param grouping the grouping
     * @return the moodle grouping[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MoodleGroup[] usersGroupsInGrouping(MoodleCourse course, MoodleUser user,
            MoodleGrouping grouping) throws IOException;

    /**
     * Retrieve the submissions matching the given criteria.
     *
     * @param assignments the assignments
     * @param status the status
     * @param since the since
     * @param before the before
     * @return the moodle submissions[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MoodleAssignment[] withSubmissions(MoodleAssignment[] assignments,
            String status, Instant since, Instant before) throws IOException;

    /**
     * Retrieve a course's contents and add it to the representation.
     *
     * @param course the course
     * @param excludeContents whether to exclude the module's contents 
     * @param modname if != null, return only modules of the given type
     * @return the moodle course sections
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MoodleCourse withContents(MoodleCourse course, boolean excludeContents,
            String modname) throws IOException;

    /**
     * Find a user given his email address.
     *
     * @param email the email
     * @return the moodle user
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Optional<MoodleUser> userByEmail(String email) throws IOException;

    /**
     * Returns the courses that the user is enrolled in.
     *
     * @param user the user
     * @return the moodle course[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MoodleCourse[] courses(MoodleUser user) throws IOException;
}
