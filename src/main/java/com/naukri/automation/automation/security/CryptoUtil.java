package com.naukri.automation.automation.security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.nio.file.*;
import java.io.IOException;

public class CryptoUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int KEY_LEN = 256;
    private static final int PBKDF2_ITER = 65536;
    private static final String PBKDF2_ALGO = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";

    private static SecretKey deriveKey(char[] passphrase, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITER, KEY_LEN);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGO);
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plaintext, char[] passphrase) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        RANDOM.nextBytes(salt);

        SecretKey key = deriveKey(passphrase, salt);

        byte[] iv = new byte[IV_LEN];
        RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] cipherBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

        byte[] out = new byte[salt.length + iv.length + cipherBytes.length];
        System.arraycopy(salt, 0, out, 0, salt.length);
        System.arraycopy(iv, 0, out, salt.length, iv.length);
        System.arraycopy(cipherBytes, 0, out, salt.length + iv.length, cipherBytes.length);

        return Base64.getEncoder().encodeToString(out);
    }

    public static String decrypt(String b64Combined, char[] passphrase) throws Exception {
        byte[] all = Base64.getDecoder().decode(b64Combined);

        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        System.arraycopy(all, 0, salt, 0, SALT_LEN);
        System.arraycopy(all, SALT_LEN, iv, 0, IV_LEN);

        byte[] cipherBytes = new byte[all.length - SALT_LEN - IV_LEN];
        System.arraycopy(all, SALT_LEN + IV_LEN, cipherBytes, 0, cipherBytes.length);

        SecretKey key = deriveKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] plain = cipher.doFinal(cipherBytes);
        return new String(plain, "UTF-8");
    }

    public static void writeEncryptedToFile(Path path, String encrypted) throws IOException {
        Files.write(path, encrypted.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String readEncryptedFromFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), "UTF-8").trim();
    }
}
