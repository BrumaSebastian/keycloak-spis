package com.keycloak.spis.common;

public enum GroupRealmRoles {
    GroupAdmin("group-admin"),
    GroupMember("group-member");

    private String roleName;

    GroupRealmRoles(String string) {
        this.roleName = string;
    }

    public String getRoleName() {
        return roleName;
    }
}
