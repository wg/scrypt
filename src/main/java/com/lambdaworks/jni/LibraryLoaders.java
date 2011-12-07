// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni;

/**
 * {@code LibraryLoaders} will create the appropriate {@link LibraryLoader} for
 * the VM it is running on.
 *
 * @author Will Glozer
 */
public class LibraryLoaders {
    /**
     * Create a new {@link LibraryLoader} for the current VM.
     *
     * @return the loader.
     */
    public static LibraryLoader loader() {
        String vmSpec = System.getProperty("java.vm.specification.name");
        return vmSpec.startsWith("Java") ? new JarLibraryLoader() : new SystemLibraryLoader();
    }
}
