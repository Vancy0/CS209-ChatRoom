package cn.edu.sustech.cs209.chatting.server;

public class Main {

    public static void main(String[] args) {
        int port = 8888;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
