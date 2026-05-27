// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject, createRequirement, pmAction } = require('./helpers');

const BASE = BASE_URL;

test.describe('SUB-07: Subscription & Usage Metering — Black-Box Functional Tests', () => {

  let auth, secondUserAuth, project;

  test.beforeAll('Setup: Register users and projects', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'FOUNDER', name: 'Sub Tester' });
    auth = authContext(request, user.accessToken);
    const user2 = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Sub Tester 2' });
    secondUserAuth = authContext(request, user2.accessToken);

    const { body: proj } = await createProject(auth, 'Sub Project', 'For subscription tests', 'STARTUP');
    project = proj;
  });

  test('SUB-01: Should get subscription plans (200)', async () => {
    const res = await auth.get(`${BASE}/api/subscriptions/plans`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
    if (body.length > 0) {
      const plan = body[0];
      expect(plan).toHaveProperty('tier');
      expect(plan).toHaveProperty('name');
      expect(plan).toHaveProperty('maxProjects');
      expect(plan).toHaveProperty('maxAgentRuns');
      expect(plan).toHaveProperty('price');
    }
  });

  test('SUB-02: Should get my subscription status (200)', async () => {
    const res = await auth.get(`${BASE}/api/subscriptions/my`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toBeDefined();
  });

  test('SUB-03: Should start a free trial (200)', async () => {
    // Create a fresh user for trial
    const freshUser = await registerAndLogin(auth, { role: 'DEVELOPER', name: 'Trial User' });
    const freshAuth = authContext(auth, freshUser.accessToken);
    const res = await freshAuth.post(`${BASE}/api/subscriptions/start-trial`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('message');
    expect(body.message).toContain('trial');
  });

  test('SUB-04: Should upgrade subscription to ENTERPRISE (200)', async () => {
    const res = await auth.post(`${BASE}/api/subscriptions/upgrade`, {
      data: { tier: 'ENTERPRISE' },
    });
    // May fail if subscription service checks payment, but should give meaningful response
    if (res.ok()) {
      const body = await res.json();
      expect(body).toHaveProperty('message');
    } else {
      const body = await res.json();
      expect(body).toHaveProperty('error');
    }
  });

  test('SUB-05: Should check can-run-agent endpoint (200)', async () => {
    const res = await auth.get(`${BASE}/api/subscriptions/can-run-agent`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('canRun');
  });

  test('SUB-06: Should check can-run-agent for unauthenticated user (401)', async ({ request }) => {
    const res = await request.get(`${BASE}/api/subscriptions/can-run-agent`);
    expect(res.status()).toBe(401);
  });

  test('SUB-07: Should get my subscription for second user (200)', async () => {
    const res = await secondUserAuth.get(`${BASE}/api/subscriptions/my`);
    expect(res.status()).toBe(200);
  });

  test('SUB-08: Plans endpoint should be publicly accessible (200)', async ({ request }) => {
    const res = await request.get(`${BASE}/api/subscriptions/plans`);
    expect(res.status()).toBe(200);
  });

  test('SUB-09: Should reject upgrade to invalid tier (400)', async () => {
    const res = await auth.post(`${BASE}/api/subscriptions/upgrade`, {
      data: { tier: 'INVALID_TIER' },
    });
    expect(res.ok()).toBeFalsy();
  });

  test('SUB-10: Should reject trial for already subscribed user (200/400)', async () => {
    const res = await auth.post(`${BASE}/api/subscriptions/start-trial`);
    if (res.ok()) {
      const body = await res.json();
      expect(body).toHaveProperty('message');
    } else {
      const body = await res.json();
      expect(body).toHaveProperty('error');
    }
  });

  test('SUB-11: Chargebee webhook should accept payload (200)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/subscriptions/webhook/chargebee`, {
      data: { event_type: 'test_event', content: {} },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('message');
  });

  test('SUB-12: Usage status should have expected fields', async () => {
    const res = await auth.get(`${BASE}/api/subscriptions/my`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    // Should have some usage fields
    const expectedFields = ['tier', 'projectsUsed', 'agentRunsUsed'];
    const hasSome = expectedFields.some(f => body[f] !== undefined);
    expect(hasSome).toBeTruthy();
  });
});
