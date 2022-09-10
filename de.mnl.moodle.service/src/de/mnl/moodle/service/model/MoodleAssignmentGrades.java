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

package de.mnl.moodle.service.model;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.Optional;

/**
 * The Class MoodleAssignmentGrades.
 */
public class MoodleAssignmentGrades {

    private final long assignmentid;
    private final MoodleGrade[] grades;

    /**
     * Instantiates a new moodle assignment grades.
     *
     * @param assignmentid the assignmentid
     * @param grades the grades
     */
    @ConstructorProperties({ "assignmentid", "grades" })
    @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
    public MoodleAssignmentGrades(long assignmentid, MoodleGrade[] grades) {
        this.assignmentid = assignmentid;
        this.grades = grades;
    }

    /**
     * Gets the assignmentid.
     *
     * @return the assignmentid
     */
    public long getAssignmentid() {
        return assignmentid;
    }

    /**
     * Gets the grades.
     *
     * @return the grades
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public MoodleGrade[] getGrades() {
        return grades;
    }

    /**
     * Grade of user.
     *
     * @param userid the userid
     * @return the optional
     */
    public Optional<String> gradeOfUser(MoodleUser user) {
        return Arrays.stream(grades).filter(g -> g.getUserid() == user.getId())
            .map(MoodleGrade::getGrade).findFirst();
    }
}
