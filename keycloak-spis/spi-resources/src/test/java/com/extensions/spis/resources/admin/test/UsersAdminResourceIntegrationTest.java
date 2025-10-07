package com.extensions.spis.resources.admin.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.extensions.spis.resources.admin.factories.UsersExtensionAdminResourceFactory;

import dasniko.testcontainers.keycloak.KeycloakContainer;

/**
 * Integration tests for UsersAdminResource using Testcontainers
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersAdminResourceIntegrationTest {
    @SuppressWarnings("resource")
    @Container
    private static final KeycloakContainer keycloak = new KeycloakContainer()
            .withProviderClassesFrom("target/classes");

    @BeforeAll
    static void setUpAll() {
        // Wait for Keycloak to be ready
    }

    @Test
    @Order(1)
    @DisplayName("Should create test realm and user")
    void shouldGetUsers() {
        Keycloak keycloakClient = keycloak.getKeycloakAdminClient();
        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();

        given().baseUri(keycloak.getAuthServerUrl())
                .basePath("/admin/realms/master")
                .auth().oauth2(accessTokenResponse.getToken())
                .when().get(UsersExtensionAdminResourceFactory.ID)
                .then().statusCode(200)
                .body("size()", is(1));
    }
}