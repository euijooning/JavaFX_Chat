package chat.chatserver;

import java.net.Socket;

public class Client {
    Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
        receive();
    }

    // 클라이언트로부터 메시지를 전달 받는 메서드
    private void receive() {

    }

    // 클라이언트에게 메시지를 전송하는 메서드
    public void send(String message) {

    }
}
