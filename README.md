# rdap-java

[![CI](https://github.com/slxca/rdap-java/actions/workflows/ci.yml/badge.svg)](https://github.com/slxca/rdap-java/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.slxca/rdap-java.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.slxca/rdap-java)
[![Java 11+](https://img.shields.io/badge/java-11%2B-blue.svg)](#requirements)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A lightweight, high-performance RDAP client library for Java. A modern, structured alternative to legacy WHOIS socket queries to check domain registration status and availability.

Supports **all TLDs** via IANA bootstrap data, automatic RDAP server discovery, and built-in fallbacks.

---

## Features

- **Domain Availability Check:** Instantly determine if a domain is registered or available.
- **Universal TLD Support:** Covers ~1200+ TLDs via dynamic IANA bootstrap + curated fallbacks for unlisted registries.
- **Automatic Server Discovery:** Resolves official RDAP base URLs per TLD automatically with thread-safe caching.
- **Server Failover:** Built-in fallback mechanism across multiple RDAP servers per TLD.
- **IDN & Punycode Ready:** Native handling for internationalized domain names (e.g., umlauts, non-Latin scripts).
- **Nameserver Extraction:** Easily extract authoritative nameservers from RDAP responses.
- **Custom Endpoints:** Register custom RDAP server endpoints at runtime.
- **Zero Heavyweight Frameworks:** Uses pure Java 11+ `HttpClient` and `org.json` (~5 KB library overhead).

---

## Requirements

- **Java 11** or higher

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.slxca:rdap-java:1.0.0")
}

```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.slxca:rdap-java:1.0.0'
}

```

### Maven

```xml
<dependency>
    <groupId>com.slxca</groupId>
    <artifactId>rdap-java</artifactId>
    <version>1.0.0</version>
</dependency>

```

### Manual Download

Grab the latest pre-built `.jar` from the [Releases page](https://github.com/slxca/rdap-java/releases).

---

## Usage

### Check Domain Availability

```java
import com.slxca.rdap.RDAPClient;
import com.slxca.rdap.RDAPResult;
import com.slxca.rdap.RDAPException;

public class Main {
    public static void main(String[] args) {
        RDAPClient client = new RDAPClient();

        try {
            RDAPResult result = client.checkDomain("example.com");

            if (result.isRegistered()) {
                System.out.println(result.getDomain() + " is already taken.");
                System.out.println("RDAP Server: " + result.getServerUrl());
            } else {
                System.out.println(result.getDomain() + " is available for registration!");
            }
        } catch (RDAPException e) {
            System.err.println("RDAP lookup failed: " + e.getMessage());
        }
    }
}

```

### Extract Nameservers

```java
RDAPResult result = client.checkDomain("google.com");

if (result.isRegistered()) {
    System.out.println("Nameservers:");
    for (String ns : result.getNameservers()) {
        System.out.println(" - " + ns);
    }
}

```

### Custom RDAP Server Registration

If a registry is not part of the IANA bootstrap registry or uses a dedicated endpoint, register it manually:

```java
client.registerServer("io", "[https://rdap.afilias.net/rdap/](https://rdap.afilias.net/rdap/)");
RDAPResult result = client.checkDomain("example.io");

```

### Custom `HttpClient` Configuration

Pass your own configured `HttpClient` (e.g., custom timeouts, proxy settings):

```java
import java.net.http.HttpClient;
import java.time.Duration;

HttpClient customHttp = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build();

RDAPClient client = new RDAPClient(customHttp);

```

### Command Line Interface (CLI)

Run the packaged JAR directly from your terminal:

```bash
java -jar rdap-java.jar google.com

```

---

## API Reference

### `RDAPClient`

| Method | Returns | Description |
| --- | --- | --- |
| `RDAPClient()` |  | Creates an instance using the default `HttpClient`. |
| `RDAPClient(HttpClient client)` |  | Creates an instance using a custom `HttpClient`. |
| `checkDomain(String domain)` | `RDAPResult` | Performs an RDAP lookup for the given domain. |
| `registerServer(String tld, String serverUrl)` | `void` | Registers or overrides an RDAP server URL for a specific TLD. |

### `RDAPResult`

| Method | Returns | Description |
| --- | --- | --- |
| `getDomain()` | `String` | Returns the queried domain. |
| `isRegistered()` | `boolean` | Returns `true` if HTTP 200 (registered), `false` if HTTP 404 (available). |
| `getServerUrl()` | `String` | Returns the RDAP endpoint URL used for the query. |
| `getNameservers()` | `List<String>` | Returns authoritative nameservers parsed from the RDAP response. |

### Exceptions

* `RDAPException` – Thrown on network issues, HTTP errors other than 404, or if no RDAP endpoint was discoverable.
* `IllegalArgumentException` – Thrown when passing null, empty, or malformed domain strings.

---

## How It Works

1. **TLD Extraction:** Normalizes the input and isolates the top-level domain (e.g., `sub.example.co.uk` or `münchen.de` via IDN Punycode).
2. **Endpoint Resolution:** Checks custom registered servers first, then queries cached IANA bootstrap data, and falls back to curated registry lists.
3. **HTTP Query:** Sends an authenticated HTTP GET request with header `Accept: application/rdap+json`.
4. **Status Evaluation:** Treats HTTP 200 as registered and parses details; treats HTTP 404 as available.

---

## Supported TLDs

| Registry Source | Count | Examples |
| --- | --- | --- |
| **IANA Bootstrap** | ~1200+ | `.com`, `.org`, `.net`, `.uk`, `.fr` |
| **Built-in Fallbacks** | Curated | `.de`, `.ch`, `.us`, `.no`, `.nl` |

Missing a TLD? Feel free to [open an issue](https://github.com/slxca/rdap-java/issues) or submit a Pull Request.

---

## License

This project is licensed under the **MIT License**
