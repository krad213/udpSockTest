package example;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage:\n" +
                    "Start receiver:\n" +
                    "udpSockTest port\n" +
                    "Start sender:\n" +
                    "udpSockTest address:port\n" +
                    "Send file:\n" +
                    "udpSockTest address:port fileName\n");
            return;
        }

        if (args[0].contains(":")) {
            final String[] addr = args[0].split(":");
            try (Client client = new Client(addr[0], Integer.parseInt(addr[1]))) {
                if (args.length == 1) {
                    client.send(new InputStreamReader(System.in));
                } else {
                    client.sendFile(args[1]);
                }
            }
        } else {
            try (Server server = new Server(Integer.parseInt(args[0]))) {
                server.listen();
            }
        }
    }


}
