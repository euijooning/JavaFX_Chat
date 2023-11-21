package chat.chatserver;

import javafx.application.Application;
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

    }

    // 시작점
    public static void main(String[] args) {
        launch();
    }
}