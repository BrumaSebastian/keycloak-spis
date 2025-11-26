package com.keycloak.spis.common.utils;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import com.keycloak.spis.common.GroupRealmRoles;
import com.keycloak.spis.common.PlatformRealmRoles;
import com.keycloak.spis.common.models.ExtendedUserRepresentation;
import com.keycloak.spis.common.models.GroupRolesRepresentation;
import com.keycloak.spis.common.models.GroupUserRepresentation;

public class ModelToRepresentation extends org.keycloak.models.utils.ModelToRepresentation {
    public static GroupUserRepresentation toRepresentation(KeycloakSession session, RealmModel realm, UserModel user,
            GroupModel group) {
        GroupUserRepresentation groupUserRep = new GroupUserRepresentation();
        toRepresentation(session, realm, user, groupUserRep, true);
        groupUserRep.setRole(GroupRealmRoles.valueOf(group.getName()));

        return groupUserRep;
    }

    public static ExtendedUserRepresentation toRepresentation(KeycloakSession session, RealmModel realm,
            UserModel user, PlatformRealmRoles role) {
        ExtendedUserRepresentation userRep = new ExtendedUserRepresentation();
        toRepresentation(session, realm, user, userRep, true);
        userRep.setRole(role);

        return userRep;
    }

    public static GroupRolesRepresentation toRepresentation(GroupModel group) {
        if (!EnumUtils.isValidEnumValue(GroupRealmRoles.class, group.getName())) {
            return null;
        }

        GroupRolesRepresentation representation = new GroupRolesRepresentation();
        representation.setId(group.getId());
        representation.setRole(GroupRealmRoles.valueOf(group.getName()));
        return representation;
    }
}
