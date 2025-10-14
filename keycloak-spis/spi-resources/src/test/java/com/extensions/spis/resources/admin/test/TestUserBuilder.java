package com.extensions.spis.resources.admin.test;

import org.keycloak.representations.idm.UserRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern for creating test users with specific configurations
 */
public class TestUserBuilder {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled = true;
    private boolean emailVerified = true;
    private List<String> groupNames = new ArrayList<>();
    private Map<String, List<String>> attributes = new HashMap<>();

    public static TestUserBuilder create() {
        return new TestUserBuilder();
    }

    public TestUserBuilder username(String username) {
        this.username = username;
        return this;
    }

    public TestUserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public TestUserBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public TestUserBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public TestUserBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public TestUserBuilder emailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        return this;
    }

    public TestUserBuilder inGroups(String... groupNames) {
        for (String groupName : groupNames) {
            this.groupNames.add(groupName);
        }
        return this;
    }

    public TestUserBuilder withAttribute(String key, String... values) {
        List<String> valueList = new ArrayList<>();
        for (String value : values) {
            valueList.add(value);
        }
        this.attributes.put(key, valueList);
        return this;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public UserRepresentation build() {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(enabled);
        user.setEmailVerified(emailVerified);

        if (!attributes.isEmpty()) {
            user.setAttributes(attributes);
        }

        return user;
    }
}