package com.keycloak.spis.policies.factories;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.keycloak.Config.Scope;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.util.JsonSerialization;

import com.keycloak.spis.policies.AuthorizationConstants;
import com.keycloak.spis.policies.providers.GroupRolePolicyProvider;
import com.keycloak.spis.policies.representations.GroupRolePolicyRepresentation;

public class GroupRolePolicyProviderFactory implements PolicyProviderFactory<GroupRolePolicyRepresentation> {
    public static final String NAME = "Group Role";

    private GroupRolePolicyProvider provider = new GroupRolePolicyProvider(this::toRepresentation);

    @Override
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return AuthorizationConstants.POLICY_TYPE_GROUP_ROLE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getGroup() {
        return AuthorizationConstants.POLICY_GROUP;
    }

    @Override
    public GroupRolePolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        GroupRolePolicyRepresentation representation = new GroupRolePolicyRepresentation();
        String roles = policy.getConfig().get(AuthorizationConstants.Fields.ROLES);
        representation.setRoles(getRoles(roles, authorization.getRealm()));

        return representation;
    }

    @Override
    public Class<GroupRolePolicyRepresentation> getRepresentationType() {
        return GroupRolePolicyRepresentation.class;
    }

    @Override
    public void onCreate(Policy policy, GroupRolePolicyRepresentation representation,
            AuthorizationProvider authorization) {
        updateRoles(policy, representation, authorization);
    }

    @Override
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        updateRoles(policy, authorization,
                getRoles(representation.getConfig().get(AuthorizationConstants.Fields.ROLES),
                        authorization.getRealm()));
    }

    @Override
    public void onUpdate(Policy policy, GroupRolePolicyRepresentation representation,
            AuthorizationProvider authorization) {
        updateRoles(policy, representation, authorization);
    }

    @Override
    public void onExport(Policy policy, PolicyRepresentation representation,
            AuthorizationProvider authorizationProvider) {
        Map<String, String> config = new HashMap<>();
        Set<GroupRolePolicyRepresentation.RoleDefinition> roles = toRepresentation(policy, authorizationProvider)
                .getRoles();

        for (GroupRolePolicyRepresentation.RoleDefinition roleDefinition : roles) {
            RoleModel role = authorizationProvider.getRealm().getRoleById(roleDefinition.getId());

            if (role.isClientRole()) {
                roleDefinition.setId(ClientModel.class.cast(role.getContainer()).getClientId() + "/" + role.getName());
            } else {
                roleDefinition.setId(role.getName());
            }
        }

        try {
            config.put(AuthorizationConstants.Fields.ROLES, JsonSerialization.writeValueAsString(roles));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to export role policy [" + policy.getName() + "]", cause);
        }

        representation.setConfig(config);
    }

    private void updateRoles(Policy policy, GroupRolePolicyRepresentation representation,
            AuthorizationProvider authorization) {
        updateRoles(policy, authorization, representation.getRoles());
    }

    private void updateRoles(Policy policy, AuthorizationProvider authorization,
            Set<GroupRolePolicyRepresentation.RoleDefinition> roles) {
        Set<GroupRolePolicyRepresentation.RoleDefinition> updatedRoles = new HashSet<>();
        Set<String> processedRoles = new HashSet<>();
        if (roles != null) {
            RealmModel realm = authorization.getRealm();
            for (GroupRolePolicyRepresentation.RoleDefinition definition : roles) {
                RoleModel role = getRole(definition, realm);
                if (role == null) {
                    continue;
                }

                if (!processedRoles.add(role.getId())) {
                    throw new RuntimeException("Role can't be specified multiple times - " + role.getName());
                }
                definition.setId(role.getId());
                updatedRoles.add(definition);
            }
        }

        try {
            policy.putConfig(AuthorizationConstants.Fields.ROLES, JsonSerialization.writeValueAsString(updatedRoles));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to serialize roles", cause);
        }
    }

    private Set<GroupRolePolicyRepresentation.RoleDefinition> getRoles(String rawRoles, RealmModel realm) {
        if (rawRoles != null) {
            try {
                return Arrays
                        .stream(JsonSerialization.readValue(rawRoles,
                                GroupRolePolicyRepresentation.RoleDefinition[].class))
                        .filter(definition -> getRole(definition, realm) != null)
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            } catch (IOException e) {
                throw new RuntimeException("Could not parse roles from config: [" + rawRoles + "]", e);
            }
        }

        return Collections.emptySet();
    }

    public static final Pattern UUID_PATTERN = Pattern
            .compile("[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}");

    private RoleModel getRole(GroupRolePolicyRepresentation.RoleDefinition definition, RealmModel realm) {
        String roleName = definition.getId();
        String clientId = null;
        int clientIdSeparator = roleName.indexOf("/");

        if (clientIdSeparator != -1) {
            clientId = roleName.substring(0, clientIdSeparator);
            roleName = roleName.substring(clientIdSeparator + 1);
        }

        RoleModel role;

        if (clientId == null) {
            boolean looksLikeAUuid = UUID_PATTERN.matcher(roleName).matches();
            role = looksLikeAUuid ? realm.getRoleById(roleName) : realm.getRole(roleName);

            if (role == null) {
                role = !looksLikeAUuid ? realm.getRoleById(roleName) : realm.getRole(roleName);
            }
        } else {
            ClientModel client = realm.getClientByClientId(clientId);

            if (client == null) {
                throw new RuntimeException("Client with id [" + clientId + "] not found.");
            }

            role = client.getRole(roleName);
        }

        return role;
    }
}
