package com.slxca.rdap.bootstrap;

import com.slxca.rdap.RDAPException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BootstrapLoader {
    private static final String DNS_BOOTSTRAP_URL = "https://data.iana.org/rdap/dns.json";
    private static final String FALLBACK_RESOURCE = "/rdap-fallback.json";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private volatile TldServerMap cached;

    public BootstrapLoader() {
        this(HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public BootstrapLoader(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public TldServerMap load() {
        if (cached != null) return cached;
        synchronized (this) {
            if (cached != null) return cached;
            TldServerMap map = loadFallback();
            TldServerMap iana = fetchBootstrap();
            map.putAll(iana);
            cached = map;
            return cached;
        }
    }

    private TldServerMap fetchBootstrap() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DNS_BOOTSTRAP_URL))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RDAPException(
                        "Failed to fetch IANA bootstrap: HTTP " + response.statusCode());
            }

            return parse(response.body());
        } catch (RDAPException e) {
            throw e;
        } catch (Exception e) {
            throw new RDAPException("Failed to load IANA bootstrap", e);
        }
    }

    private TldServerMap loadFallback() {
        try (InputStream in = getClass().getResourceAsStream(FALLBACK_RESOURCE)) {
            if (in == null) return new TldServerMap();
            String json = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return parse(json);
        } catch (Exception e) {
            return new TldServerMap();
        }
    }

    public void refresh() {
        synchronized (this) {
            cached = null;
        }
    }

    static TldServerMap parse(String json) {
        JSONObject root = new JSONObject(json);
        JSONArray services = getServicesArray(root);
        TldServerMap map = new TldServerMap();

        for (int i = 0; i < services.length(); i++) {
            JSONArray entry = services.getJSONArray(i);
            JSONArray tlds = entry.getJSONArray(0);
            JSONArray urls = entry.getJSONArray(1);

            List<String> serverList = new ArrayList<>();
            for (int j = 0; j < urls.length(); j++) {
                serverList.add(urls.getString(j));
            }

            for (int j = 0; j < tlds.length(); j++) {
                map.put(tlds.getString(j).toLowerCase(), serverList);
            }
        }

        return map;
    }

    private static JSONArray getServicesArray(JSONObject root) {
        if (root.has("services")) {
            return root.getJSONArray("services");
        }
        if (root.has("fallback")) {
            return root.getJSONArray("fallback");
        }
        throw new RDAPException("No 'services' or 'fallback' array found in bootstrap JSON");
    }
}
