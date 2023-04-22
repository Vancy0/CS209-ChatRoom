package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@SuppressWarnings({"checkstyle:MissingJavadocType", "checkstyle:Indentation"})
public class Controller implements Initializable {
    ObservableList<Message> contentItems = FXCollections.observableArrayList();
    @FXML
    ListView<Message> chatContentList = new ListView<>();
    ObservableList<Chat> chatItems = FXCollections.observableArrayList();
    @FXML
    private ListView<Chat> chatList = new ListView<>();
    @FXML
    public TextArea inputArea;
    @FXML
    private Label currentUsername;
    @FXML
    private Label currentOnlineCnt;
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final ChatManager chatManager = new ChatManager();
    private final HashMap<String, User> userList = new HashMap<>();
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

        chatList.setItems(chatItems);
        chatContentList.setItems(contentItems);
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
        chatList.setCellFactory(new ChatListCellFactory());
        //listener whenever selected chat changed, rerender the content
        chatList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        // 渲染右侧聊天框中的聊天记录
                        System.out.println("render chat! name is: " + newValue.getChatName());
                        renderChat(newValue);
                    }
                });

        // start to receive messages
        listenForMessages();
        // update ui
        updateUi();
    }

    private void switchChat(Chat selectedChat) {
        // 将selectedChat显示在右边的聊天框中
        MultipleSelectionModel<Chat> selectionModel = chatList.getSelectionModel();
        // 选择要显示的Chat对象
        selectionModel.select(selectedChat);
    }

    @FXML
    private void handleChatSelection() {
        Chat selectedChat = chatList.getSelectionModel().getSelectedItem();
        switchChat(selectedChat);
    }

    /*private void renderChat(Chat chat) {
        System.out.println("------------Render Chat-----------");
        List<Message> messageList = chat.getMessages();
        for (Message msg : messageList) {
            System.out.println(msg.getSentBy() + " said: " + msg.getData());
        }
        System.out.println(Platform.isFxApplicationThread());
        Platform.runLater(() -> {
            contentItems.clear();
            contentItems.setAll(messageList);
            System.out.println("before refresh" + contentItems);
            chatContentList.refresh();
            System.out.println("in ui: " + contentItems);
        });
        System.out.println(contentItems);
    }*/

    private void renderChat(Chat chat) {
        System.out.println("------------Render Chat------------");
        List<Message> messageList = chat.getMessages();
        for (Message msg : messageList) {
            System.out.println(msg.getSentBy() + " said: " + msg.getData());
        }
        contentItems.clear();
        chatContentList.requestFocus();
        chatContentList.requestLayout();
        chatContentList.refresh();
        contentItems.setAll(messageList);
        System.out.println("before refresh" + contentItems);
        System.out.println("in ui: " + contentItems);
        System.out.println(contentItems);
    }

    private void updateUi() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                Message msg;
                while ((msg = messageQueue.poll()) != null) {
                    try {
                        // handle received msg
                        System.out.println("UI update!!!");
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
                        System.out.println("msg in!!! the type and data is: "
                                + msg.getType() + " " + msg.getData());
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
        User sentBy = msg.getSentBy();
        if (msg.getSendTo() != null) {
            //private
            //judge chat if has built
            List<Chat> chats = chatManager.getChatList();
            Optional<Chat> targetChat = chats.stream()
                    .filter(c -> c.getFlag().equals(Constants.FLAG_PRIVATE))
                    .filter(c -> c.getParticipants()
                            .stream().map(User::getUsername).collect(Collectors.toList())
                            .contains(sentBy.getUsername()))
                    .findFirst();
            if (targetChat.isPresent()) {
                // directly add message
                System.out.println("New Message with exist chat!");
                targetChat.get().addMessage(msg);
                refreshContent(targetChat.get());
            } else {
                // create new chat and add message
                System.out.println("New Message with none exist chat!");
                List<User> users = new ArrayList<>();
                users.add(new User(sentBy.getUsername()));
                users.add(new User(username));
                Chat newChat = new Chat(users, sentBy.getUsername(), Constants.FLAG_PRIVATE);
                chatManager.addChat(newChat);
                chatItems.add(newChat);
                newChat.addMessage(msg);
            }
        } else {
            //group

        }


    }

    private void refreshContent(Chat target) {
        Chat selectedChat = chatList.getSelectionModel().getSelectedItem();
        if (selectedChat == target) {
            System.out.println("Refresh current content!");
            System.out.println(Platform.isFxApplicationThread());
            renderChat(target);
        }
    }

    private void handleSystemMessage(Message msg) throws IOException, ClassNotFoundException {
        int systemType = msg.getSystemType();
        switch (systemType) {
            case Constants.REPLAY_ONLINE_USER_NUM:
                handleSystemUpdateOnlineUser(msg);
                break;
            case Constants.REPLAY_USER_LIST:
                handleSystemUpdateUserList(msg);
                break;
            default:
                break;
        }
    }

    private void handleSystemUpdateUserList(Message msg) {
        String[] loggedInUsers = msg.getData().split(",");
        for (String username : loggedInUsers) {
            if (username.equals(this.username)) {
                continue;
            }
            if (!username.equals("")) {
                userList.put(username, new User(username));
            }
        }
    }

    private void handleSystemUpdateOnlineUser(Message msg) throws IOException, ClassNotFoundException {
        long size = msg.getExData();
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

    private void addListRequest(String userName) throws IOException, ClassNotFoundException {
        System.out.println("Send request for adding current login user to list");
        Message login = new Message(new User(userName), Constants.LOGIN_MESSAGE);
        sendLoginMessage(login);
    }

    private boolean checkDupName(Optional<String> input) throws IOException, ClassNotFoundException {
        System.out.println("Check if there is a user with the same name");
        Message getList = new Message(Constants.GET_USER_LIST);
        Message repeat = sendLoginMessage(getList);
        String[] loggedInUsers = repeat.getData().split(",");
        boolean nameExists = false;
        for (String user : loggedInUsers) {
            if (user.equals(input.get())) {
                nameExists = true;
                break;
            }
        }
        if (input.get().equals("")) {
            nameExists = true;
        }
        // get init resource from server: UserList
        initializeResource(nameExists, loggedInUsers);
        return nameExists;
    }

    private void initializeResource(boolean nameExists, String[] loggedInUsers) {
        if (!nameExists && !loggedInUsers[0].equals("")) {
            setCurrentOnlineCnt(loggedInUsers.length + 1);
            for (String username : loggedInUsers) {
                if (!username.equals("")) {
                    userList.put(username, new User(username));
                }
            }
        }
    }

    private String handleLoginException(boolean nameExists, Optional<String> input,
                                        URL url, ResourceBundle resourceBundle) {
        if (nameExists) {
            System.out.println("Duplicate username or Invalid name! Please retry!");
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

    private Message sendLoginMessage(Message msg) throws IOException, ClassNotFoundException {
        out.writeObject(msg);
        out.flush();
        return (Message) in.readObject();
    }

    private void sendMessage(Message msg) throws IOException, ClassNotFoundException {
        out.writeObject(msg);
        out.flush();
    }

    private void setUsername(String username) {
        if (username != null) {
            currentUsername.setText("Current User: " + username);
            System.out.println("User: {" + username + "} Connected!");
        }
    }

    private void setCurrentOnlineCnt(long size) {
        String num = Long.toString(size);
        currentOnlineCnt.setText("Online: " + num);
    }

    @FXML
    public void createPrivateChat() throws IOException, ClassNotFoundException {
        // FIXME: get the user list from server, the current user's name should be filtered out
        sendMessage(new Message(Constants.GET_USER_LIST));
        long startTime = System.currentTimeMillis();
        while (startTime + Constants.WAIT_HALF_ONE_SECOND > System.currentTimeMillis()) {
            //loop wait....
        }

        List<User> list = new ArrayList<>();
        for (String key : userList.keySet()) {
            list.add(userList.get(key));
        }

        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();
        userSel.getItems().clear();
        for (User u : list) {
            userSel.getItems().add(u.getUsername()); // 添加用户到下拉列表中
        }

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

        // Check if there is already a chat with the selected user
        List<Chat> chats = chatManager.getChatList();
        for (int i = 0; i < chats.size(); i++) {
            System.out.println(chats.get(i).getChatName());
        }
        Optional<Chat> existingChat = chats.stream()
                .filter(chat -> Objects.equals(chat.getFlag(), Constants.FLAG_PRIVATE))
                .filter(chat -> chat.getParticipants()
                        .stream().map(User::getUsername).collect(Collectors.toList())
                        .contains(user.get()))
                .findFirst();

        //have chose something in the box
        if (!userSel.getItems().isEmpty() && userSel.getValue() != null) {
            if (existingChat.isPresent()) {
                System.out.println("Open the existing chat");
                switchChat(existingChat.get());
            } else {
                System.out.println("Create a new chat item in the left panel");
                List<User> participants = new ArrayList<>();
                participants.add(new User(user.get()));
                participants.add(new User(username));
                Chat newChat = new Chat(participants, user.get(), Constants.FLAG_PRIVATE);
                chatManager.addChat(newChat);
                chatItems.add(newChat);
                switchChat(newChat);
            }
        }
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
    public void doSendMessage() throws IOException, ClassNotFoundException {
        // Get the currently selected chat
        Chat selectedChat = chatList.getSelectionModel().getSelectedItem();
        if (selectedChat == null) {
            return;
        }

        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // Send the message to the server
        Message msg = null;
        if (selectedChat.getFlag().equals(Constants.FLAG_PRIVATE)) {
            User sendTo = null;
            for (int i = 0; i < selectedChat.getParticipants().size(); i++) {
                if (!selectedChat.getParticipants().get(i).getUsername().equals(username)) {
                    sendTo = new User(selectedChat.getParticipants().get(i).getUsername());
                    break;
                }
            }
            msg = new Message(System.currentTimeMillis(), new User(username),
                    sendTo, text, MessageType.MESSAGE);
        } else {
            msg = new Message(System.currentTimeMillis(), new User(username),
                    null, text, MessageType.MESSAGE);
        }
        // send the message to the server
        sendMessage(msg);
        // Add the message to the chat content list
        selectedChat.addMessage(msg);
        // update the chat content list view in the UI
        renderChat(selectedChat);
        // Clear the input field
        inputArea.clear();
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
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy().getUsername());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(100, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy().getUsername())) {
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

    /*private class ChatCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message chat, boolean empty) {
                    super.updateItem(chat, empty);
                    if (empty || Objects.isNull(chat)) {
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(chat.getChatName());
                    Label msgLabel = new Label(chat.getMessages().get(chat.getMessages().size() - 1).getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (chat.getMessages().get(chat.getMessages().size() - 1).getSentBy().equals(username)) {
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
    }*/

    private class ChatListCellFactory implements Callback<ListView<Chat>, ListCell<Chat>> {
        @Override
        public ListCell<Chat> call(ListView<Chat> param) {
            return new ListCell<Chat>() {
                {
                    setPrefWidth(0);
                }

                @Override
                protected void updateItem(Chat chat, boolean empty) {
                    super.updateItem(chat, empty);
                    if (empty || chat == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        //Create a HBox to hold the cells contents
                        HBox hBox = new HBox();

                        //Create an ImageView to display the chat's image
                        ImageView imageView = new ImageView();
                        imageView.setFitHeight(40);
                        imageView.setFitWidth(40);
                        imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/chat.png"))));

                        //Create a VBox to hold the chat's name and last message
                        VBox vBox = new VBox();
                        vBox.setPrefWidth(200);
                        vBox.setAlignment(Pos.CENTER_LEFT);

                        //Create a Label to display the chat's name
                        Label nameLabel = new Label(chat.getChatName());
                        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                        /*//Create a Label to display the last message in the chat
                        Label lastMessageLabel = new Label();
                        lastMessageLabel.setFont(Font.font("Arial", 12));

                        //Get the last message in the chat
                        List<Message> messages = chat.getMessages();
                        if (!messages.isEmpty()) {
                            Message lastMessage = messages.get(messages.size() - 1);

                            //Set the last message text
                            if (lastMessage.getType() == MessageType.MESSAGE) {
                                if (lastMessage.getSentBy().getUsername().equals(username)) {
                                    lastMessageLabel.setText("You: " + lastMessage.getData());
                                } else {
                                    lastMessageLabel.setText(lastMessage.getSentBy().getUsername() + ": " + lastMessage.getData());
                                }
                            } else {
                                lastMessageLabel.setText(lastMessage.getData());
                            }
                        }*/

                        //Add the labels to the VBox
                        vBox.getChildren().addAll(nameLabel);

                        //Create a VBox to hold the chat's unread message count
                        VBox unreadCountBox = new VBox();
                        unreadCountBox.setPrefWidth(50);
                        unreadCountBox.setAlignment(Pos.CENTER_RIGHT);

                        //Create a Label to display the chat's unread message count
                        Label unreadCountLabel = new Label("0");
                        unreadCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

/*                        //Get the chat's unread message count
                        int unreadCount = 0;
                        for (Message message : messages) {
                            if (message.getType() == MessageType.MESSAGE && !message.getSentBy().getUsername().equals(username)) {
                                if (!message.isRead()) {
                                    unreadCount++;
                                }
                            }
                        }*/
/*
                        //Set the unread message count
                        if (unreadCount > 0) {
                            unreadCountLabel.setText(Integer.toString(unreadCount));
                            unreadCountLabel.setTextFill(Color.WHITE);
                            Circle circle = new Circle(10);
                            circle.setFill(Color.RED);
                            unreadCountBox.getChildren().add(circle);
                            unreadCountBox.getChildren().add(unreadCountLabel);
                        }*/

                        //Add the image view, VBoxes, and unread message count to the HBox
                        hBox.getChildren().addAll(imageView, vBox, unreadCountBox);

                        //Set the HBox as the cell's graphic
                        setGraphic(hBox);
                    }
                }
            };
        }
    }

    @FXML
    private void addEmoji1(ActionEvent event) {
        // 将 Unicode 编码的表情添加到输入框中
        inputArea.appendText("\uD83D\uDE0A");
    }

    @FXML
    private void addEmoji2(ActionEvent event) {
        // 将 Unicode 编码的表情添加到输入框中
        inputArea.appendText("\uD83D\uDE02");
    }


    @FXML
    private void addEmoji4(ActionEvent event) {
        // 将 Unicode 编码的表情添加到输入框中
        inputArea.appendText("\uD83E\uDD23");
    }

    @FXML
    private void addEmoji5(ActionEvent event) {
        // 将 Unicode 编码的表情添加到输入框中
        inputArea.appendText("\uD83D\uDE2D");
    }

    @FXML
    private void addEmoji6(ActionEvent event) {
        // 将 Unicode 编码的表情添加到输入框中
        inputArea.appendText("\uD83D\uDE05");
    }
}
