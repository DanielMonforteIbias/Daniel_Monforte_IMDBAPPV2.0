package edu.pmdm.monforte_danielimdbapp.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class KeystoreManager {
    private static final String ALGORITHM="AES";
    private static final String KEY="secretKey29384738293847102910291"; //Importante que mida 32 caracteres


    public static String encrypt(String value) {
        try {
            SecretKeySpec secretKey=new SecretKeySpec(KEY.getBytes(),ALGORITHM);
            Cipher cipher=Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            byte[] encryptedBytes =cipher.doFinal(value.getBytes());
            return java.util.Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String decrypt(String value) {
        try {
            SecretKeySpec secretKey=new SecretKeySpec(KEY.getBytes(),ALGORITHM);
            Cipher cipher=Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,secretKey);
            byte[] decryptedBytes=cipher.doFinal(java.util.Base64.getDecoder().decode(value));
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}