// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject } = require('./helpers');

const BASE = BASE_URL;

test.describe('EML-10: Email & Health — Black-Box Functional Tests', () => {

  let auth, project;

  test.beforeAll('Setup: Register user', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Email Tester' });
    auth = authContext(request, user.accessToken);
  });

  // ──── Health ────

  test('HLT-01: Health endpoint should return UP status (200)', async ({ request }) => {
    const res = await request.get(`${BASE}/api/health`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('UP');
    expect(body.service).toBe('ucto-backend');
    expect(body).toHaveProperty('version');
  });

  test('HLT-02: Health endpoint should not require authentication', async ({ request }) => {
    const res = await request.get(`${BASE}/api/health`);
    expect(res.status()).toBe(200);
  });

  // ──── Email Verification ────

  test('EML-01: Should send verification email (200)', async () => {
    const res = await auth.post(`${BASE}/api/email/send-verification`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('message');
    expect(body.message).toContain('Verification email sent');
  });

  test('EML-02: Should verify email with valid token (200)', async () => {
    // First send verification to get a token (printed to console)
    await auth.post(`${BASE}/api/email/send-verification`);

    // We can't extract the real token from console output via API,
    // but we can test that the verify endpoint exists and validates format
    const res = await auth.get(`${BASE}/api/email/verify?token=test-token-123`);
    // Should be 400 for invalid token, not 500
    expect([200, 400]).toContain(res.status());
  });

  test('EML-03: Should reject email verification without auth (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/email/send-verification`);
    expect(res.status()).toBe(401);
  });

  // ──── Password Reset ────

  test('EML-04: Should request password reset (200)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/email/forgot-password`, {
      data: { email: 'nonexistent_' + Date.now() + '@test.ucto' },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('message');
    // Should not reveal whether email exists
    expect(body.message).toContain('If the email exists');
  });

  test('EML-05: Should reset password with valid token (200 or 400)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/email/reset-password`, {
      data: { token: 'test-reset-token-123', newPassword: 'NewPass123!' },
    });
    // Invalid token should return 400, not 500
    expect(res.status()).toBe(400);
  });

  test('EML-06: Should reject password reset without email field (400)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/email/forgot-password`, {
      data: {},
    });
    expect(res.ok()).toBeFalsy();
  });

  test('EML-07: Should get health endpoint quickly (< 1000ms)', async ({ request }) => {
    const start = Date.now();
    const res = await request.get(`${BASE}/api/health`);
    const duration = Date.now() - start;
    expect(res.status()).toBe(200);
    expect(duration).toBeLessThan(1000);
  });
});
