package cn.edu.sustech.cs209.chatting.common;

import java.time.LocalDateTime;

public class Message {

    private Long timestamp;

    private User sentBy;

    private User sendTo;

    private String data;

    private MessageType type;

    public Message(Long timestamp, User sentBy, User sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Message(User sender, String content) {
        this.sentBy = sender;
        this.data = content;
    }
    public Long getTimestamp() {
        return timestamp;
    }

    public User getSentBy() {
        return sentBy;
    }

    public User getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public MessageType getType(){
        return type;
    }
}
