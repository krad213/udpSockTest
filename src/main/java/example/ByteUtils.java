package example;

import java.nio.ByteBuffer;

public class ByteUtils {
    public static byte[] longToBytes(long x) {
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
        longBuffer.putLong(0, x);
        return longBuffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
        longBuffer.put(bytes, 0, bytes.length);
        return longBuffer.flip().getLong();
    }
}
