package example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Segment {
    public static Map<Long, Segment> createMap(long size) {
        Map<Long, Segment> segments = new ConcurrentHashMap<>();
        long segmentCount = size / Main.BUFFER_SIZE;
        for (long i = 0; i <= segmentCount; i++) {
            segments.put(i, new Segment(i, Main.BUFFER_SIZE * i));
        }
        return segments;
    }

    long id;
    long offset;
    boolean confirmed;

    public Segment(long id, long offset) {
        this.id = id;
        this.offset = offset;
    }

    public long getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
