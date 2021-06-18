package com.edcs.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Nodes {

    @JsonProperty("management_port")
    private String managementPort;
    @JsonProperty("amqp_port")
    private String amqpPort;

    @JsonProperty("management_port")
    public String getManagementPort() {
        return managementPort;
    }

    @JsonProperty("management_port")
    public void setManagementPort(String managementPort) {
        this.managementPort = managementPort;
    }

    @JsonProperty("amqp_port")
    public String getAmqpPort() {
        return amqpPort;
    }

    @JsonProperty("amqp_port")
    public void setAmqpPort(String amqpPort) {
        this.amqpPort = amqpPort;
    }
}
