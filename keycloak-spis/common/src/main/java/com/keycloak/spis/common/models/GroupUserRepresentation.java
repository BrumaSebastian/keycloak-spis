package com.keycloak.spis.common.models;

import org.keycloak.representations.idm.UserRepresentation;

import com.keycloak.spis.common.GroupRealmRoles;

public class GroupUserRepresentation extends UserRepresentation {
    private GroupRealmRoles role;

    public GroupRealmRoles getRole() {
        return role;
    }

    public void setRole(GroupRealmRoles role) {
        this.role = role;
    }
}
