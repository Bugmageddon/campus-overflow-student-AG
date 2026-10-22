package com.campusoverflow.identity.infrastructure.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 个人信息字段加密（AES-256-GCM）与可检索摘要（HMAC-SHA256）。
 * 密钥来自环境变量 CO_FIELD_KEY（Base64 编码的 32 字节），切勿提交到代码仓库。
 */
@Component
public class FieldEncryptor {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec aesKey;
    private final SecretKeySpec macKey;
    private final SecureRandom random = new SecureRandom();

    public FieldEncryptor(@Value("${co.security.field-key}") String base64Key) {
        byte[] key = Base64.getDecoder().decode(base64Key);
        if (key.length != 32) {
            throw new IllegalStateException("co.security.field-key 必须是 Base64 编码的 32 字节密钥");
        }
        this.aesKey = new SecretKeySpec(key, "AES");
        this.macKey = new SecretKeySpec(key, "HmacSHA256");
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + ct.length).put(iv).put(ct).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("字段加密失败", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(TAG_BITS, all, 0, IV_LENGTH));
            return new String(cipher.doFinal(all, IV_LENGTH, all.length - IV_LENGTH), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("字段解密失败（密钥是否被更换？）", e);
        }
    }

    /** 确定性摘要，用于唯一性约束与精确查找，不可逆。 */
    public String digest(String plain) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(macKey);
            return HexFormat.of().formatHex(mac.doFinal(plain.toLowerCase().getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("摘要计算失败", e);
        }
    }
}
