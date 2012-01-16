package de.tum.in.db.vwis;


import java.math.BigInteger;
import java.security.SecureRandom;
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
        return new BigInteger(n, new SecureRandom()).or(BigInteger.ONE);
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
            final BigInteger j = jacobi(a.divide(TWO), b);
            return isOdd(exp) ? j.negate() : j;
        } else {
            final BigInteger exp = a.subtract(BigInteger.ONE).multiply(
                    b.subtract(BigInteger.ONE)).divide(FOUR);
            final BigInteger j = jacobi(b.mod(a), a);
            return isOdd(exp) ? j.negate() : j;
        }
    }

    /**
     * Computes m witnesses for the primality of p.
     *
     * @param p a big integer
     * @param m the number of witnesses
     * @return a list of m witnesses
     */
    static List<BigInteger> computeWitnesses(final BigInteger p, final int m) {
        // assure that the witnesses are smaller by choosing a smaller bitsize
        final int n = p.bitLength() - 1;
        final Random rng = new SecureRandom();
        List<BigInteger> witnesses = new ArrayList<>(m);
        for (int i = 0; i < m; ++i) {
            BigInteger w;
            do {
                w = new BigInteger(n, rng);
            } while (w.compareTo(p) >= 0);
            witnesses.add(w);
        }
        return witnesses;
    }

    /**
     * Indicates that a number is not a prime number.
     */
    private static class NotAPrimeException extends Exception {
        public NotAPrimeException() {
            super("Not a prime");
        }
    }

    /**
     * Tries to compute a single prime with probability of 1-(0.5)^m.
     *
     * @param n the number digits of the computed prime
     * @param m a measure for the probability of the primality of the result
     * @return a probable prime number
     * @throws NotAPrimeException if no prime could be computed
     */
    private static BigInteger tryComputePrime(
            final int n, final int m) throws NotAPrimeException {
        final BigInteger p = randomOddInteger(n);
        final List<BigInteger> witnesses = computeWitnesses(p, m);

        for (final BigInteger w : witnesses) {
            if (p.gcd(w).compareTo(BigInteger.ONE) != 0) {
                throw new NotAPrimeException();
            }

            final BigInteger i = w.modPow(
                    p.subtract(BigInteger.ONE).divide(TWO), p);
            final BigInteger j = jacobi(w, p);
            if (j.mod(p).compareTo(i.mod(p)) != 0) {
                throw new NotAPrimeException();
            }
        }

        return p;
    }

    /**
     * Computes a number which is prime with a probability of 1-(0.5)^m.
     *
     * @param n the number of binary digits of the computed prime
     * @param m the number of witnesses for the primality of the result
     * @return a probable prime
     */
    public static BigInteger computePrime(final int n, final int m) {
        //return new BigInteger(n, m, new SecureRandom());
        while (true) {
            try {
                return tryComputePrime(n, m);
            } catch (NotAPrimeException e) {
                continue;
            }
        }
    }

    /**
     * Computes a number larger than the given big integer that is prime with
     * a probability of 1-(0.5)^m.
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
            return new BigInteger[]{r, y, x.subtract(a.divide(b).multiply(y))};
        }
    }
}
