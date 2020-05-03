package example;

public class Client {
    private byte[] buffer = new byte[1024-Long.BYTES];
    private Sender sender;
    private Receiver receiver;

    public Client(String ip, int port) {
        sender = new Sender(ip, port);
        sender.setCorrupt(true);
        if ("localhost".equals(ip) || "127.0.0.1".equals(ip)) {
            receiver = new Receiver(port + 1);
        } else {
            receiver = new Receiver(port);
        }
    }


    public void sendFile(String filePath) {
        receiver.listen();
        FileSender fileSender = new FileSender(filePath, sender, receiver);
        fileSender.sendInfo();
        fileSender.send();
    }
}
