package example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Server implements AutoCloseable {
    private byte[] buf = new byte[1024];
    private final DatagramSocket socket;

    public Server(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() throws IOException {
        while (true) {
            String received = receiveString();
            if (received.startsWith("[") && received.endsWith("]")) {
                receiveFile(received.substring(1, received.length() - 1));
            } else {
                System.out.print(received);
            }
        }
    }

    private void receiveFile(String fileName) {
        try {
            try (FileOutputStream fw = new FileOutputStream(fileName)) {
                while (true) {
                    byte[] b = receiveBytes();
                    String s = new String(b);
                    if ("[EOF]".equals(s)) {
                        break;
                    } else {
                        fw.write(b);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private byte[] receiveBytes() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
    }

    private String receiveString() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return getString(packet);
    }

    private String getString(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength());
    }

    @Override
    public void close() {
        socket.close();
    }
}
