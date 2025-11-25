package org.example.coinmate.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.example.coinmate.auth.CoinmateAuth;
import org.example.coinmate.config.CoinmateConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base HTTP client for Coinmate API communication.
 */
public class CoinmateHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(CoinmateHttpClient.class);

    private final CoinmateConfig config;
    private final CoinmateAuth auth;
    private final CloseableHttpClient httpClient;
    private final Gson gson;

    public CoinmateHttpClient(CoinmateConfig config) {
        this.config = config;
        this.auth = new CoinmateAuth(config.getClientId(), config.getPublicKey(), config.getPrivateKey());
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
    }

    /**
     * Makes a GET request to a public endpoint (no authentication required).
     */
    public JsonObject getPublic(String endpoint) throws IOException {
        return getPublic(endpoint, Map.of());
    }

    /**
     * Makes a GET request to a public endpoint with query parameters.
     */
    public JsonObject getPublic(String endpoint, Map<String, String> params) throws IOException {
        String url = buildUrl(endpoint, params);
        HttpGet request = new HttpGet(url);

        logger.debug("GET {}", url);

        return executeRequest(request);
    }

    /**
     * Makes a POST request to a public endpoint (no authentication required).
     */
    public JsonObject postPublic(String endpoint, Map<String, String> params) throws IOException {
        String url = config.getApiUrl() + endpoint;
        HttpPost request = new HttpPost(url);

        List<NameValuePair> formParams = new ArrayList<>();
        if (params != null) {
            params.forEach((key, value) -> formParams.add(new BasicNameValuePair(key, value)));
        }

        if (!formParams.isEmpty()) {
            request.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));
        }

        logger.debug("POST {} (public)", url);

        return executeRequest(request);
    }

    /**
     * Makes a POST request to a private endpoint (requires authentication).
     */
    public JsonObject postPrivate(String endpoint, Map<String, String> params) throws IOException {
        String url = config.getApiUrl() + endpoint;
        HttpPost request = new HttpPost(url);

        // Add authentication parameters
        long nonce = auth.generateNonce();
        String signature = auth.createSignature(nonce);

        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("clientId", auth.getClientId()));
        formParams.add(new BasicNameValuePair("publicKey", auth.getPublicKey()));
        formParams.add(new BasicNameValuePair("nonce", String.valueOf(nonce)));
        formParams.add(new BasicNameValuePair("signature", signature));

        // Add additional parameters
        if (params != null) {
            params.forEach((key, value) -> formParams.add(new BasicNameValuePair(key, value)));
        }

        request.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));

        logger.debug("POST {} with {} parameters", url, formParams.size());

        return executeRequest(request);
    }

    /**
     * Executes the HTTP request and returns the JSON response.
     */
    private JsonObject executeRequest(org.apache.hc.core5.http.ClassicHttpRequest request) throws IOException {
        return httpClient.execute(request, response -> {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            logger.debug("Response: {}", responseBody);

            if (response.getCode() != 200) {
                throw new IOException("HTTP error code: " + response.getCode() + ", body: " + responseBody);
            }

            return gson.fromJson(responseBody, JsonObject.class);
        });
    }

    /**
     * Builds URL with query parameters.
     */
    private String buildUrl(String endpoint, Map<String, String> params) {
        StringBuilder url = new StringBuilder(config.getApiUrl() + endpoint);

        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> url.append(key).append("=").append(value).append("&"));
            url.setLength(url.length() - 1); // Remove trailing &
        }

        return url.toString();
    }

    /**
     * Closes the HTTP client.
     */
    public void close() throws IOException {
        httpClient.close();
    }

    public Gson getGson() {
        return gson;
    }
}
