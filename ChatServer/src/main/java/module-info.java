module chat.chatserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens chat.chatserver to javafx.fxml;
    exports chat.chatserver;
}