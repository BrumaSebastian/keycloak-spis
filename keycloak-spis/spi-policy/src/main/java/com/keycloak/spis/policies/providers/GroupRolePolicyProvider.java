package com.keycloak.spis.policies.providers;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.attribute.Attributes.Entry;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import com.keycloak.spis.common.utils.RoleUtils;
import com.keycloak.spis.policies.AuthorizationConstants;
import com.keycloak.spis.policies.representations.GroupRolePolicyRepresentation;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.jboss.logging.Logger;

public class GroupRolePolicyProvider implements PolicyProvider {
    private static final Logger logger = Logger.getLogger(GroupRolePolicyProvider.class);

    private final BiFunction<Policy, AuthorizationProvider, GroupRolePolicyRepresentation> representationFunction;

    public GroupRolePolicyProvider(
            BiFunction<Policy, AuthorizationProvider, GroupRolePolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        AuthorizationProvider authorizationProvider = evaluation.getAuthorizationProvider();
        EvaluationContext evaluationContext = evaluation.getContext();
        Attributes attributes = evaluationContext.getAttributes();
        GroupRolePolicyRepresentation policyRep = representationFunction.apply(evaluation.getPolicy(),
                evaluation.getAuthorizationProvider());

        if (!attributes.exists(AuthorizationConstants.Claims.GROUP_ID)) {
            evaluation.deny();
            logger.debugf("policy %s evaluated missing claim $s", evaluation.getPolicy().getName(),
                    AuthorizationConstants.Claims.GROUP_ID);

            return;
        }

        Entry orgId = attributes.getValue(AuthorizationConstants.Claims.GROUP_ID);

        if (isGranted(authorizationProvider.getKeycloakSession(), policyRep, orgId, evaluationContext.getIdentity()))
            evaluation.grant();
        else
            evaluation.deny();

        logger.debugf("policy %s evaluated with status %s on identity %s", evaluation.getPolicy().getName(),
                evaluation.getEffect(),
                evaluationContext.getIdentity().getId());
    }

    @Override
    public void close() {

    }

    private boolean isGranted(KeycloakSession session, GroupRolePolicyRepresentation policyRep, Entry groupIdEntry,
            Identity identity) {
        RealmModel realm = session.getContext().getRealm();
        GroupModel group = realm.getGroupById(groupIdEntry.asString(0));

        if (Objects.isNull(group)) {
            return false;
        }

        UserModel user = session.users().getUserById(realm, identity.getId());
        GroupModel userGroupRole = user.getGroupsStream()
                .filter(g -> g.getParentId().equals(group.getId()))
                .findFirst()
                .orElse(null);

        if (Objects.isNull(userGroupRole)) {
            return false;
        }

        Set<RoleModel> userRolesWithinGroup = RoleUtils.getDeepGroupRoleMappings(userGroupRole);

        for (GroupRolePolicyRepresentation.RoleDefinition roleDefinition : policyRep.getRoles()) {
            RoleModel role = realm.getRoleById(roleDefinition.getId());

            if (Objects.isNull(role) || !userRolesWithinGroup.contains(role)) {
                return false;
            }
        }

        return true;
    }
}
