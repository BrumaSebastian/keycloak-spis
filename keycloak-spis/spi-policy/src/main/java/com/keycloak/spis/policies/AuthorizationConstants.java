package com.keycloak.spis.policies;

public class AuthorizationConstants {
    public static final String POLICY_GROUP = "Custom Policies";
    public static final String POLICY_TYPE_GROUP_ROLE = "groupRole";
    public static final String POLICY_TYPE_GROUP_MEMBERSHIP = "group-membership";

    public class Claims {
        public static final String GROUP_ID = "groupId";
    }

    public class Fields {
        public static final String ROLES = "roles";
    }
}
