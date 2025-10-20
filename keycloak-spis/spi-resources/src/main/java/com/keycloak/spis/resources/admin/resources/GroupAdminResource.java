package com.keycloak.spis.resources.admin.resources;

import java.util.List;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import jakarta.ws.rs.Path;

public class GroupAdminResource {
    private final KeycloakSession session;
    private final RealmModel realm;
    private final GroupModel group;
    private final List<GroupModel> groupRoles;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    public GroupAdminResource(KeycloakSession session, RealmModel realm, GroupModel group,
            List<GroupModel> groupRoles, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.realm = realm;
        this.group = group;
        this.groupRoles = groupRoles;
        this.auth = auth;
        this.adminEvent = adminEvent;
    }

    @Path("members")
    public GroupMembersAdminResource members() {
        return new GroupMembersAdminResource(session, realm, group, groupRoles, auth, adminEvent);
    }
}
