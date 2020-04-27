package example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

public class Sender implements AutoCloseable {
    private final Random random = new Random();
    private byte[] buf = new byte[1024];
    private final DatagramSocket socket;
    private InetAddress address;
    private int port;
    private boolean corrupt = false;

    public Sender(String ip, int port) {
        this(toAddress(ip), port);
    }

    public Sender(InetAddress address, int port) {
        try {
            this.address = address;
            this.port = port;
            socket = new DatagramSocket();
            System.out.println("Sender created for " + address + ":" + port);
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
            System.out.println("Sending [" + new String(data) + "]");
            Packet packet = new Packet(data);
            byte[] raw = packet.toBytes();
            DatagramPacket datagramPacket;
            if (corrupt && random.nextFloat() <= 0.3) {
                System.out.println("Data corrupted");
                datagramPacket = new DatagramPacket(corrupt(raw), 0, raw.length, address, port);
            } else {
                datagramPacket = new DatagramPacket(raw, 0, raw.length, address, port);
            }
            socket.send(datagramPacket);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    private byte[] corrupt(byte[] data) {
        byte[] corruptedData = Arrays.copyOf(data, data.length);
        for (int i = 0; i < corruptedData.length / 5; i++) {
            corruptedData[random.nextInt(corruptedData.length)] = (byte) random.nextInt(255);
        }
        return corruptedData;
    }


    @Override
    public void close() {
        socket.close();
    }

    public void setCorrupt(boolean corrupt) {
        this.corrupt = corrupt;
    }
}
