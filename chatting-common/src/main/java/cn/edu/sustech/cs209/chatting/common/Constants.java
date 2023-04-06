package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Constants implements Serializable {
    public static final int MAX_MESSAGE_LENGTH = 1024;
    public static final String LOGIN_MESSAGE = "LOGIN";
    public static final String GET_USER_LIST = "GET_USER_LIST";
    public static final String USER_LIST_ADDED = "USER_LIST_ADDED";
    public static final String UPDATE_USER_LIST = "UPDATE_USER_LIST";
    public static final String CLIENT_ACK = "CLIENT_ACK";

}
