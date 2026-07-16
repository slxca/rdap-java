# rdap-java

[![CI](https://github.com/slxca/rdap-java/actions/workflows/ci.yml/badge.svg)](https://github.com/slxca/rdap-java/actions/workflows/ci.yml)
[![Java 11+](https://img.shields.io/badge/java-11%2B-blue)](#requirements)
[![MIT License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.slxca/rdap-java)](https://central.sonatype.com/artifact/com.slxca/rdap-java)

Lightweight RDAP client library to check if a domain is registered.

Supports **all TLDs** via IANA bootstrap data + built-in fallback for TLDs not yet registered with IANA.

## Features

- Check domain registration status (registered / available)
- Supports **all TLDs** (including IDN TLDs) via IANA bootstrap + curated fallback
- Automatic RDAP server discovery per TLD
- Fallback across multiple RDAP servers per TLD
- Custom server registration for unsupported TLDs
- IDN/Punycode support
- Nameserver extraction from RDAP responses
- Thread-safe, cached IANA bootstrap
- Zero heavy frameworks – just Java 11+ and `org.json`
- ~5 KB library code

## Requirements

- Java 11+

## Installation

### Gradle

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

### Manual

Download the JAR from [Releases](https://github.com/slxca/rdap-java/releases).

## Usage

### Basic check

```java
import com.slxca.rdap.RDAPClient;
import com.slxca.rdap.RDAPResult;

RDAPClient client = new RDAPClient();
RDAPResult result = client.checkDomain("google.com");

if (result.isRegistered()) {
    System.out.println(result.getDomain() + " is registered");
} else {
    System.out.println(result.getDomain() + " is available");
}
```

Output:

```
google.com is registered
```

### Nameservers

```java
RDAPResult result = client.checkDomain("google.com");
for (String ns : result.getNameservers()) {
    System.out.println("NS: " + ns);
}
```

### Custom RDAP server

```java
client.registerServer("io", "https://rdap.afilias.net/rdap/");
RDAPResult result = client.checkDomain("example.io");
```

### Command line

```bash
java -jar rdap-java.jar google.com
```

## API

### `RDAPClient`

| Method | Returns | Description |
|---|---|---|
| `RDAPClient()` | | Creates client with default `HttpClient` |
| `RDAPClient(HttpClient)` | | Creates client with custom `HttpClient` |
| `checkDomain(String domain)` | `RDAPResult` | Queries RDAP for the domain |
| `registerServer(String tld, String serverUrl)` | `void` | Registers a custom RDAP server for a TLD |

### `RDAPResult`

| Method | Returns | Description |
|---|---|---|
| `getDomain()` | `String` | The queried domain |
| `isRegistered()` | `boolean` | `true` if HTTP 200, `false` if 404 |
| `getServerUrl()` | `String` | RDAP server used |
| `getNameservers()` | `List<String>` | Nameservers from the RDAP response |

### Exceptions

- `RDAPException` – network errors or no RDAP server found for TLD
- `IllegalArgumentException` – null/blank/invalid domain

## How it works

1. **TLD extraction** – Extracts the top-level domain from the input (e.g. `example.com` → `com`)
2. **Server lookup** – Checks custom servers → IANA bootstrap → built-in fallback
3. **RDAP query** – Sends `GET {serverUrl}/domain/{domain}` with `Accept: application/rdap+json`
4. **Result** – HTTP 200 → registered, HTTP 404 → not registered

## Supported TLDs

| Source | Count | Examples |
|---|---|---|
| IANA bootstrap | ~1200 | `.com`, `.org`, `.net`, `.uk`, `.fr` |
| Built-in fallback | 5 | `.de`, `.ch`, `.us`, `.no`, `.nl` |

Missing a TLD? [Open an issue](https://github.com/slxca/rdap-java/issues) or use `registerServer()` to add it yourself.