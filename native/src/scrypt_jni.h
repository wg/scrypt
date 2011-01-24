#include <jni.h>

/*
 * Class:     com_lambdaworks_crypto_SCrypt
 * Method:    scryptN
 * Signature: ([B[BIIII)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_lambdaworks_crypto_SCrypt_scryptN
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jint, jint, jint, jint);
