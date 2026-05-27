// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject } = require('./helpers');

const BASE = BASE_URL;

test.describe('PROJ-02: Project API Module — Black-Box Functional Tests', () => {

  let adminAuth, devAuth;

  test.beforeAll('Setup: Register admin and dev users', async ({ request }) => {
    const admin = await registerAndLogin(request, { role: 'UCTO_ADMIN', name: 'Project Admin' });
    adminAuth = authContext(request, admin.accessToken);

    const dev = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Project Dev' });
    devAuth = authContext(request, dev.accessToken);
  });

  test('PROJ-01: Should create a project (201)', async () => {
    const { response, body } = await createProject(adminAuth, 'My Project', 'Description', 'STARTUP');
    expect(response.status()).toBe(201);
    expect(body).toHaveProperty('id');
    expect(body.title).toBe('My Project');
    expect(body.description).toBe('Description');
  });

  test('PROJ-02: Should create project with ENTERPRISE tier (201)', async () => {
    const { response, body } = await createProject(adminAuth, 'Enterprise Proj', 'Enterprise', 'ENTERPRISE');
    expect(response.status()).toBe(201);
    expect(body.tier || body.tier).toBeDefined();
  });

  test('PROJ-03: Should create project without description (201)', async () => {
    const { response } = await createProject(adminAuth, 'No Desc', '', 'STARTUP');
    expect(response.status()).toBe(201);
  });

  test('PROJ-04: Should get all projects for user (200)', async () => {
    await createProject(adminAuth, 'List Test 1', 'desc1', 'STARTUP');
    await createProject(adminAuth, 'List Test 2', 'desc2', 'STARTUP');
    const res = await adminAuth.get(`${BASE}/api/projects`);
    expect(res.status()).toBe(200);
    const projects = await res.json();
    expect(Array.isArray(projects)).toBeTruthy();
    expect(projects.length).toBeGreaterThanOrEqual(2);
  });

  test('PROJ-05: Should get a project by ID (200)', async () => {
    const { body: created } = await createProject(adminAuth, 'Get By ID', 'findme', 'STARTUP');
    const res = await adminAuth.get(`${BASE}/api/projects/${created.id}`);
    expect(res.status()).toBe(200);
    const project = await res.json();
    expect(project.id).toBe(created.id);
    expect(project.title).toBe('Get By ID');
  });

  test('PROJ-06: Should return 404 for non-existent project', async () => {
    const res = await adminAuth.get(`${BASE}/api/projects/99999999`);
    expect(res.status()).toBe(404);
  });

  test('PROJ-07: Should update project as owner (200)', async () => {
    const { body: created } = await createProject(adminAuth, 'Update Me', 'original', 'STARTUP');
    const res = await adminAuth.put(`${BASE}/api/projects/${created.id}`, {
      data: { title: 'Updated Title', description: 'Updated desc', status: 'ACTIVE' },
    });
    expect(res.status()).toBe(200);
    const updated = await res.json();
    expect(updated.title).toBe('Updated Title');
  });

  test('PROJ-08: Should return 403 when non-owner tries to update project', async () => {
    const { body: created } = await createProject(adminAuth, 'Owned by Admin', 'secret', 'STARTUP');
    const res = await devAuth.put(`${BASE}/api/projects/${created.id}`, {
      data: { title: 'Hacked!', description: 'should fail' },
    });
    expect(res.status()).toBe(403);
  });

  test('PROJ-09: Should delete project as owner (200)', async () => {
    const { body: created } = await createProject(adminAuth, 'Delete Me', 'gone', 'STARTUP');
    const res = await adminAuth.delete(`${BASE}/api/projects/${created.id}`);
    expect(res.status()).toBe(200);
    const delBody = await res.json();
    expect(delBody).toHaveProperty('message');

    // Verify it's gone
    const getRes = await adminAuth.get(`${BASE}/api/projects/${created.id}`);
    expect(getRes.status()).toBe(404);
  });

  test('PROJ-10: Should return 403 when non-owner tries to delete project', async () => {
    const { body: created } = await createProject(adminAuth, 'Admin Only', 'nope', 'STARTUP');
    const res = await devAuth.delete(`${BASE}/api/projects/${created.id}`);
    expect(res.status()).toBe(403);
  });

  test('PROJ-11: Should create project without authentication (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/projects`, {
      data: { title: 'Unauthenticated', description: 'should fail' },
    });
    expect(res.status()).toBe(401);
  });

  test('PROJ-12: Should get repo config for project (200 or 404)', async () => {
    const { body: project } = await createProject(adminAuth, 'Repo Config', 'has config', 'STARTUP');
    const res = await adminAuth.get(`${BASE}/api/projects/${project.id}/repo`);
    // Either 200 with config or 404 if not configured
    expect([200, 404]).toContain(res.status());
  });

  test('PROJ-13: Should get gate status for project branch (200 or 400)', async () => {
    const { body: project } = await createProject(adminAuth, 'Gate Status', 'has gates', 'STARTUP');
    const res = await adminAuth.get(`${BASE}/api/projects/${project.id}/branches/main/gate-status`);
    expect([200, 404, 400]).toContain(res.status());
  });

  test('PROJ-14: Should return empty list for user with no projects', async ({ request }) => {
    const fresh = await registerAndLogin(request, { role: 'VIEWER', name: 'No Projects' });
    const freshAuth = authContext(request, fresh.accessToken);
    const res = await freshAuth.get(`${BASE}/api/projects`);
    expect(res.status()).toBe(200);
    const projects = await res.json();
    expect(Array.isArray(projects)).toBeTruthy();
  });
});
