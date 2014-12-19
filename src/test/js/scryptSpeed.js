/**
 * Example command for running this testing utility:
 * 
 * <pre>
 *   jjs -cp ~/.m2/repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:target/scrypt-1.4.0.jar scryptTimer.js
 * </pre>
 * 
 * If you don't have commons-codec, use the following command to download with Maven:
 * 
 * <pre>
 *   mvn dependency:get -Dartifact=commons-codec:commons-codec:1.10
 * </pre>
 * 
 */

var SCrypt = com.lambdaworks.crypto.SCrypt;
var System = java.lang.System;
var Hex = org.apache.commons.codec.binary.Hex;

var f = Hex.class.getClassLoader().loadClass("com.lambdaworks.crypto.SCrypt").getDeclaredField("native_library_loaded");
f.setAccessible(true);
var native = f.get(null);
System.out.println("Using " + (native ? "Native" : "Java") + " scrypt implementation." );

var startTs = System.currentTimeMillis();

var P, S, N, r, p, dkLen;
var expected, actual;

P = "pleaseletmein".getBytes("UTF-8");
S = "SodiumChloride".getBytes("UTF-8");
N = 1048576;
r = 8;
p = 1;
dkLen = 64;
expected = "2101cb9b6a511aaeaddbbe09cf70f881ec568d574a2ffd4dabe5ee9820adaa478e56fd8f4ba5d09ffa1c6d927c40f4c337304049e8a952fbcbf45c6fa77a41a4";

actual = SCrypt.scrypt(P, S, N, r, p, dkLen);
actual = Hex.encodeHexString(actual);

var endTs = System.currentTimeMillis();

System.out.println("expect: " + expected);
System.out.println("actual: " + actual);
System.out.println("Time: " + (endTs - startTs) / 1000 + " sec");
