package com.keycloak.spis.services.providers;

import java.util.Arrays;
import java.util.Objects;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.RoleUtils;

import com.keycloak.spis.common.GroupRealmRoles;
import com.keycloak.spis.common.PlatformRealmRoles;
import com.keycloak.spis.common.models.UserPermissionsModel;

public class DefaultUserPermissionsProvider implements UserPermissionProvider {
    protected final KeycloakSession session;

    public DefaultUserPermissionsProvider(KeycloakSession session) {
        super();
        this.session = session;
    }

    @Override
    public UserPermissionsModel getUserPermissions(UserModel user) {
        UserPermissionsModel userPermissionsModel = new UserPermissionsModel();
        setApplicationLevelRoleAndPermissions(userPermissionsModel, user);
        // setGroupLevelRoleAndPermissions(userPermissionsModel, user);

        return userPermissionsModel;
    }

    private void setGroupLevelRoleAndPermissions(UserPermissionsModel userPermissionsModel, UserModel user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setGroupLevelRoleAndPermissions'");
    }

    private void setApplicationLevelRoleAndPermissions(UserPermissionsModel userPermissionsModel, UserModel user) {
        RoleModel platformRole = user.getRealmRoleMappingsStream()
                .filter(r -> Arrays.stream(PlatformRealmRoles.values())
                        .anyMatch(e -> e.getRoleName().equals(r.getName())))
                .reduce((a, b) -> {
                    throw new IllegalStateException("User has more than one role assigned");
                })
                .orElse(null);

        if (Objects.isNull(platformRole)) {
            return;
        }

        System.out.println(platformRole.getName());

        userPermissionsModel.setRole(platformRole.getName());
        userPermissionsModel.setPermissions(platformRole.getCompositesStream().map(r -> r.getName()).toList());
    }

    @Override
    public void close() {
    }
}
