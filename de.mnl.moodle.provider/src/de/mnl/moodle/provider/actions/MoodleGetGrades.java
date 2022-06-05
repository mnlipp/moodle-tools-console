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
import de.mnl.moodle.service.model.MoodleAssignmentGrades;
import de.mnl.moodle.service.model.MoodleGetAssignmentGradesResponse;
import de.mnl.moodle.service.model.MoodleGrade;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Saves a course grade. 
 */
public class MoodleGetGrades extends RestAction {

    /**
     * Instantiates the action.
     *
     * @param client the client
     */
    public MoodleGetGrades(RestClient client) {
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
    public MoodleAssignmentGrades invoke(MoodleAssignment assignment)
            throws IOException {
        var result = client.invoke(MoodleGetAssignmentGradesResponse.class,
            Map.of("wsfunction", "mod_assign_get_grades"), Map.of(
                "assignmentids", List.of(assignment.getId())));
        if (result.getAssignments().length > 0) {
            return result.getAssignments()[0];
        }
        return new MoodleAssignmentGrades(assignment.getId(),
            new MoodleGrade[0]);
    }
}
