// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni;

import java.io.*;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A native library loader that will extract and load a shared library contained in a jar.
 * This loader will attempt to detect the {@link Platform platform} (CPU architecture and OS)
 * it is running on and load the appropriate shared library.
 *
 * Given a library path and name this loader looks for a native library with path
 * [libraryPath]/[arch]/[os]/lib[name].[ext]
 *
 * @author Will Glozer
 */
public class JarLibraryLoader implements LibraryLoader {
    private final CodeSource codeSource;
    private final String libraryPath;

    /**
     * Initialize a new instance that looks for shared libraries located in the same jar
     * as this class and with a path starting with {@code lib}.
     */
    public JarLibraryLoader() {
        this(JarLibraryLoader.class.getProtectionDomain().getCodeSource(), "lib");
    }

    /**
     * Initialize a new instance that looks for shared libraries located in the specified
     * directory of the supplied code source.
     *
     * @param codeSource    Code source containing shared libraries.
     * @param libraryPath   Path prefix of shared libraries.
     */
    public JarLibraryLoader(CodeSource codeSource, String libraryPath) {
        this.codeSource  = codeSource;
        this.libraryPath = libraryPath;
    }

    /**
     * Load a shared library, and optionally verify the jar signatures.
     *
     * @param name      Name of the library to load.
     * @param verify    Verify the jar file if signed.
     *
     * @return true if the library was successfully loaded.
     */
    public boolean load(String name, boolean verify) {
        boolean loaded = false;

        try {
            Platform platform = Platform.detect();
            JarFile jar = new JarFile(codeSource.getLocation().getPath(), verify);
            try {
                for (String path : libCandidates(platform, name)) {
                    JarEntry entry = jar.getJarEntry(path);
                    if (entry == null) continue;

                    File lib = extract(name, jar.getInputStream(entry));
                    System.load(lib.getAbsolutePath());
                    lib.delete();

                    loaded = true;
                    break;
                }
            } finally {
                jar.close();
            }
        } catch (Throwable e) {
            loaded = false;
        }

        return loaded;
    }

    /**
     * Extract a jar entry to a temp file.
     *
     * @param name  Name prefix for temp file.
     * @param is    Jar entry input stream.
     *
     * @return A temporary file.
     *
     * @throws IOException when an IO error occurs.
     */
    private static File extract(String name, InputStream is) throws IOException {
        byte[] buf = new byte[4096];
        int len;

        File lib = File.createTempFile(name, "lib");
        FileOutputStream os = new FileOutputStream(lib);

        try {
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
        } catch (IOException e) {
            lib.delete();
            throw e;
        } finally {
            os.close();
            is.close();
        }

        return lib;
    }

    /**
     * Generate a list of candidate libraries for the supplied library name and suitable
     * for the current platform.
     *
     * @param platform  Current platform.
     * @param name      Library name.
     *
     * @return List of potential library names.
     */
    private List<String> libCandidates(Platform platform, String name) {
        List<String> candidates = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        sb.append(libraryPath).append("/");
        sb.append(platform.arch).append("/");
        sb.append(platform.os).append("/");
        sb.append("lib").append(name);

        switch (platform.os) {
            case darwin:
                candidates.add(sb + ".dylib");
                candidates.add(sb + ".jnilib");
                break;
            case linux:
            case freebsd:
                candidates.add(sb + ".so");
                break;
        }

        return candidates;
    }
}
