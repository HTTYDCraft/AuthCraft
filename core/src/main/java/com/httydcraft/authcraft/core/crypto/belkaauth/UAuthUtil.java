package com.httydcraft.authcraft.core.crypto.belkaauth;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// #region Class Documentation
/**
 * Utility class for UAuth-specific cryptographic operations.
 * Provides methods for salt generation and PBKDF2 hashing.
 */
public final class UAuthUtil {
  private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
  private static final SecureRandom RANDOM;
  private static final char[] LOOKUP = "0123456789abcdef".toCharArray();

  static {
    try {
      RANDOM = SecureRandom.getInstance("SHA1PRNG");
      LOGGER.atFine().log("Initialized SecureRandom with SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      LOGGER.atSevere().withCause(e).log("Failed to initialize SecureRandom");
      throw new RuntimeException("SecureRandom initialization failed", e);
    }
  }

  private UAuthUtil() {
    throw new AssertionError("UAuthUtil cannot be instantiated");
  }
  // #endregion

  // #region Salt Generation
  /**
   * Generates a random salt for hashing.
   *
   * @return The generated salt as a hexadecimal string.
   */
  public static String generateSalt() {
    byte[] salt = new byte[20];
    RANDOM.nextBytes(salt);
    String result = new String(bytesToString(salt));
    LOGGER.atFine().log("Generated salt: %s", result);
    return result;
  }
  // #endregion

  // #region Hashing Methods
  /**
   * Computes a PBKDF2 hash for the given password and salt.
   *
   * @param pass The password to hash. Must not be null.
   * @param salt The salt bytes. Must not be null.
   * @param iter The number of iterations.
   * @param len The key length in bits.
   * @param alg The PBKDF2 algorithm (e.g., PBKDF2WithHmacSHA256). Must not be null.
   * @return The hashed bytes.
   * @throws Exception If hashing fails.
   */
  public static byte[] getHash(String pass, byte[] salt, int iter, int len, String alg) throws Exception {
    Preconditions.checkNotNull(pass, "pass must not be null");
    Preconditions.checkNotNull(salt, "salt must not be null");
    Preconditions.checkNotNull(alg, "alg must not be null");
    LOGGER.atFine().log("Computing PBKDF2 hash with algorithm: %s", alg);

    PBEKeySpec ks = new PBEKeySpec(pass.toCharArray(), salt, iter, len);
    SecretKeyFactory skf = SecretKeyFactory.getInstance(alg);
    byte[] result = skf.generateSecret(ks).getEncoded();
    LOGGER.atFine().log("Computed hash of length: %d", result.length);
    return result;
  }

  /**
   * Computes a PBKDF2 hash for the given password and salt string.
   *
   * @param pass The password to hash. Must not be null.
   * @param salt The salt string. Must not be null.
   * @param iter The number of iterations.
   * @param len The key length in bits.
   * @param alg The PBKDF2 algorithm (e.g., PBKDF2WithHmacSHA256). Must not be null.
   * @return The hashed string.
   * @throws Exception If hashing fails.
   */
  public static String getHash(String pass, String salt, int iter, int len, String alg) throws Exception {
    Preconditions.checkNotNull(pass, "pass must not be null");
    Preconditions.checkNotNull(salt, "salt must not be null");
    Preconditions.checkNotNull(alg, "alg must not be null");
    LOGGER.atFine().log("Computing PBKDF2 hash with algorithm: %s", alg);

    byte[] hash = getHash(pass, stringToBytes(salt), iter, len, alg);
    String result = new String(bytesToString(hash));
    LOGGER.atFine().log("Computed hash: %s", result);
    return result;
  }

  /**
   * Computes a UAuth-specific hash using a fixed salt and parameters.
   *
   * @param salt The salt string. Must not be null.
   * @param password The password to hash. Must not be null.
   * @return The hashed string, or null if hashing fails.
   */
  public static String getHash(String salt, String password) {
    Preconditions.checkNotNull(salt, "salt must not be null");
    Preconditions.checkNotNull(password, "password must not be null");
    LOGGER.atFine().log("Computing UAuth-specific hash");

    try {
      String result = getHash(
              password + "1_jm6H3tbLZ3DwZAqy8kVxAmjEJhl8ASypKUP-3d",
              salt,
              50000,
              160,
              "PBKDF2WithHmacSHA256"
      );
      LOGGER.atFine().log("Computed UAuth hash: %s", result);
      return result;
    } catch (Exception e) {
      LOGGER.atWarning().withCause(e).log("Failed to compute UAuth hash");
      return null;
    }
  }
  // #endregion

  // #region Byte-String Conversion
  /**
   * Converts a byte array to a hexadecimal string.
   *
   * @param b The byte array. Must not be null.
   * @return The hexadecimal string representation.
   */
  public static char[] bytesToString(byte[] b) {
    Preconditions.checkNotNull(b, "b must not be null");
    char[] res = new char[b.length * 2];
    for (int i = 0; i < b.length; i++) {
      res[i * 2] = LOOKUP[b[i] >>> 4 & 0xF];
      res[i * 2 + 1] = LOOKUP[b[i] & 0xF];
    }
    LOGGER.atFine().log("Converted bytes to string of length: %d", res.length);
    return res;
  }

  /**
   * Converts a hexadecimal string to a byte array.
   *
   * @param s The hexadecimal string. Must not be null.
   * @return The byte array.
   */
  public static byte[] stringToBytes(String s) {
    Preconditions.checkNotNull(s, "s must not be null");
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    LOGGER.atFine().log("Converted string to bytes of length: %d", data.length);
    return data;
  }
  // #endregion
}