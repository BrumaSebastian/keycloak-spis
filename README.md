# Keycloak SPIs

Custom Service Provider Interface (SPI) extensions for Keycloak.

## Overview

This multi-module Maven project provides custom SPIs that extend Keycloak's functionality (authorization policies, REST resources, and provider wiring). Modules are designed to be packaged as JARs and dropped into a Keycloak deployment.

## Project Structure

```
keycloak-spis/
├── README.md
├── LICENSE
└── keycloak-spis/                    # Parent Maven project
    ├── pom.xml                       # Parent POM and dependency management
    ├── common/                       # Shared utilities and common code
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/
    │       └── test/java/
    ├── spi-policy/                   # Authorization Policy SPI implementation(s)
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/
    │       ├── main/resources/
    │       │   └── META-INF/services # SPI service declarations
    │       └── test/java/
    ├── spi-providers/                # Provider SPI and factories wiring
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/
    │       ├── main/resources/
    │       │   └── META-INF/services # Provider + factory registrations
    │       └── test/java/
    └── spi-resources/                # Additional REST resources and testing helpers
        ├── pom.xml
        └── src/
            ├── main/java/
            ├── main/resources/
            │   └── META-INF/services
            └── test/java/
```

## Modules

- **`common`**: Shared code used across modules.
- **`spi-policy`**: Custom `PolicyProvider` implementations and related wiring.
- **`spi-providers`**: SPI definitions, factories, and provider registrations (e.g., `org.keycloak.provider.Spi`).
- **`spi-resources`**: REST endpoints and testing utilities, including test realm files.

## Technical Details

- **Keycloak Version**: 26.3.3
- **Java Version**: 17
- **Build Tool**: Maven
- **Group ID**: `com.keycloak.spis`
- **Version**: 1.0.0

## Dependencies

Managed at the parent POM; commonly used:

- `keycloak-core`
- `keycloak-server-spi`
- `keycloak-server-spi-private`

## Build

On Windows, use a terminal (PowerShell or Git Bash).

```bash
mvn -v
mvn clean install
```

Artifacts are produced under each module's `target/` folder as JARs.

## Maven Only

This project is a standard multi-module Maven build.

- **Prerequisites**: Java 17, Maven 3.6+
- **Modules built**: `common`, `spi-policy`, `spi-providers`, `spi-resources`
- **Top-level build**:

  ```bash
  mvn clean install
  ```

- **Build a single module** (from the repo root or module dir):

  ```bash
  mvn -pl keycloak-spis/spi-policy -am clean install
  ```

  - `-pl` selects the module; `-am` builds required dependencies.

- **Run tests only**:

  ```bash
  mvn -q test
  ```

- **Skip tests**:

  ```bash
  mvn -DskipTests clean install
  ```

- **View effective POM** (to inspect dependency management):

  ```bash
  mvn help:effective-pom
  ```

- **Local install vs. package**:
  - `mvn install` installs artifacts to your local Maven repo (`~/.m2/repository`).
  - `mvn package` creates JARs in each module's `target/` without installing.

## Install in Keycloak

Copy the built JARs into your Keycloak `providers/` folder.

## Testing

- Unit tests live in each module under `src/test/java`.
- The `spi-resources` module includes test resources like `main-realm.json` and `test.properties`.

Run all tests:

```bash
mvn -q test
```

## Development Status

- [x] Maven project structure created
- [x] Parent POM configuration
- [x] Modules: common, spi-policy, spi-providers, spi-resources
- [x] Keycloak dependencies configured
- [ ] SPI implementations finalized
- [ ] Expanded service declarations
- [ ] Unit and integration tests
- [ ] Usage documentation and examples

## Contributing

PRs and issues are welcome. Please include module, Keycloak version, and a minimal reproduction when reporting problems.

## License

See LICENSE for details.
