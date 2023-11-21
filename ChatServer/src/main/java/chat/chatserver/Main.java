package chat.chatserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    public static ExecutorService threadPool; //한정된 자원으로 안정적 운영
    public static Vector<Client> clients = new Vector<Client>();

    ServerSocket serverSocket;

    // 서버를 구동시켜서 클라이언트의 연결을 기다리는 메서드
    public void startServer(String IP, int port) {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(IP, port));
        } catch (IOException e) {
            e.printStackTrace();
            if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
        }

        // 클라이언트가 접속할 때까지 계속 기다리는 쓰레드
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        clients.add(new Client(socket));
                        System.out.println("[클라이언트 접속]"
                        + socket.getRemoteSocketAddress()
                        + " : " + Thread.currentThread().getName());
                    } catch (Exception e) {
                        if (!serverSocket.isClosed()) {
                            stopServer();
                        }
                        break;
                    }
                }
            }
        };
        // 쓰레드풀 객체 초기화
        threadPool = Executors.newCachedThreadPool();
        threadPool.submit(thread); // 이후 첫번째 쓰레드 담아주기
    }

    // 서버의 작동을 중지시키는 메서드
    public void stopServer() {
        try {
            // 현재 작동 중인 모든 소켓 닫기
            Iterator<Client> iterator = clients.iterator();
            while (iterator.hasNext()) {
                Client client = iterator.next();
                client.socket.close();
                iterator.remove();
            }
            // 서버 소켓 객체 닫기
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // 쓰레드풀 종료하기
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // UI를 생성하고, 실질적으로 프로그램을 동작시키는 메서드
    @Override
    public void start(Stage stage) throws IOException {
        // 레이아웃을 하나 만들어서 요소를 담기
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: red;");
        pane.setPadding(new Insets(5));;

        TextArea textArea = new TextArea();
        textArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: white;");
        textArea.setEditable(false); // 수정 불가
        textArea.setFont(new Font("맑은 고딕", 17));
        pane.setCenter(textArea);

        // 시작 스위치 만들기
        Button toggleButton = new Button("서버 시작하기");
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
        pane.setBottom(toggleButton); // 아래 위치에 담기

        String IP = "127.0.0.1"; // 내 로컬(루프백) 주소, 일단 내 컴퓨터 안에서
        int port = 9876;

        // 이벤트 발생(버튼 눌렀을 때) 처리
        toggleButton.setOnAction(event -> {
            if (toggleButton.getText().equals("서버 시작하기")) {
                startServer(IP, port);
                // 버튼을 눌렀을 때 함수를 이용하여 처리하는 형태이다.
                Platform.runLater(() -> {
                    String message = String.format("[서버가 시작되었습니다.]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("서버 종료하기");

                    // 추가: 서버 시작 메시지 팝업
                    showAlert("시작", "서버가 시작되었습니다. 클라이언트를 연결하세요.");
                });
            } else {
                // 종료하기 버튼 누르면
                stopServer(); // 버튼 종료되고
                Platform.runLater(() -> {
                    String message = String.format("[서버가 종료되었습니다. 종료]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("시작하기");

                    // 추가: 서버 종료 메시지 팝업
                    showAlert("종료", "서버가 종료되었습니다..");
                });
            }
        });

        Scene scene = new Scene(pane, 500, 500);
        stage.setTitle("[채팅 서버]");
        stage.setOnCloseRequest(event -> stopServer());
        stage.setScene(scene);
        stage.show();
    }


    // 추가: Alert 팝업 메시지 표시 메서드
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 시작점
    public static void main(String[] args) {
        launch();
    }
}