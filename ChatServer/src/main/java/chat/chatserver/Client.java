package chat.chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
        receive();
    }

    // 클라이언트로부터 메시지를 전달 받는 메서드
    private void receive() {
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        InputStream inputStream = socket.getInputStream();
                        byte[] buffer = new byte[512];
                        int length = inputStream.read(buffer);

                        while (length == -1) throw new IOException();
                        System.out.println("[메시지 수신 성공] "
                                + socket.getRemoteSocketAddress()
                                + " : " + Thread.currentThread().getName());
                        String message = new String(buffer, 0, length, "UTF-8");
                        // 메시지를 받으면 다른 클라이언트에게도 보내주기
                        for (Client client : Main.clients) {
                            client.send(message);
                        }
                    }
                } catch (Exception e) {
                    try {
                        System.out.println("[메시지 수신 오류]"
                            + socket.getRemoteSocketAddress()
                            + " : " + Thread.currentThread().getName());

                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        };
        Main.threadPool.submit(thread);
    }

    // 클라이언트에게 메시지를 전송하는 메서드
    public void send(String message) {
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    byte[] buffer = message.getBytes("UTF-8");
                    outputStream.write(buffer);
                    outputStream.flush();
                } catch (Exception e) {
                    try {
                        System.out.println("[메시지 송신 오류]"
                                + socket.getRemoteSocketAddress()
                                + " : " + Thread.currentThread().getName());
                        Main.clients.remove(Client.this);
                        socket.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        };
        Main.threadPool.submit(thread);

    }
}
