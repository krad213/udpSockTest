package example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private byte[] buf = new byte[1];

    public void connect(String ip, int port) throws IOException {
        InetAddress address = InetAddress.getByName(ip);
        try (
                DatagramSocket socket = new DatagramSocket();
                InputStreamReader reader = new InputStreamReader(System.in)
        ) {
            int chr;
            while ((chr = reader.read()) != -1) {
                buf[0] = (byte) chr;
                DatagramPacket packet = new DatagramPacket(buf, 0, buf.length, address, port);
                socket.send(packet);
            }
        }
    }
}
