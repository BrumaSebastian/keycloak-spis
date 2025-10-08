package com.extensions.spis.resources.admin.test;

/**
 * Predefined test data profiles for common scenarios
 */
public class TestProfiles {

    public static class Users {
        public static TestUserBuilder simpleUser() {
            return TestUserBuilder.create()
                    .username("simple-user")
                    .email("simple@test.com")
                    .firstName("Simple")
                    .lastName("User");
        }

        public static TestUserBuilder adminUser() {
            return TestUserBuilder.create()
                    .username("admin-user")
                    .email("admin@test.com")
                    .firstName("Admin")
                    .lastName("User")
                    .inGroups("admin", "managers")
                    .withAttribute("role", "administrator")
                    .withAttribute("clearance", "high");
        }

        public static TestUserBuilder managerUser() {
            return TestUserBuilder.create()
                    .username("manager-user")
                    .email("manager@test.com")
                    .firstName("Manager")
                    .lastName("User")
                    .inGroups("managers", "users")
                    .withAttribute("role", "manager")
                    .withAttribute("department", "HR");
        }

        public static TestUserBuilder regularUser() {
            return TestUserBuilder.create()
                    .username("regular-user")
                    .email("regular@test.com")
                    .firstName("Regular")
                    .lastName("User")
                    .inGroups("users")
                    .withAttribute("role", "user");
        }
    }

    public static class Groups {
        public static final String ADMIN = "admin";
        public static final String MANAGERS = "managers";
        public static final String USERS = "users";
        public static final String GUESTS = "guests";
    }
}