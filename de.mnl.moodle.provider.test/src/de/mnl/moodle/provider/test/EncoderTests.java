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

package de.mnl.moodle.provider.test;

import de.mnl.moodle.provider.RestClient;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EncoderTests {

    @Test
    public void testSimpleValues() {
        Map<String, Object> data = Map.of("k1", "p1", "k2", "p2");
        var res = Arrays.asList(RestClient.encodeData(data).split("&"));
        assertTrue(res.contains("k1=p1"));
        assertTrue(res.contains("k2=p2"));
    }

    @Test
    public void testNestedList() {
        Map<String, Object> data
            = Map.of("k1", "p1", "k2", List.of("l1", "l2"));
        var query = RestClient.encodeData(data);
        var decoded = URLDecoder.decode(query, Charset.forName("utf-8"));
        var res = Arrays.asList(decoded.split("&"));
        assertTrue(res.contains("k1=p1"));
        assertTrue(res.contains("k2[0]=l1"));
        assertTrue(res.contains("k2[1]=l2"));
    }

    @Test
    public void testNestedMap() {
        Map<String, Object> data
            = Map.of("k1", "p1", "k2", Map.of("n1", "l1", "n2", "l2"));
        var query = RestClient.encodeData(data);
        var decoded = URLDecoder.decode(query, Charset.forName("utf-8"));
        var res = Arrays.asList(decoded.split("&"));
        assertTrue(res.contains("k1=p1"));
        assertTrue(res.contains("k2[n1]=l1"));
        assertTrue(res.contains("k2[n2]=l2"));
    }
}
