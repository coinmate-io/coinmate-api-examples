<?php

declare(strict_types=1);

namespace Coinmate\Auth;

/**
 * Authentication utility for Coinmate API
 * Implements HMAC-SHA256 signature generation
 */
class CoinmateAuth
{
    public function __construct(
        private readonly string $clientId,
        private readonly string $publicKey,
        private readonly string $privateKey
    ) {}

    /**
     * Generate a nonce (unix timestamp in milliseconds)
     * Each request must have a nonce greater than the previous one
     */
    public function generateNonce(): int
    {
        return (int) (microtime(true) * 1000);
    }

    /**
     * Create HMAC-SHA256 signature for API authentication
     * Signature input: nonce + clientId + publicKey
     *
     * @param int $nonce The nonce value (must be unique and increasing)
     * @return string Uppercase hexadecimal signature string
     */
    public function createSignature(int $nonce): string
    {
        $message = $nonce . $this->clientId . $this->publicKey;

        $signature = hash_hmac('sha256', $message, $this->privateKey);

        return strtoupper($signature);
    }

    /**
     * Create authentication parameters for a request
     *
     * @return array{clientId: string, publicKey: string, nonce: string, signature: string}
     */
    public function getAuthParams(): array
    {
        $nonce = $this->generateNonce();
        $signature = $this->createSignature($nonce);

        return [
            'clientId' => $this->clientId,
            'publicKey' => $this->publicKey,
            'nonce' => (string) $nonce,
            'signature' => $signature,
        ];
    }

    public function getClientId(): string
    {
        return $this->clientId;
    }

    public function getPublicKey(): string
    {
        return $this->publicKey;
    }
}
