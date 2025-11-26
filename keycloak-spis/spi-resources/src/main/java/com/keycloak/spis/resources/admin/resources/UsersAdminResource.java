package com.keycloak.spis.resources.admin.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.light.LightweightUserAdapter;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.UserPermissionEvaluator;

import com.keycloak.spis.common.GroupRealmRoles;
import com.keycloak.spis.common.PlatformRealmRoles;
import com.keycloak.spis.common.models.ExtendedUserRepresentation;
import com.keycloak.spis.common.utils.EnumUtils;
import com.keycloak.spis.common.utils.ModelToRepresentation;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

public class UsersAdminResource {
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    private final KeycloakSession session;
    private final RealmModel realm;

    public UsersAdminResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        this.auth = auth;
        this.adminEvent = adminEvent;
        this.session = session;
        this.realm = realm;
    }

    @Path("{user-id}")
    public UserAdminResource user(final @PathParam("user-id") String id) {
        UserModel user = null;
        if (LightweightUserAdapter.isLightweightUser(id)) {
            UserSessionModel userSession = session.sessions().getUserSession(realm,
                    LightweightUserAdapter.getLightweightUserId(id));
            if (userSession != null) {
                user = userSession.getUser();
            }
        } else {
            user = session.users().getUserById(realm, id);
        }

        if (user == null) {
            // we do this to make sure somebody can't phish ids
            if (auth.users().canQuery())
                throw new NotFoundException("User not found");
            else
                throw new ForbiddenException();
        }

        return new UserAdminResource(session, user, auth, adminEvent);
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.USERS)
    @Operation(summary = "Get users Returns a stream of users, filtered according to query parameters.")
    public Stream<ExtendedUserRepresentation> getUsers(
            @Parameter(description = "A String contained in username, first or last name, or email. Default search behavior is prefix-based (e.g., foo or foo*). Use *foo* for infix search and \"foo\" for exact search.") @QueryParam("search") String search,
            @Parameter(description = "Pagination offset") @QueryParam("first") Integer firstResult,
            @Parameter(description = "Maximum results size (defaults to 100)") @QueryParam("max") Integer maxResults,
            @Parameter(description = "Boolean representing if user is enabled or not") @QueryParam("enabled") Boolean enabled) {
        UserPermissionEvaluator userPermissionEvaluator = auth.users();

        userPermissionEvaluator.requireQuery();

        firstResult = firstResult != null ? firstResult : -1;
        maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;

        Stream<UserModel> userModels = Stream.empty();
        Map<String, String> attributes = new HashMap<>();
        attributes.put(UserModel.INCLUDE_SERVICE_ACCOUNT, "false");

        if (Objects.nonNull(search)) {
            attributes.put(UserModel.SEARCH, search.trim());
        }

        if (Objects.nonNull(enabled)) {
            attributes.put(UserModel.ENABLED, enabled.toString());
        }

        userModels = session.users().searchForUserStream(realm, attributes, firstResult, maxResults);

        return userModels.map(u -> {
            RoleModel roleModel = u.getRealmRoleMappingsStream()
                    .filter(r -> EnumUtils.isValidEnumValue(PlatformRealmRoles.class, r.getName()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("User " + u.getId() + " has no role"));

            PlatformRealmRoles platformRole = Arrays.stream(PlatformRealmRoles.values())
                    .filter(pr -> pr.name().equalsIgnoreCase(roleModel.getName()))
                    .findFirst().orElse(null);

            ExtendedUserRepresentation representation = ModelToRepresentation.toRepresentation(session, realm, u,
                    platformRole);

            List<String> groups = u.getGroupsStream()
                    .map(g -> {
                        var groupParent = g.getParent();

                        while (Objects.nonNull(groupParent.getParentId())) {
                            groupParent = groupParent.getParent();
                        }

                        return groupParent.getId();
                    })
                    .toList();

            representation.setGroups(groups);

            return representation;
        });
    }
}