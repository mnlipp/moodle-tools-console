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

package de.mnl.moodle.service.model;

import java.beans.ConstructorProperties;

/**
 * The Class MoodleGetAssignmentGradesResponse.
 */
public class MoodleGetAssignmentGradesResponse {
    private final MoodleAssignmentGrades[] assignments;

    /**
     * Instantiates a new moodle get assignment grades response.
     *
     * @param assignments the assignments
     */
    @ConstructorProperties({ "assignments" })
    @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
    public MoodleGetAssignmentGradesResponse(
            MoodleAssignmentGrades[] assignments) {
        this.assignments = assignments;
    }

    /**
     * Gets the assignments.
     *
     * @return the assignments
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public MoodleAssignmentGrades[] getAssignments() {
        return assignments;
    }

}