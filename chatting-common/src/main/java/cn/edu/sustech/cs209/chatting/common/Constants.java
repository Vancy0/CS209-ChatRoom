package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class Constants implements Serializable {
    public static final int WAIT_HALF_ONE_SECOND = 500;
    public static final int WAIT_ONE_SECOND = 1000;
    public static final int WAIT_FIVE_SECOND = 5000;
    public static final String FLAG_PRIVATE = "PRIVATE";
    public static final String FLAG_GROUP = "GROUP";
    public static final String LOGIN_MESSAGE = "LOGIN";
    public static final String GET_USER_LIST = "GET_USER_LIST";
    public static final String USER_LIST_ADDED = "USER_LIST_ADDED";
    public static final String UPDATE_USER_LIST = "UPDATE_USER_LIST";
    public static final String CLIENT_ACK = "CLIENT_ACK";
    public static final String CLIENT_CLOSE = "CLIENT_CLOSE";
    public static final int NONE = 0;
    public static final int REPLAY_USER_LIST = 1;
    public static final int REPLAY_ONLINE_USER_NUM = 2;


}
