package com.keycloak.spis.resources.admin.factories;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;

import com.keycloak.spis.resources.admin.providers.UsersExtensionAdminProvider;

public class UsersExtensionAdminResourceFactory implements AdminRealmResourceProviderFactory {
    public static final String ID = "users-extension";

    @Override
    public UsersExtensionAdminProvider create(KeycloakSession session) {
        return new UsersExtensionAdminProvider();
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