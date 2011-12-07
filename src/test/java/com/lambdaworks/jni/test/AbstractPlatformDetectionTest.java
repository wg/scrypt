// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni.test;

import com.lambdaworks.jni.Platform;
import org.junit.After;
import org.junit.Before;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

public class AbstractPlatformDetectionTest {
    private String osArch;
    private String osName;

    @Before
    public final void saveProperties() {
        osArch = getProperty("os.arch");
        osName = getProperty("os.name");
    }

    @After
    public final void restoreProperties() {
        setProperty("os.arch", osArch);
        setProperty("os.name", osName);
    }

    protected Platform detectArch(String arch) {
        setPlatform(arch, "Linux");
        return Platform.detect();
    }

    protected Platform detectOs(String os) {
        setPlatform("x86_64", os);
        return Platform.detect();
    }

    protected void setPlatform(String arch, String os) {
        setProperty("os.arch", arch);
        setProperty("os.name", os);
    }
}
