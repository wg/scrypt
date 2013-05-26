Java implementation of scrypt

  A pure Java implementation of the scrypt key derivation function and a JNI
  interface to the C implementations, including the SSE2 optimized version.

  The Java implementation is based in large part on Colin Percival's
  reference implementation contained in crypto_scrypt-ref.c, but any errors
  in this port are solely the fault of its author, Will Glozer.

    https://www.tarsnap.com/scrypt/scrypt.pdf
    https://scrypt.googlecode.com

  Join the lambdaWorks-OSS Google Group to discuss this project:

    http://groups.google.com/group/lambdaworks-oss
    lambdaworks-oss@googlegroups.com

Usage

  com.lambdaworks.crypto.SCryptUtil implements a modified version of MCF,
  the modular crypt format, similar to the one used for storage of Unix
  passwords in the MD5, SHA-256, and bcrypt formats.

    SCryptUtil.scrypt(passwd, N, r, p)
    SCryptUtil.check(passwd, hashed)

  The output of SCryptUtil.scrypt is a string in the modified MCF format:

  $s0$params$salt$key

  s0     - version 0 of the format with 128-bit salt and 256-bit derived key
  params - 32-bit hex integer containing log2(N) (16 bits), r (8 bits), and p (8 bits)
  salt   - base64-encoded salt
  key    - base64-encoded derived key

  Example:

    $s0$e0801$epIxT/h6HbbwHaehFnh/bw==$7H0vsXlY8UxxyW/BWx/9GuY7jEvGjT71GFd6O4SZND0=

    passwd = "secret"
         N = 16384
         r = 8
         p = 1

Native Code Implementation

  When the native library can be loaded it will be used instead of the pure
  Java implementation. On a J2SE compliant JVM the native library will be
  extracted from the jar and loaded, and on other VMs System.loadLibrary will
  be called.

  The system property "com.lambdaworks.jni.loader" may be set to override
  the default native library loader with one of the following values:

    nil: refuse to load native libraries and revert to pure Java implementation
    jar: extract native library from jar and load with System.load
    sys: use System.loadLibrary, which may require java.library.path to be set

Maven Artifacts

  Releases containing the pure Java implementation, as well as native libraries
  for a limited number of platforms, are available in the maven central repository.

  <dependency>
    <groupId>com.lambdaworks</groupId>
    <artifactId>scrypt</artifactId>
    <version>1.4.0</version>
  </dependency>

Building Native Implementation

  A native shared library for the current platform may be built by running GNU
  make. The Makefile attempts to detect the runtime platform and JDK location,
  but in some cases one or more of the following variables should be passed
  to make:

  TARGET    - target operating system, use "android" to build for Android
  SSE2      - use the SSE2 optimized scrypt implementation when set
  JAVA_HOME - base directory of a Java 6+ JDK
  NDK_ROOT  - base directory of Android NDK

  A precompiled native library for Android 2.3 running on ARM is located in
  src/android/resources/lib/arm5/libscrypt.so. If placed in an .apk file's
  lib/armeabi directory it will be automatically loaded.
