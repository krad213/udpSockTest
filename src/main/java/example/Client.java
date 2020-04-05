package example;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client implements AutoCloseable {
    private byte[] buf = new byte[1024];
    private final DatagramSocket socket;
    private InetAddress address;
    private int port;

    public Client(String ip, int port) {
        try {
            this.address = InetAddress.getByName(ip);
            this.port = port;
            socket = new DatagramSocket();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void send(InputStreamReader reader) {
        try {
            int chr;
            while ((chr = reader.read()) != -1) {
                buf[0] = (byte) chr;
                DatagramPacket packet = new DatagramPacket(buf, 0, 1, address, port);
                socket.send(packet);
            }
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    public void send(InputStream inputStream) {
        try {
            int length;
            while ((length = inputStream.read(buf)) != -1) {
                DatagramPacket packet = new DatagramPacket(buf, 0, length, address, port);
                socket.send(packet);
            }
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }


    public void send(byte[] bytes) {
        try {
            DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, address, port);
            socket.send(packet);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    public void sendFile(String fileName) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(fileName);
        send(("["+fileName+"]").getBytes());
        send(inputStream);
        send(("[EOF]").getBytes());
    }

    @Override
    public void close() {
        socket.close();
    }
}
