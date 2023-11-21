package chat.chatclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main extends Application {

    Socket socket;
    TextArea textArea;


    // 클라이언트 프로그램 작동 시작하는 메서드
    public void startClient(String IP, int port) { // 앞서 서버에서 지정한 ip와 포트

        // 여기서는 동시다발성 가능성이 떨어지기 때문에 굳이 Runnerble 사용 x
        Thread thread = new Thread() {
            public void run() {
                try {
                    socket = new Socket(IP, port);
                    receive();
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        stopClient();
                        System.out.println("[서버 접속 실패]");
                        Platform.exit();
                    }
                }
            }
        };
        thread.start();
    }

    // 서버로부터 메시지를 전달받는 메서드
    private void receive() {
        while (true) {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[512];
                int length = inputStream.read(buffer);
                if (length == -1) throw new IOException();
                String message = new String(buffer, 0, length, "UTF-8");
                Platform.runLater(() -> {
                    textArea.appendText(message);
                });
            } catch (Exception e) {
                stopClient();
                break;
            }
        }
    }


    // 서버로부터 메시지를 전송하는 메서드
    public void send(String message) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    byte[] buffer = message.getBytes("UTF-8");
                    outputStream.write(buffer);
                    outputStream.flush(); // 메시지 전송 보내기
                } catch (Exception e) {
                    stopClient();
                }
            }
        };
        thread.start();
    }

    // 클라이언트 프로그램 작동 종료하는 메서드
    public void stopClient() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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