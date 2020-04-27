package example;

import java.net.DatagramPacket;

public class InvalidPacketException extends Exception {
    private DatagramPacket datagramPacket;

    public InvalidPacketException(String message, DatagramPacket datagramPacket) {
        super(message);
        this.datagramPacket = datagramPacket;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }
}
