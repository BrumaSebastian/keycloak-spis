# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Git Workflow

**Never commit or push directly to `main`.** Always create a feature branch:

```bash
git checkout -b feature/<short-description>
```

After implementing and verifying the feature, open a PR to merge into `main`.

## Build Commands

All commands run from `keycloak-spis/` (the Maven root):

```bash
# Build all modules (skips tests by default)
mvn clean install

# Build without assembly JAR
mvn clean install -P all

# Build specific module
mvn -pl spi-resources -am clean install

# Run all tests
mvn test

# Run tests for a single module
mvn -pl common -am test

# Skip tests
mvn -DskipTests clean install
```

The assembly module produces `assembly/target/keycloak-spis-all-1.0.0.jar` — the deployable uber JAR.

## Deployment

Copy the JAR(s) to Keycloak's `providers/` directory, then run `kc.sh build` to register the extensions.

## Architecture

Multi-module Maven project (Java 17, Keycloak 26.4.0) with four modules:

| Module | Purpose |
|--------|---------|
| `common` | Shared models, constants, enums, and utility classes |
| `spi-providers` | Custom SPI definition (`UserPermissionsSpi`) with provider/factory interfaces |
| `spi-policy` | Authorization policy provider ("Group Role" policy) |
| `spi-resources` | Custom Admin REST endpoints for users and groups |
| `assembly` | Maven Shade plugin combines all modules into one JAR |

### SPI Registration

Services are registered via `META-INF/services/` files (Java ServiceLoader pattern). Each module has its own services directory mapping Keycloak interfaces to implementation classes.

### REST Endpoints Added

Custom endpoints extend the Keycloak Admin API (`/admin/realms/{realm}/`):

- `GET /users` — Extended user list with platform role and group membership
- `GET /users/count` — User count with filtering (role, group, search)
- `GET /groups` — Group hierarchy
- `GET /groups/count` — Group count with optional search
- `POST /groups` — Create group with automatic role subgroups (GroupAdmin, GroupMember)

### Role Model

- **Platform roles** (realm-level): `Admin`, `User` (defined in `PlatformRealmRoles` enum)
- **Group roles** (subgroup-level): `GroupAdmin`, `GroupMember` (defined in `GroupRealmRoles` enum)
- Groups are created with subgroups that represent roles within that group

### Key Patterns

- `ModelToRepresentation` — Converts Keycloak `UserModel`/`GroupModel` to custom representations with additional role/group data
- `SPIUtils` — Central utility for validation, logging, and config loading
- `AdminPermissionEvaluator` — Used in resources for authorization checks before returning data
- Provider + Factory pairs — Every SPI component has a corresponding factory registered via services

### Dependency Flow

```
spi-resources → spi-providers → common
spi-policy → common
assembly → (shades all)
```
