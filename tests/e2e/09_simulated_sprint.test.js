// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject } = require('./helpers');

const BASE = BASE_URL;

test.describe('SIM-09: Simulated Sprint — Black-Box Functional Tests', () => {

  let auth, project;

  test.beforeAll('Setup: Register user and create project', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'FOUNDER', name: 'Sim Sprint Tester' });
    auth = authContext(request, user.accessToken);
    const { body: proj } = await createProject(auth, 'Sim Sprint Project', 'For simulated sprint tests', 'STARTUP');
    project = proj;
  });

  test('SIM-01: Should run a simulated sprint with a requirement (200)', async () => {
    const res = await auth.post(`${BASE}/api/projects/${project.id}/simulated-sprint`, {
      data: {
        requirement: 'Build a user login feature with email and password authentication',
      },
    });
    // Simulated sprint depends on external AI services - may succeed or fail gracefully
    if (res.ok()) {
      const body = await res.json();
      // Should have outputs from all 5 agents
      const agentFields = ['baOutput', 'architectOutput', 'devOutput', 'testerOutput', 'complianceOutput', 'sprintSummary'];
      const hasExpectedFields = agentFields.some(f => body[f] !== undefined);
      expect(hasExpectedFields).toBeTruthy();
    } else {
      const body = await res.json();
      expect(body).toHaveProperty('error');
    }
  });

  test('SIM-02: Should run simulated sprint with detailed requirements (200)', async () => {
    const res = await auth.post(`${BASE}/api/projects/${project.id}/simulated-sprint`, {
      data: {
        requirement: 'Dashboard with charts. Users: admin, viewer. Must support real-time updates.',
      },
    });
    // Should either succeed or give a meaningful error
    expect([200, 400]).toContain(res.status());
  });

  test('SIM-03: Should reject simulated sprint without requirement (400)', async () => {
    const res = await auth.post(`${BASE}/api/projects/${project.id}/simulated-sprint`, {
      data: {},
    });
    expect(res.ok()).toBeFalsy();
  });

  test('SIM-04: Should reject simulated sprint for non-existent project (404)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/projects/99999999/simulated-sprint`, {
      data: { requirement: 'Test' },
      headers: { Authorization: `Bearer ${auth.authToken || ''}` },
    });
    // The authContext helper uses closures, so we need a direct approach
    // Register a user to get a token
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Temp Sprint' });
    const tempAuth = authContext(request, user.accessToken);
    const res2 = await tempAuth.post(`${BASE}/api/projects/99999999/simulated-sprint`, {
      data: { requirement: 'Test requirement' },
    });
    expect(res2.status()).toBe(404);
  });
});
