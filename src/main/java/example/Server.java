package example;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private byte[] buf = new byte[1];

    public void listen(int port) throws IOException {
        try (
                DatagramSocket socket = new DatagramSocket(port)
        ) {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.print(received);
            }
        }
    }
}
