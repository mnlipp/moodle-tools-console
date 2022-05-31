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
import de.mnl.moodle.service.model.MoodleErrorValues;
import de.mnl.moodle.service.model.MoodleUser;
import java.io.IOException;
import java.util.Map;

/**
 * Find a user by his (login) name.
 */
public class MoodleUserByName extends RestAction {

    /**
     * Creates the action.
     *
     * @param client the client
     */
    public MoodleUserByName(RestClient client) {
        super(client);
    }

    /**
     * Must be public in order for the JSON decoder to work. For
     * internal use only. 
     */
    public static class ResultWrapper extends MoodleErrorValues {
        private MoodleUser[] users;

        /**
         * @param users the users to set
         */
        @SuppressWarnings({ "PMD.UseVarargs", "PMD.ArrayIsStoredDirectly" })
        public void setUsers(MoodleUser[] users) {
            this.users = users;
        }

        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public MoodleUser[] getUsers() {
            return users;
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
    public MoodleUser invoke(String userName) throws IOException {
        var users = client.invoke(MoodleUser[].class, Map.of(
            "wsfunction", "core_user_get_users_by_field",
            "field", "username",
            "values[0]", userName));
        if (users.length != 1) {
            throw new IllegalArgumentException(
                "Course \"" + userName + "\"not found.");
        }
        return users[0];
    }
}
