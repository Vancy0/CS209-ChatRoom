package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;
    private static final List<User> userList = new ArrayList<>();

    public ChatServer(int port) {
        this.port = port;
        this.clientHandlers = new ArrayList<>();
    }
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(this, clientSocket);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendOne(Message msg) {
    }

    public synchronized void broadcast(Message message) throws IOException {
        for (ClientHandler client : clientHandlers) {
            client.sendMessage(message);
        }
    }

    public void addUser(User user, ClientHandler clientHandler) {
        userList.add(user);
        clientHandlers.add(clientHandler);
    }

    public void removeUser(User user) {
        userList.remove(user);
    }

    public static List<User> getLoggedInUsers(){
        return userList;
    }


}
