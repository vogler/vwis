package de.tum.in.db.vwis;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.tum.in.db.vwis.BigMath.*;

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
     * Chunk size for encode() and decode().
     */
    private static final int CHUNK_SIZE = 4;

    /**
     * The character set used by encode() and decode().
     */
    private static final Charset ENCODING = Charset.forName("UTF-8");

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
        final BigInteger p = computePrime(n / 2, NUMBER_OF_WITNESSES);
        final BigInteger q = computePrime(n / 2, NUMBER_OF_WITNESSES);

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
     * @param m   the message
     * @param key the RSA key
     * @return the transformed message
     */
    public static BigInteger rsa(final BigInteger m, final Key key) {
        return m.modPow(key.getKey(), key.getModulus());
    }

    /**
     * Transforms a message with RSA.
     *
     * @param m   the message
     * @param key the key
     * @return the transformed message
     */
    public static List<BigInteger> rsa(final List<BigInteger> m,
                                       final Key key) {
        List<BigInteger> result = new ArrayList<>(m.size());
        for (final BigInteger i : m) {
            result.add(rsa(i, key));
        }
        return result;
    }

    /**
     * Encrypts the given message with the given key.
     *
     * @param m the message to encrypt
     * @param k the key to use
     * @return the encrypted message
     */
    public static List<BigInteger> encrypt(final String m, final Key k) {
        return rsa(encode(m), k);
    }

    /**
     * Decrypts the given message with the given key.
     *
     * @param c the cypher text
     * @param k the key
     * @return the decrypted message
     */
    public static String decrypt(final List<BigInteger> c, final Key k) {
        return decode(rsa(c, k));
    }

    /**
     * Encodes the given string into a sequence of big numbers suitable for
     * RSA transformation.
     *
     * @param m the message
     * @return the encoded message
     */
    public static List<BigInteger> encode(final String m) {
        final byte[] bytes = m.getBytes(ENCODING);
        final int padding = bytes.length % CHUNK_SIZE != 0 ?
                CHUNK_SIZE - (bytes.length % CHUNK_SIZE) : 0;
        final ByteBuffer buffer = ByteBuffer.allocate(
                4 + bytes.length + padding);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        final byte[] padBytes = new byte[buffer.remaining()];
        new SecureRandom().nextBytes(padBytes);
        buffer.put(padBytes);

        final List<BigInteger> result = new ArrayList<>(m.length());
        for (int i = 0; i < buffer.array().length; i += CHUNK_SIZE) {
            final byte[] chunk = Arrays.copyOfRange(
                    buffer.array(), i, i + CHUNK_SIZE);
            result.add(new BigInteger(chunk));
        }
        return result;
    }

    /**
     * Decodes an encoded message. Inverse of #encode(String m)
     *
     * @param m the message
     * @return the decoded message
     */
    public static String decode(final List<BigInteger> m) {
        final ByteBuffer buffer = ByteBuffer.allocate(
                m.size() * CHUNK_SIZE - 1);
        for (int i = 1; i < m.size(); ++i) {
            final byte[] bytes = m.get(i).toByteArray();
            final byte[] padding = new byte[CHUNK_SIZE - bytes.length];
            buffer.put(padding);
            buffer.put(bytes);
        }
        final int length = m.get(0).intValue();
        final byte[] bytes = Arrays.copyOfRange(buffer.array(), 0, length);
        return new String(bytes, ENCODING);
    }
}
