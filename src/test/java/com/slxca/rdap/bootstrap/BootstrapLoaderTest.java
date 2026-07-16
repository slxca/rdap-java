package com.slxca.rdap.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BootstrapLoaderTest {

    @Test
    void parseIanaFormat() {
        String json = "{" +
                "\"services\":[" +
                "[[\"com\"],[\"https://rdap.verisign.com/com/\"]]" +
                "]}";
        TldServerMap map = BootstrapLoader.parse(json);
        assertTrue(map.containsTld("com"));
        assertEquals("https://rdap.verisign.com/com/", map.getServers("com").get(0));
    }

    @Test
    void parseFallbackFormat() {
        String json = "{" +
                "\"fallback\":[" +
                "[[\"de\"],[\"https://rdap.denic.de/\"]]" +
                "]}";
        TldServerMap map = BootstrapLoader.parse(json);
        assertTrue(map.containsTld("de"));
        assertEquals("https://rdap.denic.de/", map.getServers("de").get(0));
    }

    @Test
    void parseIanaTakesPrecedenceOverFallback() {
        String iana = "{" +
                "\"services\":[" +
                "[[\"com\"],[\"https://rdap.iana.com/\"]]" +
                "]}";
        String fallback = "{" +
                "\"fallback\":[" +
                "[[\"com\"],[\"https://rdap.fallback.com/\"]]" +
                "]}";
        TldServerMap merged = BootstrapLoader.parse(fallback);
        merged.putAll(BootstrapLoader.parse(iana));
        assertEquals("https://rdap.iana.com/", merged.getServers("com").get(0));
    }

    @Test
    void parseMergesDifferentTlds() {
        String iana = "{" +
                "\"services\":[" +
                "[[\"com\"],[\"https://rdap.iana.com/\"]]" +
                "]}";
        String fallback = "{" +
                "\"fallback\":[" +
                "[[\"de\"],[\"https://rdap.denic.de/\"]]" +
                "]}";
        TldServerMap ianaMap = BootstrapLoader.parse(iana);
        TldServerMap fallbackMap = BootstrapLoader.parse(fallback);
        ianaMap.putAll(fallbackMap);
        assertTrue(ianaMap.containsTld("com"));
        assertTrue(ianaMap.containsTld("de"));
        assertEquals("https://rdap.iana.com/", ianaMap.getServers("com").get(0));
        assertEquals("https://rdap.denic.de/", ianaMap.getServers("de").get(0));
    }

    @Test
    void parseMultipleTldsPerEntry() {
        String json = "{" +
                "\"services\":[" +
                "[[\"com\",\"net\"],[\"https://rdap.verisign.com/\"]]" +
                "]}";
        TldServerMap map = BootstrapLoader.parse(json);
        assertEquals("https://rdap.verisign.com/", map.getServers("com").get(0));
        assertEquals("https://rdap.verisign.com/", map.getServers("net").get(0));
    }

    @Test
    void parseTldsAreLowercased() {
        String json = "{" +
                "\"services\":[" +
                "[[\"COM\"],[\"https://rdap.test.com/\"]]" +
                "]}";
        TldServerMap map = BootstrapLoader.parse(json);
        assertTrue(map.containsTld("com"));
        assertTrue(map.containsTld("COM"));
    }

    @Test
    void parseEmptyServices() {
        TldServerMap map = BootstrapLoader.parse("{\"services\":[]}");
        assertTrue(map.isEmpty());
    }

    @Test
    void parseEmptyFallback() {
        TldServerMap map = BootstrapLoader.parse("{\"fallback\":[]}");
        assertTrue(map.isEmpty());
    }
}
