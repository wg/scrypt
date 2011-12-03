// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni.test;

import com.lambdaworks.jni.Platform;
import com.lambdaworks.jni.UnsupportedPlatformException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlatformTest extends AbstractPlatformDetectionTest {
    @Test
    public void arch() throws Exception {
        assertEquals(Platform.Arch.x86_64, detectArch("x86_64").arch);
        assertEquals(Platform.Arch.x86_64, detectArch("amd64").arch);
        assertEquals(Platform.Arch.x86,    detectArch("i386").arch);
    }

    @Test
    public void os() throws Exception {
        assertEquals(Platform.OS.darwin,  detectOs("Mac OS X").os);
        assertEquals(Platform.OS.darwin,  detectOs("Darwin").os);
        assertEquals(Platform.OS.freebsd, detectOs("FreeBSD").os);
        assertEquals(Platform.OS.linux,   detectOs("Linux").os);
    }

    @Test(expected = UnsupportedPlatformException.class)
    public void unsupported() throws Exception {
        setPlatform("PA-RISC", "MPE/iX");
        Platform.detect();
    }
}
