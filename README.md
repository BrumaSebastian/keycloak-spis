# Keycloak SPIs

Custom Service Provider Interface (SPI) extensions for Keycloak.

## Overview

This project provides custom SPIs for Keycloak, built as a multi-module Maven project. The SPIs extend Keycloak's functionality by implementing custom providers for various authentication and authorization scenarios.

## Project Structure

```
keycloak-spis/
├── README.md
├── LICENSE
└── keycloak-spis/              # Main Maven project
    ├── pom.xml                 # Parent POM
    ├── common/                 # Shared utilities and common code
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/
    │       ├── main/resources/
    │       └── test/java/
    └── spi-policy/             # Policy SPI implementation
        ├── pom.xml
        └── src/
            ├── main/java/
            ├── main/resources/
            │   └── META-INF/
            │       └── services/   # SPI service declarations
            └── test/java/
```

## Modules

### Common Module

- **Artifact ID**: `common`
- **Purpose**: Shared utilities and common code used across multiple SPI implementations
- **Status**: Structure created, implementation pending

### SPI Policy Module

- **Artifact ID**: `spi-policy`
- **Purpose**: Custom policy SPI implementation for Keycloak
- **Status**: Structure created, implementation pending

## Technical Details

- **Keycloak Version**: 26.3.3
- **Java Version**: 17
- **Build Tool**: Maven
- **Group ID**: `com.keycloak.spis`
- **Version**: 1.0.0

## Dependencies

The project uses the following Keycloak dependencies (managed in parent POM):

- `keycloak-core`
- `keycloak-server-spi`
- `keycloak-server-spi-private`

## Build Profiles

### Default Profile

```bash
mvn clean install
```

Builds: `common` and `spi-policy` modules

### Available Profiles

- **`with-policy`**: Includes common and spi-policy modules (default behavior)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Keycloak 26.3.3 (for testing and deployment)

### Building the Project

1. Clone the repository:

```bash
git clone <repository-url>
cd keycloak-spis
```

2. Build all modules:

```bash
cd keycloak-spis
mvn clean install
```

3. Build specific profile:

```bash
mvn clean install -Pwith-policy
```

## Development Status

🚧 **Project is currently in initial setup phase**

- [x] Maven project structure created
- [x] Parent POM configuration
- [x] Module structure (common, spi-policy)
- [x] Keycloak dependencies configured
- [ ] SPI implementations
- [ ] Service provider declarations
- [ ] Unit tests
- [ ] Integration tests
- [ ] Documentation

## Deployment

(Deployment instructions will be added once SPI implementations are complete)

## Contributing

(Contributing guidelines will be added)

## License

This project is licensed under the terms specified in the LICENSE file.
