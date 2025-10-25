package com.keycloak.spis.common.models;

import java.util.List;
import java.util.Set;

public class UserPermissionsModel {
    private String role;
    private Set<String> permissions;
    private List<UserGroupPermissionsModel> Groups;

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public List<UserGroupPermissionsModel> getGroups() {
        return Groups;
    }

    public void setGroups(List<UserGroupPermissionsModel> groups) {
        Groups = groups;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
