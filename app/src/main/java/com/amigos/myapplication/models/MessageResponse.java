package com.amigos.myapplication.models;

import java.util.List;

public class MessageResponse {
    private List<Message> messages;

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
