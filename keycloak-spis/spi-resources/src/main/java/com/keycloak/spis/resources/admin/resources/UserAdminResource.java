package com.keycloak.spis.resources.admin.resources;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import com.keycloak.spis.common.models.UserPermissionsModel;
import com.keycloak.spis.services.providers.UserPermissionProvider;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class UserAdminResource {
    private final UserModel user;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    private final KeycloakSession session;

    public UserAdminResource(KeycloakSession session, UserModel user, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        this.user = user;
        this.auth = auth;
        this.adminEvent = adminEvent;
        this.session = session;
    }

    @Path("custom-info")
    @GET
    @Tag(name = KeycloakOpenAPI.Admin.Tags.USERS)
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public UserRepresentation getCustomInfo() {
        System.out.println("Custom info endpoint called for user: " + user.getUsername());
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId(user.getId());
        userRep.setUsername(user.getUsername());
        userRep.setEmail(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        return userRep;
    }

    @GET
    @Path("permissions")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.USERS)
    @Operation(summary = "Get user permissions")
    @Produces(MediaType.APPLICATION_JSON)
    public UserPermissionsModel getPermissions() {
        UserPermissionProvider userPermissionProvider = session.getProvider(UserPermissionProvider.class);

        return userPermissionProvider.getUserPermissions(user);
    }
}