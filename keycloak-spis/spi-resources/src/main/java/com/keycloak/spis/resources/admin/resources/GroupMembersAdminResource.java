package com.keycloak.spis.resources.admin.resources;

import java.util.List;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
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
import com.keycloak.spis.common.models.CountRepresentation;
import com.keycloak.spis.common.models.GroupUserRepresentation;
import com.keycloak.spis.common.utils.ModelToRepresentation;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
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

    @PUT
    @Path("{user-id}/roles/{role-id}")
    public Response addMemberToRole(@PathParam("user-id") String userId, @PathParam("role-id") String roleId) {
        UserProvider userProvider = session.getProvider(UserProvider.class);
        UserModel user = userProvider.getUserById(realm, userId);

        if (Objects.isNull(user)) {
            throw ErrorResponse.error("User not found " + userId, Status.NOT_FOUND);
        }

        GroupModel groupRole = groupRoles.stream()
                .filter(g -> g.getId().equals(roleId))
                .findFirst()
                .orElseThrow(() -> ErrorResponse.error("Group role not found",
                        Status.NOT_FOUND));

        if (user.isMemberOf(groupRole)) {
            throw ErrorResponse.error("User is already a member of the group", Status.CONFLICT);
        }

        groupRoles.stream().filter(g -> user.isMemberOf(g))
                .forEach(g -> user.leaveGroup(g));

        user.joinGroup(groupRole);

        UserCache userCache = session.getProvider(UserCache.class);

        if (Objects.nonNull(userCache)) {
            userCache.evict(realm, user);
        }

        return Response.noContent().build();
    }

    @Path("count")
    @GET
    public Response getMembersCount(
            @Parameter(description = "Search by username, first name, last name, email") @QueryParam("search") String search,
            @Parameter(description = "Boolean which defines whether the params \"last\", \"first\", \"email\" and \"username\" must match exactly") @QueryParam("exact") Boolean exact) {
        UserProvider userProvider = session.users();
        Long results;

        results = groupRoles.stream()
                .flatMap(g -> userProvider.getGroupMembersStream(realm, g, search, exact, null, null))
                .distinct()
                .count();

        return Response.ok(new CountRepresentation(results)).build();
    }

    @GET
    public Response getMembers(
            @Parameter(description = "Search by username, first name, last name, email") @QueryParam("search") String search,
            @Parameter(description = "Pagination offset") @QueryParam("first") Integer firstResult,
            @Parameter(description = "Maximum results size (defaults to 100)") @QueryParam("max") Integer maxResults,
            @Parameter(description = "Boolean which defines whether the params \"last\", \"first\", \"email\" and \"username\" must match exactly") @QueryParam("exact") Boolean exact) {
        UserProvider userProvider = session.users();

        List<GroupUserRepresentation> users = groupRoles.stream()
                .flatMap(g -> userProvider.getGroupMembersStream(realm, g, search, exact, firstResult, maxResults)
                        .map(u -> ModelToRepresentation.toRepresentation(session, realm, u, g)))
                .distinct()
                .toList();

        return Response.ok(users).build();
    }
}
