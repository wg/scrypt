// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.crypto;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import static com.lambdaworks.codec.Base64.*;

/**
 * Simple {@link SCrypt} interface for hashing passwords using the
 * <a href="http://www.tarsnap.com/scrypt.html">scrypt</a> key derivation function
 * and comparing a plain text password to a hashed one. The hashed output is an
 * extended implementation of the Modular Crypt Format that also includes the scrypt
 * algorithm parameters.
 *
 * Format: <code>$s0$PARAMS$SALT$KEY</code>.
 *
 * <dl>
 * <dd>PARAMS</dd><dt>32-bit hex integer containing log2(N) (16 bits), r (8 bits), and p (8 bits)</dt>
 * <dd>SALT</dd><dt>base64-encoded salt</dt>
 * <dd>KEY</dd><dt>base64-encoded derived key</dt>
 * </dl>
 *
 * <code>s0</code> identifies version 0 of the scrypt format, using a 128-bit salt and 256-bit derived key.
 *
 * @author  Will Glozer
 */
public class SCryptUtil {
    // for timedIterations()
    private static final byte[] BENCH_PASSWD = "secret".getBytes();
    private static final byte[] BENCH_SALT = "1234".getBytes();
    private static final int BENCH_DK_LEN = 32;
    private static final int BENCH_INITIAL_N = 64;

    /**
     * Hash the supplied plaintext password and generate output in the format described
     * in {@link SCryptUtil}.
     *
     * @param passwd    Password.
     * @param N         CPU cost parameter.
     * @param r         Memory cost parameter.
     * @param p         Parallelization parameter.
     *
     * @return The hashed password.
     */
    public static String scrypt(String passwd, int N, int r, int p) {
        try {
            byte[] salt = new byte[16];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);

            byte[] derived = SCrypt.scrypt(passwd.getBytes("UTF-8"), salt, N, r, p, 32);

            String params = Long.toString(log2(N) << 16L | r << 8 | p, 16);

            StringBuilder sb = new StringBuilder((salt.length + derived.length) * 2);
            sb.append("$s0$").append(params).append('$');
            sb.append(encode(salt)).append('$');
            sb.append(encode(derived));

            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("JVM doesn't support UTF-8?");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?");
        }
    }

    /**
     * Compare the supplied plaintext password to a hashed password.
     *
     * @param   passwd  Plaintext password.
     * @param   hashed  scrypt hashed password.
     *
     * @return true if passwd matches hashed value.
     */
    public static boolean check(String passwd, String hashed) {
        try {
            String[] parts = hashed.split("\\$");

            if (parts.length != 5 || !parts[1].equals("s0")) {
                throw new IllegalArgumentException("Invalid hashed value");
            }

            long params = Long.parseLong(parts[2], 16);
            byte[] salt = decode(parts[3].toCharArray());
            byte[] derived0 = decode(parts[4].toCharArray());

            int N = (int) Math.pow(2, params >> 16 & 0xffff);
            int r = (int) params >> 8 & 0xff;
            int p = (int) params      & 0xff;

            byte[] derived1 = SCrypt.scrypt(passwd.getBytes("UTF-8"), salt, N, r, p, 32);

            if (derived0.length != derived1.length) return false;

            int result = 0;
            for (int i = 0; i < derived0.length; i++) {
                result |= derived0[i] ^ derived1[i];
            }
            return result == 0;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("JVM doesn't support UTF-8?");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?");
        }
    }

    private static int log2(int n) {
        int log = 0;
        if ((n & 0xffff0000 ) != 0) { n >>>= 16; log = 16; }
        if (n >= 256) { n >>>= 8; log += 8; }
        if (n >= 16 ) { n >>>= 4; log += 4; }
        if (n >= 4  ) { n >>>= 2; log += 2; }
        return log + (n >>> 1);
    }

    /**
     * Determines a CPU cost value (i.e. a value for the N parameter) that will cause password
     * verification to take (roughly) a given time on the current CPU for the specified
     * <code>r</code> and <code>p</code> values.<br/>
     * N is rounded to the nearest power of two because only powers of two are valid
     * choices for N. The actual time spent will be between about .7*<code>milliseconds</code>
     * and 1.4*<code>milliseconds</code>.
     *
     * @param milliseconds the time scrypt should spend verifying a password
     * @param r            memory cost parameter
     * @param p            parallelization parameter
     *
     * @return a value for N such that <code>scrypt(N, r, p)</code> runs for roughly <code>milliseconds</code>
     *
     * @throws GeneralSecurityException when HMAC_SHA256 is not available.
     */
    public static int timedIterations(int milliseconds, int r, int p) throws GeneralSecurityException {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        boolean cpuTimeSupported = threadBean.isCurrentThreadCpuTimeSupported();
        boolean origEnabledFlag = false;
        if (cpuTimeSupported) {
            origEnabledFlag = threadBean.isThreadCpuTimeEnabled();
            if (!origEnabledFlag)
                threadBean.setThreadCpuTimeEnabled(true);
        }

        int N = BENCH_INITIAL_N;
        long lastDelta = 0;
        while (true) {
          // prefer CPU time over real world time so the result is load independent
          long startTime = cpuTimeSupported ? threadBean.getCurrentThreadUserTime() : System.nanoTime();
          SCrypt.scrypt(BENCH_PASSWD, BENCH_SALT, N, r, p, BENCH_DK_LEN);
          long endTime = cpuTimeSupported ? threadBean.getCurrentThreadUserTime() : System.nanoTime();
          long delta = (endTime-startTime) / 1000000;

          // start over if a speed increase is detected due to the code being JITted
          if (delta < lastDelta) {
            N = BENCH_INITIAL_N;
            lastDelta = 0;
            continue;
          }

          if (delta > milliseconds) {
            if (cpuTimeSupported)
                threadBean.setThreadCpuTimeEnabled(origEnabledFlag);
            // round to the nearest power of two
            if (delta-delta/4 > milliseconds)
                N /= 2;
            return N;
          }
          N *= 2;
          lastDelta = delta;
        }
    }
}
