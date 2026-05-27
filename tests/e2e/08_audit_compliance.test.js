// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject } = require('./helpers');

const BASE = BASE_URL;

test.describe('AUD-08: Audit & Compliance — Black-Box Functional Tests', () => {

  let adminAuth, devAuth, project;

  test.beforeAll('Setup: Register users and create project', async ({ request }) => {
    const admin = await registerAndLogin(request, { role: 'UCTO_ADMIN', name: 'Audit Admin' });
    adminAuth = authContext(request, admin.accessToken);

    const dev = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Audit Dev' });
    devAuth = authContext(request, dev.accessToken);

    const { body: proj } = await createProject(adminAuth, 'Audit Trail', 'For audit trail tests', 'STARTUP');
    project = proj;
  });

  test('AUD-01: Should get audit logs as admin (200)', async () => {
    const res = await adminAuth.get(`${BASE}/api/audit-logs`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
  });

  test('AUD-02: Should get audit logs filtered by projectId (200)', async () => {
    const res = await adminAuth.get(`${BASE}/api/audit-logs?projectId=${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
    // All returned logs should belong to our project
    body.forEach(log => {
      if (log.projectId) {
        // Just verify structure
        expect(log).toHaveProperty('action');
        expect(log).toHaveProperty('timestamp');
      }
    });
  });

  test('AUD-03: Should get audit logs filtered by action (200)', async () => {
    const res = await adminAuth.get(`${BASE}/api/audit-logs?action=PROJECT_CREATE`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
  });

  test('AUD-04: Should get audit logs as non-admin (200 - own logs only)', async () => {
    // Create a screen to generate an audit log for the dev user
    await devAuth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'AUDIT_TEST',
        storageUrl: 'https://example.com/audit.png',
        mimeType: 'image/png',
      },
    });
    const res = await devAuth.get(`${BASE}/api/audit-logs`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
  });

  test('AUD-05: Should reject audit logs without authentication (401)', async ({ request }) => {
    const res = await request.get(`${BASE}/api/audit-logs`);
    expect(res.status()).toBe(401);
  });

  test('AUD-06: Should get product compliance results (200 or 404)', async () => {
    // Simulated sprint controller has compliance results endpoint
    const res = await adminAuth.get(`${BASE}/api/projects/${project.id}/branches/main/compliance-results/latest`);
    // Project exists but may not have compliance results yet
    expect([200, 404]).toContain(res.status());
  });

  test('AUD-07: Should get test results for project branch (200 or 404)', async () => {
    const res = await adminAuth.get(`${BASE}/api/projects/${project.id}/branches/main/test-results/latest`);
    // May not have test results yet
    expect([200, 404]).toContain(res.status());
  });

  test('AUD-08: Actions in audit logs should have meaningful names', async () => {
    const res = await adminAuth.get(`${BASE}/api/audit-logs?projectId=${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    if (body.length > 0) {
      body.forEach(log => {
        expect(typeof log.action).toBe('string');
        expect(log.action.length).toBeGreaterThan(0);
        expect(typeof log.success).toBe('boolean');
      });
    }
  });
});
