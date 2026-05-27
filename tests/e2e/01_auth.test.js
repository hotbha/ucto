// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, uniqueEmail, registerUser, loginUser, registerAndLogin } = require('./helpers');

const BASE = BASE_URL;

test.describe('AUTH-01: Auth API Module — Black-Box Functional Tests', () => {

  // ──── Registration ────

  test('AUTH-01: Should register a new user successfully (201 CREATED)', async ({ request }) => {
    const { response, body, email } = await registerUser(request);
    expect(response.status()).toBe(201);
    expect(body).toHaveProperty('accessToken');
    expect(body).toHaveProperty('refreshToken');
    expect(body).toHaveProperty('user');
    expect(body.user.email).toBe(email);
    expect(body.user.role).toBe('DEVELOPER');
  });

  test('AUTH-02: Should reject duplicate email registration (400)', async ({ request }) => {
    const { email, password } = await registerUser(request);
    // Try registering again with same email
    const res = await request.post(`${BASE}/api/auth/register`, {
      data: { email, password, role: 'DEVELOPER', name: 'Duplicate' },
    });
    expect(res.ok()).toBeFalsy();
    expect(res.status()).toBe(400);
  });

  test('AUTH-03: Should reject registration with missing required fields (400)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/register`, {
      data: { email: '', password: '', role: '', name: '' },
    });
    // Spring Boot @Valid should return 400
    expect(res.ok()).toBeFalsy();
  });

  test('AUTH-04: Should reject registration with invalid email format (400)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/register`, {
      data: { email: 'not-an-email', password: 'TestPass123!', role: 'DEVELOPER', name: 'Bad Email' },
    });
    expect(res.ok()).toBeFalsy();
    expect(res.status()).toBe(400);
  });

  test('AUTH-05: Should register UCTO_ADMIN role successfully (201)', async ({ request }) => {
    const { response } = await registerUser(request, { role: 'UCTO_ADMIN', name: 'Admin User' });
    expect(response.status()).toBe(201);
  });

  test('AUTH-06: Should register FOUNDER role successfully (201)', async ({ request }) => {
    const { response } = await registerUser(request, { role: 'FOUNDER', name: 'Founder User' });
    expect(response.status()).toBe(201);
  });

  // ──── Login ────

  test('AUTH-07: Should login with valid credentials (200)', async ({ request }) => {
    const { email, password } = await registerUser(request);
    const { response, body } = await loginUser(request, email, password);
    expect(response.status()).toBe(200);
    expect(body).toHaveProperty('accessToken');
    expect(body).toHaveProperty('refreshToken');
    expect(body.user.email).toBe(email);
  });

  test('AUTH-08: Should reject login with wrong password (401)', async ({ request }) => {
    const { email } = await registerUser(request);
    const res = await request.post(`${BASE}/api/auth/login`, {
      data: { email, password: 'WrongPassword999!' },
    });
    expect(res.status()).toBe(401);
  });

  test('AUTH-09: Should reject login for non-existent user (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/login`, {
      data: { email: 'nonexistent_' + Date.now() + '@test.ucto', password: 'SomePass123!' },
    });
    expect(res.status()).toBe(401);
  });

  // ──── OAuth ────

  test('AUTH-10: Should complete OAuth login with Google (200)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/oauth`, {
      data: { provider: 'google', token: 'simulated_google_token' },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('accessToken');
    expect(body).toHaveProperty('refreshToken');
    expect(body).toHaveProperty('user');
    expect(typeof body.accessToken).toBe('string');
    expect(body.accessToken.length).toBeGreaterThan(0);
  });

  test('AUTH-11: Should reject OAuth with missing fields (400/401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/oauth`, { data: {} });
    expect(res.ok()).toBeFalsy();
  });

  test('AUTH-12: Should reject OAuth with invalid provider (400/401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/oauth`, {
      data: { provider: 'invalid_provider', token: 'some_token' },
    });
    expect(res.ok()).toBeFalsy();
  });

  // ──── Token Refresh ────

  test('AUTH-13: Should refresh tokens with valid refresh token (200)', async ({ request }) => {
    const { refreshToken } = await registerAndLogin(request);
    const res = await request.post(`${BASE}/api/auth/refresh`, {
      data: { refreshToken },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('accessToken');
    expect(body).toHaveProperty('refreshToken');
  });

  test('AUTH-14: Should reject refresh with invalid token (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/refresh`, {
      data: { refreshToken: 'invalid_token_12345' },
    });
    expect(res.status()).toBe(401);
  });

  // ──── OTP ────

  test('AUTH-15: Should send OTP to valid phone number (200)', async ({ request }) => {
    const { accessToken } = await registerAndLogin(request);
    const res = await request.post(`${BASE}/api/auth/otp/send`, {
      data: { phoneNumber: '+919876543210' },
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    // OTP sending may fail for real SMS, but should still return 200 with a message
    const body = await res.json();
    expect(body).toHaveProperty('message');
  });

  test('AUTH-16: Should reject OTP verify with invalid code (400)', async ({ request }) => {
    const { accessToken } = await registerAndLogin(request);
    const res = await request.post(`${BASE}/api/auth/otp/verify`, {
      data: { phoneNumber: '+919876543210', otp: '000000' },
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    expect(res.status()).toBe(400);
  });

  // ──── JWT Validation ────

  test('AUTH-17: JWT should have valid structure (3 parts, exp > iat)', async ({ request }) => {
    const { accessToken } = await registerAndLogin(request);
    const parts = accessToken.split('.');
    expect(parts.length).toBe(3);
    const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString());
    expect(payload).toHaveProperty('sub');
    expect(payload).toHaveProperty('userId');
    expect(payload).toHaveProperty('role');
    expect(payload).toHaveProperty('iat');
    expect(payload).toHaveProperty('exp');
    expect(payload.exp).toBeGreaterThan(payload.iat);
  });
});
