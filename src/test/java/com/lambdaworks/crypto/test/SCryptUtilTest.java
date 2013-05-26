// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.crypto.test;

import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCrypt;
import com.lambdaworks.crypto.SCryptUtil;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.GeneralSecurityException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class SCryptUtilTest {
    String passwd = "secret";

    @Test
    public void scrypt() {
        int N = 16384;
        int r = 8;
        int p = 1;

        String hashed = SCryptUtil.scrypt(passwd, N, r, p);
        String[] parts = hashed.split("\\$");

        assertEquals(5, parts.length);
        assertEquals("s0", parts[1]);
        Assert.assertEquals(16, Base64.decode(parts[3].toCharArray()).length);
        assertEquals(32, Base64.decode(parts[4].toCharArray()).length);

        int params = Integer.valueOf(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >> 16 & 0xffff));
        assertEquals(r, params >> 8 & 0xff);
        assertEquals(p, params >> 0 & 0xff);
    }

    @Test
    public void check() {
        String hashed = SCryptUtil.scrypt(passwd, 16384, 8, 1);

        assertTrue(SCryptUtil.check(passwd, hashed));
        assertFalse(SCryptUtil.check("s3cr3t", hashed));
    }

    @Test
    public void format_0_rp_max() throws Exception {
        int N = 2;
        int r = 255;
        int p = 255;

        String hashed = SCryptUtil.scrypt(passwd, N, r, p);
        assertTrue(SCryptUtil.check(passwd, hashed));

        String[] parts = hashed.split("\\$");
        int params = Integer.valueOf(parts[2], 16);

        assertEquals(N, (int) Math.pow(2, params >>> 16 & 0xffff));
        assertEquals(r, params >> 8 & 0xff);
        assertEquals(p, params >> 0 & 0xff);
    }

    @Test
    public void testTimedIterations() throws GeneralSecurityException {
        byte[] salt = "1234".getBytes();
        int dkLen = 32;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        boolean cpuTimeSupported = threadBean.isCurrentThreadCpuTimeSupported();
        Random random = new Random();
        for (int i=0; i<5; i++) {
            int targetDuration = 100 + random.nextInt(900);
            int numIterations = SCryptUtil.timedIterations(targetDuration, 8, 1);
            long startTime = cpuTimeSupported ? threadBean.getCurrentThreadUserTime() : System.nanoTime();
            SCrypt.scrypt(passwd.getBytes(), salt, numIterations, 8, 1, dkLen);
            long endTime = cpuTimeSupported ? threadBean.getCurrentThreadUserTime() : System.nanoTime();
            long actualDuration = (endTime-startTime) / 1000000;
            assertTrue(actualDuration>targetDuration*5/10 && actualDuration<targetDuration*16/10);   // be generous
      }
    }
}
