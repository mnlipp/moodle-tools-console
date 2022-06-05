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
