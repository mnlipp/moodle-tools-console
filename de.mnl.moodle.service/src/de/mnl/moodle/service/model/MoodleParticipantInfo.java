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

import java.time.Instant;

/**
 * Models the participant specific info of a user.
 */
@SuppressWarnings("PMD.DataClass")
public class MoodleParticipantInfo extends MoodleErrorValues {

    private long id;
    private long assignmentId;
    private boolean submitted;
    private boolean requiregrading;
    private long duedate;
    private long cutoffdate;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the assignmentId
     */
    public long getAssignmentId() {
        return assignmentId;
    }

    /**
     * @param assignmentId the assignmentId to set
     */
    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    /**
     * @return the submitted
     */
    public boolean isSubmitted() {
        return submitted;
    }

    /**
     * @param submitted the submitted to set
     */
    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    /**
     * @return the requiregrading
     */
    public boolean isRequiregrading() {
        return requiregrading;
    }

    /**
     * @param requiregrading the requiregrading to set
     */
    public void setRequiregrading(boolean requiregrading) {
        this.requiregrading = requiregrading;
    }

    /**
     * @return the duedate
     */
    public long getDuedate() {
        return duedate;
    }

    /**
     * @param duedate the duedate to set
     */
    public void setDuedate(long duedate) {
        this.duedate = duedate;
    }

    /**
     * @return the cutoffdate
     */
    public long getCutoffdate() {
        return cutoffdate;
    }

    /**
     * @param cutoffdate the cutoffdate to set
     */
    public void setCutoffdate(long cutoffdate) {
        this.cutoffdate = cutoffdate;
    }

    /**
     * Due date.
     *
     * @return the instant
     */
    public Instant dueDate() {
        return Instant.ofEpochSecond(duedate);
    }

    /**
     * Cut off date.
     *
     * @return the instant
     */
    public Instant cutOffDate() {
        return Instant.ofEpochSecond(cutoffdate);
    }

    @Override
    public String toString() {
        return "MoodleParticipantInfo [id=" + id + ", assignmentId="
            + assignmentId + ", submitted=" + submitted + ", requiregrading="
            + requiregrading + ", duedate=" + dueDate() + ", cutoffdate="
            + cutOffDate() + "]";
    }
}
