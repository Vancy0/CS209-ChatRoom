package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        if (username == null) {
            return "null";
        }
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
