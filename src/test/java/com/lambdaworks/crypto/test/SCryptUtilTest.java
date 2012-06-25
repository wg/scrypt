// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.crypto.test;

import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCryptUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SCryptUtilTest {

    private static final int N = 16384;
    private static final int r = 8;
    private static final int p = 1;

    @Test
    public void scrypt() {
        String hashed = SCryptUtil.scrypt("secret", N, r, p);
        String[] parts = hashed.split("\\$");

        assertEquals(5, parts.length);
        assertEquals("s0", parts[1]);
        Assert.assertEquals(16, Base64.decode(parts[3].toCharArray()).length);
        assertEquals(32, Base64.decode(parts[4].toCharArray()).length);

        int params = Integer.valueOf(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >> 16 & 0xff));
        assertEquals(r, params >>  8 & 0x0f);
        assertEquals(p, params >>  0 & 0x0f);
    }

    @Test
    public void scrypt_WithSpecifiedSalt() {
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 1);

        String hashed = SCryptUtil.scrypt("secret", salt, N, r, p);
        String[] parts = hashed.split("\\$");

        assertEquals(5, parts.length);
        assertEquals("s0", parts[1]);
        Assert.assertEquals(16, Base64.decode(parts[3].toCharArray()).length);
        assertEquals(32, Base64.decode(parts[4].toCharArray()).length);

        int params = Integer.valueOf(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >> 16 & 0xff));
        assertEquals(r, params >>  8 & 0x0f);
        assertEquals(p, params >>  0 & 0x0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scrypt_WithNullSalt() {
        SCryptUtil.scrypt("secret", null, N, r, p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scrypt_WithTooShortSalt() {
        byte[] salt = new byte[15];
        Arrays.fill(salt, (byte) 1);

        SCryptUtil.scrypt("secret", salt, N, r, p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void scrypt_WithTooLongSalt() {
        byte[] salt = new byte[17];
        Arrays.fill(salt, (byte) 1);

        SCryptUtil.scrypt("secret", salt, N, r, p);
    }

    @Test
    public void scrypt_WithSameSalt() {
        String passwd = "secret";
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 1);

        String hashed1 = SCryptUtil.scrypt(passwd, salt, N, r, p);
        String hashed2 = SCryptUtil.scrypt(passwd, salt, N, r, p);

        assertTrue(hashed1.equals(hashed2));
    }

    @Test
    public void check() {
        String passwd = "secret";

        String hashed = SCryptUtil.scrypt(passwd, N, r, p);

        assertTrue(SCryptUtil.check(passwd, hashed));
        assertFalse(SCryptUtil.check("s3cr3t", hashed));
    }

    @Test
    public void check_WithSpecifiedSalt() {
        String passwd = "secret";
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 1);

        String hashed = SCryptUtil.scrypt(passwd, salt, N, r, p);

        assertTrue(SCryptUtil.check(passwd, hashed));
        assertFalse(SCryptUtil.check("s3cr3t", hashed));
    }

    @Test(expected = IllegalArgumentException.class)
    public void check_InvalidHashValueWrongNumberOfParts() {
        SCryptUtil.check("secret", "$s0$PARAMS$SALT$KEY$EXTRA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void check_InvalidHashValueWrongVersion() {
        SCryptUtil.check("secret", "$s1$PARAMS$SALT$KEY");
    }

    @Test
    public void generateSalt() {
        final byte[] bytes = SCryptUtil.generateSalt();
        assertTrue(bytes != null);
        // verify 128 bit salt generation
        assertTrue(bytes.length == 16);
    }

}
