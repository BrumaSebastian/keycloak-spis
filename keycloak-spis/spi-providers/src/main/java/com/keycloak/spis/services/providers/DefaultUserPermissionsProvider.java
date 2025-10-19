package com.keycloak.spis.services.providers;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import com.keycloak.spis.common.models.UserPermissionsModel;

public class DefaultUserPermissionsProvider implements UserPermissionProvider {
    protected final KeycloakSession session;

    public DefaultUserPermissionsProvider(KeycloakSession session) {
        super();
        this.session = session;
    }

    @Override
    public UserPermissionsModel getUserPermissions(UserModel user) {
        return new UserPermissionsModel();
    }

    @Override
    public void close() {
    }
}
