import axios, { AxiosInstance } from 'axios';
import { CoinmateAuth } from '../auth/CoinmateAuth';
import { CoinmateConfig } from '../types/models';

/**
 * HTTP client for Coinmate API communication
 */
export class CoinmateHttpClient {
  private readonly apiUrl: string;
  private readonly auth: CoinmateAuth;
  private readonly httpClient: AxiosInstance;

  constructor(config: CoinmateConfig) {
    this.apiUrl = config.apiUrl || 'https://coinmate.io/api';
    this.auth = new CoinmateAuth(config.clientId, config.publicKey, config.privateKey);

    this.httpClient = axios.create({
      baseURL: this.apiUrl,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
  }

  /**
   * Make a GET request to a public endpoint (no authentication required)
   */
  async getPublic<T>(endpoint: string, params?: Record<string, string>): Promise<T> {
    const response = await this.httpClient.get(endpoint, { params });
    return response.data;
  }

  /**
   * Make a POST request to a public endpoint (no authentication required)
   */
  async postPublic<T>(endpoint: string, params?: Record<string, string>): Promise<T> {
    const formData = new URLSearchParams(params || {});
    const response = await this.httpClient.post(endpoint, formData.toString());
    return response.data;
  }

  /**
   * Make a POST request to a private endpoint (requires authentication)
   */
  async postPrivate<T>(endpoint: string, params?: Record<string, string>): Promise<T> {
    const authParams = this.auth.getAuthParams();

    const allParams = {
      ...authParams,
      ...(params || {}),
    };

    const formData = new URLSearchParams(allParams);
    const response = await this.httpClient.post(endpoint, formData.toString());

    return response.data;
  }
}
