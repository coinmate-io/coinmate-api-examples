"""HTTP client for Coinmate API"""
import aiohttp
from typing import Dict, Optional, Any
from ..auth.coinmate_auth import CoinmateAuth
from ..types.models import CoinmateConfig


class CoinmateHttpClient:
    """Handles HTTP communication with Coinmate API"""

    def __init__(self, config: CoinmateConfig):
        """
        Initialize HTTP client

        Args:
            config: Coinmate configuration
        """
        self.config = config
        self.auth = CoinmateAuth(
            config.client_id,
            config.public_key,
            config.private_key
        )
        self.session: Optional[aiohttp.ClientSession] = None

    async def __aenter__(self):
        """Async context manager entry"""
        self.session = aiohttp.ClientSession()
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """Async context manager exit"""
        if self.session:
            await self.session.close()

    async def close(self):
        """Close the HTTP session"""
        if self.session:
            await self.session.close()

    async def get_public(
        self,
        endpoint: str,
        params: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        Make GET request to public endpoint

        Args:
            endpoint: API endpoint path
            params: Optional query parameters

        Returns:
            JSON response as dictionary
        """
        url = f"{self.config.api_url}{endpoint}"

        if not self.session:
            self.session = aiohttp.ClientSession()

        async with self.session.get(url, params=params) as response:
            return await response.json()

    async def post_public(
        self,
        endpoint: str,
        data: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        Make POST request to public endpoint

        Args:
            endpoint: API endpoint path
            data: Optional form data

        Returns:
            JSON response as dictionary
        """
        url = f"{self.config.api_url}{endpoint}"

        if not self.session:
            self.session = aiohttp.ClientSession()

        async with self.session.post(url, data=data or {}) as response:
            return await response.json()

    async def post_private(
        self,
        endpoint: str,
        params: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        Make POST request to private endpoint with authentication

        Args:
            endpoint: API endpoint path
            params: Optional parameters

        Returns:
            JSON response as dictionary
        """
        url = f"{self.config.api_url}{endpoint}"

        # Get authentication parameters
        auth_params = self.auth.get_auth_params()

        # Merge with request parameters
        data = {**auth_params}
        if params:
            data.update(params)

        if not self.session:
            self.session = aiohttp.ClientSession()

        async with self.session.post(url, data=data) as response:
            return await response.json()
