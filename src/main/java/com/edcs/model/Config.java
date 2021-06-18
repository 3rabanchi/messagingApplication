package com.edcs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Config {

    @JsonProperty("nodes")
    private Nodes[] nodes;

    @JsonProperty("user")
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Nodes[] getNodes() {
        return nodes;
    }

    public void setNodes(Nodes[] nodes) {
        this.nodes = nodes;
    }
}
