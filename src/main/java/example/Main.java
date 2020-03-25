package example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage:\n" +
                    "Start receiver:\n" +
                    "sockTest port\n" +
                    "Start sender:\n" +
                    "sockTest address:port");
        }

        if (args[0].contains(":")) {
            final String[] addr = args[0].split(":");
            new Client().connect(addr[0], Integer.parseInt(addr[1]));
        } else {
            new Server().listen(Integer.parseInt(args[0]));
        }
    }
}
