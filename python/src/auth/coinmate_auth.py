"""HMAC-SHA256 authentication for Coinmate API"""
import hmac
import hashlib
import time


class CoinmateAuth:
    """Handles authentication for Coinmate API requests"""

    def __init__(self, client_id: str, public_key: str, private_key: str):
        """
        Initialize authentication handler

        Args:
            client_id: Coinmate client ID
            public_key: Coinmate public API key
            private_key: Coinmate private API key
        """
        self.client_id = client_id
        self.public_key = public_key
        self.private_key = private_key

    def get_nonce(self) -> int:
        """
        Generate nonce (current timestamp in milliseconds)

        Returns:
            Current timestamp in milliseconds
        """
        return int(time.time() * 1000)

    def create_signature(self, nonce: int) -> str:
        """
        Create HMAC-SHA256 signature

        Args:
            nonce: Timestamp in milliseconds

        Returns:
            Uppercase hex signature
        """
        message = f"{nonce}{self.client_id}{self.public_key}"
        signature = hmac.new(
            self.private_key.encode('utf-8'),
            message.encode('utf-8'),
            hashlib.sha256
        ).hexdigest().upper()
        return signature

    def get_auth_params(self) -> dict:
        """
        Get authentication parameters for API request

        Returns:
            Dictionary with clientId, publicKey, nonce, and signature
        """
        nonce = self.get_nonce()
        signature = self.create_signature(nonce)

        return {
            'clientId': self.client_id,
            'publicKey': self.public_key,
            'nonce': str(nonce),
            'signature': signature
        }
