package de.tum.in.db.vwis;

public class Tuple {

    private long key;
    private long payload;

    public Tuple(final long key, final long payload) {
        this.key = key;
        this.payload = payload;
    }

    public long getKey() {
        return key;
    }

    public long getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;

        Tuple tuple = (Tuple) o;

        if (key != tuple.key) return false;
        if (payload != tuple.payload) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (key ^ (key >>> 32));
        result = 31 * result + (int) (payload ^ (payload >>> 32));
        return result;
    }
}
