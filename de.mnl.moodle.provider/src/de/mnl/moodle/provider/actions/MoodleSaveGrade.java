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
import de.mnl.moodle.service.model.MoodleUser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Saves a course grade. 
 */
public class MoodleSaveGrade extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleSaveGrade(RestClient client) {
        super(client);
    }

    /**
     * Invokes the action.
     *
     * @param assignment the assignment to be graded
     * @param user the user
     * @param grade the grade
     * @param text the text
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void invoke(MoodleAssignment assignment, MoodleUser user,
            double grade, String text) throws IOException {
        @SuppressWarnings("PMD.UseConcurrentHashMap")
        Map<String, Object> params = new HashMap<>(Map.of(
            "wsfunction", "mod_assign_save_grade",
            "assignmentid", assignment.getId(),
            "userid", user.getId(),
            "grade", grade,
            "attemptnumber", -1,
            "addattempt", 0,
            "workflowstate", "graded",
            "applytoall", 0,
            "plugindata[assignfeedbackcomments_editor][text]", text,
            "plugindata[assignfeedbackcomments_editor][format]", 1));
        params.put("plugindata[files_filemanager]", 0);
        client.invoke(Object.class, params);
    }
}
