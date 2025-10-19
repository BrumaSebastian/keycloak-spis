package com.keycloak.spis.services.factories;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import com.keycloak.spis.common.Constants;
import com.keycloak.spis.services.providers.DefaultUserPermissionsProvider;
import com.keycloak.spis.services.providers.UserPermissionProvider;

public class DefaultUserPermissionsProviderFactory implements UserPermissionsProviderFactory {

    @Override
    public UserPermissionProvider create(KeycloakSession session) {
        return new DefaultUserPermissionsProvider(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return Constants.SPI_PROVIDERS.USER_PERMISSIONS_PROVIDER;
    }
}
