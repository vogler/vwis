package de.tum.in.db.vwis;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for big integer mathematics.
 */
public class BigMath {

    public static final BigInteger TWO = new BigInteger("2");
    public static final BigInteger FOUR = new BigInteger("4");
    public static final BigInteger EIGHT = new BigInteger("8");


    /**
     * Tests if the given big integer is odd.
     *
     * @param i a big integer
     * @return true, if i is odd, false otherwise.
     */
    public static boolean isOdd(final BigInteger i) {
        return i.testBit(0);
    }

    /**
     * Tests if the given big integer is even.
     *
     * @param i a big integer
     * @return true, if i is odd, false otherwise.
     */
    public static boolean isEven(final BigInteger i) {
        return !isOdd(i);
    }

    /**
     * Computes a random odd integer.
     *
     * @param n the bit size
     * @return a random odd integer
     */
    public static BigInteger randomOddInteger(final int n) {
        return new BigInteger(n, new Random()).or(BigInteger.ONE);
    }

    /**
     * Computes the Jakobi symbol for a and b.
     *
     * @param a
     * @param b
     * @return the jakobi symbol of a and b
     */
    public static BigInteger jacobi(final BigInteger a, final BigInteger b) {
        if (a.equals(BigInteger.ONE)) {
            return BigInteger.ONE;
        } else if (isEven(a)) {
            final BigInteger exp = b.pow(2).subtract(
                    BigInteger.ONE).divide(EIGHT);
            final BigInteger j = jacobi(a.divide(BigMath.TWO), b);
            return isOdd(exp) ? j.negate() : j;
        } else {
            final BigInteger exp = a.subtract(BigInteger.ONE).multiply(
                    b.subtract(BigInteger.ONE)).divide(FOUR);
            final BigInteger j = jacobi(b.mod(a), a);
            return isOdd(exp) ? j.negate() : j;
        }

    }

    /**
     * Computes m witnesses for the primaliy of p.
     *
     * @param p a big integer
     * @param m the number of witnesses
     * @return a list of m witnesses
     */
    static List<BigInteger> computeWitnesses(final BigInteger p, final int m) {
        // assure that the witnesses are smaller by choosing a smaller bitsize
        final int n = p.bitLength() - 1;
        final Random rng = new Random();
        List<BigInteger> witnesses = new ArrayList<>(m);
        for (int i = 0; i < m; ++i) {
            witnesses.add(new BigInteger(n, rng));
        }
        return witnesses;
    }

    /**
     * Computes a probable prime.
     * <p/>
     * The probability of the primality of the returned number is 1-(0.5)^m.
     *
     * @param n the number of binary places of the computed prime
     * @param m the number of witnesses for the primality of the result
     * @return a probable prime
     */
    public static BigInteger computePrime(final int n, final int m) {
        // compute random integer
        final BigInteger p = randomOddInteger(n);
        final List<BigInteger> witnesses = computeWitnesses(p, m);

        for (final BigInteger w : witnesses) {
            if (!p.gcd(w).equals(BigInteger.ONE)) {
                // restart if any witness divides p
                return computePrime(n, m);
            }

            final BigInteger i = w.modPow(
                    p.subtract(BigInteger.ONE).divide(TWO), p);
            if (jacobi(w, p).equals(i)) {
                return computePrime(n, m);
            }
        }
        return p;
    }

    /**
     * Computes a probable prime larger than the given big integer.
     * <p/>
     * The probability of the primality of the returned number is 1-(0.5)^m.
     *
     * @param i a big integer
     * @param m the number of witnesses for the primality of the result
     * @return a probable prime larger than i
     */
    public static BigInteger computeLargerPrime(final BigInteger i,
                                                final int m) {
        final int n = i.bitLength() * 2;
        BigInteger d;
        do {
            d = computePrime(n, m);
        } while (d.compareTo(i) <= 0);
        return d;
    }

    /**
     * Computes the extended euclidian of a and b.
     *
     * @param a
     * @param b
     * @return an array of three elements with the results of the extended
     *         euclidian of a and b
     */
    public static BigInteger[] extendedEuclidian(final BigInteger a,
                                                 final BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return new BigInteger[]{a, BigInteger.ONE, BigInteger.ZERO};
        } else {
            final BigInteger[] rxy = extendedEuclidian(b, a.mod(b));
            final BigInteger r = rxy[0];
            final BigInteger x = rxy[1];
            final BigInteger y = rxy[2];
            return new BigInteger[]{
                    r, y, x.subtract(a.divide(b).multiply(y))
            };
        }
    }
}
