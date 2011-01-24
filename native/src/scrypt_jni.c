// Copyright (C) 2011 - Will Glozer.  All rights reserved.

#include <errno.h>
#include <stdlib.h>
#include <inttypes.h>

#include "crypto_scrypt.h"
#include "scrypt_jni.h"

JNIEXPORT jbyteArray JNICALL Java_com_lambdaworks_crypto_SCrypt_scryptN(
    JNIEnv *env, jclass cls, jbyteArray passwd, jbyteArray salt,
    jint N, jint r, jint p, jint dkLen)
{
    jint Plen = (*env)->GetArrayLength(env, passwd);
    jint Slen = (*env)->GetArrayLength(env, salt);
    jbyte *P = (*env)->GetByteArrayElements(env, passwd, NULL);
    jbyte *S = (*env)->GetByteArrayElements(env, salt,   NULL);
    uint8_t *buf = malloc(sizeof(uint8_t) * dkLen);
    jbyteArray DK = NULL;
    int rc;

    if (P == NULL || S == NULL || buf == NULL) goto cleanup;

    rc = crypto_scrypt((uint8_t *) P, Plen, (uint8_t *) S, Slen, N, r, p, buf, dkLen);
    if (rc != 0) {
        jclass e = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        char *msg;
        switch (errno) {
            case EINVAL:
                msg = "N must be > 0 and a power of 2";
                break;
            case EFBIG:
            case ENOMEM:
                msg = "Insufficient memory available";
                break;
            default:
                msg = "Memory allocation failed";
        }
        (*env)->ThrowNew(env, e, msg);
        goto cleanup;
    }

    DK = (*env)->NewByteArray(env, dkLen);
    if (DK == NULL) goto cleanup;

    (*env)->SetByteArrayRegion(env, DK, 0, dkLen, (jbyte *) buf);

  cleanup:

    if (P) (*env)->ReleaseByteArrayElements(env, passwd, P, JNI_ABORT);
    if (S) (*env)->ReleaseByteArrayElements(env, salt,   S, JNI_ABORT);
    if (buf) free(buf);

    return DK;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_6;
}
