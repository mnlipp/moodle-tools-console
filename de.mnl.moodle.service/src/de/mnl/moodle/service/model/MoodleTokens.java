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

/**
 * Models the result of a Moodle token request.
 */
@SuppressWarnings("PMD.DataClass")
public class MoodleTokens extends MoodleErrorValues {

    private String token;
    private String privatetoken;

    /**
     * Gets the token.
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @param privatetoken the privatetoken to set
     */
    public void setPrivatetoken(String privatetoken) {
        this.privatetoken = privatetoken;
    }

    /**
     * Gets the privatetoken.
     *
     * @return the privatetoken
     */
    public String getPrivatetoken() {
        return privatetoken;
    }

}
