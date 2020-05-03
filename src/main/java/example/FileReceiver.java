package example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import static example.Replies.OK;

public class FileReceiver {
    private Map<Long, Segment> segments;
    private Receiver receiver;
    private Sender sender;
    private FileOutputStream os;

    public FileReceiver(String fileName, long size, Receiver receiver, Sender sender) {
        try {
            this.receiver = receiver;
            this.sender = sender;

            this.segments = Segment.createMap(size);
            this.os = new FileOutputStream(fileName);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void receive() {
        try {
            boolean stop = false;
            while (!stop) {
                Packet packet = receiver.take();
                Segment segment = segments.get(packet.getId());
                System.out.println("Received segment #" + segment.getId());
                os.getChannel().position(segment.offset);
                os.write(packet.getData());
                segment.setConfirmed(true);
                sender.send(packet.getId(), OK);
                stop = segments.values().stream().allMatch(Segment::isConfirmed);
            }
        } catch (InterruptedException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
