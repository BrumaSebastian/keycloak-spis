package com.keycloak.spis.resources.admin.resources;

import java.util.List;
import java.util.Objects;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.cache.UserCache;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import com.keycloak.spis.common.GroupRealmRoles;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class GroupMembersAdminResource {
    private final KeycloakSession session;
    private final RealmModel realm;
    private final GroupModel group;
    private final List<GroupModel> groupRoles;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    public GroupMembersAdminResource(KeycloakSession session, RealmModel realm, GroupModel group,
            List<GroupModel> groupRoles, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.realm = realm;
        this.group = group;
        this.groupRoles = groupRoles;
        this.auth = auth;
        this.adminEvent = adminEvent;
    }

    @POST
    @Path("{user-id}")
    public Response addMember(@PathParam("user-id") String userId) {
        UserProvider userProvider = session.getProvider(UserProvider.class);
        UserCache userCache = session.getProvider(UserCache.class);
        UserModel user = userProvider.getUserById(realm, userId);

        if (Objects.isNull(user)) {
            throw ErrorResponse.error("User not found " + userId, Status.NOT_FOUND);
        }

        GroupModel groupRole = groupRoles.stream()
                .filter(g -> g.getName().equals(GroupRealmRoles.GroupMember.name()))
                .findFirst()
                .orElseThrow(() -> ErrorResponse.error("Group role not found" + GroupRealmRoles.GroupMember.name(),
                        Status.NOT_FOUND));

        if (user.isMemberOf(groupRole)) {
            throw ErrorResponse.error("User is already a member of the group", Status.CONFLICT);
        }

        if (Objects.nonNull(userCache)) {
            userCache.evict(realm, user);
        }

        user.joinGroup(groupRole);

        return Response.noContent().build();
    }

    @DELETE
    @Path("{user-id}")
    public Response removeMember(@PathParam("user-id") String userId) {
        UserProvider userProvider = session.getProvider(UserProvider.class);
        UserCache userCache = session.getProvider(UserCache.class);
        UserModel user = userProvider.getUserById(realm, userId);

        if (Objects.isNull(user)) {
            throw ErrorResponse.error("User not found " + userId, Status.NOT_FOUND);
        }

        List<GroupModel> memberOfGroups = groupRoles.stream()
                .filter(g -> user.isMemberOf(g))
                .toList();

        if (memberOfGroups.isEmpty())
            throw ErrorResponse.error("User is not a member of the group", Status.NOT_FOUND);

        if (Objects.nonNull(userCache))
            userCache.evict(realm, user);

        memberOfGroups.forEach(g -> user.leaveGroup(g));

        return Response.noContent().build();
    }
}
