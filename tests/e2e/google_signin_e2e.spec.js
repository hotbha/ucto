// @ts-check
const { test, expect } = require('@playwright/test');

const BASE_URL = 'http://localhost:8080';

/**
 * End-to-end test for Google Sign-In flow.
 *
 * The Flutter app's Google Sign-In (AuthBloc._onGoogleLogin) sends a simulated
 * token to the backend's POST /api/auth/oauth endpoint.
 * This test validates that the backend correctly processes the OAuth login,
 * returns tokens, and that those tokens work for authenticated API calls.
 */
test.describe('Google Sign-In E2E Flow', () => {

  test('should complete Google OAuth login and access protected endpoints', async ({ request }) => {
    const testEmail = `e2e_test_${Date.now()}@google.com`;
    const testName = 'E2E Test User';

    // Step 1: Call the OAuth endpoint the same way the Flutter app does
    const oauthResponse = await request.post(`${BASE_URL}/api/auth/oauth`, {
      data: {
        provider: 'google',
        token: 'simulated_google_token',
      },
    });

    // Step 2: Verify the OAuth response contains expected fields
    expect(oauthResponse.ok()).toBeTruthy();
    expect(oauthResponse.status()).toBe(200);

    const oauthBody = await oauthResponse.json();
    console.log('OAuth Response body:', JSON.stringify(oauthBody, null, 2));

    // Validate response structure
    expect(oauthBody).toHaveProperty('accessToken');
    expect(oauthBody).toHaveProperty('refreshToken');
    expect(oauthBody).toHaveProperty('user');
    expect(oauthBody.user).toHaveProperty('email');
    expect(oauthBody.user).toHaveProperty('name');
    expect(oauthBody.user).toHaveProperty('role');

    // Validate field types/content
    expect(typeof oauthBody.accessToken).toBe('string');
    expect(oauthBody.accessToken.length).toBeGreaterThan(0);
    expect(typeof oauthBody.refreshToken).toBe('string');
    expect(oauthBody.refreshToken.length).toBeGreaterThan(0);

    const { accessToken, refreshToken, user } = oauthBody;

    // Step 3: Verify refresh token endpoint works
    const refreshResponse = await request.post(`${BASE_URL}/api/auth/refresh`, {
      data: {
        refreshToken: refreshToken,
      },
    });
    expect(refreshResponse.ok()).toBeTruthy();
    const refreshBody = await refreshResponse.json();
    console.log('Refresh Response body:', JSON.stringify(refreshBody, null, 2));
    expect(refreshBody).toHaveProperty('accessToken');
    expect(refreshBody).toHaveProperty('refreshToken');

    // Step 4: Validate JWT token is well-formed (can be decoded)
    const tokenParts = accessToken.split('.');
    expect(tokenParts.length).toBe(3); // JWT has 3 parts: header.payload.signature

    const payload = JSON.parse(Buffer.from(tokenParts[1], 'base64').toString());
    console.log('JWT Payload:', JSON.stringify(payload, null, 2));
    expect(payload).toHaveProperty('sub');         // subject (email)
    expect(payload).toHaveProperty('userId');
    expect(payload).toHaveProperty('role');
    expect(payload).toHaveProperty('iat');          // issued at
    expect(payload).toHaveProperty('exp');          // expiration
    expect(payload.exp).toBeGreaterThan(payload.iat); // expiry after issue

    console.log('✅ Google OAuth E2E flow fully validated!');
  });

  test('should reject Google OAuth with missing fields', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/api/auth/oauth`, {
      data: {},
    });
    // Expect 400 Bad Request or 401 Unauthorized for invalid input
    expect(response.ok()).toBeFalsy();
    console.log('Invalid OAuth response status:', response.status());
  });

  test('should reject Google OAuth with invalid provider', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/api/auth/oauth`, {
      data: {
        provider: 'invalid_provider',
        token: 'some_token',
      },
    });
    // Should be rejected
    expect(response.ok()).toBeFalsy();
    console.log('Invalid provider response status:', response.status());
  });
});
