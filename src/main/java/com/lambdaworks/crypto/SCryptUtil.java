// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.crypto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /**
     * Hash the supplied plaintext password and generate output in the format described
     * in {@link SCryptUtil}. This method maybe unsafe as it uses Strings, which are not guaranteed to be 
     * freed immediately. 
     *
     * @param passwd    Password.
     * @param N         CPU cost parameter.
     * @param r         Memory cost parameter.
     * @param p         Parallelization parameter.
     *
     * @return The hashed password.
     */
    public static String scrypt(String passwd, int N, int r, int p) {
        byte[] bytes = passwd.getBytes(StandardCharsets.UTF_8);
        try {
            return new String(scrypt(bytes, N, r, p), StandardCharsets.UTF_8);
        } finally {
            wipeArray(bytes);
        }
    }
    
    /**
     * Hash the supplied plaintext password and generate output in the format described
     * in {@link SCryptUtil}. This call will aggressively clean up password data in memory.
     *
     * @param passwd    Password in UTF-8 encoding.
     * @param N         CPU cost parameter.
     * @param r         Memory cost parameter.
     * @param p         Parallelization parameter.
     *
     * @return The hashed password.
     */
    public static byte[] scrypt(byte[] passwordBytes, int N, int r, int p) {
        try {
            byte[] salt = new byte[16];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);

            byte[] derived = SCrypt.scrypt(passwordBytes, salt, N, r, p, 32);

            byte[] params = Long.toString(log2(N) << 16L | r << 8 | p, 16).getBytes(StandardCharsets.UTF_8);

            byte[] prefix = "$s0$".getBytes(StandardCharsets.UTF_8);
            byte[] dollar = "$".getBytes(StandardCharsets.UTF_8);
            final char[] charEncodedSalt = encode(salt);
            byte[] byteEncodedSalt = toBytes(charEncodedSalt);
            wipeArray(charEncodedSalt);
            final char[] charEncodedDerived = encode(derived);
            byte[] byteEncodedDerived = toBytes(charEncodedDerived);
            wipeArray(charEncodedDerived);
            
            byte[] result = new byte[prefix.length 
                                     + params.length 
                                     + dollar.length 
                                     + byteEncodedSalt.length 
                                     + dollar.length 
                                     + byteEncodedDerived.length];
            System.arraycopy(prefix, 0, result, 0, prefix.length);
            System.arraycopy(params, 0, result, prefix.length, params.length);
            System.arraycopy(dollar, 0, result, prefix.length 
                                              + params.length, dollar.length);
            System.arraycopy(byteEncodedSalt, 0, result, prefix.length 
                                                   + params.length
                                                   + dollar.length, byteEncodedSalt.length);
            System.arraycopy(dollar, 0, result, prefix.length 
                                              + params.length
                                              + dollar.length
                                              + byteEncodedSalt.length, dollar.length);
            System.arraycopy(byteEncodedDerived, 0, result, prefix.length 
                                               + params.length
                                               + dollar.length
                                               + byteEncodedSalt.length
                                               + dollar.length, byteEncodedDerived.length);
            wipeArray(salt);
            wipeArray(derived);
            wipeArray(params);
            wipeArray(byteEncodedSalt);
            wipeArray(byteEncodedDerived);
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?");
        }
    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        try {
            return Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        } finally {
            wipeArray(byteBuffer.array());
        }
    }

    /**
     * Compare the supplied plaintext password to a hashed password.
     * This method maybe unsafe as it uses Strings, which are not guaranteed to be 
     * freed immediatelly.
     *
     * @param   passwd  Plaintext password.
     * @param   hashed  scrypt hashed password.
     *
     * @return true if passwd matches hashed value.
     */
    public static boolean check(String passwd, String hashed) {
        return check(passwd.getBytes(StandardCharsets.UTF_8), hashed.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Compare the supplied plaintext password to a hashed password.
     * This call will aggressively clean up password data in memory.
     * 
     * @param   passwd  Plaintext password encoded in UTF-8.
     * @param   hashed  scrypt hashed password encoded in UTF-8.
     *
     * @return true if passwd matches hashed value.
     */
    public static boolean check(byte[] passwordBytes, byte[] hashed) {
        try {
            byte[][] parts = split(hashed, "$".getBytes(StandardCharsets.UTF_8));

            if (parts.length != 5 || !Arrays.equals(parts[1], "s0".getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid hashed value");
            }

            long params = Long.parseLong(new String(parts[2], StandardCharsets.UTF_8), 16);
            final char[] charEncodedSalt = toCharArray(parts[3]);
            byte[] salt = decode(charEncodedSalt);
            wipeArray(charEncodedSalt);
            final char[] charEncodedDerived = toCharArray(parts[4]);
            byte[] derived0 = decode(charEncodedDerived);
            wipeArray(charEncodedDerived);

            int N = (int) Math.pow(2, params >> 16 & 0xffff);
            int r = (int) params >> 8 & 0xff;
            int p = (int) params      & 0xff;

            byte[] derived1 = SCrypt.scrypt(passwordBytes, salt, N, r, p, 32);

            if (derived0.length != derived1.length) return false;

            int result = 0;
            for (int i = 0; i < derived0.length; i++) {
                result |= derived0[i] ^ derived1[i];
            }
            wipeArray(derived0);
            wipeArray(derived1);
            return result == 0;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?");
        }
    }

    private static char[] toCharArray(byte[] bs) {
        char[] result = new char[bs.length];
        for(int i = 0; i < bs.length; i++) {
            result[i] = (char) bs[i];
        }
        return result;
    }

    /**
     * Splites a byte array into chunks delimited by <code>separatorBytes</code>, similar
     * to {@link String#split}.
     */
    private static byte[][] split(byte[] array, byte[] separatorBytes) {
        List<byte[]> result = new ArrayList<byte[]>();
        int lastSplitIndex = 0;
        for(int i = 0; i < array.length - separatorBytes.length; i++)
        {
            boolean found = false;
            int j = 0;
            while (j < separatorBytes.length && array[i + j] == separatorBytes[j]) 
                j++;
            found = (j == separatorBytes.length);
            if(found) {
                byte[] substring = new byte[i - lastSplitIndex];
                System.arraycopy(array, lastSplitIndex, substring, 0, i - lastSplitIndex);
                result.add(substring);
                lastSplitIndex = i + j;
            } else if(array.length == i + separatorBytes.length + 1) {
                byte[] substring = new byte[i + separatorBytes.length + 1 - lastSplitIndex];
                System.arraycopy(array, lastSplitIndex, substring, 0, i + separatorBytes.length + 1 - lastSplitIndex);
                result.add(substring);
            }
        }
        if(result.isEmpty()) {
            result.add(array);
        }
        return result.toArray(new byte[0][]);
    }

    private static int log2(int n) {
        int log = 0;
        if ((n & 0xffff0000 ) != 0) { n >>>= 16; log = 16; }
        if (n >= 256) { n >>>= 8; log += 8; }
        if (n >= 16 ) { n >>>= 4; log += 4; }
        if (n >= 4  ) { n >>>= 2; log += 2; }
        return log + (n >>> 1);
    }

    private static void wipeArray(byte[] array) {
        Arrays.fill(array, (byte) 0);
    }
    
    private static void wipeArray(char[] array) {
        Arrays.fill(array, (char) 0);
    }

}
