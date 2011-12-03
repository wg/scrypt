// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.crypto;

import com.lambdaworks.jni.JarLibraryLoader;
import com.lambdaworks.jni.UnsupportedPlatformException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.arraycopy;

/**
 * An implementation of the <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt</a>
 * key derivation function. This class will attempt to load a native library
 * containing the optimized C implementation from
 * <a href="http://www.tarsnap.com/scrypt.html">http://www.tarsnap.com/scrypt.html<a> and
 * fall back to the pure Java version if that fails.
 *
 * @author  Will Glozer
 */
public class SCrypt {
    private static boolean native_library_loaded = false;

    static {
        try {
            JarLibraryLoader loader = new JarLibraryLoader();
            native_library_loaded = loader.load("libscrypt", true);
        } catch (UnsupportedPlatformException e) {
            // windows, etc
        }
    }

    private final int N;
    private final int r;
    private final int p;
    private final Mac mac;
    
    private final byte[] B;
    private final byte[] XY;
    private final byte[] V;
    
    /**
     * Allocates working memory for computing hash. Operating in a nearly constant memory profile can significantly
     * improve performance in a JVM.
     *
     * @param N         CPU cost parameter.
     * @param r         Memory cost parameter.
     * @param p         Parallelization parameter.
     *
     * @throws GeneralSecurityException when HMAC_SHA256 is not available.
     */
    public SCrypt(int N, int r, int p) throws NoSuchAlgorithmException {
        if (N == 0 || (N & (N - 1)) != 0) throw new IllegalArgumentException("N must be > 0 and a power of 2");

        if (N > MAX_VALUE / 128 / r) throw new IllegalArgumentException("Parameter N is too large");
        if (r > MAX_VALUE / 128 / p) throw new IllegalArgumentException("Parameter r is too large");
        
        mac = Mac.getInstance("HmacSHA256");
        
        this.N = N;
        this.r = r;
        this.p = p;

        B = new byte[128 * r * p];
        XY = new byte[256 * r];
        V  = new byte[128 * r * N];
    }

	/**
     * Implementation of the <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt KDF</a>.
     * Calls the native implementation {@link #scryptN} when the native library was successfully
     * loaded, otherwise calls {@link #scryptJ}.
     *
     * @param passwd    Password.
     * @param salt      Salt.
     * @param N         CPU cost parameter.
     * @param r         Memory cost parameter.
     * @param p         Parallelization parameter.
     * @param dkLen     Intended length of the derived key.
     *
     * @return The derived key.
     *
     * @throws GeneralSecurityException when HMAC_SHA256 is not available.
     */
    public static byte[] scrypt(byte[] passwd, byte[] salt, int N, int r, int p, int dkLen) throws GeneralSecurityException {
        return native_library_loaded ? scryptN(passwd, salt, N, r, p, dkLen) : scryptJ(passwd, salt, N, r, p, dkLen);
    }

    /**
     * Native C implementation of the <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt KDF</a> using
     * the code from <a href="http://www.tarsnap.com/scrypt.html">http://www.tarsnap.com/scrypt.html<a>.
     *
     * @param passwd    Password.
     * @param salt      Salt.
     * @param N         CPU cost parameter.
     * @param r         Memory cost parameter.
     * @param p         Parallelization parameter.
     * @param dkLen     Intended length of the derived key.
     *
     * @return The derived key.
     */
    public static native byte[] scryptN(byte[] passwd, byte[] salt, long N, int r, int p, int dkLen);

    /**
     * Pure Java implementation of the <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt KDF</a>.
     *
     * @param passwd    Password.
     * @param salt      Salt.
     * @param N         CPU cost parameter.
     * @param r         Memory cost parameter.
     * @param p         Parallelization parameter.
     * @param dkLen     Intended length of the derived key.
     *
     * @return The derived key.
     *
     * @throws GeneralSecurityException when HMAC_SHA256 is not available.
     */
    public static byte[] scryptJ(byte[] passwd, byte[] salt, int N, int r, int p, int dkLen) throws GeneralSecurityException {
        SCrypt sc = new SCrypt(N,r,p);
        return sc.scrypt(passwd, salt, dkLen);
    }
    
    public byte[] scrypt(byte[] passwd, byte[] salt, int dkLen) throws GeneralSecurityException {
    
        mac.init(new SecretKeySpec(passwd, "HmacSHA256"));

        byte[] DK = new byte[dkLen];

        int i;

        // B overwritten here
        PBKDF.pbkdf2(mac, salt, 1, B, p * 128 * r);

        for (i = 0; i < p; i++) {
            // XY, V overwritten in parts
            smix(B, i * 128 * r, r, N, V, XY);
        }

        PBKDF.pbkdf2(mac, B, 1, DK, dkLen);

        return DK;
    }

    public static void smix(byte[] B, int Bi, int r, int N, byte[] V, byte[] XY) {
        byte[] X = new byte[64];
        int[] B32 = new int[16];
        int[] x   = new int[16];
        
        int Xi = 0;
        int Yi = 128 * r;
        int i;

        // XY overwritten in parts
        arraycopy(B, Bi, XY, Xi, 128 * r);

        for (i = 0; i < N; i++) {
            // V overwritten in parts
            arraycopy(XY, Xi, V, i * (128 * r), 128 * r);
            blockmix_salsa8(XY, Xi, Yi, r, X, B32, x);
        }

        for (i = 0; i < N; i++) {
            int j = integerify(XY, Xi, r) & (N - 1);
            blockxor(V, j * (128 * r), XY, Xi, 128 * r);
            blockmix_salsa8(XY, Xi, Yi, r, X, B32, x);
        }

        arraycopy(XY, Xi, B, Bi, 128 * r);
    }

    public static void blockmix_salsa8(byte[] BY, int Bi, int Yi, int r, byte[] X, int[] B32, int[] x) {
        int i;

        // X initialized
        arraycopy(BY, Bi + (2 * r - 1) * 64, X, 0, 64);

        for (i = 0; i < 2 * r; i++) {
            blockxor(BY, i * 64, X, 0, 64);
            salsa20_8(X,B32,x);
            arraycopy(X, 0, BY, Yi + (i * 64), 64);
        }

        for (i = 0; i < r; i++) {
            arraycopy(BY, Yi + (i * 2) * 64, BY, Bi + (i * 64), 64);
        }

        for (i = 0; i < r; i++) {
            arraycopy(BY, Yi + (i * 2 + 1) * 64, BY, Bi + (i + r) * 64, 64);
        }
    }

    public static int R(int a, int b) {
        return (a << b) | (a >>> (32 - b));
    }

    public static void salsa20_8(byte[] B, int[] B32, int[] x) {
        int i;

        // B32 initialized
        for (i = 0; i < 16; i++) {
            B32[i]  = (B[i * 4 + 0] & 0xff) << 0;
            B32[i] |= (B[i * 4 + 1] & 0xff) << 8;
            B32[i] |= (B[i * 4 + 2] & 0xff) << 16;
            B32[i] |= (B[i * 4 + 3] & 0xff) << 24;
        }

        // x initialized
        arraycopy(B32, 0, x, 0, 16);

        for (i = 8; i > 0; i -= 2) {
            x[ 4] ^= R(x[ 0]+x[12], 7);  x[ 8] ^= R(x[ 4]+x[ 0], 9);
            x[12] ^= R(x[ 8]+x[ 4],13);  x[ 0] ^= R(x[12]+x[ 8],18);
            x[ 9] ^= R(x[ 5]+x[ 1], 7);  x[13] ^= R(x[ 9]+x[ 5], 9);
            x[ 1] ^= R(x[13]+x[ 9],13);  x[ 5] ^= R(x[ 1]+x[13],18);
            x[14] ^= R(x[10]+x[ 6], 7);  x[ 2] ^= R(x[14]+x[10], 9);
            x[ 6] ^= R(x[ 2]+x[14],13);  x[10] ^= R(x[ 6]+x[ 2],18);
            x[ 3] ^= R(x[15]+x[11], 7);  x[ 7] ^= R(x[ 3]+x[15], 9);
            x[11] ^= R(x[ 7]+x[ 3],13);  x[15] ^= R(x[11]+x[ 7],18);
            x[ 1] ^= R(x[ 0]+x[ 3], 7);  x[ 2] ^= R(x[ 1]+x[ 0], 9);
            x[ 3] ^= R(x[ 2]+x[ 1],13);  x[ 0] ^= R(x[ 3]+x[ 2],18);
            x[ 6] ^= R(x[ 5]+x[ 4], 7);  x[ 7] ^= R(x[ 6]+x[ 5], 9);
            x[ 4] ^= R(x[ 7]+x[ 6],13);  x[ 5] ^= R(x[ 4]+x[ 7],18);
            x[11] ^= R(x[10]+x[ 9], 7);  x[ 8] ^= R(x[11]+x[10], 9);
            x[ 9] ^= R(x[ 8]+x[11],13);  x[10] ^= R(x[ 9]+x[ 8],18);
            x[12] ^= R(x[15]+x[14], 7);  x[13] ^= R(x[12]+x[15], 9);
            x[14] ^= R(x[13]+x[12],13);  x[15] ^= R(x[14]+x[13],18);
        }

        for (i = 0; i < 16; ++i) B32[i] = x[i] + B32[i];

        for (i = 0; i < 16; i++) {
            B[i * 4 + 0] = (byte) (B32[i] >> 0  & 0xff);
            B[i * 4 + 1] = (byte) (B32[i] >> 8  & 0xff);
            B[i * 4 + 2] = (byte) (B32[i] >> 16 & 0xff);
            B[i * 4 + 3] = (byte) (B32[i] >> 24 & 0xff);
        }
    }

    public static void blockxor(byte[] S, int Si, byte[] D, int Di, int len) {
        for (int i = 0; i < len; i++) {
            D[Di + i] ^= S[Si + i];
        }
    }

    public static int integerify(byte[] B, int Bi, int r) {
        int n;

        Bi += (2 * r - 1) * 64;

        n  = (B[Bi + 0] & 0xff) << 0;
        n |= (B[Bi + 1] & 0xff) << 8;
        n |= (B[Bi + 2] & 0xff) << 16;
        n |= (B[Bi + 3] & 0xff) << 24;

        return n;
    }
}
