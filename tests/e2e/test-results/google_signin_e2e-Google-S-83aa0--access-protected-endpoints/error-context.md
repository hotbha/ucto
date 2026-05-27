# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: google_signin_e2e.spec.js >> Google Sign-In E2E Flow >> should complete Google OAuth login and access protected endpoints
- Location: google_signin_e2e.spec.js:16:3

# Error details

```
Error: expect(received).toBeTruthy()

Received: false
```

# Test source

```ts
  1   | // @ts-check
  2   | const { test, expect } = require('@playwright/test');
  3   | 
  4   | const BASE_URL = 'http://localhost:8080';
  5   | 
  6   | /**
  7   |  * End-to-end test for Google Sign-In flow.
  8   |  *
  9   |  * The Flutter app's Google Sign-In (AuthBloc._onGoogleLogin) sends a simulated
  10  |  * token to the backend's POST /api/auth/oauth endpoint.
  11  |  * This test validates that the backend correctly processes the OAuth login,
  12  |  * returns tokens, and that those tokens work for authenticated API calls.
  13  |  */
  14  | test.describe('Google Sign-In E2E Flow', () => {
  15  | 
  16  |   test('should complete Google OAuth login and access protected endpoints', async ({ request }) => {
  17  |     const testEmail = `e2e_test_${Date.now()}@google.com`;
  18  |     const testName = 'E2E Test User';
  19  | 
  20  |     // Step 1: Call the OAuth endpoint the same way the Flutter app does
  21  |     const oauthResponse = await request.post(`${BASE_URL}/api/auth/oauth`, {
  22  |       data: {
  23  |         provider: 'google',
  24  |         token: 'simulated_google_token',
  25  |       },
  26  |     });
  27  | 
  28  |     // Step 2: Verify the OAuth response contains expected fields
> 29  |     expect(oauthResponse.ok()).toBeTruthy();
      |                                ^ Error: expect(received).toBeTruthy()
  30  |     expect(oauthResponse.status()).toBe(200);
  31  | 
  32  |     const oauthBody = await oauthResponse.json();
  33  |     console.log('OAuth Response body:', JSON.stringify(oauthBody, null, 2));
  34  | 
  35  |     // Validate response structure
  36  |     expect(oauthBody).toHaveProperty('accessToken');
  37  |     expect(oauthBody).toHaveProperty('refreshToken');
  38  |     expect(oauthBody).toHaveProperty('user');
  39  |     expect(oauthBody.user).toHaveProperty('email');
  40  |     expect(oauthBody.user).toHaveProperty('name');
  41  |     expect(oauthBody.user).toHaveProperty('role');
  42  | 
  43  |     // Validate field types/content
  44  |     expect(typeof oauthBody.accessToken).toBe('string');
  45  |     expect(oauthBody.accessToken.length).toBeGreaterThan(0);
  46  |     expect(typeof oauthBody.refreshToken).toBe('string');
  47  |     expect(oauthBody.refreshToken.length).toBeGreaterThan(0);
  48  | 
  49  |     const { accessToken, refreshToken, user } = oauthBody;
  50  | 
  51  |     // Step 3: Verify refresh token endpoint works
  52  |     const refreshResponse = await request.post(`${BASE_URL}/api/auth/refresh`, {
  53  |       data: {
  54  |         refreshToken: refreshToken,
  55  |       },
  56  |     });
  57  |     expect(refreshResponse.ok()).toBeTruthy();
  58  |     const refreshBody = await refreshResponse.json();
  59  |     console.log('Refresh Response body:', JSON.stringify(refreshBody, null, 2));
  60  |     expect(refreshBody).toHaveProperty('accessToken');
  61  |     expect(refreshBody).toHaveProperty('refreshToken');
  62  | 
  63  |     // Step 4: Validate JWT token is well-formed (can be decoded)
  64  |     const tokenParts = accessToken.split('.');
  65  |     expect(tokenParts.length).toBe(3); // JWT has 3 parts: header.payload.signature
  66  | 
  67  |     const payload = JSON.parse(Buffer.from(tokenParts[1], 'base64').toString());
  68  |     console.log('JWT Payload:', JSON.stringify(payload, null, 2));
  69  |     expect(payload).toHaveProperty('sub');         // subject (email)
  70  |     expect(payload).toHaveProperty('userId');
  71  |     expect(payload).toHaveProperty('role');
  72  |     expect(payload).toHaveProperty('iat');          // issued at
  73  |     expect(payload).toHaveProperty('exp');          // expiration
  74  |     expect(payload.exp).toBeGreaterThan(payload.iat); // expiry after issue
  75  | 
  76  |     console.log('✅ Google OAuth E2E flow fully validated!');
  77  |   });
  78  | 
  79  |   test('should reject Google OAuth with missing fields', async ({ request }) => {
  80  |     const response = await request.post(`${BASE_URL}/api/auth/oauth`, {
  81  |       data: {},
  82  |     });
  83  |     // Expect 400 Bad Request or 401 Unauthorized for invalid input
  84  |     expect(response.ok()).toBeFalsy();
  85  |     console.log('Invalid OAuth response status:', response.status());
  86  |   });
  87  | 
  88  |   test('should reject Google OAuth with invalid provider', async ({ request }) => {
  89  |     const response = await request.post(`${BASE_URL}/api/auth/oauth`, {
  90  |       data: {
  91  |         provider: 'invalid_provider',
  92  |         token: 'some_token',
  93  |       },
  94  |     });
  95  |     // Should be rejected
  96  |     expect(response.ok()).toBeFalsy();
  97  |     console.log('Invalid provider response status:', response.status());
  98  |   });
  99  | });
  100 | 
```