package example;

import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static final int BUFFER_SIZE = 1024;
    public static int PACKET_LIMIT = 10;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage:\n" +
                    "Start receiver:\n" +
                    "udpSockTest port\n" +
                    "Send file:\n" +
                    "udpSockTest address:port fileName [packet limit]\n");
            return;
        }


        if (args[0].contains(":")) {
            final String[] addr = args[0].split(":");
            Client client = new Client(addr[0], Integer.parseInt(addr[1]));
            if (args.length == 3) {
                PACKET_LIMIT = Integer.parseInt(args[2]);
            }
            client.sendFile(args[1]);
        } else {
            Server receiver = new Server(Integer.parseInt(args[0]));
            receiver.receiveFile();
        }
        System.exit(0);
    }


}
