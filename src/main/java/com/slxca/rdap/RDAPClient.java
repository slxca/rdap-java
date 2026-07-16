package com.slxca.rdap;

import com.slxca.rdap.bootstrap.BootstrapLoader;
import com.slxca.rdap.bootstrap.TldServerMap;

import java.net.IDN;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class RDAPClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final BootstrapLoader bootstrapLoader;
    private final HttpClient httpClient;
    private final Map<String, List<String>> customServers = new ConcurrentHashMap<>();

    public RDAPClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public RDAPClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.bootstrapLoader = new BootstrapLoader(httpClient);
    }

    public void registerServer(String tld, String serverUrl) {
        customServers.put(tld.toLowerCase(), Collections.singletonList(serverUrl));
    }

    public RDAPResult checkDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("Domain must not be null or blank");
        }

        String normalizedDomain = domain.trim().toLowerCase();
        try {
            normalizedDomain = IDN.toASCII(normalizedDomain);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid domain: " + domain, e);
        }

        String tld = extractTld(normalizedDomain);

        List<String> servers = customServers.get(tld);
        if (servers == null) {
            TldServerMap serverMap = bootstrapLoader.load();
            servers = serverMap.getServers(tld);
        }

        if (servers == null || servers.isEmpty()) {
            throw new RDAPException("No RDAP server found for TLD: ." + tld);
        }

        RDAPException lastError = null;
        for (String serverUrl : servers) {
            try {
                return queryServer(serverUrl, normalizedDomain);
            } catch (RDAPException e) {
                lastError = e;
            }
        }

        throw new RDAPException("All RDAP servers failed for domain: " + domain,
                lastError);
    }

    private RDAPResult queryServer(String baseUrl, String domain) {
        String url = baseUrl.endsWith("/")
                ? baseUrl + "domain/" + domain
                : baseUrl + "/domain/" + domain;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/rdap+json, application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            boolean registered = response.statusCode() == 200;
            List<String> nameservers = extractNameservers(response.body());
            return new RDAPResult(domain, registered, baseUrl, nameservers);
        } catch (RDAPException e) {
            throw e;
        } catch (Exception e) {
            throw new RDAPException("Failed to query RDAP server at " + url, e);
        }
    }

    static List<String> extractNameservers(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return Collections.emptyList();
        }
        try {
            JSONObject json = new JSONObject(responseBody);
            if (!json.has("nameservers")) {
                return Collections.emptyList();
            }
            JSONArray nsArray = json.getJSONArray("nameservers");
            List<String> nameservers = new ArrayList<>(nsArray.length());
            for (int i = 0; i < nsArray.length(); i++) {
                JSONObject ns = nsArray.getJSONObject(i);
                if (ns.has("ldhName")) {
                    nameservers.add(ns.getString("ldhName").toLowerCase());
                }
            }
            return nameservers;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    static String extractTld(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot == -1 || lastDot == domain.length() - 1) {
            throw new IllegalArgumentException("Invalid domain: " + domain);
        }
        return domain.substring(lastDot + 1);
    }
}
