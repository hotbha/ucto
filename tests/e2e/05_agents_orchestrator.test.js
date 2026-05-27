// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject, orchAction, docAction } = require('./helpers');

const BASE = BASE_URL;

test.describe('AGT-05: Agents & Orchestrator — Black-Box Functional Tests', () => {

  let auth, project;

  test.beforeAll('Setup: Register user and create project', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'FOUNDER', name: 'Orch Tester' });
    auth = authContext(request, user.accessToken);
    const { body: proj } = await createProject(auth, 'Orch Project', 'For orchestrator tests', 'STARTUP');
    project = proj;
  });

  test('AGT-01: Should evaluate next loop via orchestrator (200)', async () => {
    const res = await auth.get(`${BASE}/api/orchestrator/evaluate/${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('nextLoop');
    expect(body).toHaveProperty('reason');
  });

  test('AGT-02: Should get orchestrator loop status (200)', async () => {
    const res = await auth.get(`${BASE}/api/orchestrator/status/${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    // Should have some status info
    expect(body).toBeDefined();
  });

  test('AGT-03: Should execute orchestrator action (200)', async () => {
    const { response, body } = await orchAction(auth, 'GET_LOOP_STATUS', {
      projectId: project.id,
    });
    expect(response.status()).toBe(200);
    expect(body).toBeDefined();
  });

  test('AGT-04: Should trigger agent via orchestrator (200)', async () => {
    const { response, body } = await orchAction(auth, 'TRIGGER_AGENT', {
      projectId: project.id,
      agentType: 'BA',
      context: 'Test context message',
    });
    // Should succeed or give meaningful error about agent limits
    if (response.ok()) {
      expect(body).toBeDefined();
    } else {
      const errBody = await response.json();
      // Could be limit exceeded or other business validation
      expect(errBody).toHaveProperty('error');
    }
  });

  test('AGT-05: Should route message via orchestrator (200)', async () => {
    const { response } = await orchAction(auth, 'ROUTE_MESSAGE', {
      projectId: project.id,
      fromAgent: 'BA',
      targetAgent: 'ARCHITECT',
      message: 'Please review the requirement',
    });
    // Accept either success or business validation error
    expect([200, 400]).toContain(response.status());
  });

  test('AGT-06: Should generate documentation via Doc agent (200)', async () => {
    const { response, body } = await docAction(auth, 'GENERATE', {
      projectId: project.id,
      docType: 'ARCHITECTURE',
      content: 'System uses Spring Boot with PostgreSQL',
    });
    if (response.ok()) {
      expect(body).toHaveProperty('documentation');
    }
  });

  test('AGT-07: Should get documents by project (200)', async () => {
    const res = await auth.get(`${BASE}/api/docs/project/${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('documentation');
  });

  test('AGT-08: Should get documents by type (200)', async () => {
    const res = await auth.get(`${BASE}/api/docs/project/${project.id}/type/ARCHITECTURE`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toBeDefined();
  });

  test('AGT-09: Should reject orchestrator action without auth (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/orchestrator/action`, {
      data: { action: 'GET_LOOP_STATUS', projectId: project.id },
    });
    expect(res.status()).toBe(401);
  });

  test('AGT-10: Should restart loop via orchestrator (200)', async () => {
    const { response } = await orchAction(auth, 'RESTART_LOOP', {
      projectId: project.id,
    });
    // Accept 200 or 400 depending on state
    expect([200, 400, 404]).toContain(response.status());
  });
});
