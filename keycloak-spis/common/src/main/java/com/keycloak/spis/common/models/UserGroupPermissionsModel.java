package com.keycloak.spis.common.models;

import java.util.Set;

public class UserGroupPermissionsModel {
    private String id;
    private String name;
    private String role;
    private Set<String> permissions;

    public String getId() {
        return id;
    }

    public void setId(String groupId) {
        this.id = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String groupName) {
        this.name = groupName;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
