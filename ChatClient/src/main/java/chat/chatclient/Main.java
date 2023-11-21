package chat.chatclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class Main extends Application {

    Socket socket;
    TextArea textArea;


    // 클라이언트 프로그램 작동 시작하는 메서드
    public void startClient(String IP, int port) {

    }

    // 클라이언트 프로그램 작동 종료하는 메서드
    public void stopClient(String ip, int port) {

    }

    // 서버로부터 메시지를 전송하는 메서드
    public void send(String message) {

    }

    // 실제 프로그램 작동 시작 메서드
    @Override
    public void start(Stage stage) throws IOException {
    }


    // 실행
    public static void main(String[] args) {
        launch();
    }
}