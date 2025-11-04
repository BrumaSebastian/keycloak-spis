package com.keycloak.spis.common.models;

import com.keycloak.spis.common.GroupRealmRoles;

public class GroupRolesRepresentation {
    private String id;
    private GroupRealmRoles role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GroupRealmRoles getRole() {
        return role;
    }

    public void setRole(GroupRealmRoles role) {
        this.role = role;
    }
}
