package com.dsprenkels.sss.android.jni;

/**
 * Shamir secret sharing in Java
 *
 * @author  Daan Sprenkels
 * @version 0.1
 * @since   2017-08-07
 *
 * This class exposes bindings to the Shamir secret sharing library at
 * <a href="https://github.com/dsprenkels/sss">github.com/dsprenkels/sss</a>.
 *
 * The main methods that a normal user should need are {@link #createShares(byte[], int, int)}
 * and {@link #combineShares(byte[][])}. These allow you to secret-share buffers of 64 bytes long
 * easy and safely.
 *
 * The other functions {@link #createKeyshares(byte[], int, int)} and
 * {@link #combineKeyshares(byte[][])} are for sharing cryptographic keys. These function do not
 * guarantee the same security as the other ones. Use it only when you really <em>know</em> what you
 * are doing! If you don't, please just stick with the standard functions. An example use case (for
 * people familiar with cryptographic primitives) is to use the keyshare functions to use an AEAD
 * wrapper to share arbitrary lengths of data. Hold the key under escrow by secret-sharing it and
 * distributing the shares. This way you are not limited by the fact that
 * {@link #createShares(byte[], int, int)} can only share arrays of 64 bytes.
 */
public final class ShamirSecretSharing {
    static {
        System.loadLibrary("sss-jni");
    }

    /**
     * The length of a to be secret-shared data buffer
     */
    public static final int DATA_LEN = 64;

    /**
     * The length of a share, as created by {@link #createShares(byte[], int, int)}
     */
    public static final int SHARE_LEN = 113;

    /**
     * The length of a key that will be shared using {@link #createKeyshares(byte[], int, int)}
     */
    public static final int KEY_LEN = 32;

    /**
     * The length of a keyshare, as created by {@link #createKeyshares(byte[], int, int)}
     */
    public static final int KEYSHARE_LEN = 33;

    /**
     * Create a set of shares from the data param
     * @param data the data buffer that needs to be split. The size of this array has to be equal
     *        to {@link #DATA_LEN}.
     * @param count the amount of shares that will be created from the data. This has to be greater
     *        than 0 and less than 256.
     * @param threshold the restoration threshold for restoring the secret-shared data. This has to be
     *        greater than 0 and less than count.
     * @return a byte[][] array of {@code count} shares, each with a length of {@link #SHARE_LEN}.
     * @throws IllegalArgumentException if one of the arguments to this function is not given in the
     *         correct format.
     */
    public static native byte[][] createShares(byte[] data, int count, int threshold);

    /**
     * Try to restore the data using a set of shares.
     * @param shares the shares used to restore the data.
     * @return the original shared data, if it was possible to restore this from the shares that
     *         were provided. If the shares could not reconstruct a valid secret (threshold was too
     *         high, or some of the shares were corrupted) the return value will be {@code null}
     *         instead.
     * @throws IllegalArgumentException if one of the arguments to this function is not given in the
     *         correct format, e.g. when one of the shares has an invalid length.
     */
    public static native byte[] combineShares(byte[][] shares);

    /**
     * Create a set of keyshares from the key param.
     * @param key       key buffer that needs to be split. The size of this array has to be equal
     *                  to {@link #KEY_LEN}.
     * @param count     amount of shares that will be created from the key. This has to be greater
     *                  than 0 and less than 256.
     * @param threshold restoration threshold for restoring the secret-shared key. This has to be
     *                  greater than 0 and less than count.
     * @return a byte[][] array of {@code count} keyshares, each with a length of
     *         {@link #KEYSHARE_LEN}.
     * @throws IllegalArgumentException if one of the arguments to this function is not given in the
     *         correct format.
     */
    public static native byte[][] createKeyshares(byte[] key, int count, int threshold);

    /**
     * Try to restore the original key using a set of keyshares.
     * @param keyshares the keyshares we will use to try to reconstruct the key.
     * @return the original key. In contrast to {@link #combineShares(byte[][])}, there is no
     *         checksum on the key, so the library cannot check if the key recovery was successful.
     *         If restoring the key fails, this function gives back an array that looks like
     *         garbage. However, treat this return value as a secret! Even when the keyshare
     *         combination has failed, the return value may tell a lot about the keyshares that were
     *         used in the process.
     * @throws IllegalArgumentException if one of the arguments to this function is not given in the
     *         correct format, e.g. when one of the keyshares has an invalid length.
     */
    public static native byte[] combineKeyshares(byte[][] keyshares);
}
