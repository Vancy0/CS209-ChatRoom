package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.*;
import cn.edu.sustech.cs209.chatting.server.ChatServer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"checkstyle:MissingJavadocType", "checkstyle:Indentation"})
public class Controller implements Initializable {
    @FXML
    ListView<Message> chatContentList;
    @FXML
    private Label currentUsername;
    @FXML
    private Label currentOnlineCnt;
    private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    String username;
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        //login module
        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            try {
                socket = new Socket("localhost", 8888);
                System.out.println("Connecting...");
                this.out = new ObjectOutputStream(socket.getOutputStream());
                this.in = new ObjectInputStream(socket.getInputStream());
                username = login(input, url, resourceBundle);
                //symbol of connected
                setUsername(username);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }
        chatContentList.setCellFactory(new MessageCellFactory());

        //start to receive messages
        listenForMessages();
        //update ui
        updateUi();
    }

    private void updateUi() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                Message msg;
                while ((msg = messageQueue.poll()) != null) {
                    try {
                        // handle received msg
                        handleMessage(msg);
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();
    }

    private void listenForMessages() {
        // new Thread to receive messages avoid blocking the main thread
        new Thread(() -> {
            while (true) {
                try {
                    // Read messages from the server blockingly
                    Message msg = (Message) in.readObject();
                    // staging received msg
                    if (msg != null) {
                        System.out.println("msg in!!!!");
                        System.out.println(msg.getType() + " " + msg.getData());
                        messageQueue.put(msg);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void handleMessage(Message msg) throws IOException, ClassNotFoundException {
        switch (msg.getType()) {
            case MESSAGE:
                handleMsgMessage(msg);
                break;
            case SYSTEM:
                handleSystemMessage(msg);
                break;
            default:
                break;
        }
    }

    private void handleMsgMessage(Message msg) {
        // Adding messages to the chat content list
        Platform.runLater(() -> {
            chatContentList.getItems().add(msg);
            chatContentList.scrollTo(msg);
        });
    }


    private void handleSystemMessage(Message msg) throws IOException, ClassNotFoundException {
        String content = msg.getData();
        switch (content) {
            case Constants.UPDATE_USER_LIST:
                handleSystemUpdateUserList(msg);
                break;
            default:
                break;
        }
    }

    private void handleSystemUpdateUserList(Message msg) throws IOException, ClassNotFoundException {
        int size = msg.getExData();
        setCurrentOnlineCnt(size);
        System.out.println("Updated current online user!");
    }

    public String login(Optional<String> input, URL url,
                        ResourceBundle resourceBundle) throws IOException, ClassNotFoundException {
        boolean nameExists = checkDupName(input);
        String userName = handleLoginException(nameExists, input, url, resourceBundle);
        if (userName != null) {
            addListRequest(userName);
        }
        return userName;
    }

    public void addListRequest(String userName) throws IOException, ClassNotFoundException {
        System.out.println("Send request for adding current login user to list");
        Message login = new Message(new User(userName), Constants.LOGIN_MESSAGE);
        sendSystemMessage(login);
    }

    public boolean checkDupName(Optional<String> input) throws IOException, ClassNotFoundException {
        System.out.println("Check if there is a user with the same name");
        Message getList = new Message(Constants.GET_USER_LIST);
        Message repeat = sendSystemMessage(getList);
        String[] loggedInUsers = repeat.getData().split(",");
        boolean nameExists = false;
        for (String user : loggedInUsers) {
            if (user.equals(input.get())) {
                nameExists = true;
                break;
            }
        }
        if (!nameExists && !loggedInUsers[0].equals("")) {
            setCurrentOnlineCnt(loggedInUsers.length + 1);
        }
        return nameExists;
    }

    public String handleLoginException(boolean nameExists, Optional<String> input,
                                       URL url, ResourceBundle resourceBundle) {
        if (nameExists) {
            System.out.println("Duplicate username! Please retry!");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("The username " + input.get() + " already exists, please choose a different name.");
            alert.showAndWait();
            initialize(url, resourceBundle);
        } else {
            return input.get();
        }
        return null;
    }

    private Message sendSystemMessage(Message msg) throws IOException, ClassNotFoundException {
        out.writeObject(msg);
        out.flush();
        return (Message) in.readObject();
    }

    private void setUsername(String username) {
        if (username != null) {
            currentUsername.setText("Current User: " + username);
            System.out.println("User: {" + username + "} Connected!");
        }
    }

    private void setCurrentOnlineCnt(int size) {
        String num = Integer.toString(size);
        currentOnlineCnt.setText("Online: " + num);
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll("Item 1", "Item 2", "Item 3");

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        /*// Check if there is already a chat with the selected user
        // TODO: get the list of chats from the server or local storage
        List<Chat> chats = ChatClient.getChats();
        Optional<Chat> existingChat = chats.stream()
                .filter(chat -> chat.getParticipants().contains(user.get()))
                .findFirst();
        if (existingChat.isPresent()) {
            // Open the existing chat
            ChatController controller = existingChat.get().getController();
            controller.show();
        } else {
            // Create a new chat item in the left panel
            Chat newChat = new Chat(user.get());
            // TODO: add the new chat to the list of chats
            // for example: chats.add(newChat);
            // TODO: update the chat list view in the UI
            // for example: chatList.getItems().add(newChat);
        }*/
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        /*// Get the currently selected chat
        Chat selectedChat = chatList.getSelectionModel().getSelectedItem();
        if (selectedChat == null) {
            return;
        }

        String text = messageInput.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // Send the message to the server
        Message message = new Message(username, text);
        // TODO: send the message to the server
        // for example: ChatClient.sendMessage(selectedChat, message);

        // Add the message to the chat content list
        selectedChat.getMessages().add(message);
        // TODO: update the chat content list view in the UI
        // for example: chatContentList.getItems().setAll(selectedChat.getMessages());

        // Clear the input field
        messageInput.clear();*/
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy().getUsername());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
