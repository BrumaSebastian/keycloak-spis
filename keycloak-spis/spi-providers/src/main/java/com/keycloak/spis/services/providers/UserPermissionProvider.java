package com.keycloak.spis.services.providers;

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

import com.keycloak.spis.common.models.UserPermissionsModel;

public interface UserPermissionProvider extends Provider {
    public UserPermissionsModel getUserPermissions(UserModel user);
}
