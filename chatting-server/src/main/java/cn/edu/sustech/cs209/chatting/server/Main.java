package cn.edu.sustech.cs209.chatting.server;

@SuppressWarnings({"checkstyle:MissingJavadocType", "checkstyle:Indentation"})
public class Main {

    public static void main(String[] args) {
        int port = 8888;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
