package com.keycloak.spis.common.models;

import java.util.List;

public class UserPermissionsModel {
    private String role;
    private List<String> permissions;
    private List<UserGroupPermissionsModel> Groups;

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
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
