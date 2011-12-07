// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni.test;

import com.lambdaworks.jni.*;
import org.junit.*;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
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
    }

    @Test
    public void loader() throws Exception {
        assertTrue(loader("Java")   instanceof JarLibraryLoader);
        assertTrue(loader("Dalvik") instanceof SystemLibraryLoader);
    }

    private LibraryLoader loader(String spec) {
        setProperty("java.vm.specification.name", spec + " Virtual Machine Specification");
        return LibraryLoaders.loader();
    }
}
