module imggame {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;

    opens imggame.controller to javafx.fxml;
    exports imggame;
    exports imggame.controller;
    exports imggame.utils;
    exports imggame.network;
    exports imggame.network.packets;
}
