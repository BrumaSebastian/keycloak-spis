package com.keycloak.spis.common.utils;

import org.keycloak.models.GroupModel;
import com.keycloak.spis.common.GroupRealmRoles;

import java.util.stream.Stream;

public class GroupUtils {
    public static Stream<GroupModel> getRoleSubgroups(GroupModel group) {
        return group.getSubGroupsStream()
                .filter(g -> EnumUtils.isValidEnumValue(GroupRealmRoles.class, g.getName(),
                        GroupRealmRoles::getRoleName));
    }
}
