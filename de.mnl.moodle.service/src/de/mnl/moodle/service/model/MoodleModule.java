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

// TODO: Auto-generated Javadoc
/**
 * Models a Moodle module with the properties required in this context.
 */
public class MoodleModule {

    private final long id;
    private final String name;
    private final long instance;

    /**
     * Instantiates a new moodle module.
     *
     * @param id the id
     * @param name the name
     * @param instance the instance
     */
    @ConstructorProperties({ "id", "name", "instance" })
    public MoodleModule(long id, String name, long instance) {
        this.id = id;
        this.name = name;
        this.instance = instance;
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
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the single instance of MoodleModule.
     *
     * @return single instance of MoodleModule
     */
    public long getInstance() {
        return instance;
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
        MoodleModule other = (MoodleModule) obj;
        return id == other.id;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "MoodleModule [id=" + id + ", name=" + name + ", instance="
            + instance + "]";
    }

}
