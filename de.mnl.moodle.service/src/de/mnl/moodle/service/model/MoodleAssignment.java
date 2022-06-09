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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Models a Moodle assignment with the properties required in this context.
 */
@SuppressWarnings("PMD.DataClass")
public class MoodleAssignment {

    private final long id;
    private String name;
    private final List<?> configs = new ArrayList<>();
    private MoodleSubmission[] submissions;

    /**
     * Instantiates a new moodle assignment.
     *
     * @param id the id
     * @param name the name
     */
    @ConstructorProperties({ "id" })
    public MoodleAssignment(long id) {
        this.id = id;
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
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the configs
     */
    public List<?> getConfigs() {
        return configs;
    }

    /**
     * @return the submissions
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public MoodleSubmission[] getSubmissions() {
        return submissions;
    }

    /**
     * @param submissions the submissions to set
     */
    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    public void setSubmissions(MoodleSubmission... submissions) {
        this.submissions = submissions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
        MoodleAssignment other = (MoodleAssignment) obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return "MoodleAssignment [id=" + id + ", name=" + name + "]";
    }

}
