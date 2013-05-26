// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni.test;

import com.lambdaworks.jni.*;
import org.junit.*;

import static java.lang.System.*;
import static org.junit.Assert.assertTrue;

public class LibraryLoadersTest {
    private String vmSpec;

    @Before
    public final void saveProperties() {
        vmSpec = getProperty("java.vm.specification.name");
    }

    @After
    public final void restoreProperties() {
        setProperty("java.vm.specification.name", vmSpec);
        clearProperty("com.lambdaworks.jni.loader");
    }

    @Test
    public void autoDetectLoader() throws Exception {
        assertTrue(loader("Java")   instanceof JarLibraryLoader);
        assertTrue(loader("Dalvik") instanceof SysLibraryLoader);
    }

    @Test
    public void overrideLoaderDetection() throws Exception {
        assertTrue(loaderForName("jar") instanceof JarLibraryLoader);
        assertTrue(loaderForName("nil") instanceof NilLibraryLoader);
        assertTrue(loaderForName("sys") instanceof SysLibraryLoader);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidLoaderProperty() throws Exception {
        loaderForName("invalid");
    }

    private LibraryLoader loader(String spec) {
        setProperty("java.vm.specification.name", spec + " Virtual Machine Specification");
        return LibraryLoaders.loader();
    }

    private LibraryLoader loaderForName(String name) {
        setProperty("com.lambdaworks.jni.loader", name);
        return LibraryLoaders.loader();
    }
}
