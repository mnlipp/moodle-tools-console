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
 * Models the result of a Moodle token request.
 */
public class MoodleTokens {

    private final String token;
    private final String privatetoken;

    /**
     * Instantiates a new moodle tokens.
     *
     * @param token the token
     * @param privatetoken the privatetoken
     */
    @ConstructorProperties({ "token", "privatetoken" })
    public MoodleTokens(String token, String privatetoken) {
        super();
        this.token = token;
        this.privatetoken = privatetoken;
    }

    /**
     * Gets the token.
     *
     * @return the token
     */
    public String getToken() {
        return token;
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
