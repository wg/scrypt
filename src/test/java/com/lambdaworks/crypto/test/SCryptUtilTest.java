// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.crypto.test;

import com.lambdaworks.crypto.Base64;
import com.lambdaworks.crypto.SCryptUtil;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class SCryptUtilTest {
    @Test
    public void scrypt() {
        int N = 16384;
        int r = 8;
        int p = 1;

        String hashed = SCryptUtil.scrypt("secret", N, r, p);
        String[] parts = hashed.split("\\$");

        assertEquals(5, parts.length);
        assertEquals("s0", parts[1]);
        Assert.assertEquals(16, Base64.decodeFast(parts[3]).length);
        assertEquals(32, Base64.decodeFast(parts[4]).length);

        int params = Integer.valueOf(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >> 16 & 0xff));
        assertEquals(r, params >>  8 & 0x0f);
        assertEquals(p, params >>  0 & 0x0f);
    }

    @Test
    public void check() {
        String passwd = "secret";

        String hashed = SCryptUtil.scrypt(passwd, 16384, 8, 1);

        assertTrue(SCryptUtil.check(passwd, hashed));
        assertFalse(SCryptUtil.check("s3cr3t", hashed));
    }
}
