package com.extensions.spis.resources.admin.test;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages test data creation and cleanup for Keycloak integration tests
 */
public class TestDataManager {

    private final Keycloak keycloakClient;
    private final Map<String, String> createdUsers = new ConcurrentHashMap<>();
    private final Map<String, String> createdGroups = new ConcurrentHashMap<>();
    private final Map<String, String> createdRealms = new ConcurrentHashMap<>();

    public TestDataManager(Keycloak keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    /**
     * Creates a test realm with specified configuration
     */
    public String createTestRealm(String realmName) {
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(realmName);
        realm.setEnabled(true);
        realm.setRegistrationAllowed(true);
        realm.setLoginWithEmailAllowed(true);
        realm.setDuplicateEmailsAllowed(false);

        keycloakClient.realms().create(realm);
        createdRealms.put(realmName, realmName);
        return realmName;
    }

    /**
     * Creates a test group in the specified realm
     */
    public String createGroup(String realmName, String groupName) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);

        RealmResource realm = keycloakClient.realm(realmName);
        realm.groups().add(group);

        // Get the created group ID
        String groupId = realm.groups().groups().stream()
                .filter(g -> groupName.equals(g.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Group not found after creation"))
                .getId();

        createdGroups.put(groupName, groupId);
        return groupId;
    }

    /**
     * Creates a test user with specified attributes
     */
    public String createUser(String realmName, TestUserBuilder userBuilder) {
        UserRepresentation user = userBuilder.build();

        RealmResource realm = keycloakClient.realm(realmName);
        realm.users().create(user).close();

        // Get the created user ID
        String userId = realm.users().search(user.getUsername()).get(0).getId();
        createdUsers.put(user.getUsername(), userId);

        // Add user to groups if specified
        if (userBuilder.getGroupNames() != null) {
            for (String groupName : userBuilder.getGroupNames()) {
                String groupId = createdGroups.get(groupName);
                if (groupId != null) {
                    realm.users().get(userId).joinGroup(groupId);
                }
            }
        }

        return userId;
    }

    /**
     * Gets a created user ID by username
     */
    public String getUserId(String username) {
        return createdUsers.get(username);
    }

    /**
     * Gets a created group ID by name
     */
    public String getGroupId(String groupName) {
        return createdGroups.get(groupName);
    }

    /**
     * Cleans up all created test data
     */
    public void cleanup() {
        // Clean up users
        for (Map.Entry<String, String> entry : createdUsers.entrySet()) {
            try {
                keycloakClient.realm("master").users().delete(entry.getValue());
            } catch (Exception e) {
                // Log but don't fail cleanup
                System.err.println("Failed to cleanup user: " + entry.getKey());
            }
        }

        // Clean up groups
        for (Map.Entry<String, String> entry : createdGroups.entrySet()) {
            try {
                keycloakClient.realm("master").groups().group(entry.getValue()).remove();
            } catch (Exception e) {
                System.err.println("Failed to cleanup group: " + entry.getKey());
            }
        }

        // Clean up realms
        for (String realmName : createdRealms.keySet()) {
            try {
                if (!"master".equals(realmName)) {
                    keycloakClient.realms().realm(realmName).remove();
                }
            } catch (Exception e) {
                System.err.println("Failed to cleanup realm: " + realmName);
            }
        }

        createdUsers.clear();
        createdGroups.clear();
        createdRealms.clear();
    }
}