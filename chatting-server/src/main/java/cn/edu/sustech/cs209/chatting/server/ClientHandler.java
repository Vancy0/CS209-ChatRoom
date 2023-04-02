package cn.edu.sustech.cs209.chatting.server;
import cn.edu.sustech.cs209.chatting.common.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final ChatServer chatServer;
    private final Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private User user;

    public ClientHandler(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
        try {
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Message msg;
            while ((msg = (Message) input.readObject()) != null) {
                switch (msg.getType()) {
                    case CONNECT:
                        handleConnectMessage(msg);
                        break;
                    case MESSAGE:
                        handleMessageMessage(msg);
                        break;
                    case DISCONNECT:
                        handleDisconnectMessage(msg);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (user != null) {
                    chatServer.removeUser(user);
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleConnectMessage(Message msg) throws IOException {
        user = msg.getSentBy();
        chatServer.addUser(user, this);
        sendMessage(new Message(Chat.SERVER_USER, "Welcome to the chat room!"));
        chatServer.broadcast(new Message(Chat.SERVER_USER, user.getUsername() + " has joined the chat room."));
    }

    private void handleMessageMessage(Message msg) throws IOException {
        if (msg.getSendTo() == null) {
            chatServer.broadcast(msg);
        } else {
            chatServer.sendOne(msg);
        }
    }

    private void handleDisconnectMessage(Message msg) throws IOException {
        chatServer.removeUser(user);
        chatServer.broadcast(new Message(Chat.SERVER_USER, user.getUsername() + " has left the chat room."));
        sendMessage(new Message(Chat.SERVER_USER, "You have left the chat room."));
    }

    public synchronized void sendMessage(Message msg) throws IOException {
        output.writeObject(msg);
        output.flush();
    }
}
