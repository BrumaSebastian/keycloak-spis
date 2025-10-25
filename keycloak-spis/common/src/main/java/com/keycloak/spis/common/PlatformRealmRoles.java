package com.keycloak.spis.common;

public enum PlatformRealmRoles {
    Admin("admin"),
    User("user");

    private String roleName;

    PlatformRealmRoles(String string) {
        this.roleName = string;
    }

    public String getRoleName() {
        return roleName;
    }
}
