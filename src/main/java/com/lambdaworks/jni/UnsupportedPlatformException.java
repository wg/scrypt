// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.jni;

/**
 * Exception thrown when the current platform cannot be detected.
 *
 * @author Will Glozer
 */
public class UnsupportedPlatformException extends RuntimeException {
    public UnsupportedPlatformException(String s) {
        super(s);
    }
}
