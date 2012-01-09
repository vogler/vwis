package de.tum.in.db.vwis;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HashJoin {

    private int numberOfThreads;

    public HashJoin() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public HashJoin(final int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    private int getTupleThreadNumber(final Tuple t) {
        return new Long(t.getKey()).hashCode() % this.numberOfThreads;
    }

    public Collection<JoinedPair> join(
            final Collection<Tuple> a, final Collection<Tuple> b) throws
            InterruptedException {
        final List<HashJoinThread> threads = this.createThreads(a, b);
        StopWatch watch = new StopWatch();
        watch.start();
        for (final Thread t : threads) {
            t.start();
        }
        for (final Thread t : threads) {
            t.join();
        }
        watch.stop();
        this.log("joined in %d ms", watch.getTime());
        return this.mergeResults(threads);
    }

    private Collection<JoinedPair> mergeResults(
            final List<HashJoinThread> threads) {
        final Collection<JoinedPair> results = new ArrayList<>(threads.size());
        for (final HashJoinThread t : threads) {
            results.addAll(t.getResults());
        }
        return results;
    }

    private List<HashJoinThread> createThreads(
            final Collection<Tuple> a, final Collection<Tuple> b) {
        StopWatch watch = new StopWatch();
        watch.start();
        final Partition[] pa = this.getPartitions(a);
        watch.stop();
        this.log("partitioned a in %d ms", watch.getTime());
        watch.reset();
        watch.start();
        // XXX: strangely enough the second invocation of ".getPartitions()"
        // takes *significantly* (up to two or three times) longer than the
        // first.
        final Partition[] pb = this.getPartitions(b);
        watch.stop();
        this.log("partitioned b in %d ms", watch.getTime());
        final List<HashJoinThread> threads = new ArrayList<>(
                this.numberOfThreads);
        for (int i = 0; i < this.numberOfThreads; ++i) {
            threads.add(new HashJoinThread(pa[i], pb[i]));
        }
        return threads;
    }

    private Partition[] getPartitions(final Collection<Tuple> c) {
        final Partition[] partitions = new Partition[numberOfThreads];
        for (int i = 0; i < partitions.length; ++i) {
            partitions[i] = new Partition();
        }
        for (final Tuple t : c) {
            final int threadNumber = this.getTupleThreadNumber(t);
            final Partition p = partitions[threadNumber];
            p.put(t.getKey(), t);
        }
        return partitions;
    }

    private void log(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

}
