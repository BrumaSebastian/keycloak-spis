package com.keycloak.spis.services.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import com.keycloak.spis.common.Constants;
import com.keycloak.spis.common.GroupRealmRoles;
import com.keycloak.spis.common.PlatformRealmRoles;
import com.keycloak.spis.common.models.UserGroupPermissionsModel;
import com.keycloak.spis.common.models.UserPermissionsModel;
import com.keycloak.spis.common.utils.EnumUtils;
import com.keycloak.spis.common.utils.RoleUtils;

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
        setGroupLevelRoleAndPermissions(userPermissionsModel, user);

        return userPermissionsModel;
    }

    private void setGroupLevelRoleAndPermissions(UserPermissionsModel userPermissionsModel, UserModel user) {
        List<GroupModel> rolesSubgroups = user.getGroupsStream()
                .filter(g -> EnumUtils.isValidEnumValue(GroupRealmRoles.class, g.getName()))
                .toList();

        if (rolesSubgroups.isEmpty()) {
            return;
        }

        userPermissionsModel.setGroups(new ArrayList<>());
        Map<GroupModel, GroupModel> groupsWithRoleSubgroups = getGroupWithRoleSubgroups(rolesSubgroups);
        addUserGroupsAndPermissions(userPermissionsModel, groupsWithRoleSubgroups);
    }

    private void setApplicationLevelRoleAndPermissions(UserPermissionsModel userPermissionsModel, UserModel user) {
        RoleModel platformRole = user.getRealmRoleMappingsStream()
                .filter(r -> EnumUtils.isValidEnumValue(PlatformRealmRoles.class, r.getName(),
                        PlatformRealmRoles::getRoleName))
                .reduce((a, b) -> {
                    throw new IllegalStateException("User has more than one role assigned");
                })
                .orElse(null);

        if (Objects.isNull(platformRole)) {
            return;
        }

        userPermissionsModel.setRole(platformRole.getName());
        userPermissionsModel
                .setPermissions(platformRole.getCompositesStream().map(r -> r.getName()).collect(Collectors.toSet()));
    }

    private Map<GroupModel, GroupModel> getGroupWithRoleSubgroups(List<GroupModel> roleSubgroup) {
        return roleSubgroup.stream()
                .collect(Collectors.toMap(group -> group.getParent(), group -> group));
    }

    private void addUserGroupsAndPermissions(
            UserPermissionsModel userPermissionsModel,
            Map<GroupModel, GroupModel> groupsWithRoleSubgroups) {
        groupsWithRoleSubgroups.entrySet()
                .stream()
                .forEach(g -> buildUserGroupPermission(userPermissionsModel, g));
    }

    private void buildUserGroupPermission(UserPermissionsModel userPermissionsModel,
            Entry<GroupModel, GroupModel> entry) {
        UserGroupPermissionsModel userGroupPermissions = new UserGroupPermissionsModel();

        // entry.getKey() is the parent (top-level group)
        userGroupPermissions.setId(entry.getKey().getId());
        userGroupPermissions.setName(entry.getKey().getName());

        // entry.getValue() is the role subgroup
        GroupModel roleGroup = entry.getValue();
        userGroupPermissions.setRole(roleGroup.getName());
        Set<RoleModel> roles = RoleUtils.getDeepGroupRoleMappings(roleGroup);
        userGroupPermissions.setPermissions(mapGroupPermissions(roles));
        userPermissionsModel.getGroups().add(userGroupPermissions);
        userPermissionsModel.getPermissions().addAll(mapGroupPermissionsToPlatformLevel(roles));
    }

    private Set<String> mapGroupPermissions(Set<RoleModel> roles) {
        return roles.stream()
                .filter(role -> role.getName().startsWith(Constants.Permissions.GROUP_PERMISSIONS_PREFIX))
                .map(RoleModel::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> mapGroupPermissionsToPlatformLevel(Set<RoleModel> roles) {
        return roles.stream()
                .filter(role -> !role.getName().startsWith(Constants.Permissions.GROUP_PERMISSIONS_PREFIX))
                .map(RoleModel::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public void close() {
    }
}
