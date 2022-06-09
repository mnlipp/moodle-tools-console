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
import de.mnl.moodle.service.model.MoodleSubmission;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Retrieves all assignments from a course.
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class MoodleSubmissions extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleSubmissions(RestClient client) {
        super(client);
    }

    /**
     * Must be public in order for the JSON decoder to work. For
     * internal use only. 
     */
    public static class ResultWrapper {
        private SubmissionsWrapper[] assignments = new SubmissionsWrapper[0];

        /**
         * @return the assignments
         */
        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public SubmissionsWrapper[] getAssignments() {
            return assignments;
        }

        /**
         * @param assignments the assignments to set
         */
        @SuppressWarnings({ "PMD.UseVarargs", "PMD.ArrayIsStoredDirectly" })
        public void setAssignments(SubmissionsWrapper[] assignments) {
            this.assignments = assignments;
        }

    }

    /**
     * The Class SubmissionsWrapper.
     */
    @SuppressWarnings("PMD.DataClass")
    public static class SubmissionsWrapper {
        private long assignmentid;
        private MoodleSubmission[] submissions;

        /**
         * @return the assignmentid
         */
        public long getAssignmentid() {
            return assignmentid;
        }

        /**
         * @param assignmentid the assignmentid to set
         */
        public void setAssignmentid(long assignmentid) {
            this.assignmentid = assignmentid;
        }

        /**
         * @return the submissions
         */
        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public MoodleSubmission[] getSubmissions() {
            return submissions;
        }

        /**
         * @param submissions the submissions to set
         */
        @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
        public void setSubmissions(MoodleSubmission[] submissions) {
            this.submissions = submissions;
        }
    }

    /**
     * Invokes the action.
     *
     * @param assignments the assignments
     * @param status the status
     * @param since the since
     * @param before the before
     * @return the moodle assignment[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MoodleAssignment[] invoke(MoodleAssignment[] assignments,
            String status, Instant since, Instant before) throws IOException {
        // For faster access
        @SuppressWarnings("PMD.UseConcurrentHashMap")
        Map<String, Object> params = new HashMap<>();
        params.put("assignmentids", Stream.of(assignments)
            .map(MoodleAssignment::getId).toArray(s -> new Long[s]));
        if (status != null && status != null) {
            params.put("status", status);
        }
        if (since != null) {
            params.put("since", since.toEpochMilli() / 1000);
        }
        if (before != null) {
            params.put("before", before.toEpochMilli() / 1000);
        }
        var res = client.invoke(ResultWrapper.class, Map.of(
            "wsfunction", "mod_assign_get_submissions"), params);
        Map<Long, MoodleAssignment> asMap
            = Stream.of(assignments).collect(
                Collectors.toMap(MoodleAssignment::getId, Function.identity()));
        Stream.of(res.getAssignments())
            .forEach(sw -> asMap.get(sw.getAssignmentid())
                .setSubmissions(sw.getSubmissions()));
        return assignments;
    }

}
