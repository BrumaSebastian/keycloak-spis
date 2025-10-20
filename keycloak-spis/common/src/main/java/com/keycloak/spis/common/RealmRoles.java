package com.keycloak.spis.common;

import java.util.Arrays;
import java.util.List;

public enum RealmRoles {
    Admin("admin"),
    User("user"),
    GroupAdmin("group-admin"),
    GroupMember("group-member");

    private String roleName;

    RealmRoles(String string) {
        this.roleName = string;
    }

    public String getRoleName() {
        return roleName;
    }

    public static List<String> getGroupRoles() {
        return Arrays.asList(GroupAdmin.getRoleName(), GroupMember.getRoleName());
    }
}
