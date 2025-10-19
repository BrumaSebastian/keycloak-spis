package com.keycloak.spis.resources.admin.resources;

import java.net.URI;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleProvider;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import com.keycloak.spis.common.RealmRoles;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class GroupsAdminResource extends org.keycloak.services.resources.admin.GroupsResource {
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    private final RealmModel realm;
    private final KeycloakSession session;

    public GroupsAdminResource(RealmModel realm, KeycloakSession session, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        super(realm, session, auth, adminEvent);
        this.auth = auth;
        this.realm = realm;
        this.adminEvent = adminEvent;
        this.session = session;
    }

    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Created"),
            @APIResponse(responseCode = "204", description = "No Content"),
            @APIResponse(responseCode = "400", description = "Bad Request"),
            @APIResponse(responseCode = "409", description = "Conflict")
    })
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation(summary = "create a top level group with children", description = "Create a top level group with sub groups for that represent roles")
    public Response addTopLevelGroup(GroupRepresentation rep) {
        auth.groups().requireManage();
        System.out.println("asdf");
        if (ObjectUtil.isBlank(rep.getName())) {
            throw ErrorResponse.error("Group name is missing", Response.Status.BAD_REQUEST);
        }

        try {
            GroupModel group = realm.createGroup(rep.getName());
            GroupModel adminGroup = realm.createGroup(RealmRoles.GroupAdmin.name(), group);
            GroupModel memberGroup = realm.createGroup(RealmRoles.GroupMember.name(), group);

            RoleProvider roleProvider = session.getProvider(RoleProvider.class);
            adminGroup.grantRole(roleProvider.getRealmRole(realm, RealmRoles.GroupAdmin.getRoleName()));
            memberGroup.grantRole(roleProvider.getRealmRole(realm, RealmRoles.GroupMember.getRoleName()));

            URI uri = session.getContext().getUri().getAbsolutePathBuilder()
                    .path(group.getId()).build();

            rep.setId(group.getId());
            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), group.getId());
            adminEvent.representation(rep).success();

            return Response.created(uri).build();
        } catch (ModelDuplicateException mde) {
            throw ErrorResponse.exists("Top level group named '" + rep.getName() + "' already exists.");
        }
    }
}