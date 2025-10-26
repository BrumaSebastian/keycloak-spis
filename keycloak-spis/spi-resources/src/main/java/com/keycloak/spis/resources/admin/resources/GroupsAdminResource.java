package com.keycloak.spis.resources.admin.resources;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleProvider;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.GroupPermissionEvaluator;
import org.keycloak.utils.GroupUtils;
import org.keycloak.utils.SearchQueryUtils;

import com.keycloak.spis.common.GroupRealmRoles;
import com.keycloak.spis.common.models.CountRepresentation;
import com.keycloak.spis.common.utils.EnumUtils;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class GroupsAdminResource {
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    private final RealmModel realm;
    private final KeycloakSession session;

    public GroupsAdminResource(RealmModel realm, KeycloakSession session, AdminPermissionEvaluator auth,
            AdminEventBuilder adminEvent) {
        this.auth = auth;
        this.realm = realm;
        this.adminEvent = adminEvent;
        this.session = session;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Created"),
            @APIResponse(responseCode = "204", description = "No Content"),
            @APIResponse(responseCode = "400", description = "Bad Request"),
            @APIResponse(responseCode = "409", description = "Conflict")
    })
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation(summary = "create a top level group with children", description = "Create a top level group with sub groups for that represent roles")
    public Response addTopLevelGroup(GroupRepresentation rep) {
        auth.groups().requireManage();
        if (ObjectUtil.isBlank(rep.getName())) {
            throw ErrorResponse.error("Group name is missing", Response.Status.BAD_REQUEST);
        }

        try {
            GroupModel group = realm.createGroup(rep.getName());
            GroupModel adminGroup = realm.createGroup(GroupRealmRoles.GroupAdmin.name(), group);
            GroupModel memberGroup = realm.createGroup(GroupRealmRoles.GroupMember.name(), group);

            RoleProvider roleProvider = session.getProvider(RoleProvider.class);
            adminGroup.grantRole(roleProvider.getRealmRole(realm, GroupRealmRoles.GroupAdmin.getRoleName()));
            memberGroup.grantRole(roleProvider.getRealmRole(realm, GroupRealmRoles.GroupMember.getRoleName()));

            URI uri = session.getContext().getUri().getAbsolutePathBuilder()
                    .path(group.getId()).build();

            rep.setId(group.getId());
            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), group.getId());
            adminEvent.representation(rep).success();

            return Response.created(uri).build();
        } catch (ModelDuplicateException mde) {
            throw ErrorResponse.exists("Top level group named '" + rep.getName() + "' already exists.");
        }
    }

    @Path("{group-id}")
    public GroupAdminResource getGroupById(@PathParam("group-id") String groupId) {
        auth.groups().requireView();
        GroupModel group = realm.getGroupById(groupId);

        if (Objects.isNull(group)) {
            throw new NotFoundException("Group not found");
        }

        // Ensure it's a top-level group
        if (Objects.nonNull(group.getParentId())) {
            throw new NotFoundException("Group not found");
        }

        List<GroupModel> groupRoles = group.getSubGroupsStream()
                .filter(subGroup -> EnumUtils.isValidEnumValue(GroupRealmRoles.class, subGroup.getName()))
                .toList();

        return new GroupAdminResource(session, realm, group, groupRoles, auth, adminEvent);
    }

    @GET
    @NoCache
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation(summary = "Returns the groups counts.")
    public Response getGroupCount(@QueryParam("search") String search) {
        GroupPermissionEvaluator groupsEvaluator = auth.groups();
        groupsEvaluator.requireList();
        Long results;
        if (Objects.nonNull(search)) {
            results = session.groups()
                    .getTopLevelGroupsStream(realm, search, false, null, null)
                    .count();
        } else {
            results = session.groups().getTopLevelGroupsStream(realm).count();
        }

        return Response.ok(new CountRepresentation(results)).build();
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation(summary = "Get group hierarchy.  Only `name` and `id` are returned.  `subGroups` are only returned when using the `search` or `q` parameter. If none of these parameters is provided, the top-level groups are returned without `subGroups` being filled.")
    public Stream<GroupRepresentation> getGroups(@QueryParam("search") String search,
            @QueryParam("q") String searchQuery,
            @QueryParam("exact") @DefaultValue("false") Boolean exact,
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation,
            @QueryParam("populateHierarchy") @DefaultValue("true") boolean populateHierarchy,
            @Parameter(description = "Boolean which defines whether to return the count of subgroups for each group (default: true") @QueryParam("subGroupsCount") @DefaultValue("true") Boolean subGroupsCount,
            @QueryParam("top") @DefaultValue("false") boolean onlyTopGroups) {
        GroupPermissionEvaluator groupsEvaluator = auth.groups();
        groupsEvaluator.requireList();

        Stream<GroupModel> stream;
        if (Objects.nonNull(searchQuery)) {
            Map<String, String> attributes = SearchQueryUtils.getFields(searchQuery);
            stream = session.groups().searchGroupsByAttributes(realm, attributes, firstResult, maxResults);
        } else if (Objects.nonNull(search)) {
            if (onlyTopGroups) {
                stream = session.groups().getTopLevelGroupsStream(realm, search, exact, firstResult, maxResults);
            } else {
                stream = session.groups().searchForGroupByNameStream(realm, search.trim(), exact, firstResult,
                        maxResults);
            }
        } else {
            stream = session.groups().getTopLevelGroupsStream(realm, firstResult, maxResults);
        }

        if (populateHierarchy) {
            return GroupUtils.populateGroupHierarchyFromSubGroups(session, realm, stream, !briefRepresentation,
                    groupsEvaluator, subGroupsCount);
        }

        if (!AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(realm)) {
            stream = stream.filter(groupsEvaluator::canView);
        }

        return stream.map(g -> {
            GroupRepresentation rep = GroupUtils.toRepresentation(groupsEvaluator, g, !briefRepresentation);

            if (subGroupsCount) {
                return GroupUtils.populateSubGroupCount(g, rep);
            }

            return rep;
        });
    }
}