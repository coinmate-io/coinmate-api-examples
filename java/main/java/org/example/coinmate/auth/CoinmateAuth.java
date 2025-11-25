package org.example.coinmate.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Authentication utility for Coinmate API.
 * Implements HMAC-SHA256 signature generation as required by the API.
 */
public class CoinmateAuth {
    private final String clientId;
    private final String publicKey;
    private final String privateKey;

    private static final String HMAC_SHA256 = "HmacSHA256";

    public CoinmateAuth(String clientId, String publicKey, String privateKey) {
        this.clientId = clientId;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * Generates a nonce (unix timestamp in milliseconds).
     * Each request must have a nonce greater than the previous one.
     */
    public long generateNonce() {
        return System.currentTimeMillis();
    }

    /**
     * Creates HMAC-SHA256 signature for API authentication.
     * Signature input: nonce + clientId + publicKey
     *
     * @param nonce The nonce value (must be unique and increasing)
     * @return Uppercase hexadecimal signature string
     */
    public String createSignature(long nonce) {
        String message = nonce + clientId + publicKey;

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                privateKey.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256
            );
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).toUpperCase();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to create signature", e);
        }
    }

    /**
     * Converts byte array to hexadecimal string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public String getClientId() {
        return clientId;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
