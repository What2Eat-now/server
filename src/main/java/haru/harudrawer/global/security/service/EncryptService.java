package haru.harudrawer.global.security.service;


import haru.harudrawer.global.exception.CommonErrorCode;
import haru.harudrawer.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class EncryptService {

    /**
     * AES-GCM에서 권장되는 인증 태그(Authenticity Tag) 길이는 128비트(16바이트).
     * -  GCMParameterSpec(tagLengthInBits, IV)에서 단위는 비트 단위.
     */
    private final int GCM_TAG_LENGTH = 128;

    /**
     * GCM 모드에서 일반적으로 12바이트(96비트) IV(Nonce)를 사용.
     * - 너무 길거나 짧으면 보안성/성능에 영향을 미칠 수 있음.
     */
    private final int GCM_IV_LENGTH = 12;

    /**
     * AES-GCM NoPadding 알고리즘 스펙.
     * - "NoPadding"은 GCM 모드 내부적으로 필수 Padding이 아닌 "분리된 인증 태그" 방식을 쓰기 때문.
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private final SecretKeySpec secretKeySpec;


    public EncryptService(@Value("${app.encryption.key}") String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        // 만약 key 길이가 32바이트(256비트)가 아니라면 예외 처리하거나 로깅
        this.secretKeySpec = new SecretKeySpec(decodedKey, "AES");
    }

    /**
     * 평문(plainText)을 AES-GCM으로 암호화하여 Base64로 인코딩한 문자열을 반환
     *
     * @param plainText 암호화할 문자열
     * @return IV + 암호문(태그 포함)을 Base64로 인코딩한 문자열
     */
    public String encrypt(String plainText) {
        if (plainText.isEmpty() || plainText == null) {
            return plainText;
        }

        try {
            // 1. GCM용 IV(Nonce) 12바이트 랜덤 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // 2. Cipher 초기화 (AES/GCM/NoPadding)
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

            // 3. 암호화 수행
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 4. (IV + 암호문)을 한 덩어리로 묶어 Base64 인코딩
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);
            byte[] cipherMessage = byteBuffer.array();

            log.info("encrypted = " + Base64.getEncoder().encodeToString(cipherMessage));
            return Base64.getEncoder().encodeToString(cipherMessage);
        }catch (Exception e) {
            throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * encrypt(...)로 암호화된 문자열(cipherText)을 복호화
     *
     * @param cipherText Base64로 인코딩된 (IV + 암호문 + 태그) 문자열
     * @return 복호화된 평문
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        try {
            // 1. Base64 디코딩
            byte[] cipherMessage = Base64.getDecoder().decode(cipherText);

            // 2. ByteBuffer로부터 IV(12바이트)와 암호문(태그 포함) 분리
            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            log.info("암호문 분리");
            byte[] encryptedBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedBytes);

            // 3. 복호화 모드 Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);

            log.info("Cipher 초기화");
            // 4. 복호화 수행
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // 결과를 헥스 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
