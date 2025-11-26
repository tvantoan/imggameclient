module imggame {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.media;

    opens imggame.controller to javafx.fxml;
    exports imggame;
    exports imggame.controller;
    exports imggame.utils;
    exports imggame.network;
    exports imggame.network.packets;
    exports imggame.models;
    exports imggame.network.types;
}
