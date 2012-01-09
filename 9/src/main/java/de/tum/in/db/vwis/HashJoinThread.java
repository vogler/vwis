package de.tum.in.db.vwis;

import java.util.ArrayList;
import java.util.Collection;

class HashJoinThread extends Thread {

    private Partition a;
    private Partition b;
    private Collection<JoinedPair> results;

    public HashJoinThread(Partition a, Partition b) {
        this.a = a;
        this.b = b;
        this.results = null;
    }

    public void run() {
        this.results = new ArrayList<>(Math.max(a.size(), b.size()));
        for (final Tuple ta : a.values()) {
            final Tuple tb = b.get(ta.getKey());
            if (tb != null && ta.getKey() == tb.getKey()) {
                this.results.add(new JoinedPair(ta, tb));
            }
        }
    }

    public Collection<JoinedPair> getResults() {
        return results;
    }
}
