package de.tum.in.db.vwis;


import com.sun.xml.internal.ws.util.ReadAllStream;

import java.math.BigInteger;
import java.util.List;

public class Main {
    
    private static String M = "ARE THE SQUEAMISH WORDS MAGIC AND OSSIFRAGE";
    private static int[] KEYS = new int[]{64, 256, 1024, 4096};

    public static void main(String[] args) {
        for (final int n : KEYS) {
            testRSA(n);
            System.out.println();
        }
    }

    public static void testRSA(final int n) {
        System.out.printf("Key length: %d\n", n);
        final KeyPair keys = RSA.computeKeyPair(n);
        System.out.printf("Public key: %s\n", keys.getPublicKey());
        System.out.printf("Private key: %s\n", keys.getPrivateKey());

        final List<BigInteger> C = RSA.encrypt(M, keys.getPrivateKey());
        System.out.printf("Encrypted message: %s\n", C);
        final List<BigInteger> M1 = RSA.rsa(C, keys.getPublicKey());
        System.out.printf("Decrypted message: %s\n", M1);
        System.out.printf("Original message: %s\n", RSA.encode(M));
        System.out.printf("Equals original message? %s\n",
                RSA.encode(M).equals(M1));
    }

}
