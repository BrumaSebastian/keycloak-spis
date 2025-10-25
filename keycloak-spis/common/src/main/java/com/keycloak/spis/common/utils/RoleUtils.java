package com.keycloak.spis.common.utils;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;

import static org.keycloak.models.utils.RoleUtils.expandCompositeRoles;

public class RoleUtils {
    public static Set<RoleModel> getDeepGroupRoleMappings(GroupModel group) {
        Set<RoleModel> roleMappings = group.getRoleMappingsStream().collect(Collectors.toSet());

        if (Objects.isNull(group.getParentId()))
            return roleMappings;

        addGroupRoles(group.getParent(), roleMappings);

        return expandCompositeRoles(roleMappings);
    }

    public static void addGroupRoles(GroupModel group, Set<RoleModel> roleMappings) {
        roleMappings.addAll(group.getRoleMappingsStream().collect(Collectors.toSet()));

        if (Objects.isNull(group.getParentId()))
            return;

        addGroupRoles(group.getParent(), roleMappings);
    }
}
