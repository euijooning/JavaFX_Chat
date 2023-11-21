package chat.chatclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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


    // 실제 프로그램 작동시키는 메서드
    @Override
    public void start(Stage stage) throws IOException {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(5));

        HBox hBox = new HBox();
        hBox.setPrefWidth(150);

        TextField username = new TextField();
        username.setPrefWidth(150);
        username.setPromptText("채팅에서 사용할 닉네임을 입력하세요 > ");
        HBox.setHgrow(username, Priority.ALWAYS);

        TextField ipField = new TextField("127.0.0.1");
        TextField portField = new TextField("9876");
        mainLayout.setCenter(textArea);

        hBox.getChildren().addAll(username, ipField, portField);
        mainLayout.setTop(hBox);

        textArea = new TextArea();
        textArea.setEditable(false);
        mainLayout.setCenter(textArea);

        TextField input = new TextField();
        input.setPrefWidth(Double.MAX_VALUE);
        input.setDisable(true);

        input.setOnAction(event -> {
            send(username.getText() + " : " + input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        });

        Button sendButton = new Button("보내기");
        sendButton.setDisable(true);

        sendButton.setOnAction(event -> {
            send(username.getText() + " : " + input.getText() + "\n");
            input.setText("");
            input.requestFocus();
        });

        Button connectButton = new Button("접속하기");
        connectButton.setOnAction(event -> {
            if (connectButton.getText().equals("접속하기")) {
                int port = 9876;
                try {
                    port = Integer.parseInt(portField.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startClient(ipField.getText(), port);
                Platform.runLater(() -> {
                    textArea.appendText(" [ 채팅방 접속 ]\n");
                });
                connectButton.setText("종료하기");
                input.setDisable(false);
                sendButton.setDisable(false);
                input.requestFocus();
            }
            else  {
                stopClient();
                Platform.runLater(() -> {
                    textArea.appendText("[ 채팅방 퇴장 ]\n" );
                });
                connectButton.setText("접속하기");
                input.setDisable(true);
                sendButton.setDisable(true);
            }
        });

        BorderPane chatControls = new BorderPane();
        chatControls.setLeft(connectButton);
        chatControls.setCenter(input);
        chatControls.setRight(sendButton);

        mainLayout.setBottom(chatControls);

        Scene scene = new Scene(mainLayout, 500, 500);
        stage.setTitle(" [ 채팅 클라이언트 ]");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> stopClient());
        stage.show();

        connectButton.requestFocus();
    }


    // 실행
    public static void main(String[] args) {
        launch();
    }
}