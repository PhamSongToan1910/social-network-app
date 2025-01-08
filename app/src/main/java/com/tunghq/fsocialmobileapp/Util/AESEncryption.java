package com.tunghq.fsocialmobileapp.Util;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import android.util.Base64;

import java.util.Arrays;


public class AESEncryption {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // 12 bytes for GCM
    private static final int TAG_LENGTH = 128;

    public static String encrypt(String plaintext, SecretKey secretKey) throws Exception {
        // Khởi tạo Cipher với thuật toán AES/GCM/NoPadding
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Lấy IV tự động tạo
        byte[] iv = cipher.getIV();

        // Mã hóa dữ liệu
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Ghép IV và ciphertext
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        // Trả về Base64 của dữ liệu đã ghép
        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        // Giải mã Base64 thành byte[]
        byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);

        // Tách IV và ciphertext
        int ivSize = 12; // IV kích thước cố định (12 bytes cho AES-GCM)
        byte[] iv = Arrays.copyOfRange(combined, 0, ivSize);
        byte[] ciphertext = Arrays.copyOfRange(combined, ivSize, combined.length);

        // Thiết lập Cipher để giải mã
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        // Giải mã dữ liệu
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, "UTF-8");
    }

}
