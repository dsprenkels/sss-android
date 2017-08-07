//
// Created by daan on 04/08/17.
//

#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "sss/sss.h"

#define DATA_LEN 64
#define SHARE_LEN 113
#define KEY_LEN 32
#define KEYSHARE_LEN 33

static jint throw(JNIEnv *env, char *className, char *message)
{
    jclass class;
    class = (*env)->FindClass(env, className);
    if (class == NULL) {
        class = (*env)->FindClass(env, "java/lang/NoClassDefFoundError");
        // In the case this call to FindClass gives back NULL, we have an unrecoverable error.
        // We should just let this trigger an JNI exception to indicate that there is a bug in
        // the code.
    }
    return (*env)->ThrowNew(env, class, message);
}

static jboolean checkNK(JNIEnv *env, long n, long k) {
    if (n < 1 || n > 255) {
        throw(env, "java/lang/IllegalArgumentException", "`count` must be in 1..255");
        return JNI_FALSE;
    }
    if (k < 1 || k > n) {
        throw(env, "java/lang/IllegalArgumentException", "`threshold` must be in 1..n");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

JNIEXPORT jobjectArray JNICALL Java_com_dsprenkels_sss_android_jni_ShamirSecretSharing_createShares(
        JNIEnv *env, jobject this,
        jbyteArray data, jint count, jint threshold)
{
    jsize data_len = (*env)->GetArrayLength(env, data);
    if (data_len != DATA_LEN) {
        throw(env, "java/lang/IllegalArgumentException", "invalid data length");
        return NULL;
    }
    if (!checkNK(env, count, threshold)) {
        return NULL;
    }

    // Allocate i/o buffers
    jbyte in[DATA_LEN] = {0};
    jbyte out[count * SHARE_LEN];
    memset(out, 0, (size_t) count * SHARE_LEN);

    // Call sss_create_shares
    (*env)->GetByteArrayRegion(env, data, 0, DATA_LEN, in);
    sss_create_shares((sss_Share*) out, (const uint8_t*) in, count, threshold);

    // Group output shares into chunks of SHARE_LEN size
    jclass clsArray = (*env)->FindClass(env, "[B");
    if (clsArray == NULL) {
        throw(env, "java/lang/NoClassDefFoundError", "no class '[B' found");
        return NULL;
    }
    jobjectArray shares = (*env)->NewObjectArray(env, count, clsArray, NULL);
    for (int idx = 0; idx < count; ++idx) {
        jbyteArray tmp = (*env)->NewByteArray(env, SHARE_LEN);
        if (tmp == NULL) {
            throw(env, "java/lang/OutOfMemoryError", "could not allocate jbyteArray");
            return NULL;
        }
        (*env)->SetByteArrayRegion(env, tmp, 0, SHARE_LEN, &out[idx * SHARE_LEN]);
        (*env)->SetObjectArrayElement(env, shares, idx, tmp);
    }
    return shares;
}

JNIEXPORT jbyteArray JNICALL Java_com_dsprenkels_sss_android_jni_ShamirSecretSharing_combineShares(
        JNIEnv *env, jobject this, jobjectArray shares)
{
    // Fail if the amount of shares is not too big
    jsize count = (*env)->GetArrayLength(env, shares);
    if (count > 255) {
        throw(env, "java/lang/IllegalArgumentException", "too many shares given");
        return NULL;
    }

    // Pack the shares into a consecutive buffer
    jbyte tmp[count * SHARE_LEN];
    memset(tmp, 0, count * SHARE_LEN);
    for (int idx = 0; idx < count; ++idx) {
        jbyteArray share = (*env)->GetObjectArrayElement(env, shares, idx);
        if ((*env)->GetArrayLength(env, share) != SHARE_LEN) {
            throw(env, "java/lang/IllegalArgumentException", "invalid share length");
            return NULL;
        }
        (*env)->GetByteArrayRegion(env, share, 0, SHARE_LEN, &tmp[idx * SHARE_LEN]);
    }

    // Call sss library
    jbyte out[DATA_LEN] = {0};
    jint ret = sss_combine_shares((uint8_t*) out, (const sss_Share*) tmp, count);
    if (ret != 0) {
        // Failed to combine the shares
        return NULL;
    }

    // Convert result to byte array
    jbyteArray data = (*env)->NewByteArray(env, DATA_LEN);
    if (data == NULL) {
        throw(env, "java/lang/OutOfMemoryError", "could not allocate array for restored data");
        return NULL;
    }
    (*env)->SetByteArrayRegion(env, data, 0, DATA_LEN, out);
    return data;
}

JNIEXPORT jobjectArray JNICALL Java_com_dsprenkels_sss_android_jni_ShamirSecretSharing_createKeyshares(
        JNIEnv *env, jobject this,
        jbyteArray key, jint count, jint threshold)
{
    jsize key_len = (*env)->GetArrayLength(env, key);
    if (key_len != KEY_LEN) {
        throw(env, "java/lang/IllegalArgumentException", "invalid key length");
        return NULL;
    }
    if (!checkNK(env, count, threshold)) {
        return NULL;
    }

    // Allocate i/o buffers
    jbyte in[KEY_LEN] = {0};
    jbyte out[count * KEYSHARE_LEN];
    memset(out, 0, (size_t) count * KEYSHARE_LEN);

    // Call sss_create_shares
    (*env)->GetByteArrayRegion(env, key, 0, KEY_LEN, in);
    sss_create_keyshares((sss_Keyshare*) out, (const uint8_t*) in, count, threshold);

    // Group output shares into chunks of SHARE_LEN size
    jclass clsArray = (*env)->FindClass(env, "[B");
    if (clsArray == NULL) {
        throw(env, "java/lang/NoClassDefFoundError", "no class '[B' found");
        return NULL;
    }
    jobjectArray keyshares = (*env)->NewObjectArray(env, count, clsArray, NULL);
    for (int idx = 0; idx < count; ++idx) {
        jbyteArray tmp = (*env)->NewByteArray(env, KEYSHARE_LEN);
        if (tmp == NULL) {
            throw(env, "java/lang/OutOfMemoryError", "could not allocate jbyteArray");
            return NULL;
        }
        (*env)->SetByteArrayRegion(env, tmp, 0, KEYSHARE_LEN, &out[idx * KEYSHARE_LEN]);
        (*env)->SetObjectArrayElement(env, keyshares, idx, tmp);
    }
    return keyshares;
}

JNIEXPORT jbyteArray JNICALL Java_com_dsprenkels_sss_android_jni_ShamirSecretSharing_combineKeyshares(
        JNIEnv *env, jobject this, jobjectArray keyshares)
{
    // Fail if the amount of shares is not too big
    jsize count = (*env)->GetArrayLength(env, keyshares);
    if (count > 255) {
        throw(env, "java/lang/IllegalArgumentException", "too many keyshares given");
        return NULL;
    }

    // Pack the shares into a consecutive buffer
    jbyte tmp[count * KEYSHARE_LEN];
    memset(tmp, 0, count * KEYSHARE_LEN);
    for (int idx = 0; idx < count; ++idx) {
        jbyteArray keyshare = (*env)->GetObjectArrayElement(env, keyshares, idx);
        if ((*env)->GetArrayLength(env, keyshare) != KEYSHARE_LEN) {
            throw(env, "java/lang/IllegalArgumentException", "invalid keyshare length");
            return NULL;
        }
        (*env)->GetByteArrayRegion(env, keyshare, 0, KEYSHARE_LEN, &tmp[idx * KEYSHARE_LEN]);
    }

    // Call sss library
    jbyte out[KEY_LEN] = {0};
    sss_combine_keyshares((uint8_t*) out, (const sss_Keyshare*) tmp, count);

    // Convert result to byte array
    jbyteArray key = (*env)->NewByteArray(env, KEY_LEN);
    if (key == NULL) {
        throw(env, "java/lang/OutOfMemoryError", "could not allocate array for restored key");
        return NULL;
    }
    (*env)->SetByteArrayRegion(env, key, 0, KEY_LEN, out);
    return key;
}