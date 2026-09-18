# Jibble Attendance Integration – Spring Boot + PostgreSQL

This project connects a Java Spring Boot application to Jibble's API and continuously copies Jibble time-entry/attendance events into PostgreSQL.

## Mental model

Think of three boxes:

1. **Jibble** – employees clock in, take breaks, and clock out.
2. **Spring Boot** – a messenger that securely asks Jibble for recent time entries.
3. **PostgreSQL** – the notebook where the Spring Boot app keeps its local copy.

Flow:

Employee -> Jibble -> Jibble API -> Spring Boot -> PostgreSQL -> your own REST endpoints

The application never scrapes Jibble's web pages and never connects to Jibble's database.

## Requirements

- JDK 21
- Maven
- PostgreSQL
- Eclipse or another Java IDE
- Postman (recommended for testing)
- A Jibble organization with API credentials

## Database

Create a PostgreSQL database:

```sql
CREATE DATABASE jibble_attendance;
```

The application uses Hibernate `ddl-auto=update` for local development, so the table is created automatically.

## Jibble credentials

The application supports either:

### Personal access token

Set:

```text
JIBBLE_PERSONAL_ACCESS_TOKEN=...
```

### Client ID + Client Secret

Set:

```text
JIBBLE_CLIENT_ID=...
JIBBLE_CLIENT_SECRET=...
```

When Client ID and Client Secret are used, the app requests a bearer token from:

```text
https://identity.prod.jibble.io/connect/token
```

Never commit a real token or secret into Git.

## Windows PowerShell example

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/jibble_attendance"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="YOUR_POSTGRES_PASSWORD"

# Use EITHER the personal token:
$env:JIBBLE_PERSONAL_ACCESS_TOKEN="YOUR_TOKEN"

# OR the two client credentials:
$env:JIBBLE_CLIENT_ID="YOUR_CLIENT_ID"
$env:JIBBLE_CLIENT_SECRET="YOUR_CLIENT_SECRET"

mvn spring-boot:run
```

## Eclipse

1. File -> Import.
2. Maven -> Existing Maven Projects.
3. Select this project folder.
4. Finish.
5. Right-click project -> Maven -> Update Project.
6. Open Run -> Run Configurations.
7. Select the Spring Boot application / Java application.
8. Add the same environment variables under **Environment**.
9. Run `JibbleAttendanceApplication`.

## Test connection

```http
GET http://localhost:8080/api/jibble/test
```

This authenticates with Jibble and reads today's time entries. It does not write to PostgreSQL.

## Manually synchronize a date range

```http
POST http://localhost:8080/api/jibble/sync?from=2026-09-01&to=2026-09-16
```

## Read locally stored attendance

```http
GET http://localhost:8080/api/attendance?from=2026-09-01&to=2026-09-16
```

For one Jibble person:

```http
GET http://localhost:8080/api/attendance/person/JIBBLE_PERSON_ID?from=2026-09-01&to=2026-09-16
```

## Automatic synchronization

By default the app waits 15 seconds after startup, then synchronizes recent Jibble attendance every 5 minutes.

The sync intentionally re-reads the previous 2 days. This makes edited Jibble entries update the local PostgreSQL copy.

Settings:

```properties
jibble.sync.fixed-delay-ms=300000
jibble.sync.initial-delay-ms=15000
jibble.sync.lookback-days=2
```

## Why the raw JSON is also stored

Jibble may add or change optional fields over time. The application extracts common attendance fields into normal PostgreSQL columns, but it also stores the original Jibble JSON in `raw_payload`.

That gives you a safety net: you can inspect fields that were not mapped yet without re-downloading old data.

## Production notes

Before production use:

- keep credentials in a secret manager or protected environment variables;
- place this service behind authentication;
- use HTTPS;
- replace `ddl-auto=update` with Flyway/Liquibase migrations;
- add monitoring and alerting;
- confirm the exact Jibble API endpoints and permissions in the current Jibble API documentation;
- define your retention/privacy rules for employee attendance, GPS, selfie, and other personal data.
