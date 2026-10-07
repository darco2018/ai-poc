---
sessionId: session-261007-155037-3mva
---

# Requirements

### Overview & Goals
Set H2 as the default in-memory database profile instead of MySQL so the application can run out-of-the-box without requiring an external MySQL server instance. Ensure the H2 web console is cleanly configured and disabled when MySQL is active, and verify via logs that the respective database and datasource initialize properly when switching profiles.

### Scope
- **In Scope**:
  - Update `application.properties` to set `spring.profiles.default=h2` (or set default active profile to `h2`).
  - Disable H2 console in base/MySQL profile (`spring.h2.console.enabled=false` in `application.properties` or `application-mysql.properties`) while enabling it in `application-h2.properties`.
  - Validate profile switching and check startup logs for both H2 and MySQL profiles to confirm correct database initialization and console availability.
- **Out of Scope**:
  - Modifying Anthropic document question services or controller logic.
  - Adding new database entities or schema migrations.

### User Stories
- As a developer, I want H2 to be the default database so that I can start and test the application immediately without running a local MySQL server.
- As a developer, I want the H2 console to only be active when running with H2, avoiding misleading console log messages when MySQL is selected.

### Functional Requirements
- When the application starts with default properties (no explicit profile specified), the `h2` profile must be loaded with an in-memory datasource `jdbc:h2:mem:ai_poc` and H2 console enabled at `/h2-console`.
- When `spring.profiles.default=mysql` (or when the `mysql` profile is explicitly active), the datasource must connect to `jdbc:mysql://...` and the H2 console must not be exposed.
- Log outputs must clearly verify which database datasource and console configuration is active.

# Technical Design

### Current Implementation
- `src/main/resources/application.properties` defines `spring.profiles.default=mysql`.
- `build.gradle` includes `developmentOnly 'org.springframework.boot:spring-boot-h2console'` and `runtimeOnly 'com.h2database:h2'`.
- Spring Boot's `H2ConsoleAutoConfiguration` enables the H2 console whenever `h2` and the web starter are on classpath and not explicitly disabled.
- `src/main/resources/application-h2.properties` sets `spring.h2.console.enabled=true`, but `application.properties` and `application-mysql.properties` do not explicitly disable it. Thus, when `mysql` profile is active, the H2 console still auto-configures.

### Key Decisions
1. **Change Default Profile to H2**:
   Update `src/main/resources/application.properties` to set `spring.profiles.default=h2`.
2. **Explicitly Control H2 Console**:
   Set `spring.h2.console.enabled=false` by default in `src/main/resources/application.properties` (or `application-mysql.properties`), and keep `spring.h2.console.enabled=true` in `application-h2.properties`. This ensures the H2 console is only available when the H2 profile is active.
3. **Log & Profile Verification**:
   Execute the application under both profiles (`h2` default and `mysql`) and inspect Spring Boot startup logs to verify the correct database URLs and H2 console status.

### Proposed Changes

#### 1. `src/main/resources/application.properties`
- Change `spring.profiles.default=mysql` to `spring.profiles.default=h2`.
- Update the documentation comment to reflect that H2 is now the default and MySQL is optional.
- Explicitly set `spring.h2.console.enabled=false` in base `application.properties` so non-H2 profiles do not start the H2 console.

#### 2. `src/main/resources/application-h2.properties`
- Keep `spring.h2.console.enabled=true` and `spring.h2.console.path=/h2-console`.

#### 3. `src/main/resources/application-mysql.properties`
- Ensure `spring.h2.console.enabled=false` is enforced for MySQL profile.

# Testing

### Validation Approach
Verify configuration changes and behavior through automated tests and application startup log inspection for both profiles.

### Key Scenarios
1. **Default Startup (H2)**:
   - Start application with default settings.
   - Verify logs show H2 in-memory URL `jdbc:h2:mem:ai_poc` and `H2 console available at '/h2-console'`.
2. **MySQL Profile Startup**:
   - Start application with `--spring.profiles.active=mysql` or `spring.profiles.default=mysql`.
   - Verify logs show MySQL connection attempt to `jdbc:mysql://...` and verify H2 console is NOT activated.
3. **Automated Test Suite**:
   - Run `./gradlew test` to ensure existing context loads and unit tests pass.

# Delivery Steps

### ✓ Step 1: Update application properties to set H2 as default and restrict H2 console
H2 is set as the default database profile and the H2 console is disabled when running non-H2 profiles.

- Update `spring.profiles.default=h2` in `src/main/resources/application.properties`.
- Set `spring.h2.console.enabled=false` in `src/main/resources/application.properties` and `src/main/resources/application-mysql.properties`.
- Verify `src/main/resources/application-h2.properties` retains `spring.h2.console.enabled=true`.

### ✓ Step 2: Test settings across H2 and MySQL profiles and verify startup logs
Startup logs confirm the appropriate database and console configurations are spun up for each profile.

- Run `./gradlew.bat test` to verify test suite execution.
- Start application with default profile (`h2`) and verify logs confirm H2 datasource and `/h2-console` endpoint.
- Start application with `mysql` profile and verify logs confirm MySQL datasource and that H2 console is disabled.