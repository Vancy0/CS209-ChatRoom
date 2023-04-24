package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

@SuppressWarnings("checkstyle:MissingJavadocType")
public enum MessageType implements Serializable {
    CONNECT,
    MESSAGE,
    DISCONNECT,
    SYSTEM,
}


