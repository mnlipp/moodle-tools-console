/*
 * Moodle Console Application
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

package de.mnl.mtc.credentialsmgr.events;

import de.mnl.mtc.credentialsmgr.Credentials;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Event;

/**
 * Informs handlers about new or updated {@link Credentials}.
 */
public class UpdateCredentials extends Event<Boolean> {

    private Credentials credentials;

    /**
     * Instantiates a new event.
     *
     * @param credentials the credentials
     * @param channels the channels
     */
    public UpdateCredentials(Credentials credentials, Channel... channels) {
        super(channels);
        this.credentials = credentials;
    }

    /**
     * @return the credentials
     */
    public Credentials credentials() {
        return credentials;
    }

    /**
     * @param credentials the credentials to set
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
