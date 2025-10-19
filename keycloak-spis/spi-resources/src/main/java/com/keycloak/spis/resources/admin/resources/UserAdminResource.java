package com.keycloak.spis.resources.admin.resources;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class UserAdminResource extends org.keycloak.services.resources.admin.UserResource {
    private final UserModel user;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    public UserAdminResource(KeycloakSession session, UserModel user, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        super(session, user, auth, adminEvent);
        System.out.println("asdf");
        this.user = user;
        this.auth = auth;
        this.adminEvent = adminEvent;
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

    @Path("extended-info")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public UserRepresentation getExtendedInfo() {
        System.out.println("Extended info endpoint called for user: " + user.getUsername());
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId(user.getId());
        userRep.setUsername(user.getUsername());
        userRep.setEmail(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        // Add any custom attributes or additional info here
        return userRep;
    }
}