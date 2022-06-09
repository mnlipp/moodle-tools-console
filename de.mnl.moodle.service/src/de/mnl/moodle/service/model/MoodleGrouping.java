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
import java.util.Objects;

/**
 * Models a Moodle grouping with the properties required in this context.
 */
@SuppressWarnings("PMD.DataClass")
public class MoodleGrouping {

    private final long id;
    private final long courseid;
    private String name;
    private String description;
    private int descriptionformat;
    private MoodleGroup[] groups;

    /**
     * Instantiates a new moodle grouping.
     *
     * @param id the id
     * @param courseid the courseid
     */
    @ConstructorProperties({ "id", "courseid" })
    public MoodleGrouping(long id, long courseid) {
        this.id = id;
        this.courseid = courseid;
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
     * @return the courseid
     */
    public long getCourseid() {
        return courseid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the descriptionformat
     */
    public int getDescriptionformat() {
        return descriptionformat;
    }

    /**
     * @param descriptionformat the descriptionformat to set
     */
    public void setDescriptionformat(int descriptionformat) {
        this.descriptionformat = descriptionformat;
    }

    /**
     * @return the groups
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public MoodleGroup[] getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    @SuppressWarnings({ "PMD.UseVarargs", "PMD.ArrayIsStoredDirectly" })
    public void setGroups(MoodleGroup[] groups) {
        this.groups = groups;
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
        MoodleGrouping other = (MoodleGrouping) obj;
        return id == other.id;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "MoodleGrouping [id=" + id + ", name=" + name + "]";
    }

}
