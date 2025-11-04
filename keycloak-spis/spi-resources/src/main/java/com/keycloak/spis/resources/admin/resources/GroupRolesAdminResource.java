package com.keycloak.spis.resources.admin.resources;

import java.util.List;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import com.keycloak.spis.common.utils.ModelToRepresentation;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Response;

public class GroupRolesAdminResource {
    private final KeycloakSession session;
    private final RealmModel realm;
    private final GroupModel group;
    private final List<GroupModel> groupRoles;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    public GroupRolesAdminResource(KeycloakSession session, RealmModel realm, GroupModel group,
            List<GroupModel> groupRoles, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.realm = realm;
        this.group = group;
        this.groupRoles = groupRoles;
        this.auth = auth;
        this.adminEvent = adminEvent;
    }

    @GET
    public Response getRoles() {
        var groupRolesRepresentation = groupRoles.stream()
                .map(g -> ModelToRepresentation.toRepresentation(g));

        return Response.ok(groupRolesRepresentation).build();
    }
}
