# Modernization Notes

## Baseline chosen

- Spring Boot 4.1.1: current stable Spring Boot release at the time this project was corrected.
- Java 21: LTS target chosen for compatibility with the existing Eclipse/Windows development setup.
- Java 25: current LTS alternative if long-term operational stability matters more than using the newest supported Java language/runtime.
- Maven 3.9.16: current recommended Maven 3 release.

## Why the original JSON code failed

Spring Boot 4 uses Jackson 3 as its preferred/default JSON implementation. Jackson 3 moved databind packages from `com.fasterxml.jackson.databind` to `tools.jackson.databind` and renamed/deprecated several text-oriented APIs. Mixing a Jackson 2 `JsonNode` type into a Spring Boot 4/Jackson 3 HTTP stack can lead to incorrect conversion and confusing runtime behavior.

The corrected code uses `tools.jackson.databind.JsonNode` throughout and `asString()` instead of deprecated `asText()`.

## JDK 27

Spring Boot 4.1.1 supports Java 21, so there is no need to raise the project compiler target to Java 26. Keeping the target at 21 prevents compiler-release mismatches on a JDK 21 development environment.
