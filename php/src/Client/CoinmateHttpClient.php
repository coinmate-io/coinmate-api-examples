<?php

declare(strict_types=1);

namespace Coinmate\Client;

use Coinmate\Auth\CoinmateAuth;
use GuzzleHttp\Client;
use GuzzleHttp\Exception\GuzzleException;

/**
 * HTTP client for Coinmate API communication
 */
class CoinmateHttpClient
{
    private readonly string $apiUrl;
    private readonly CoinmateAuth $auth;
    private readonly Client $httpClient;

    /**
     * @param array{clientId: string, publicKey: string, privateKey: string, apiUrl?: string} $config
     */
    public function __construct(array $config)
    {
        $this->apiUrl = $config['apiUrl'] ?? 'https://coinmate.io/api';
        $this->auth = new CoinmateAuth(
            $config['clientId'],
            $config['publicKey'],
            $config['privateKey']
        );

        // Ensure base_uri has trailing slash for proper URL resolution
        $baseUri = rtrim($this->apiUrl, '/') . '/';
        $this->httpClient = new Client([
            'base_uri' => $baseUri,
            'headers' => [
                'Content-Type' => 'application/x-www-form-urlencoded',
            ],
        ]);
    }

    /**
     * Make a GET request to a public endpoint (no authentication required)
     *
     * @param string $endpoint API endpoint
     * @param array<string, string> $params Query parameters
     * @return array<string, mixed> Response data
     * @throws GuzzleException
     */
    public function getPublic(string $endpoint, array $params = []): array
    {
        // Remove leading slash for proper URL resolution with base_uri
        $endpoint = ltrim($endpoint, '/');
        $response = $this->httpClient->get($endpoint, [
            'query' => $params,
        ]);

        return json_decode($response->getBody()->getContents(), true);
    }

    /**
     * Make a POST request to a public endpoint (no authentication required)
     *
     * @param string $endpoint API endpoint
     * @param array<string, string> $params Form parameters
     * @return array<string, mixed> Response data
     * @throws GuzzleException
     */
    public function postPublic(string $endpoint, array $params = []): array
    {
        // Remove leading slash for proper URL resolution with base_uri
        $endpoint = ltrim($endpoint, '/');
        $response = $this->httpClient->post($endpoint, [
            'form_params' => $params,
        ]);

        return json_decode($response->getBody()->getContents(), true);
    }

    /**
     * Make a POST request to a private endpoint (requires authentication)
     *
     * @param string $endpoint API endpoint
     * @param array<string, string> $params Form parameters
     * @return array<string, mixed> Response data
     * @throws GuzzleException
     */
    public function postPrivate(string $endpoint, array $params = []): array
    {
        // Remove leading slash for proper URL resolution with base_uri
        $endpoint = ltrim($endpoint, '/');
        $authParams = $this->auth->getAuthParams();
        $allParams = array_merge($authParams, $params);

        $response = $this->httpClient->post($endpoint, [
            'form_params' => $allParams,
        ]);

        return json_decode($response->getBody()->getContents(), true);
    }
}
