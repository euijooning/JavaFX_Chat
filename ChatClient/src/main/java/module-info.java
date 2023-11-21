module chat.chatclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens chat.chatclient to javafx.fxml;
    exports chat.chatclient;
}