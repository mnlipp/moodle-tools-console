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

package de.mnl.mtc.credentialsmgr;

import java.io.Serializable;

/**
 * UpdateCredentials as stored.
 */
@SuppressWarnings({ "serial", "PMD.DataClass" })
public class Credentials implements Serializable {

    private String resource;
    private String username;
    private String password;

    /**
     * Instantiates new credentials.
     *
     * @param resource the resource
     * @param username the username
     * @param password the password
     */
    public Credentials(String resource, String username, String password) {
        this.resource = resource;
        this.username = username;
        this.password = password;
    }

    /**
     * Instantiates a new credentials.
     */
    @SuppressWarnings("PMD.UncommentedEmptyConstructor")
    public Credentials() {
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
