package example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class Receiver implements AutoCloseable {
    private byte[] buf = new byte[Main.BUFFER_SIZE+Long.BYTES*2];
    private final DatagramSocket socket;
    private BlockingQueue<Packet> receivedPackets = new LinkedBlockingQueue<>();
    private boolean stop = false;
    private Consumer<Packet> onOk;
    private Consumer<DatagramPacket> onNok;

    public Receiver(int port) {
        this(port, null, null);
    }

    public Receiver(int port, Consumer<Packet> onOk, Consumer<DatagramPacket> onNok) {
        this.onOk = onOk;
        this.onNok = onNok;
        try {
            socket = new DatagramSocket(port);
            System.out.println("Receiver created for port:"+ port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        new Thread(() -> {
            while (!stop) {
                try {
                    Packet received = receive();
                    //System.out.println("Received ["+new String(received.getData())+"]");
                    receivedPackets.add(received);
                    Optional.ofNullable(onOk).ifPresent(it->it.accept(received));
                } catch (InvalidPacketException ex) {
                    System.out.println(ex.getMessage());
                    Optional.ofNullable(onNok).ifPresent(it->it.accept(ex.getDatagramPacket()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
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
