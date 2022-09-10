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
import de.mnl.moodle.service.model.MoodleCompletionStatus;
import de.mnl.moodle.service.model.MoodleCourse;
import de.mnl.moodle.service.model.MoodleUser;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.Map;

/**
 * Retrieves the completion status of a Moodle activity.
 */
public class MoodleActivityCompletionStatus extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleActivityCompletionStatus(RestClient client) {
        super(client);
    }

    /**
     * Must be public in order for the JSON decoder to work. For
     * internal use only. 
     */
    public static class ResultWrapper {
        private final MoodleCompletionStatus[] statuses;

        /**
         * Instantiates a new result wrapper.
         *
         * @param statuses the statuses
         */
        @ConstructorProperties({ "statuses" })
        @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
        public ResultWrapper(MoodleCompletionStatus[] statuses) {
            super();
            this.statuses = statuses;
        }

        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public MoodleCompletionStatus[] getStatuses() {
            return statuses;
        }
    }

    /**
     * Invokes the action.
     *
     * @param course the course
     * @param user the user
     * @return the moodle completion statuses
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MoodleCompletionStatus[] invoke(MoodleCourse course, MoodleUser user)
            throws IOException {
        var result = client.invoke(ResultWrapper.class, Map.of(
            "wsfunction", "core_completion_get_activities_completion_status"),
            Map.of("courseid", course.getId(), "userid", user.getId()));
        return result.getStatuses();
    }
}
