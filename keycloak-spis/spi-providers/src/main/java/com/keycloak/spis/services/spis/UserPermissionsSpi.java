package com.keycloak.spis.services.spis;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

import com.keycloak.spis.services.factories.UserPermissionsProviderFactory;
import com.keycloak.spis.services.providers.UserPermissionProvider;

public class UserPermissionsSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "user-permissions-spi";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return UserPermissionProvider.class;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UserPermissionsProviderFactory.class;
    }

}
