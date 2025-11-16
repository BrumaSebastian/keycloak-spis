package com.keycloak.spis.policies.representations;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;
import org.keycloak.representations.idm.authorization.RolePolicyRepresentation;

import com.keycloak.spis.policies.AuthorizationConstants;

public class GroupRolePolicyRepresentation extends AbstractPolicyRepresentation {
    private Set<RoleDefinition> roles;

    @Override
    public String getType() {
        return AuthorizationConstants.POLICY_TYPE_GROUP_ROLE;
    }

    public Set<RoleDefinition> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDefinition> roles) {
        this.roles = roles;
    }

    public void addRole(String name) {
        if (Objects.isNull(roles)) {
            roles = new HashSet<>();
        }

        roles.add(new RoleDefinition(name, true));
    }

    public void addClientRole(String clientId, String name) {
        addRole(clientId + "/" + name);
    }

    public void addClientRole(String clientId, String name, boolean required) {
        addRole(clientId + "/" + name);
    }

    public static class RoleDefinition extends RolePolicyRepresentation.RoleDefinition {
        public RoleDefinition() {
            super();
        }

        public RoleDefinition(String id, Boolean required) {
            super(id, required);
        }
    }
}
