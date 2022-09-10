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

/**
 * Models a Moodle course section with the properties required in this context.
 */
public class MoodleCourseSection {

    private final long id;
    private final String name;
    private final MoodleModule[] modules;

    /**
     * Instantiates a new moodle course section.
     *
     * @param id the id
     * @param name the name
     * @param modules the modules
     */
    @ConstructorProperties({ "id", "name", "modules" })
    @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
    public MoodleCourseSection(long id, String name, MoodleModule[] modules) {
        super();
        this.id = id;
        this.name = name;
        this.modules = modules;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public MoodleModule[] getModules() {
        return modules;
    }

    @Override
    public String toString() {
        return "MoodleCourseSection [id=" + id + ", name=" + name + ", modules="
            + Arrays.toString(modules) + "]";
    }

}
