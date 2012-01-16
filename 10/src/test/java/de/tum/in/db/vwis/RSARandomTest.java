package de.tum.in.db.vwis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RSARandomTest {

    private static final int NUMBER_OF_TESTS = 100;

    private String s;
    private KeyPair keys;

    @Parameterized.Parameters
    public static Collection<Object[]> randomString() {
        final List<Object[]> params = new ArrayList<>(NUMBER_OF_TESTS);
        for (int i = 0; i < NUMBER_OF_TESTS; ++i) {
            params.add(new Object[]{UUID.randomUUID().toString()});
        }
        return params;
    }

    public RSARandomTest(final String s) {
        this.s = s;
        this.keys = RSA.computeKeyPair(64);
    }

    @Test
    public void testEncodeDecodeRoundtrip() {
        assertEquals(s, RSA.decode(RSA.encode(s)));
    }

    @Test
    public void testRsaRoundtrip() {
        final List<BigInteger> m = RSA.encode(this.s);
        assertEquals(m, RSA.rsa(
                RSA.rsa(m, keys.getPrivateKey()), keys.getPublicKey()));
        assertEquals(m, RSA.rsa(
                RSA.rsa(m, keys.getPublicKey()), keys.getPrivateKey()));
    }

    @Test
    public void testEncryptDecryptRoundtrip() {
        assertEquals(s, RSA.decrypt(
                RSA.encrypt(s, keys.getPrivateKey()), keys.getPublicKey()));
        assertEquals(s, RSA.decrypt(
                RSA.encrypt(s, keys.getPublicKey()), keys.getPrivateKey()));
    }
}
