package example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Client {
    private byte[] buffer = new byte[1024-Long.BYTES];
    private Sender sender;
    private Receiver receiver;
    private int retries = 10;

    public Client(String ip, int port) {
        sender = new Sender(ip, port);
        if ("localhost".equals(ip) || "127.0.0.1".equals(ip)) {
            receiver = new Receiver(port + 1);
        } else {
            receiver = new Receiver(port);
        }
    }


    public void sendFile(String filePath) throws IOException {
        System.out.println("File: "+filePath);
        receiver.listen();
        try {
            Path path = Paths.get(filePath);
            long size = Files.size(path);
            System.out.println("Size: "+size);
            String fileName = path.getFileName().toString();
            trySend(fileName + "|" + size);
            InputStream inputStream = Files.newInputStream(path);
            int length;
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            while ((length = inputStream.read(buffer)) >= 0) {
                byte[] data = Arrays.copyOf(buffer, length);
                md5.update(data);
                trySend(data);
            }
            System.out.println("File complete");
            System.out.println("Sending MD5 checksum");
            trySend(md5.digest());
            receiver.stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void trySend(String str) throws InterruptedException {
        trySend(str.getBytes());
    }

    public void trySend(byte[] data) throws InterruptedException {
        for (int i = 0; i < retries; i++) {
            System.out.println("Try #"+i);
            sender.send(data);
            String reply = new String(receiver.take().getData());
            if ("OK".equals(reply)) {
                return;
            }
        }
        throw new CommunicationException("Number of tries exceded");
    }
}
