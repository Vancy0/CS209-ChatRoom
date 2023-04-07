package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chat;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"checkstyle:MissingJavadocType", "checkstyle:Indentation"})
public class ChatManager {
    List<Chat> chatList = new ArrayList<>();

    public List<Chat> getChatList() {
        return chatList;
    }

    public void setChatList(List<Chat> chatList) {
        this.chatList = chatList;
    }
}
