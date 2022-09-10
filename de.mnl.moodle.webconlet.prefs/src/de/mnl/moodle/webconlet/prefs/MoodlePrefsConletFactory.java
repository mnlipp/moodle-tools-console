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

package de.mnl.moodle.webconlet.prefs;

import java.util.Map;
import java.util.Optional;
import org.jgrapes.core.Channel;
import org.jgrapes.core.ComponentType;
import org.jgrapes.webconsole.base.ConletComponentFactory;

/**
 * A factory for creating HelloWorldConlet objects.
 */
public class MoodlePrefsConletFactory implements ConletComponentFactory {

    @Override
    public Class<? extends ComponentType> componentType() {
        return MoodlePrefsConlet.class;
    }

    @Override
    public Optional<ComponentType> create(
            Channel componentChannel, Map<?, ?> properties) {
        return Optional.of(new MoodlePrefsConlet(componentChannel));
    }

}
