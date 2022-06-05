/*
 * Moodle-Tools
 * Copyright (C) 2021  Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.mnl.moodle.provider;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Wraps a collection to enforce non-standard encoding in a query.  
 */
public class CommaSeparatedValues implements QueryValueEncoder {

    private final Collection<?> values;

    /**
     * Instantiates a new object.
     *
     * @param values the values
     */
    public CommaSeparatedValues(Collection<?> values) {
        this.values = values;
    }

    /**
     * Returns the string to use as value in a query. 
     *
     * @return the string
     */
    public String asQueryValue() {
        return values.stream()
            .map(v -> URLEncoder.encode(v.toString(), Charset.forName("utf-8")))
            .collect(Collectors.joining(","));
    }

}
