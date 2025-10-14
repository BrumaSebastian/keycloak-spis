package com.extensions.spis.resources.admin.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.logging.Log;

/**
 * Integration tests for UsersAdminResource using Testcontainers
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersAdminResourceIntegrationTest {
    @SuppressWarnings("resource")
    @Container
    private static final KeycloakContainer keycloak = new KeycloakContainer()
            .withRealmImportFile("/main-realm.json")
            .withProviderClassesFrom("target/classes");

    private static TestDataManager testDataManager;
    private static String accessToken;
    private static final String TEST_REALM = "test-realm";

    @BeforeAll
    static void setUpAll() {
        Keycloak keycloakClient = keycloak.getKeycloakAdminClient();
        // testDataManager = new TestDataManager(keycloakClient);

        // // Get access token
        // AccessTokenResponse tokenResponse =
        // keycloakClient.tokenManager().getAccessToken();
        // accessToken = tokenResponse.getToken();

        // // Create test realm
        // testDataManager.createTestRealm(TEST_REALM);
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
    void shouldRealmExist() {
        assertTrue(keycloak.isRunning());
        Keycloak keycloakClient = keycloak.getKeycloakAdminClient();
        Log.info(keycloak.getAuthServerUrl());
        RealmResource realm = keycloakClient.realm("main");
        RealmRepresentation realmRepresentation = realm.toRepresentation();
        assertNotNull(realmRepresentation);
    }

    // @Test
    // @Order(1)
    // @DisplayName("Should get users from UsersAdminResource")
    // void shouldGetUsers() {
    // // Keycloak keycloakClient = keycloak.getKeycloakAdminClient();
    // // AccessTokenResponse accessTokenResponse =
    // // keycloakClient.tokenManager().getAccessToken();

    // // given().baseUri(keycloak.getAuthServerUrl())
    // // .basePath("/admin/realms/" + TEST_REALM + "/" +
    // // UsersExtensionAdminResourceFactory.ID)
    // // .auth().oauth2(accessToken)
    // // .when().get(")
    // // .then().statusCode(200)
    // // .body("size()", is(1));
    // }

    // @Test
    // @Order(2)
    // @DisplayName("Should get a user from UsersAdminResource")
    // void shouldGetUser() {
    // String userId = testDataManager.createUser(TEST_REALM,
    // TestProfiles.Users.simpleUser());

    // Log.info(userId);

    // given().baseUri(keycloak.getAuthServerUrl())
    // .basePath("/admin/realms/" + TEST_REALM + "/" +
    // UsersExtensionAdminResourceFactory.ID + "/")
    // .auth().oauth2(accessToken)
    // .when().get(userId)
    // .then().statusCode(200)
    // .body("id", is(userId));
    // }
}