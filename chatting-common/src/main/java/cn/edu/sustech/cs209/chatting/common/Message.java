package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings({"checkstyle:Indentation", "checkstyle:MissingJavadocMethod"})
public class Message implements Serializable {

    private Long timestamp;

    private User sentBy;

    private User sendTo;

    private String data;

    private long exData;

    private MessageType type = MessageType.SYSTEM;

    private boolean isRead = false;

    private int systemType = Constants.NONE;

    //chat message
    public Message(Long timestamp, User sentBy, User sendTo, String data, MessageType type) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.type = type;
    }

    //system messages
    public Message(User sender, String content) {
        this.sentBy = sender;
        this.data = content;
    }

    public Message(String content) {
        this.data = content;
    }

    public Message(String content, int systemType) {
        this.data = content;
        this.systemType = systemType;
    }

    public Message(String content, long exData) {
        this.data = content;
        this.exData = exData;
    }

    public Message(String content, long exData, int systemType) {
        this.data = content;
        this.exData = exData;
        this.systemType = systemType;
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

    public long getExData() {
        return exData;
    }

    public int getSystemType() {
        return systemType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String toString() {
        return "Msg{" +
                "time='" + timestamp + '\'' +
                "sendBy='" + sentBy.getUsername() + '\'' +
                "sendTo='" + sendTo.getUsername() + '\'' +
                "type='" + type + '\'' +
                '}';
    }
}
