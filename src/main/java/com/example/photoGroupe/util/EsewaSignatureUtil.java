package com.example.photoGroupe.util;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EsewaSignatureUtil {

    public String generateSignature(String totalAmount, String transactionUuid,
                                    String productCode, String secretKey) throws Exception {
        String message = "total_amount=" + totalAmount +
                ",transaction_uuid=" + transactionUuid +
                ",product_code=" + productCode;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean verifySignature(String totalAmount, String transactionUuid,
                                   String productCode, String secretKey,
                                   String receivedSignature) throws Exception {
        String expected = generateSignature(totalAmount, transactionUuid, productCode, secretKey);
        return expected.equals(receivedSignature);
    }
}