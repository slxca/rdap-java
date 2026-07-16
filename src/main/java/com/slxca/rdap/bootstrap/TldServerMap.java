package com.slxca.rdap.bootstrap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TldServerMap {
    private final Map<String, List<String>> tldToServers = new ConcurrentHashMap<>();

    public void put(String tld, List<String> servers) {
        tldToServers.put(tld, Collections.unmodifiableList(servers));
    }

    public void putAll(TldServerMap other) {
        tldToServers.putAll(other.tldToServers);
    }

    public List<String> getServers(String tld) {
        return tldToServers.get(tld.toLowerCase());
    }

    public boolean containsTld(String tld) {
        return tldToServers.containsKey(tld.toLowerCase());
    }

    public boolean isEmpty() {
        return tldToServers.isEmpty();
    }

    public int size() {
        return tldToServers.size();
    }
}
