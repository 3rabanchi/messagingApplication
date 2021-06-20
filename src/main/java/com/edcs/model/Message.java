package com.edcs.model;

public class Message implements Cloneable{

    private String content;

    public Message(String content){
        this.content = content;
    }
    public Message(){

    };

    @Override
    public Message clone() {
        Message clondeMessage = null;
        try {
            clondeMessage = (Message) super.clone();
            clondeMessage.setContent(this.content);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return clondeMessage;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
