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
 * Models a Moodle completion status with the properties required in this context.
 */
public class MoodleCompletionStatus {

    private final long cmId;
    private final long instance;
    private final long state;

    /**
     * Instantiates a new moodle completion status.
     *
     * @param cmId the cm id
     * @param instance the instance
     * @param state the state
     */
    @ConstructorProperties({ "cmid", "instance", "state" })
    public MoodleCompletionStatus(long cmId, long instance, long state) {
        super();
        this.cmId = cmId;
        this.instance = instance;
        this.state = state;
    }

    public long getCmId() {
        return cmId;
    }

    public long getInstance() {
        return instance;
    }

    public long getState() {
        return state;
    }

    @Override
    public String toString() {
        return "MoodleCompletionStatus [cmId=" + cmId + ", instance=" + instance
            + ", state=" + state + "]";
    }

}
