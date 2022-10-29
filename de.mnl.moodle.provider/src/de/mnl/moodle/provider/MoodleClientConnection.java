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

import de.mnl.moodle.provider.actions.MoodleCourseAssignments;
import de.mnl.moodle.provider.actions.MoodleCourseContents;
import de.mnl.moodle.provider.actions.MoodleCourseDetails;
import de.mnl.moodle.provider.actions.MoodleCoursesOfUser;
import de.mnl.moodle.provider.actions.MoodleEnrolledUsers;
import de.mnl.moodle.provider.actions.MoodleGetGroupings;
import de.mnl.moodle.provider.actions.MoodleSubmissions;
import de.mnl.moodle.provider.actions.MoodleUserByEmail;
import de.mnl.moodle.provider.actions.MoodleUsersGroupsInGrouping;
import de.mnl.moodle.service.MoodleClient;
import de.mnl.moodle.service.model.MoodleAssignment;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleGroup;
import de.mnl.moodle.service.model.MoodleGrouping;
import de.mnl.moodle.service.model.MoodleSiteInfo;
import de.mnl.moodle.service.model.MoodleUser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an open connection to a moodle instance.
 */
public class MoodleClientConnection implements MoodleClient {

    private final URI siteUri;
    private final RestClient restClient;
    private final MoodleUser moodleUser;
    private final MoodleSiteInfo siteInfo;

    /**
     * Instantiates a new moodle client connection.
     *
     * @param restClient the rest client
     */
    public MoodleClientConnection(URI siteUri, RestClient restClient,
            MoodleUser moodleUser, MoodleSiteInfo siteInfo) {
        this.siteUri = siteUri;
        this.restClient = restClient;
        this.moodleUser = moodleUser;
        this.siteInfo = siteInfo;
    }

    @Override
    public Object invoke(String wsfunction, Map<String, Object> params)
            throws IOException {
        return restClient.invoke(Object.class, Map.of("wsfunction", wsfunction),
            params);
    }

    @Override
    public MoodleUser moodleUser() {
        return moodleUser;
    }

    @Override
    public MoodleSiteInfo siteInfo() {
        return siteInfo;
    }

    @Override
    public MoodleCourse[] enrolledIn() throws IOException {
        return new MoodleCoursesOfUser(restClient).invoke(moodleUser);
    }

    @Override
    public MoodleCourse[] courseDetails(MoodleCourse... courses)
            throws IOException {
        return new MoodleCourseDetails(restClient).invoke(courses);
    }

    @Override
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public URI courseUri(MoodleCourse course) {
        try {
            return siteUri.resolve(new URI(null, null,
                "course/view.php", "id=" + course.getId(), null));
        } catch (URISyntaxException e) {
            // Cannot happen.
        }
        return siteUri;
    }

    @Override
    public MoodleGrouping[] groupings(MoodleCourse course) throws IOException {
        return new MoodleGetGroupings(restClient).invoke(course);
    }

    @Override
    public Set<MoodleUser> enrolled(MoodleCourse course) throws IOException {
        return new MoodleEnrolledUsers(restClient).invoke(course);
    }

    @Override
    public MoodleCourse[] withAssignments(MoodleCourse[] courses,
            String... capabilities) throws IOException {
        return new MoodleCourseAssignments(restClient).invoke(courses,
            capabilities);
    }

    @Override
    public MoodleGroup[] usersGroupsInGrouping(MoodleCourse course,
            MoodleUser user, MoodleGrouping grouping) throws IOException {
        return new MoodleUsersGroupsInGrouping(restClient).invoke(course, user,
            grouping);
    }

    @Override
    public MoodleAssignment[] withSubmissions(MoodleAssignment[] assignments,
            String status, Instant since, Instant before) throws IOException {
        return new MoodleSubmissions(restClient).invoke(assignments, status,
            since, before);
    }

    @Override
    public MoodleCourse withContents(MoodleCourse course,
            boolean excludeContents, String modname) throws IOException {
        course.setContents(new MoodleCourseContents(restClient).invoke(course,
            excludeContents, modname));
        return course;
    }

    @Override
    public Optional<MoodleUser> userByEmail(String email) throws IOException {
        return new MoodleUserByEmail(restClient).invoke(email);
    }

    @Override
    public MoodleCourse[] courses(MoodleUser moodleUser) throws IOException {
        return new MoodleCoursesOfUser(restClient).invoke(moodleUser);
    }

    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingGenericException",
        "PMD.EmptyCatchBlock" })
    public void close() {
        try {
            restClient.close();
        } catch (Exception e) {
            // Only trying to be nice
        }
    }

}
