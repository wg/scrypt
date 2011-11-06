// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.crypto.test;

import com.lambdaworks.crypto.SCrypt;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.lambdaworks.crypto.test.CryptoTestUtil.*;
import static com.lambdaworks.crypto.SCrypt.*;

public class SCryptTest {
    @Test
    public void scrypt_paper_appendix_b() throws Exception {
        byte[] P, S;
        int N, r, p, dkLen;
        String DK;

        // empty key & salt test missing because unsupported by JCE

        P = "password".getBytes("UTF-8");
        S = "NaCl".getBytes("UTF-8");
        N = 1024;
        r = 8;
        p = 16;
        dkLen = 64;
        DK = "fdbabe1c9d3472007856e7190d01e9fe7c6ad7cbc8237830e77376634b3731622eaf30d92e22a3886ff109279d9830dac727afb94a83ee6d8360cbdfa2cc0640";

        assertArrayEquals(decode(DK), SCrypt.scrypt(P, S, N, r, p, dkLen));

        P = "pleaseletmein".getBytes("UTF-8");
        S = "SodiumChloride".getBytes("UTF-8");
        N = 16384;
        r = 8;
        p = 1;
        dkLen = 64;
        DK = "7023bdcb3afd7348461c06cd81fd38ebfda8fbba904f8e3ea9b543f6545da1f2d5432955613f0fcf62d49705242a9af9e61e85dc0d651e40dfcf017b45575887";

        assertArrayEquals(decode(DK), scrypt(P, S, N, r, p, dkLen));

        P = "pleaseletmein".getBytes("UTF-8");
        S = "SodiumChloride".getBytes("UTF-8");
        N = 1048576;
        r = 8;
        p = 1;
        dkLen = 64;
        DK = "2101cb9b6a511aaeaddbbe09cf70f881ec568d574a2ffd4dabe5ee9820adaa478e56fd8f4ba5d09ffa1c6d927c40f4c337304049e8a952fbcbf45c6fa77a41a4";

        assertArrayEquals(decode(DK), SCrypt.scrypt(P, S, N, r, p, dkLen));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scrypt_invalid_N_zero() throws Exception {
        byte[] P = "pleaseletmein".getBytes("UTF-8");
        byte[] S = "SodiumChloride".getBytes("UTF-8");
        scrypt(P, S, 0, 1, 1, 64);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scrypt_invalid_N_odd() throws Exception {
        byte[] P = "pleaseletmein".getBytes("UTF-8");
        byte[] S = "SodiumChloride".getBytes("UTF-8");
        scrypt(P, S, 3, 1, 1, 64);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scrypt_invalid_N_large() throws Exception {
        byte[] P = "pleaseletmein".getBytes("UTF-8");
        byte[] S = "SodiumChloride".getBytes("UTF-8");
        int    r = 8;
        int    N = Integer.MAX_VALUE / 128;
        scrypt(P, S, N, r, 1, 64);
    }

//    @Test(expected = IllegalArgumentException.class)
//    public void scrypt_invalid_r_large() throws Exception {
//        byte[] P = "pleaseletmein".getBytes("UTF-8");
//        byte[] S = "SodiumChloride".getBytes("UTF-8");
//        int    N = 1024;
//        int    r = Integer.MAX_VALUE / 128 + 1;
//        int    p = 0;
//        scrypt(P, S, N, r, p, 64);
//    }
}
