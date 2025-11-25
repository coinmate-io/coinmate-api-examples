import * as crypto from 'crypto';

/**
 * Authentication utility for Coinmate API
 * Implements HMAC-SHA256 signature generation
 */
export class CoinmateAuth {
  constructor(
    private readonly clientId: string,
    private readonly publicKey: string,
    private readonly privateKey: string
  ) {}

  /**
   * Generate a nonce (unix timestamp in milliseconds)
   * Each request must have a nonce greater than the previous one
   */
  generateNonce(): number {
    return Date.now();
  }

  /**
   * Create HMAC-SHA256 signature for API authentication
   * Signature input: nonce + clientId + publicKey
   *
   * @param nonce The nonce value (must be unique and increasing)
   * @returns Uppercase hexadecimal signature string
   */
  createSignature(nonce: number): string {
    const message = `${nonce}${this.clientId}${this.publicKey}`;

    const hmac = crypto.createHmac('sha256', this.privateKey);
    hmac.update(message);

    return hmac.digest('hex').toUpperCase();
  }

  /**
   * Create authentication parameters for a request
   */
  getAuthParams(): {
    clientId: string;
    publicKey: string;
    nonce: string;
    signature: string;
  } {
    const nonce = this.generateNonce();
    const signature = this.createSignature(nonce);

    return {
      clientId: this.clientId,
      publicKey: this.publicKey,
      nonce: nonce.toString(),
      signature,
    };
  }

  getClientId(): string {
    return this.clientId;
  }

  getPublicKey(): string {
    return this.publicKey;
  }
}
