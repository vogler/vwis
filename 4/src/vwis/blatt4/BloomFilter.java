package vwis.blatt4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A bloom filter implementation
 */
public class BloomFilter implements Serializable {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The number of hash functions to use
     */
    private final int numberOfHashFunctions;

    /**
     * The bit vector
     */
    private final byte[] bitVector;

    /**
     * Creates a new bloom filter.
     *
     * @param bitmapSize
     *            the size of the bitmap in bytes
     * @param numberOfHashFunctions
     *            the number of hash functions to use
     */
    public BloomFilter(int bitmapSize, int numberOfHashFunctions) {
        if (bitmapSize < 1) {
            throw new IllegalArgumentException(
                    "Bitmap must be one byte large as least");
        }
        if (numberOfHashFunctions < 1) {
            throw new IllegalArgumentException(
                    "Need at least one hash function");
        }
        this.bitVector = new byte[bitmapSize];
        this.numberOfHashFunctions = numberOfHashFunctions;
    }

    /**
     * Gets the size of the filter bitmap.
     *
     * @return the size of the filter bitmap, in bytes
     */
    public int getBitmapSize() {
        return this.bitVector.length;
    }

    /**
     * Gets the number of bits in the internal bitmap.
     *
     * @return the number of bits
     */
    public int getNumberOfBits() {
        return this.getBitmapSize() * 8;
    }

    /**
     * Gets the number of hash functions used.
     *
     * @return the number of hash functions used
     */
    public int getNumberOfHashFunctions() {
        return numberOfHashFunctions;
    }

    /**
     * Gets the internal bit vector.
     *
     * Changes to the returned array are <em>not</em> reflected to the internal
     * bit vector.
     *
     * @return a copy of the internal bit vector
     */
    public byte[] getBitVector() {
        return Arrays.copyOf(this.bitVector, this.bitVector.length);
    }

    /**
     * Hashes the given object with the nth hash function.
     *
     * The hash is a murmur hash of the serialized object representation.
     *
     * @param obj
     *            the object to hash
     * @param hashFunction
     *            the index of the hash function to use
     * @return the hash value
     * @throws IOException
     *             if serialization failed
     */
    private int getObjectHash(Object obj, int hashFunction) throws IOException {
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        new ObjectOutputStream(sink).writeObject(obj);
        return MurmurHash2.hash(sink.toByteArray(), hashFunction + 1);
    }

    /**
     * Gets all hashes for the given object.
     *
     * @param obj
     *            the object to hash
     * @return an array with the hashes of the given object
     * @throws IOException
     *             if hashing failed
     */
    private int[] getObjectHashes(Object obj) throws IOException {
        final int[] hashes = new int[this.numberOfHashFunctions];
        for (int i = 0; i < this.numberOfHashFunctions; ++i) {
            hashes[i] = this.getObjectHash(obj, i);
        }
        return hashes;
    }

    /**
     * Gets all bits to set for the given object.
     *
     * @param obj
     *            the object to hash
     * @return an array with the bit positions for the given objects
     * @throws IOException
     *             if hashing failed
     */
    private int[] getObjectBits(Object obj) throws IOException {
        final int[] hashes = this.getObjectHashes(obj);
        final int[] bits = new int[hashes.length];
        for (int i = 0; i < hashes.length; ++i) {
            bits[i] = Math.abs(hashes[i] % this.getNumberOfBits());
        }
        return bits;
    }

    /**
     * Sets the bit at the given position, retaining the values of all other
     * bits.
     *
     * @param bitPos
     *            the bit to set
     */
    private void setBit(int bitPos) {
        final int bytePos = bitPos / 8;
        final int bitMask = 1 << (bitPos % 8);
        this.bitVector[bytePos] = (byte) (this.bitVector[bytePos] | bitMask);
    }

    /**
     * Tests the bit at the given position.
     *
     * @param bitPos
     *            the position of the bit
     * @return true, if the bit is set, false otherwise
     */
    private boolean testBit(int bitPos) {
        final int bytePos = bitPos / 8;
        final int bitMask = 1 << (bitPos % 8);
        return (this.bitVector[bytePos] & bitMask) != 0;
    }

    /**
     * Adds the given object to this filter.
     *
     * @param obj
     *            the object to add
     * @throws IOException
     *             if hashing failed
     */
    public void addObject(Object obj) throws IOException {
        for (final int bit : this.getObjectBits(obj)) {
            this.setBit(bit);
        }
    }

    /**
     * Tests whether the given object matches this filter.
     *
     * @param obj
     *            the object to match
     * @return true, if the given object matches this filter, false otherwise
     * @throws IOException
     *             if hashing failed
     */
    public boolean matches(Object obj) throws IOException {
        for (final int bit : this.getObjectBits(obj)) {
            if (!this.testBit(bit)) {
                return false;
            }
        }
        return true;
    }
}
