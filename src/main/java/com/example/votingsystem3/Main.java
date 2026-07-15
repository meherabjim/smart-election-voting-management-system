package com.example.votingsystem3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/votingsystem3/login.fxml"))));
        stage.setScene(scene);

        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
