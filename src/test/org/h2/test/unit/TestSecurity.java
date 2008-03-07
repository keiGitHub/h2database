/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0
 * (license2)
 * Initial Developer: H2 Group
 */
package org.h2.test.unit;

import org.h2.security.AES;
import org.h2.security.SHA256;
import org.h2.security.XTEA;
import org.h2.test.TestBase;
import org.h2.util.ByteUtils;

/**
 * Tests various security primitives.
 */
public class TestSecurity extends TestBase {

    public void test() throws Exception {
        testSHA();
        testAES();
        testXTEA();
    }

    public void testSHA() throws Exception {
        SHA256 sha = new SHA256();
        testOneSHA(sha);
    }

    private String getHashString(SHA256 sha, byte[] data) {
        byte[] result = sha.getHash(data);
        return ByteUtils.convertBytesToString(result);
    }
    
    private void testOneSHA(SHA256 sha) throws Exception {
        if (!getHashString(sha, new byte[] {}).equals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")) {
            throw new Exception("x");
        }
        if (!getHashString(sha, new byte[] { 0x19 }).equals(
                "68aa2e2ee5dff96e3355e6c7ee373e3d6a4e17f75f9518d843709c0c9bc3e3d4")) {
            throw new Exception("x");
        }
        if (!getHashString(
                sha,
                new byte[] { (byte) 0xe3, (byte) 0xd7, 0x25, 0x70, (byte) 0xdc, (byte) 0xdd, 0x78, 0x7c, (byte) 0xe3,
                        (byte) 0x88, 0x7a, (byte) 0xb2, (byte) 0xcd, 0x68, 0x46, 0x52 }).equals(
                "175ee69b02ba9b58e2b0a5fd13819cea573f3940a94f825128cf4209beabb4e8")) {
            throw new Exception("x");
        }
        check("", "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
        check("a", "CA978112CA1BBDCAFAC231B39A23DC4DA786EFF8147C4E72B9807785AFEE48BB");
        check("abc", "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
        check("message digest", "F7846F55CF23E14EEBEAB5B4E1550CAD5B509E3348FBC4EFA3A1413D393CB650");
        check("abcdefghijklmnopqrstuvwxyz", "71C480DF93D6AE2F1EFAD1447C66C9525E316218CF51FC8D9ED832F2DAF18B73");
        check("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", "248D6A61D20638B8E5C026930C3E6039A33CE45964FF2167F6ECEDD419DB06C1");
        check("12345678901234567890123456789012345678901234567890123456789012345678901234567890", "F371BC4A311F2B009EEF952DD83CA80E2B60026C8E935592D0F9C308453C813E");
        StringBuffer buff = new StringBuffer(1000000);
        buff.append('a');
        check(buff.toString(), "CA978112CA1BBDCAFAC231B39A23DC4DA786EFF8147C4E72B9807785AFEE48BB");
    }
    
    void checkSHA256(String message, String expected) throws Exception {
        SHA256 sha = new SHA256();
        String hash = ByteUtils.convertBytesToString(sha.getHash(message.getBytes())).toUpperCase();
        check(expected, hash);
    }

    public void testXTEA() throws Exception {
        byte[] test = new byte[4096];
        XTEA xtea = new XTEA();
        xtea.setKey("abcdefghijklmnop".getBytes());
        for (int i = 0; i < 10; i++) {
            xtea.decryptBlock(test, test, 0);
        }
    }

    private void testAES() throws Exception {
        AES test = new AES();
        test.setKey(ByteUtils.convertStringToBytes("000102030405060708090A0B0C0D0E0F"));

        byte[] in = new byte[128];
        byte[] enc = new byte[128];
        test.encrypt(enc, 0, 128);
        test.decrypt(enc, 0, 128);
        if (ByteUtils.compareNotNull(in, enc) != 0) {
            throw new Error("hey!");
        }

        for (int i = 0; i < 10; i++) {
            test.encrypt(in, 0, 128);
            test.decrypt(enc, 0, 128);
        }
    }

}
