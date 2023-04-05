package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import cn.edu.sustech.cs209.chatting.common.Chat;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;


@SuppressWarnings({"checkstyle:Indentation", "checkstyle:MissingJavadocMethod"})
public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;
    private final List<User> userList = new ArrayList<>();
    private final List<Chat> chatList = new ArrayList<>();

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
                System.out.println("New Client connect!");
                ClientHandler handler = new ClientHandler(this, clientSocket);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendOne(Message msg) throws IOException {
        User sendTo = msg.getSendTo();
        for (ClientHandler client : clientHandlers) {
            if (client.getUser() == sendTo) {
                client.sendMessage(msg);
                break;
            }
        }
    }

    public synchronized void broadcast(Message msg) throws IOException {
        for (ClientHandler client : clientHandlers) {
            client.sendMessage(msg);
        }
    }

    public void removeThread(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    public void addUser(User user) {
        userList.add(user);
    }

    public void removeUser(User user) {
        userList.remove(user);
    }

    public List<User> getLoggedInUsers() {
        return userList;
    }


}
