// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni.test;

import com.lambdaworks.jni.JarLibraryLoader;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;

import static java.lang.System.getProperty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JarLibraryLoaderTest extends AbstractPlatformDetectionTest {
    @Test
    public void loadSigned() throws Exception {
        JarLibraryLoader loader = jarLibraryLoader("native-libs-signed");
        assertTrue(loader.load("scrypt", true));
    }

    @Test
    public void loadUnsigned() throws Exception {
        JarLibraryLoader loader = jarLibraryLoader("native-libs-unsigned");
        assertTrue(loader.load("scrypt", false));
    }
    @Test
    public void loadVerifyBadSig() throws Exception {
        JarLibraryLoader loader = jarLibraryLoader("native-libs-badsig");
        assertFalse(loader.load("scrypt", true));
    }

    @Test
    public void loadUnsupportedPlatform() throws Exception {
        setPlatform("PA-RISC", "MPE/iX");
        JarLibraryLoader loader = jarLibraryLoader("native-libs-signed");
        assertFalse(loader.load("scrypt", true));
    }

    @Test
    public void loadWrongPlatform() throws Exception {
        String os = getProperty("os.name").equals("Linux") ? "Darwin" : "Linux";
        setPlatform("x86_64", os);
        JarLibraryLoader loader = jarLibraryLoader("native-libs-signed");
        assertFalse(loader.load("scrypt", true));
    }

    @Test
    public void loadInvalidEntry() throws Exception {
        JarLibraryLoader loader = jarLibraryLoader("native-libs-invalid");
        assertFalse(loader.load("scrypt", true));
    }

    protected JarLibraryLoader jarLibraryLoader(String name) throws Exception {
        URL url = getClass().getResource("/" + name + ".jar");
        return new JarLibraryLoader(new CodeSource(url, codeSigners()), "lib");
    }

    protected CodeSigner[] codeSigners() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = getClass().getResourceAsStream("/lambdaWorks.p7");
        CertPath certPath = cf.generateCertPath(is, "PKCS7");
        is.close();
        return new CodeSigner[] { new CodeSigner(certPath, null) };
    }
}
