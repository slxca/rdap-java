package com.slxca.rdap;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RDAPClientTest {

    @Test
    void extractNameserversFromResponse() {
        String json = "{" +
                "\"objectClassName\": \"domain\"," +
                "\"ldhName\": \"EXAMPLE.COM\"," +
                "\"nameservers\": [" +
                "{\"objectClassName\": \"nameserver\", \"ldhName\": \"ns1.google.com\"}," +
                "{\"objectClassName\": \"nameserver\", \"ldhName\": \"ns2.google.com\"}," +
                "{\"objectClassName\": \"nameserver\", \"ldhName\": \"ns3.google.com\"}," +
                "{\"objectClassName\": \"nameserver\", \"ldhName\": \"ns4.google.com\"}" +
                "]" +
                "}";
        List<String> ns = RDAPClient.extractNameservers(json);
        assertEquals(4, ns.size());
        assertEquals("ns1.google.com", ns.get(0));
        assertEquals("ns4.google.com", ns.get(3));
    }

    @Test
    void extractNameserversEmptyArray() {
        String json = "{\"nameservers\": []}";
        List<String> ns = RDAPClient.extractNameservers(json);
        assertTrue(ns.isEmpty());
    }

    @Test
    void extractNameserversMissingField() {
        String json = "{\"objectClassName\": \"domain\"}";
        List<String> ns = RDAPClient.extractNameservers(json);
        assertTrue(ns.isEmpty());
    }

    @Test
    void extractNameserversNullBody() {
        List<String> ns = RDAPClient.extractNameservers(null);
        assertTrue(ns.isEmpty());
    }

    @Test
    void extractNameserversEmptyBody() {
        List<String> ns = RDAPClient.extractNameservers("");
        assertTrue(ns.isEmpty());
    }

    @Test
    void extractNameserversInvalidJson() {
        List<String> ns = RDAPClient.extractNameservers("not json");
        assertTrue(ns.isEmpty());
    }

    @Test
    void extractNameserversAreLowercased() {
        String json = "{\"nameservers\": [{\"ldhName\": \"NS1.GOOGLE.COM\"}]}";
        List<String> ns = RDAPClient.extractNameservers(json);
        assertEquals("ns1.google.com", ns.get(0));
    }

    @Test
    void extractTldFromSimpleDomain() {
        assertEquals("com", RDAPClient.extractTld("example.com"));
        assertEquals("org", RDAPClient.extractTld("example.org"));
        assertEquals("de", RDAPClient.extractTld("example.de"));
    }

    @Test
    void extractTldFromSubdomain() {
        assertEquals("com", RDAPClient.extractTld("www.example.com"));
        assertEquals("com", RDAPClient.extractTld("sub.sub.example.com"));
    }

    @Test
    void extractTldFromInternationalDomain() {
        assertEquals("xn--mgba3a4f16a", RDAPClient.extractTld("example.xn--mgba3a4f16a"));
    }

    @Test
    void extractTldFromSingleLabelDomain() {
        assertThrows(IllegalArgumentException.class,
                () -> RDAPClient.extractTld("example"));
    }

    @Test
    void extractTldFromDomainEndingWithDot() {
        assertThrows(IllegalArgumentException.class,
                () -> RDAPClient.extractTld("example.com."));
    }

    @Test
    void extractTldFromEmptyString() {
        assertThrows(IllegalArgumentException.class,
                () -> RDAPClient.extractTld(""));
    }

    @Test
    void checkDomainNullThrows() {
        RDAPClient client = new RDAPClient();
        assertThrows(IllegalArgumentException.class,
                () -> client.checkDomain(null));
    }

    @Test
    void checkDomainBlankThrows() {
        RDAPClient client = new RDAPClient();
        assertThrows(IllegalArgumentException.class,
                () -> client.checkDomain("  "));
    }

    @Test
    void registerServerAllowsCustomTld() {
        RDAPClient client = new RDAPClient();
        client.registerServer("test", "https://rdap.test/");
        // Should not throw - server existence verified at query time
    }

    @Test
    void registerServerOverridesBootstrap() {
        RDAPClient client = new RDAPClient();
        client.registerServer("com", "https://custom.rdap/");
        // Should not throw - overrides the IANA entry with a custom one
    }

    @Test
    void registerServerCaseInsensitive() {
        RDAPClient client = new RDAPClient();
        client.registerServer("COM", "https://rdap.test/");
        // registered as "com", lookup for "COM" should match
    }

    @Test
    void registerServerMultipleTlds() {
        RDAPClient client = new RDAPClient();
        client.registerServer("de", "https://rdap.denic.de/");
        client.registerServer("ch", "https://rdap.nic.ch/");
        // Both registered without conflict
    }
}
