package com.keycloak.spis.common;

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
}
