package com.extensions.spis.resources.admin.factories;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;

import com.extensions.spis.resources.admin.providers.GroupsExtensionAdminProvider;

public class GroupsExtensionAdminResourceFactory implements AdminRealmResourceProviderFactory {
    public static final String ID = "groups-extension";

    @Override
    public GroupsExtensionAdminProvider create(KeycloakSession session) {
        return new GroupsExtensionAdminProvider();
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(Scope arg0) {
    }

    @Override
    public void postInit(KeycloakSessionFactory arg0) {
    }
}
