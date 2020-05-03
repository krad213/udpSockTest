package example;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static example.Replies.NOK;
import static example.Replies.OK;

public class Server {
    private byte[] buffer = new byte[Main.BUFFER_SIZE];
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
                    getSender().send(it.getId(), OK);
                },
                it -> {
                    if (getSender() == null) {
                        createSender(it);
                    }
                    getSender().send(-1, NOK);
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
            String md5digest = strs[2];
            System.out.println("File: " + fileName + " size:" + fileSize + " MD5:" + md5digest);
            FileReceiver fileReceiver = new FileReceiver(fileName, fileSize, receiver, sender);
            fileReceiver.receive();
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            FileInputStream inputStream = new FileInputStream(fileName);
            int length;
            while ((length = inputStream.read(buffer)) >= 0) {
                byte[] data = Arrays.copyOf(buffer, length);
                md5.update(data);
            }
            String receivedDigest = Base64.getEncoder().encodeToString(md5.digest());
            if (md5digest.equals(receivedDigest)) {
                System.out.println("MD5 check OK!");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Sender getSender() {
        return sender;
    }
}
