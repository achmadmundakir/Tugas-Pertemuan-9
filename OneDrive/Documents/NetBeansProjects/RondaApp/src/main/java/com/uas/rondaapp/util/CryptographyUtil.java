package com.uas.rondaapp.util;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
public class CryptographyUtil {
    private static final String ALGORITHM = "AES";
    private static final byte[] SECRET_KEY = "MyUASProjectKey!".getBytes(StandardCharsets.UTF_8);
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    public static byte[] encrypt(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
    public static String decrypt(byte[] encryptedData) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}