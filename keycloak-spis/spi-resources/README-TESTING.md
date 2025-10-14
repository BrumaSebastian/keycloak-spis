# Keycloak SPI Integration Tests with Testcontainers

This module provides comprehensive integration testing for the Keycloak Users Admin Resource SPI using Testcontainers.

## 🚀 Quick Start

### Prerequisites

1. **Docker** must be installed and running
2. **Java 17** or higher
3. **Maven 3.6** or higher

### Running Tests

#### 1. Simple Unit Tests

```bash
# Run basic unit tests (no Docker required)
mvn test -pl spi-resources -Dtest=SimpleUsersAdminResourceTest
```

#### 2. Integration Tests with Testcontainers

```bash
# Run full integration tests (requires Docker)
mvn clean package -pl spi-resources
mvn test -pl spi-resources -Dtest=UsersAdminResourceIntegrationTest
```

#### 3. Manual Testing with Docker Compose

```bash
# Build the SPI JAR first
mvn clean package -pl spi-resources

# Start Keycloak with your SPI
cd spi-resources
docker-compose -f docker-compose.test.yml up -d

# Wait for Keycloak to start (check logs)
docker logs keycloak-test -f

# Access Keycloak Admin Console
# URL: http://localhost:8080/admin
# Username: admin
# Password: admin
```

## 🧪 Test Structure

### Test Classes

1. **SimpleUsersAdminResourceTest** - Basic unit tests
2. **UsersAdminResourceIntegrationTest** - Full integration tests with Keycloak
3. **KeycloakTestContainer** - Testcontainer configuration
4. **KeycloakTestHelper** - Helper methods for Keycloak operations

### Test Scenarios

The integration tests cover:

- ✅ Basic SPI registration and loading
- ✅ Custom endpoint accessibility (`/users-extension/test`)
- ✅ Direct user endpoint (`/{user-id}/custom-info`)
- ✅ Standard user endpoint (`/users/{user-id}/custom-info`)
- ✅ User extended info endpoint
- ✅ Error handling (404, 401)
- ✅ Proper authentication and authorization

### Test URLs

When running tests, the following endpoints are tested:

```
# Base test endpoint
GET /admin/realms/test-realm/users-extension/test

# Direct user access
GET /admin/realms/test-realm/users-extension/{user-id}/custom-info

# Standard user access
GET /admin/realms/test-realm/users-extension/users/{user-id}/custom-info
GET /admin/realms/test-realm/users-extension/users/{user-id}/extended-info
```

## 🔧 Manual Testing

### Using Postman or curl

1. **Start Keycloak with Docker Compose**:

   ```bash
   mvn clean package -pl spi-resources
   cd spi-resources
   docker-compose -f docker-compose.test.yml up -d
   ```

2. **Get Admin Token**:

   ```bash
   curl -X POST "http://localhost:8080/realms/master/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password" \
     -d "client_id=admin-cli" \
     -d "username=admin" \
     -d "password=admin"
   ```

3. **Create Test Realm**:

   ```bash
   curl -X POST "http://localhost:8080/admin/realms" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"realm":"test","enabled":true}'
   ```

4. **Create Test User**:

   ```bash
   curl -X POST "http://localhost:8080/admin/realms/test/users" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@example.com","enabled":true}'
   ```

5. **Test Your Custom Endpoints**:

   ```bash
   # Test base endpoint
   curl -H "Authorization: Bearer YOUR_TOKEN" \
     "http://localhost:8080/admin/realms/test/users-extension/test"

   # Test user custom info
   curl -H "Authorization: Bearer YOUR_TOKEN" \
     "http://localhost:8080/admin/realms/test/users-extension/users/USER_ID/custom-info"
   ```

## 🐛 Troubleshooting

### Common Issues

1. **Docker not running**: Ensure Docker Desktop is running
2. **Port conflicts**: Make sure port 8080 is available
3. **JAR not found**: Run `mvn clean package` first
4. **Testcontainers timeout**: Increase timeout in test configuration

### Debug Mode

Enable debug logging:

```bash
mvn test -pl spi-resources -Dtest=UsersAdminResourceIntegrationTest -X
```

### Check Container Logs

```bash
# View Keycloak logs
docker logs keycloak-test -f

# Check if SPI is loaded
docker logs keycloak-test 2>&1 | grep -i "spi-resources"
```

## 📁 Files Structure

```
spi-resources/
├── src/
│   ├── main/java/
│   │   └── com/extensions/spis/resources/admin/
│   │       ├── providers/        # SPI providers
│   │       ├── resources/        # REST resources
│   │       └── factories/        # SPI factories
│   └── test/java/
│       └── com/extensions/spis/resources/admin/test/
│           ├── config/           # Test configurations
│           ├── helper/           # Test utilities
│           ├── SimpleUsersAdminResourceTest.java
│           └── UsersAdminResourceIntegrationTest.java
├── docker-compose.test.yml       # Docker compose for manual testing
└── pom.xml                      # Dependencies and test configuration
```

## 🎯 Next Steps

1. **Run the simple test** to verify setup
2. **Run integration tests** to test with real Keycloak
3. **Use Docker Compose** for manual testing
4. **Extend tests** for additional endpoints
5. **Add performance tests** if needed

The tests are designed to be comprehensive and should help you verify that your custom SPI endpoints are working correctly in a real Keycloak environment.
