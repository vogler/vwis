package de.tum.in.db.vwis;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.Collection;

public class Benchmark {

    public static final int RELATION_SIZE = 1000000;
    private static Collection<Tuple> R;
    private static Collection<Tuple> S;

    public static void main(String[] args) throws InterruptedException {
        R = makeRelation();
        S = makeRelation();

        System.out.printf("|R| = %s; |S| = %s\n", R.size(), S.size());

        final long singleThreadedTime = measureJoin(new HashJoin(1));
        printResults("singlethreaded", singleThreadedTime);
        final HashJoin multiThreadedJoin = new HashJoin();
        final long multiThreadedTime = measureJoin(multiThreadedJoin);
        printResults(String.format("%d threads",
                multiThreadedJoin.getNumberOfThreads()), multiThreadedTime);
        final double ratio = (double) multiThreadedTime /
                (double) singleThreadedTime;
        System.out.printf("multithreaded / singlethreaded = %f\n", ratio);
        final long t2 = measureJoin(new HashJoin(4));
        printResults(String.format("%d threads", 4), t2); // best result on Core2 Duo
        final long t3 = measureJoin(new HashJoin(8));
        printResults(String.format("%d threads", 8), t3);
        final long t4 = measureJoin(new HashJoin(16));
        printResults(String.format("%d threads", 16), t4);
    }

    private static void printResults(String label, long time) {
        System.out.printf("Test '%s' took %d ms\n", label, time);
    }

    private static long measureJoin(HashJoin join) throws
            InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Collection<JoinedPair> results = join.join(R, S);
        watch.stop();
        assert results.size() == RELATION_SIZE;
        return watch.getTime();
    }

    private static Collection<Tuple> makeRelation() {
        final Tuple[] relation = new Tuple[RELATION_SIZE];
        for (int i = 0; i < relation.length; ++i) {
            relation[i] = new Tuple(i, i);
        }
        return Arrays.asList(relation);
    }
}
