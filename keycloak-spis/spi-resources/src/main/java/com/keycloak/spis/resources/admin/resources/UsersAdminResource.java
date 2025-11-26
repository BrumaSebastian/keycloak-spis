package com.keycloak.spis.resources.admin.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.common.Profile;
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
import org.keycloak.utils.SearchQueryUtils;

import com.keycloak.spis.common.PlatformRealmRoles;
import com.keycloak.spis.common.models.CountRepresentation;
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
    private static final String SEARCH_ID_PARAMETER = "id:";

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

    @Path("count")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Integer.class))),
            @APIResponse(responseCode = "403", description = "Forbidden")
    })
    @Tag(name = KeycloakOpenAPI.Admin.Tags.USERS)
    @Operation(summary = "Returns the number of users that match the given criteria.", description = "It can be called in three different ways. "
            +
            "1. Don’t specify any criteria and pass {@code null}. The number of all users within that realm will be returned. <p> "
            +
            "2. If {@code search} is specified other criteria such as {@code last} will be ignored even though you set them. The {@code search} string will be matched against the first and last name, the username and the email of a user. <p> "
            +
            "3. If {@code search} is unspecified but any of {@code last}, {@code first}, {@code email} or {@code username} those criteria are matched against their respective fields on a user entity. Combined with a logical and.")
    public CountRepresentation getUsersCount(
            @Parameter(description = "A String contained in username, first or last name, or email. Default search behavior is prefix-based (e.g., foo or foo*). Use *foo* for infix search and \"foo\" for exact search.") @QueryParam("search") String search,
            @Parameter(description = "A String contained in lastName, or the complete lastName, if param \"exact\" is true") @QueryParam("lastName") String last,
            @Parameter(description = "A String contained in firstName, or the complete firstName, if param \"exact\" is true") @QueryParam("firstName") String first,
            @Parameter(description = "A String contained in email, or the complete email, if param \"exact\" is true") @QueryParam("email") String email,
            @Parameter(description = "A String contained in username, or the complete username, if param \"exact\" is true") @QueryParam("username") String username,
            @Parameter(description = "whether the email has been verified") @QueryParam("emailVerified") Boolean emailVerified,
            @Parameter(description = "The alias of an Identity Provider linked to the user") @QueryParam("idpAlias") String idpAlias,
            @Parameter(description = "The userId at an Identity Provider linked to the user") @QueryParam("idpUserId") String idpUserId,
            @Parameter(description = "Boolean representing if user is enabled or not") @QueryParam("enabled") Boolean enabled,
            @Parameter(description = "Boolean which defines whether the params \"last\", \"first\", \"email\" and \"username\" must match exactly") @QueryParam("exact") Boolean exact,
            @Parameter(description = "A query to search for custom attributes, in the format 'key1:value2 key2:value2'") @QueryParam("q") String searchQuery) {
        UserPermissionEvaluator userPermissionEvaluator = auth.users();
        userPermissionEvaluator.requireQuery();

        Map<String, String> searchAttributes = searchQuery == null
                ? Collections.emptyMap()
                : SearchQueryUtils.getFields(searchQuery);
        if (search != null) {
            if (search.startsWith(SEARCH_ID_PARAMETER)) {
                UserModel userModel = session.users().getUserById(realm,
                        search.substring(SEARCH_ID_PARAMETER.length()).trim());
                return new CountRepresentation(userModel != null && userPermissionEvaluator.canView(userModel) ? 1 : 0);
            }

            Map<String, String> parameters = new HashMap<>();
            parameters.put(UserModel.SEARCH, search.trim());

            if (enabled != null) {
                parameters.put(UserModel.ENABLED, enabled.toString());
            }
            if (emailVerified != null) {
                parameters.put(UserModel.EMAIL_VERIFIED, emailVerified.toString());
            }

            if (userPermissionEvaluator.canView()) {
                return new CountRepresentation(session.users().getUsersCount(realm, parameters));
            } else {
                if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ)) {
                    return new CountRepresentation(session.users().getUsersCount(realm, parameters,
                            auth.groups().getGroupIdsWithViewPermission()));
                } else {
                    return new CountRepresentation(session.users().getUsersCount(realm, parameters));
                }
            }
        } else if (last != null || first != null || email != null || username != null || emailVerified != null
                || enabled != null || !searchAttributes.isEmpty()) {
            Map<String, String> parameters = new HashMap<>();
            if (last != null) {
                parameters.put(UserModel.LAST_NAME, last);
            }
            if (first != null) {
                parameters.put(UserModel.FIRST_NAME, first);
            }
            if (email != null) {
                parameters.put(UserModel.EMAIL, email);
            }
            if (username != null) {
                parameters.put(UserModel.USERNAME, username);
            }
            if (emailVerified != null) {
                parameters.put(UserModel.EMAIL_VERIFIED, emailVerified.toString());
            }
            if (idpAlias != null) {
                parameters.put(UserModel.IDP_ALIAS, idpAlias);
            }
            if (idpUserId != null) {
                parameters.put(UserModel.IDP_USER_ID, idpUserId);
            }
            if (enabled != null) {
                parameters.put(UserModel.ENABLED, enabled.toString());
            }
            if (exact != null) {
                parameters.put(UserModel.EXACT, exact.toString());
            }
            parameters.putAll(searchAttributes);

            if (userPermissionEvaluator.canView()) {
                return new CountRepresentation(session.users().getUsersCount(realm, parameters));
            } else {
                if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ)) {
                    return new CountRepresentation(session.users().getUsersCount(realm, parameters,
                            auth.groups().getGroupIdsWithViewPermission()));
                } else {
                    return new CountRepresentation(session.users().getUsersCount(realm, parameters));
                }
            }
        } else if (userPermissionEvaluator.canView()) {
            return new CountRepresentation(session.users().getUsersCount(realm));
        } else {
            if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ)) {
                return new CountRepresentation(
                        session.users().getUsersCount(realm, auth.groups().getGroupIdsWithViewPermission()));
            } else {
                return new CountRepresentation(session.users().getUsersCount(realm));
            }
        }
    }
}