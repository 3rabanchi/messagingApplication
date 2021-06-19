package com.edcs.model.connections;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Chats {

    @JsonProperty("port")
    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
