# Changelog

## 1.0.0 (2026-07-16)

Initial release.

### Added
- RDAP domain registration check via IANA bootstrap
- Built-in fallback for TLDs without IANA registration (`.de`, `.ch`, `.us`, `.no`, `.nl`)
- Custom server registration via `registerServer(tld, url)`
- IDN/Punycode support
- Nameserver extraction from RDAP responses
- Thread-safe caching of IANA bootstrap data
