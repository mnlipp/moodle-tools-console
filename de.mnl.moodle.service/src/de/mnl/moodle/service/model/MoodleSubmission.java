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
 * Models a Moodle submission with the properties required in this context.
 */
@SuppressWarnings("PMD.DataClass")
public class MoodleSubmission {

    private final long id;
    private final long userid;
    private int attemptnumber;
    private int timecreated;
    private int timemodified;
    private String status;
    private long groupid;
    private long assignment;
    private String gradingstatus;

    /**
     * Instantiates a new moodle submission.
     *
     * @param id the id
     * @param userid the userid
     */
    @ConstructorProperties({ "id", "userid" })
    public MoodleSubmission(long id, long userid) {
        this.id = id;
        this.userid = userid;
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
     * Gets the userid.
     *
     * @return the userid
     */
    public long getUserid() {
        return userid;
    }

    /**
     * @return the attemptnumber
     */
    public int getAttemptnumber() {
        return attemptnumber;
    }

    /**
     * @param attemptnumber the attemptnumber to set
     */
    public void setAttemptnumber(int attemptnumber) {
        this.attemptnumber = attemptnumber;
    }

    /**
     * @return the timecreated
     */
    public int getTimecreated() {
        return timecreated;
    }

    /**
     * @param timecreated the timecreated to set
     */
    public void setTimecreated(int timecreated) {
        this.timecreated = timecreated;
    }

    /**
     * @return the timemodified
     */
    public int getTimemodified() {
        return timemodified;
    }

    /**
     * @param timemodified the timemodified to set
     */
    public void setTimemodified(int timemodified) {
        this.timemodified = timemodified;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the groupid
     */
    public long getGroupid() {
        return groupid;
    }

    /**
     * @param groupid the groupid to set
     */
    public void setGroupid(long groupid) {
        this.groupid = groupid;
    }

    /**
     * @return the assignment
     */
    public long getAssignment() {
        return assignment;
    }

    /**
     * @param assignment the assignment to set
     */
    public void setAssignment(long assignment) {
        this.assignment = assignment;
    }

    /**
     * @return the gradingstatus
     */
    public String getGradingstatus() {
        return gradingstatus;
    }

    /**
     * @param gradingstatus the gradingstatus to set
     */
    public void setGradingstatus(String gradingstatus) {
        this.gradingstatus = gradingstatus;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "MoodleSubmission [id=" + id + ", userid=" + userid + "]";
    }

}
