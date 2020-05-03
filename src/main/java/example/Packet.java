package example;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.zip.CRC32;

public class Packet {
    private long id;
    private long crc;
    private byte[] data;
    private DatagramPacket sourcePacket;

    public static Packet parse(DatagramPacket datagramPacket) throws InvalidPacketException {
        byte[] bytes = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
        long id = ByteUtils.bytesToLong(Arrays.copyOfRange(bytes, 0, Long.BYTES));
        long crc = ByteUtils.bytesToLong(Arrays.copyOfRange(bytes, Long.BYTES, Long.BYTES*2));
        byte[] data = Arrays.copyOfRange(bytes, Long.BYTES * 2, bytes.length);
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        if (crc != crc32.getValue()) {
            throw new InvalidPacketException("Crc incorrect", datagramPacket);
        }
        return new Packet(id, crc32.getValue(), data, datagramPacket);
    }

    public Packet(long id, byte[] data) {
        this.id = id;
        this.data = data;
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        crc = crc32.getValue();
    }

    private Packet(long id, long crc, byte[] data, DatagramPacket sourcePacket) {
        this.id = id;
        this.crc = crc;
        this.data = data;
        this.sourcePacket = sourcePacket;
    }

    public long getId() {
        return id;
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

    public byte[] toBytes() {
        byte[] raw = new byte[Long.BYTES * 2 + data.length];
        System.arraycopy(ByteUtils.longToBytes(id), 0, raw, 0, Long.BYTES);
        System.arraycopy(ByteUtils.longToBytes(crc), 0, raw, Long.BYTES, Long.BYTES);
        System.arraycopy(data, 0, raw, Long.BYTES*2, data.length);
        return raw;
    }
}
