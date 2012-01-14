package de.tum.in.db.vwis;


/**
 * A pair of public and secret keys.
 */
public class KeyPair extends Pair<Key> {

    public KeyPair(final Key publicKey, final Key privateKey) {
        super(publicKey, privateKey);
    }
    
    public Key getPublicKey() {
        return this.getFirst();
    }
    
    public Key getPrivateKey() {
        return this.getSecond();
    }
    
}
