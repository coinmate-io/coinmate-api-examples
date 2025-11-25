package org.example.coinmate.config;

/**
 * Configuration class for Coinmate API credentials and settings.
 */
public class CoinmateConfig {
    private final String apiUrl;
    private final String clientId;
    private final String publicKey;
    private final String privateKey;

    public static final String DEFAULT_API_URL = "https://coinmate.io/api";

    private CoinmateConfig(Builder builder) {
        this.apiUrl = builder.apiUrl;
        this.clientId = builder.clientId;
        this.publicKey = builder.publicKey;
        this.privateKey = builder.privateKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiUrl = DEFAULT_API_URL;
        private String clientId;
        private String publicKey;
        private String privateKey;

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public CoinmateConfig build() {
            if (clientId == null || clientId.isEmpty()) {
                throw new IllegalArgumentException("Client ID is required");
            }
            if (publicKey == null || publicKey.isEmpty()) {
                throw new IllegalArgumentException("Public key is required");
            }
            if (privateKey == null || privateKey.isEmpty()) {
                throw new IllegalArgumentException("Private key is required");
            }
            return new CoinmateConfig(this);
        }
    }
}
