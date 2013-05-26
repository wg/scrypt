// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni;

/**
 * {@code LibraryLoaders} will create the appropriate {@link LibraryLoader} for
 * the VM it is running on.
 *
 * The system property {@code com.lambdaworks.jni.loader} may be used to override
 * loader auto-detection, or to disable loading native libraries entirely via use
 * of the nil loader.
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
        String type = System.getProperty("com.lambdaworks.jni.loader");

        if (type != null) {
            if (type.equals("sys")) return new SysLibraryLoader();
            if (type.equals("nil")) return new NilLibraryLoader();
            if (type.equals("jar")) return new JarLibraryLoader();
            throw new IllegalStateException("Illegal value for com.lambdaworks.jni.loader: " + type);
        }

        String vmSpec = System.getProperty("java.vm.specification.name");
        return vmSpec.startsWith("Java") ? new JarLibraryLoader() : new SysLibraryLoader();
    }
}
