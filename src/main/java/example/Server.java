package example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static example.Replies.NOK;
import static example.Replies.OK;

public class Server {
    private Receiver receiver;
    private int port;
    private Sender sender;

    public Server(int port) {
        receiver = new Receiver(
                port,
                it -> {
                    if (getSender() == null) {
                        createSender(it.getSourcePacket());
                    }
                    getSender().send(OK);
                },
                it -> {
                    if (getSender() == null) {
                        createSender(it);
                    }
                    getSender().send(NOK);
                }
        );
        this.port = port;
    }

    private void createSender(DatagramPacket datagramPacket) {
        if (datagramPacket.getAddress().isLoopbackAddress()) {
            sender = new Sender(datagramPacket.getAddress(), port + 1);
        } else {
            sender = new Sender(datagramPacket.getAddress(), port);
        }
    }

    public void receiveFile() {
        try {
            receiver.listen();
            System.out.println("Waiting for file info...");
            Packet packet = receiver.take();
            System.out.println("Info received");
            DatagramPacket sourcePacket = packet.getSourcePacket();

            InetAddress address = sourcePacket.getAddress();
            System.out.println("Source: " + address + ":" + sourcePacket.getPort());

            String[] strs = new String(packet.getData()).split("\\|");
            String fileName = strs[0];
            long fileSize = Long.parseLong(strs[1]);
            System.out.println("File: " + fileName + " size:" + fileSize);
            long received = 0;

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            try (OutputStream os = new FileOutputStream(fileName)) {
                System.out.println("Waiting contents");
                while (received < fileSize) {
                    packet = receiver.take();
                    byte[] data = packet.getData();
                    md5.update(data);
                    os.write(data);
                    received += data.length;
                    System.out.println("Received " + received + "/" + fileSize);
                }
                System.out.println("Waiting for MD5 checksum...");
                if (Arrays.equals(md5.digest(), receiver.take().getData())) {
                    System.out.println("Checksum OK! Success!");
                    getSender().send(OK);
                } else {
                    System.out.println("Checksum NOK! Failure!");
                    getSender().send(NOK);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                receiver.stop();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Sender getSender() {
        return sender;
    }
}
