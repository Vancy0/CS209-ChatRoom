package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    public static final User SERVER_USER = new User("Server");
    private final List<Message> messages;
    private final List<User> users;

    public Chat() {
        messages = new ArrayList<>();
        users = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }
}
