package de.tum.in.db.vwis;

import java.math.BigInteger;

/**
 * A single RSA key.
 */
public class Key extends Pair<BigInteger> {
    
    public Key(final BigInteger key, final BigInteger modulus) {
        super(key, modulus);
    }

    public BigInteger getKey() {
        return this.getFirst();
    }

    public BigInteger getModulus() {
        return this.getSecond();
    }
}
