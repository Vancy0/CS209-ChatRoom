package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.List;


@SuppressWarnings({"checkstyle:MissingJavadocType", "checkstyle:Indentation"})
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
                handleMessage(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Shut down!");
        } finally {
            try {
                if (user != null) {
                    System.out.println("Shut down connection!");
                    chatServer.removeUser(user);
                    System.out.println(chatServer.getLoggedInUsers());
                    chatServer.removeThread(this);
                }
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleMessage(Message msg) throws IOException {
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
            case SYSTEM:
                handleSystemMessage(msg);
                break;
            default:
                break;
        }
    }

    private void handleSystemMessage(Message msg) throws IOException {
        String content = msg.getData();
        switch (content) {
            case Constants.GET_USER_LIST:
                handleSystemGetUserList();
                break;
            case Constants.LOGIN_MESSAGE:
                handleSystemLogin(msg);
                break;
            case Constants.CLIENT_CLOSE:
                handleClientClose(msg);
                break;
            default:
                break;
        }
    }

    private void handleSystemGetUserList() throws IOException {
        sendMessage(createUserList());
        System.out.println("Have sent user list to Client");

    }
    private Message createCloseUserList() {
        StringBuilder contentBuffer = new StringBuilder();
        List<User> loginUsers = chatServer.getLoggedInUsers();
        for (int i = 0; i < loginUsers.size(); i++) {
            if (loginUsers.get(i).getUsername().equals(this.user.getUsername())) {
                continue;
            }
            contentBuffer.append(loginUsers.get(i).getUsername());
            if (i != loginUsers.size() - 1) {
                contentBuffer.append(",");
            }
        }
        String content = contentBuffer.toString();
        System.out.println("User list length is: " + loginUsers.size());
        return new Message(content, Constants.REPLAY_USER_LIST);
    }

    private Message createUserList() {
        StringBuilder contentBuffer = new StringBuilder();
        List<User> loginUsers = chatServer.getLoggedInUsers();
        for (int i = 0; i < loginUsers.size(); i++) {
            contentBuffer.append(loginUsers.get(i).getUsername());
            if (i != loginUsers.size() - 1) {
                contentBuffer.append(",");
            }
        }
        String content = contentBuffer.toString();
        System.out.println("User list length is: " + loginUsers.size());
        return new Message(content, Constants.REPLAY_USER_LIST);
    }

    private void handleSystemLogin(Message msg) throws IOException {
        User sendBy = msg.getSentBy();
        chatServer.addUser(sendBy);
        //after login set the user
        user = sendBy;
        // not important
        sendMessage(new Message(Constants.USER_LIST_ADDED));
        System.out.println("Have sent verification for add list to Client");

        //broadcast for update number of online user
        chatServer.broadcast(this, new Message(Constants.UPDATE_USER_LIST,
                chatServer.getLoggedInUsers().size(), Constants.REPLAY_ONLINE_USER_NUM));
        System.out.println("broadcast for update number of online user");
        chatServer.broadcast(this, createUserList());
    }

    private void handleClientClose(Message msg) throws IOException {
        //broadcast for update number of online user
        chatServer.broadcast(this, new Message(Constants.UPDATE_USER_LIST,
                chatServer.getLoggedInUsers().size() - 1, Constants.REPLAY_ONLINE_USER_NUM));
        System.out.println("broadcast for update number of online user");
        chatServer.broadcast(this, createCloseUserList());
    }

    private void handleConnectMessage(Message msg) throws IOException {
        user = msg.getSentBy();
        chatServer.addUser(user);
        sendMessage(new Message(Chat.SERVER_USER, "Welcome to the chat room!"));
        chatServer.broadcast(this, new Message(Chat.SERVER_USER, user.getUsername() + " has joined the chat room."));
    }

    private void handleMessageMessage(Message msg) throws IOException {
        if (msg.getSendTo() == null) {
            chatServer.sendGroup(this, msg);
        } else {
            chatServer.sendOne(msg);
        }
    }

    private void handleDisconnectMessage(Message msg) throws IOException {
        chatServer.removeUser(user);
        chatServer.broadcast(this, new Message(Chat.SERVER_USER, user.getUsername() + " has left the chat room."));
        sendMessage(new Message(Chat.SERVER_USER, "You have left the chat room."));
    }

    public synchronized void sendMessage(Message msg) throws IOException {
        System.out.println("have send....");
        output.writeObject(msg);
        output.flush();
    }

    public User getUser() {
        return user;
    }
}
