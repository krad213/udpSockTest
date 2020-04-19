package example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Receiver implements AutoCloseable {
    private byte[] buf = new byte[1024];
    private final DatagramSocket socket;
    private BlockingQueue<Packet> receivedPackets = new LinkedBlockingQueue<>();
    private boolean stop = false;

    public Receiver(int port) {
        try {
            socket = new DatagramSocket(port);
            System.out.println("Receiver created for port:"+port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        new Thread(() -> {
            while (!stop) {
                try {
                    Packet received = receive();
                    System.out.println("Received ["+new String(received.getData())+"]");
                    receivedPackets.add(received);
                } catch (InvalidPacketException | IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }).start();
    }

    private Packet receive() throws IOException, InvalidPacketException {
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        socket.receive(datagramPacket);
        return Packet.parse(datagramPacket);
    }

    public void stop() {
        stop = true;
    }

    @Override
    public void close() {
        socket.close();
    }

    public Packet take() throws InterruptedException {
        return receivedPackets.take();
    }
}
