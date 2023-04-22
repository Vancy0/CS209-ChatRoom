package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

@SuppressWarnings("checkstyle:Indentation")
public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        Controller controller = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> {
            System.out.println("Closing application...");
            controller.closeSocket();
        });
        stage.setTitle("Chatting Client");
        stage.show();
    }
}
