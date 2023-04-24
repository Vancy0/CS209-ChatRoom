package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class User implements Serializable {
    @SuppressWarnings("checkstyle:Indentation")
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("checkstyle:Indentation")
    private final String username;

    @SuppressWarnings("checkstyle:Indentation")
    public User(String username) {
        this.username = username;
    }

    @SuppressWarnings("checkstyle:Indentation")
    public String getUsername() {
        if (username == null) {
            return "null";
        }
        return username;
    }

    @SuppressWarnings("checkstyle:Indentation")
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
