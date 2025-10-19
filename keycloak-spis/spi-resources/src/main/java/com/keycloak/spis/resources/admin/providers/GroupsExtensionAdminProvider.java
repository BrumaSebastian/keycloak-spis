package com.keycloak.spis.resources.admin.providers;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import com.keycloak.spis.resources.admin.resources.GroupsAdminResource;

public class GroupsExtensionAdminProvider implements AdminRealmResourceProvider {
    @Override
    public Object getResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        return new GroupsAdminResource(realm, session, auth, adminEvent);
    }

    @Override
    public void close() {
    }
}