package com.keycloak.spis.resources.admin.resources;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.light.LightweightUserAdapter;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

public class UsersAdminResource extends org.keycloak.services.resources.admin.UsersResource {
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    public UsersAdminResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        super(session, auth, adminEvent);
        this.auth = auth;
        this.adminEvent = adminEvent;
    }

    @Override
    @Path("{user-id}")
    public UserAdminResource user(final @PathParam("user-id") String id) {
        System.out.println("Accessing user with ID: " + id);
        UserModel user = null;
        if (LightweightUserAdapter.isLightweightUser(id)) {
            UserSessionModel userSession = session.sessions().getUserSession(realm,
                    LightweightUserAdapter.getLightweightUserId(id));
            if (userSession != null) {
                user = userSession.getUser();
            }
        } else {
            user = session.users().getUserById(realm, id);
        }

        if (user == null) {
            // we do this to make sure somebody can't phish ids
            if (auth.users().canQuery())
                throw new NotFoundException("User not found");
            else
                throw new ForbiddenException();
        }

        return new UserAdminResource(session, user, auth, adminEvent);
    }
}