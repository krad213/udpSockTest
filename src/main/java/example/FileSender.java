package example;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import static example.Main.PACKET_LIMIT;

public class FileSender {
    private byte[] buffer = new byte[Main.BUFFER_SIZE];
    private Map<Long, Segment> segments;
    private FileInputStream inputStream;
    private Sender sender;
    private Receiver receiver;
    private String fileName;
    private long size;
    private String md5digest;

    public FileSender(String filePath, Sender sender, Receiver receiver) {
        try {
            this.sender = sender;
            this.receiver = receiver;
            Path path = Paths.get(filePath);
            size = Files.size(path);
            segments = Segment.createMap(size);
            inputStream = new FileInputStream(path.toFile());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            fileName = path.getFileName().toString();
            int length;
            while ((length = inputStream.read(buffer)) >= 0) {
                byte[] data = Arrays.copyOf(buffer, length);
                md5.update(data);
            }
            md5digest = Base64.getEncoder().encodeToString(md5.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendInfo() {
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("Try #" + i);
                sender.send(-1, (fileName + "|" + size + "|" + md5digest).getBytes());
                String reply = new String(receiver.take().getData());
                if ("OK".equals(reply)) {
                    return;
                }
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void send() {
        startConfirmationThread();
        boolean stop = false;
        while (!stop) {
            try {
                segments.values().stream().filter(it -> !it.isConfirmed()).limit(PACKET_LIMIT).forEach(this::sendSegment);
                Thread.sleep(100);
                stop = segments.values().stream().allMatch(Segment::isConfirmed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendSegment(Segment segment) {
        try {
            System.out.println("Sending segment #"+segment.getId());
            inputStream.getChannel().position(segment.getOffset());
            int length = inputStream.read(buffer);
            byte[] data = Arrays.copyOf(buffer, length);
            sender.send(segment.getId(), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startConfirmationThread() {
        new Thread(() -> {
            boolean stop = false;
            while (!stop) {
                try {
                    Packet confirmation = receiver.take();
                    if ("OK".equals(new String(confirmation.getData()))) {
                        System.out.println("Segment confirmed #"+confirmation.getId());
                        segments.get(confirmation.getId()).setConfirmed(true);
                    }
                    stop = segments.values().stream().allMatch(Segment::isConfirmed);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}
