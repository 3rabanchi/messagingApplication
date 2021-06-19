package com.edcs.model.connections;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Connections {

    @JsonProperty("users")
    private Users[] users;

    public Users[] getUsers() {
        return users;
    }

    public void setUsers(Users[] users) {
        this.users = users;
    }
}
