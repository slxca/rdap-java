package com.slxca.rdap;

import java.util.Collections;
import java.util.List;

public class RDAPResult {
    private final String domain;
    private final boolean registered;
    private final String serverUrl;
    private final List<String> nameservers;

    public RDAPResult(String domain, boolean registered, String serverUrl) {
        this(domain, registered, serverUrl, Collections.emptyList());
    }

    public RDAPResult(String domain, boolean registered, String serverUrl, List<String> nameservers) {
        this.domain = domain;
        this.registered = registered;
        this.serverUrl = serverUrl;
        this.nameservers = nameservers != null
                ? Collections.unmodifiableList(nameservers)
                : Collections.emptyList();
    }

    public String getDomain() {
        return domain;
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public List<String> getNameservers() {
        return nameservers;
    }
}
