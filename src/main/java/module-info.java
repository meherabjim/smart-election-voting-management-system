module com.example.votingsystem3 {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;

    requires java.sql;
    requires mysql.connector.j;
    requires com.almasb.fxgl.scene;

    opens com.example.votingsystem3
            to javafx.fxml;

    opens com.example.votingsystem3.controllers
            to javafx.fxml;

    opens com.example.votingsystem3.models
            to javafx.base, javafx.fxml;

    exports com.example.votingsystem3;
    exports com.example.votingsystem3.controllers;
}