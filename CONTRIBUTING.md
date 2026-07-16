# Contributing

Thank you for contributing to rdap-java.

## How to contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -am 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

## Development setup

```bash
git clone https://github.com/your-username/rdap-java.git
cd rdap-java
./gradlew build
```

## Code style

- Java 11+
- No external dependencies except `org.json`
- No comments in code (self-documenting code)
- Follow existing naming conventions
- Keep methods small and focused

## Testing

All changes must include tests:

```bash
./gradlew test
```

- Unit tests use JUnit 5
- Tests must not require network access (mock data where needed)
- New features must have at least one positive and one negative test

## Pull request checklist

- [ ] Code compiles (`./gradlew build`)
- [ ] All tests pass (`./gradlew test`)
- [ ] New code includes tests
- [ ] Documentation updated (README.md if applicable)
- [ ] CHANGELOG.md updated

## Reporting issues

- Use the issue templates (Bug Report / Feature Request)
- Include the library version, Java version, and relevant code
- For bugs: include the full stack trace and the domain that triggered it
