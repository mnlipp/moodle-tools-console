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
import de.mnl.moodle.service.model.MoodleGroup;
import de.mnl.moodle.service.model.MoodleGrouping;
import de.mnl.moodle.service.model.MoodleUser;
import java.io.IOException;
import java.util.Map;

/**
 * Get a user's groups filtered by grouping.
 */
public class MoodleUsersGroupsInGrouping extends RestAction {

    /**
     * Creates the action.
     *
     * @param client the client
     */
    public MoodleUsersGroupsInGrouping(RestClient client) {
        super(client);
    }

    /**
     * Must be public in order for the JSON decoder to work. For
     * internal use only. 
     */
    public static class ResultWrapper {
        private MoodleGroup[] groups;

        /**
         * @param groups the groups to set
         */
        @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
        public void setGroups(MoodleGroup[] groups) {
            this.groups = groups;
        }

        /**
         * @return the groups
         */
        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public MoodleGroup[] getGroups() {
            return groups;
        }
    }

    /**
     * Invoke the action.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MoodleGroup[] invoke(MoodleCourse course, MoodleUser user,
            MoodleGrouping grouping) throws IOException {
        return client.invoke(ResultWrapper.class, Map.of(
            "wsfunction", "core_group_get_course_user_groups"),
            Map.of("courseid", course.getId(),
                "userid", user.getId(),
                "groupingid", grouping.getId()))
            .getGroups();
    }
}
