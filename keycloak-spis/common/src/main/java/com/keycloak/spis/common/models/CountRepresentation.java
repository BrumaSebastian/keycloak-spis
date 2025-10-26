package com.keycloak.spis.common.models;

public class CountRepresentation {
    private long count;

    public CountRepresentation(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
