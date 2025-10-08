package com.extensions.spis.resources.admin.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.extensions.spis.resources.admin.factories.UsersExtensionAdminResourceFactory;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import groovy.util.logging.Slf4j;
import io.quarkus.logging.Log;

/**
 * Integration tests for UsersAdminResource using Testcontainers
 */
@Slf4j
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersAdminResourceIntegrationTest {
    @SuppressWarnings("resource")
    @Container
    private static final KeycloakContainer keycloak = new KeycloakContainer()
            .withProviderClassesFrom("target/classes");
    private static TestDataManager testDataManager;
    private static String accessToken;
    private static final String TEST_REALM = "test-realm";

    @BeforeAll
    static void setUpAll() {
        Keycloak keycloakClient = keycloak.getKeycloakAdminClient();
        testDataManager = new TestDataManager(keycloakClient);

        // Get access token
        AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
        accessToken = tokenResponse.getToken();

        // Create test realm
        testDataManager.createTestRealm(TEST_REALM);
    }

    @AfterAll
    static void cleanUpAll() {
        if (Objects.nonNull(testDataManager)) {
            testDataManager.cleanup();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should get users from UsersAdminResource")
    void shouldGetUsers() {
        // Keycloak keycloakClient = keycloak.getKeycloakAdminClient();
        // AccessTokenResponse accessTokenResponse =
        // keycloakClient.tokenManager().getAccessToken();

        // given().baseUri(keycloak.getAuthServerUrl())
        // .basePath("/admin/realms/" + TEST_REALM + "/" +
        // UsersExtensionAdminResourceFactory.ID)
        // .auth().oauth2(accessToken)
        // .when().get(")
        // .then().statusCode(200)
        // .body("size()", is(1));
    }

    @Test
    @Order(1)
    @DisplayName("Should get a user from UsersAdminResource")
    void shouldGetUser() {
        String userId = testDataManager.createUser(TEST_REALM, TestProfiles.Users.simpleUser());

        Log.info(userId);

        given().baseUri(keycloak.getAuthServerUrl())
                .basePath("/admin/realms/" + TEST_REALM + "/" + UsersExtensionAdminResourceFactory.ID + "/")
                .auth().oauth2(accessToken)
                .when().get(userId)
                .then().statusCode(200)
                .body("id", is(userId));
    }
}