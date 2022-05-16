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

package de.mnl.moodle.provider;

import java.util.Iterator;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import de.mnl.moodle.service.MoodleService;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true)
public class MoodleServiceProvider implements MoodleService {

    public MoodleServiceProvider() {
        println("=== Constructor ===");
    }

    @Activate
    public void activate(Map properties) {
        println("=== Activate " + properties.get("number") + " ===");
        println(properties);
    }

    @Modified
    public void modified(Map properties) {
        println("=== Modified " + properties.get("number") + " ===");
        println(properties);
    }

    @Deactivate
    public void deactivate(Map properties) {
        println("=== Deactivate " + properties.get("number") + " ===");
        println(properties);
    }

    private void println(Map properties) {
        if (properties != null) {
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                println(it.next().toString());
            }
        }
    }

    private void println(String message) {
        System.out.println(message);
    }

}
