# Jibble Attendance Integration — modernized Spring Boot project

This project synchronizes Jibble time-entry / attendance data into PostgreSQL and exposes the local copy through a Spring Boot REST API.

## Supported toolchain

This corrected version deliberately targets a **stable, officially supported combination**:

- **Java:** 26
- **Spring Boot:** 4.1.1
- **Spring Framework:** managed by Spring Boot
- **Jackson:** 3 (`tools.jackson.*`)
- **Maven:** 3.9.16 recommended
- **PostgreSQL:** use a currently supported PostgreSQL release

### Java version used by this project

This project targets Java 21. Java 21 is an LTS release and is within the supported Java range for Spring Boot 4.1.1. Keeping the Maven target at 21 also avoids `release version 26 not supported` when Eclipse or Maven is running on JDK 21.

Do not move this project to a Spring Boot 4.2 milestone/snapshot merely to use JDK 27 in production unless you have a specific reason to run preview framework software.

## Important corrections made

1. Replaced legacy Jackson 2 imports:

   ```java
   com.fasterxml.jackson.databind.JsonNode
   ```

   with Jackson 3:

   ```java
   tools.jackson.databind.JsonNode
   ```

2. Removed deprecated Jackson 3 `JsonNode.asText()` usage and replaced it with `asString()`.
3. Added `spring-boot-starter-restclient`, which provides Spring Boot 4 REST-client auto-configuration and the injectable `RestClient.Builder`.
4. Reworked Jibble authentication JSON into a typed `TokenResponse` Java record instead of manually reading an old Jackson tree.
5. Made Jibble JSON parsing null-safe when fields or nested objects are missing.
6. Improved scheduler logging so the full exception/stack trace is available.
7. Explicitly configured the PostgreSQL JDBC driver and the `public` Hibernate schema.
8. Removed unused imports and other stale code.

## Architecture

```text
Jibble
  |
  | HTTPS / Bearer authentication
  v
Spring Boot
  |
  | JPA / Hibernate
  v
PostgreSQL
  |
  v
/api/attendance
```

## 1. Install the required local tools

On Windows, confirm:

```powershell
java -version
```

For this project, use JDK 21. Use Maven 3.6.3 or later; a current Maven 3.9.x release is recommended.

```powershell
mvn -version
```

Make sure Maven reports the same JDK you intend Eclipse to use.

## 2. Create PostgreSQL database

In pgAdmin Query Tool:

```sql
CREATE DATABASE jibble_attendance;
```

The application uses Hibernate schema update for local development, so it can create the attendance table automatically.

## 3. Configure database credentials

Preferred approach: environment variables.

PowerShell example:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/jibble_attendance"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="YOUR_POSTGRES_PASSWORD"
```

Do not commit real passwords to Git.

## 4. Configure Jibble authentication

Jibble recommends a personal access token for a custom system/testing. Configure:

```powershell
$env:JIBBLE_PERSONAL_ACCESS_TOKEN="YOUR_TOKEN"
```

The project also retains Client ID / Client Secret support:

```powershell
$env:JIBBLE_CLIENT_ID="YOUR_CLIENT_ID"
$env:JIBBLE_CLIENT_SECRET="YOUR_CLIENT_SECRET"
```

Use one authentication approach at a time. If a personal access token is present, it takes precedence.

## 5. Import into Eclipse

1. **File → Import**
2. **Maven → Existing Maven Projects**
3. Select this folder.
4. Finish.
5. Right-click the project → **Maven → Update Project**.
6. Ensure Eclipse's installed JRE/JDK points to JDK 21.
7. Add your environment variables in **Run Configurations → Environment**.

## 6. Run

From Eclipse, run:

```text
JibbleAttendanceApplication.java
```

Or from PowerShell:

```powershell
mvn spring-boot:run
```

The application listens on:

```text
http://localhost:8080
```

## 7. Test Jibble connectivity

```http
GET http://localhost:8080/api/jibble/test
```

A successful response can return zero entries if there are simply no time entries for the current day. `connected: true` is the important part.

## 8. Synchronize a date range

```http
POST http://localhost:8080/api/jibble/sync?from=2026-09-15&to=2026-09-18
```

## 9. Read stored attendance

```http
GET http://localhost:8080/api/attendance?from=2026-09-15&to=2026-09-18
```

For one Jibble person ID:

```http
GET http://localhost:8080/api/attendance/person/JIBBLE_PERSON_ID?from=2026-09-15&to=2026-09-18
```

## 10. Verify PostgreSQL

```sql
SELECT *
FROM public.jibble_attendance_records
ORDER BY entry_time DESC;
```

## Automatic synchronization

By default the scheduler starts after 15 seconds and then runs every 5 minutes:

```properties
jibble.sync.initial-delay-ms=15000
jibble.sync.fixed-delay-ms=300000
jibble.sync.lookback-days=2
```

The lookback allows recently edited Jibble entries to update the local PostgreSQL copy.

## Troubleshooting

### `RestClient.Builder` bean could not be found

Make sure `pom.xml` contains:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-restclient</artifactId>
</dependency>
```

Then **Maven → Update Project** in Eclipse.

### `Unable to determine Dialect without JDBC metadata`

This usually means the database connection itself failed. Verify PostgreSQL is running and check `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The PostgreSQL JDBC driver is explicitly configured in `application.properties`; an explicit Hibernate dialect is normally unnecessary.

### `401 Unauthorized` from Jibble

The HTTP connection is working, but the token/credentials were rejected. Re-create or verify the Jibble API credential and its permissions.

### Jibble returns no records

Test a date range in which employees definitely clocked in/out. A successful API response containing zero entries is not necessarily an integration failure.

## Production notes

Before production deployment:

- use a secrets manager rather than plain environment variables where possible;
- protect `/api/attendance` with authentication/authorization;
- use HTTPS;
- replace `spring.jpa.hibernate.ddl-auto=update` with Flyway or Liquibase migrations;
- define privacy and retention rules for employee attendance/location data;
- add operational monitoring and retry/backoff policies appropriate to Jibble's API limits.
