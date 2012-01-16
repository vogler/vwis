package de.tum.in.db.vwis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static de.tum.in.db.vwis.BigMath.computeLargerPrime;
import static de.tum.in.db.vwis.BigMath.computePrime;
import static de.tum.in.db.vwis.BigMath.extendedEuclidian;

/**
 * RSA encryption and decryption.
 */
public class RSA {

    /**
     * The number of witnesses used in internal calculations.
     */
    private static final int NUMBER_OF_WITNESSES = 50;

    /**
     * The minimum key size for key pairs.
     */
    private static final int MINIMUM_KEYSIZE = 64;

    /**
     * Computes a RSA key pair.
     *
     * @param n the key size in bits, must be 64 at least
     * @return a RSA key pair
     * @throws IllegalArgumentException if n < 64
     */
    public static KeyPair computeKeyPair(final int n) {
        if (n < MINIMUM_KEYSIZE) {
            throw new IllegalArgumentException(
                    "Key too small. Key must have at least 64 bits.");
        }
        final BigInteger p = computePrime(n/2, NUMBER_OF_WITNESSES);
        final BigInteger q = computePrime(n/2, NUMBER_OF_WITNESSES);

        final BigInteger m = p.multiply(q);
        final BigInteger d = computeLargerPrime(p.max(q), NUMBER_OF_WITNESSES);
        final BigInteger phi = p.subtract(BigInteger.ONE).multiply(
                q.subtract(BigInteger.ONE));
        final BigInteger ep = extendedEuclidian(phi, d)[2];
        final BigInteger e = ep.compareTo(BigInteger.ZERO) < 0 ?
                ep.add(phi) : ep;

        return new KeyPair(new Key(e, m), new Key(d, m));
    }

    /**
     * Transforms a message with RSA.
     *
     * @param m the message
     * @param key the RSA key
     * @return the transformed message
     */
    public static BigInteger rsa(final BigInteger m, final Key key) {
        return m.modPow(key.getKey(), key.getModulus());
    }

    /**
     * Transforms a message with RSA.
     *
     * @param m the message
     * @param key the key
     * @return the transformed message
     */
    public static List<BigInteger> rsa(final List<BigInteger> m, 
                                       final Key key)  {
        List<BigInteger> result = new ArrayList<>(m.size());
        for (final BigInteger i : m) {
            result.add(rsa(i, key));
        }
        return result;
    }
    
    public static List<BigInteger> encrypt(final String m, final Key k) {
        return rsa(encode(m), k);
    }
    
    public static String decrypt(final List<BigInteger> m, final Key k) {
        return decode(rsa(m, k));
    }
    
    public static List<BigInteger> encode(final String m) {
        final List<BigInteger> result = new ArrayList<>(m.length());
        for (final Character c : m.toCharArray()) {
            result.add(BigInteger.valueOf(c));
        }
        return result;
    }
    
    public static String decode(final List<BigInteger> m) {
        final StringBuilder sb = new StringBuilder(m.size());
        for (final BigInteger i: m) {
            sb.append((char) i.intValue());
        }
        return sb.toString();
    }
    

}
