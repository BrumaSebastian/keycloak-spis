package com.keycloak.spis.common.models;

import org.keycloak.representations.idm.UserRepresentation;

import com.keycloak.spis.common.PlatformRealmRoles;

public class ExtendedUserRepresentation extends UserRepresentation {
    private PlatformRealmRoles role;

    public PlatformRealmRoles getRole() {
        return role;
    }

    public void setRole(PlatformRealmRoles role) {
        this.role = role;
    }
}
