package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("checkstyle:Indentation")
public class Chat {
    public static final User SERVER_USER = new User("Server");
    private final List<Message> messages;
    private final List<User> participants;
    private final String flag;
    private String chatName;

    public Chat(List<User> users, String chatName, String flag) {
        messages = new ArrayList<>();
        participants = new ArrayList<>(users);
        this.chatName = chatName;
        this.flag = flag;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void addUser(User user) {
        participants.add(user);
    }

    public List<User> getParticipants() {
        return new ArrayList<>(participants);
    }

    public String getFlag() {
        return flag;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
