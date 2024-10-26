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
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Models a Moodle course with the properties required in this context.
 */
@SuppressWarnings("PMD.DataClass")
public class MoodleCourse {

    private final long id;
    private final String shortName;
    private String fullname;
    private String displayname;
    private Long startdate;
    private MoodleCourseSection[] contents;
    private MoodleAssignment[] assignments = new MoodleAssignment[0];

    /**
     * Instantiates a new moodle course.
     *
     * @param id the id
     * @param shortName the short name
     */
    @ConstructorProperties({ "id", "shortname" })
    public MoodleCourse(long id, String shortName) {
        this.id = id;
        this.shortName = shortName;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the short name.
     *
     * @return the short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @return the startdate
     */
    public long getStartdate() {
        return startdate;
    }

    /**
     * @param startdate the startdate to set
     */
    public void setStartdate(long startdate) {
        if (startdate != 0) {
            this.startdate = startdate;
        }
    }

    /**
     * @param startdate the startdate to set
     */
    public void setStartdate(Long startdate) {
        this.startdate = startdate;
    }

    /**
     * @return the fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * @param fullname the fullname to set
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * @return the displayname
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * @param displayname the displayname to set
     */
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    /**
     * Start date as instant.
     *
     * @return the optional
     */
    public Optional<Instant> startDate() {
        return Optional.ofNullable(startdate).map(Instant::ofEpochSecond);
    }

    /**
     * @return the assignments
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public MoodleAssignment[] getAssignments() {
        return assignments;
    }

    /**
     * @param assignments the assignments to set
     */
    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    public void setAssignments(MoodleAssignment... assignments) {
        this.assignments = assignments;
    }

    /**
     * @return the contents
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public MoodleCourseSection[] getContents() {
        return contents;
    }

    /**
     * @param contents the contents to set
     */
    @SuppressWarnings({ "PMD.UseVarargs", "PMD.ArrayIsStoredDirectly" })
    public void setContents(MoodleCourseSection[] contents) {
        this.contents = contents;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MoodleCourse other = (MoodleCourse) obj;
        return id == other.id;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "MoodleCourse [id=" + id + ", shortName=" + shortName
            + ", startDate=" + startDate() + "]";
    }

}
