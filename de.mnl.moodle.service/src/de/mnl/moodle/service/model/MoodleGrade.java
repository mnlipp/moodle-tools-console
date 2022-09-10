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

/**
 * The Class MoodleGrade.
 */
public class MoodleGrade {

    private final long userid;
    private final String grade;

    /**
     * Instantiates a new moodle grade.
     *
     * @param userid the userid
     * @param grade the grade
     */
    @ConstructorProperties({ "userid", "grade" })
    public MoodleGrade(long userid, String grade) {
        this.userid = userid;
        this.grade = grade;
    }

    /**
     * @return the userid
     */
    public long getUserid() {
        return userid;
    }

    /**
     * @return the grade
     */
    public String getGrade() {
        return grade;
    }

}
