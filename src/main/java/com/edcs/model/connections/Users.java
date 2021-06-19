package com.edcs.model.connections;

import com.edcs.model.connections.Chats;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Users {

    @JsonProperty("user")
    private String user;
    @JsonProperty("chats")
    private Chats[] chats;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Chats[] getChats() {
        return chats;
    }

    public void setChats(Chats[] chats) {
        this.chats = chats;
    }
}
