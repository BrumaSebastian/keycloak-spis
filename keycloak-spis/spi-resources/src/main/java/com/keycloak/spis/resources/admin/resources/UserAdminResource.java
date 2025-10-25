package com.keycloak.spis.resources.admin.resources;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import com.keycloak.spis.common.models.UserPermissionsModel;
import com.keycloak.spis.services.providers.UserPermissionProvider;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class UserAdminResource extends org.keycloak.services.resources.admin.UserResource {
    private final UserModel user;

    public UserAdminResource(KeycloakSession session, UserModel user, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        super(session, user, auth, adminEvent);
        this.user = user;
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