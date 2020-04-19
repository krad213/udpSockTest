package example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Sender implements AutoCloseable {
    private byte[] buf = new byte[1024];
    private final DatagramSocket socket;
    private InetAddress address;
    private int port;

    public Sender(String ip, int port) {
        this(toAddress(ip), port);
    }

    public Sender(InetAddress address, int port) {
        try {
            this.address = address;
            this.port = port;
            socket = new DatagramSocket();
            System.out.println("Sender created for "+address+":"+port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static InetAddress toAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(byte[] data) {
        try {
            System.out.println("Sending ["+new String(data)+"]");
            Packet packet = new Packet(data);
            byte[] raw = packet.toBytes();
            DatagramPacket datagramPacket = new DatagramPacket(raw, 0, raw.length, address, port);
            socket.send(datagramPacket);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }


    @Override
    public void close() {
        socket.close();
    }
}
