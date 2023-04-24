package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("checkstyle:Indentation")
public class Chat {
    public static final User SERVER_USER = new User("Server");
    private final List<Message> messages;
    private final List<User> participants;
    private final String flag;
    private String chatName;
    private int hashCode;

    public Chat(List<User> users, String chatName, String flag) {
        messages = new ArrayList<>();
        participants = new ArrayList<>(users);
        List<String> tmp = participants.stream()
                .map(User::getUsername).sorted().collect(Collectors.toList());
        StringBuilder hashStringBuilder = new StringBuilder();
        for (String s : tmp) {
            hashStringBuilder.append(s);
        }
        this.hashCode = hashString(hashStringBuilder.toString());
        this.chatName = chatName;
        this.flag = flag;
    }

    public int hashString(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 5) - hash + str.charAt(i);
            hash = hash & hash; // 取模运算
        }
        return hash;
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

    public int getHashCode() {
        return hashCode;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
