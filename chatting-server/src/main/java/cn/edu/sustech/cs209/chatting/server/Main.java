package cn.edu.sustech.cs209.chatting.server;

public class Main {

    public static void main(String[] args) {
        int port = 12345;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
