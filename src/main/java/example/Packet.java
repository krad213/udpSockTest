package example;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.zip.CRC32;

public class Packet {
    private long crc;
    private byte[] data;
    private DatagramPacket sourcePacket;

    public static Packet parse(DatagramPacket datagramPacket) throws InvalidPacketException {
        byte[] bytes = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
        long crc = ByteUtils.bytesToLong(Arrays.copyOfRange(bytes, 0, Long.BYTES));
        byte[] data = Arrays.copyOfRange(bytes, Long.BYTES, bytes.length);
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        if (crc != crc32.getValue()) {
            throw new InvalidPacketException("Crc incorrect", datagramPacket);
        }
        return new Packet(crc32.getValue(), data, datagramPacket);
    }

    public Packet(byte[] data) {
        this.data = data;
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        crc = crc32.getValue();
    }

    private Packet(long crc, byte[] data, DatagramPacket sourcePacket) {
        this.crc = crc;
        this.data = data;
        this.sourcePacket = sourcePacket;
    }

    public long getCrc() {
        return crc;
    }

    public byte[] getData() {
        return data;
    }

    public DatagramPacket getSourcePacket() {
        return sourcePacket;
    }

    public void setSourcePacket(DatagramPacket sourcePacket) {
        this.sourcePacket = sourcePacket;
    }

    public byte[] toBytes() {
        byte[] raw = new byte[Long.BYTES + data.length];
        System.arraycopy(ByteUtils.longToBytes(crc), 0, raw, 0, Long.BYTES);
        System.arraycopy(data, 0, raw, Long.BYTES, data.length);
        return raw;
    }
}
