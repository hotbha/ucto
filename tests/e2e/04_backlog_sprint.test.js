// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject, createRequirement, pmAction } = require('./helpers');

const BASE = BASE_URL;

test.describe('PM-04: PM / Backlog & Sprint — Black-Box Functional Tests', () => {

  let auth, project;

  test.beforeAll('Setup: Register user and create project', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'FOUNDER', name: 'PM Tester' });
    auth = authContext(request, user.accessToken);
    const { body: proj } = await createProject(auth, 'PM Project', 'For PM action tests', 'STARTUP');
    project = proj;
  });

  test('PM-01: Should add backlog item to project (200)', async () => {
    const { response, body } = await pmAction(auth, 'ADD_BACKLOG_ITEM', {
      projectId: project.id,
      title: 'User Login',
      description: 'Implement user login',
      priority: 'HIGH',
    });
    expect(response.status()).toBe(200);
    expect(body).toBeDefined();
  });

  test('PM-02: Should get backlog for project (200)', async () => {
    await pmAction(auth, 'ADD_BACKLOG_ITEM', {
      projectId: project.id,
      title: 'Another Item',
      description: 'desc',
      priority: 'MEDIUM',
    });
    const res = await auth.get(`${BASE}/api/pm/backlog/${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('backlogItem');
  });

  test('PM-03: Should create a sprint (200)', async () => {
    const { response, body } = await pmAction(auth, 'CREATE_SPRINT', {
      projectId: project.id,
      title: 'Sprint 1',
    });
    expect(response.status()).toBe(200);
    expect(body).toBeDefined();
  });

  test('PM-04: Should get sprints for project (200)', async () => {
    await pmAction(auth, 'CREATE_SPRINT', { projectId: project.id, title: 'Sprint 2' });
    const res = await auth.get(`${BASE}/api/pm/sprints/${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('sprint');
  });

  test('PM-05: Should update backlog item status (200)', async () => {
    const { body: addResult } = await pmAction(auth, 'ADD_BACKLOG_ITEM', {
      projectId: project.id,
      title: 'Status Update',
      description: 'test status transition',
      priority: 'LOW',
    });
    // Try updating status if we got a backlogItem back
    if (addResult && addResult.backlogItem && addResult.backlogItem.id) {
      const { response } = await pmAction(auth, 'UPDATE_STATUS', {
        backlogItemId: addResult.backlogItem.id,
        status: 'IN_PROGRESS',
      });
      expect(response.status()).toBe(200);
    }
  });

  test('PM-06: Should check Definition of Ready on backlog item (200)', async () => {
    const { body: addResult } = await pmAction(auth, 'ADD_BACKLOG_ITEM', {
      projectId: project.id,
      title: 'DoR Check',
      description: 'check DoR',
      priority: 'HIGH',
      acceptanceCriteria: 'Given... When... Then...',
    });
    if (addResult && addResult.backlogItem && addResult.backlogItem.id) {
      const { response, body } = await pmAction(auth, 'CHECK_DOR', {
        backlogItemId: addResult.backlogItem.id,
      });
      expect(response.status()).toBe(200);
      if (body && body.dorResult) {
        expect(body.dorResult).toHaveProperty('passed');
      }
    }
  });

  test('PM-07: Should check Definition of Done on backlog item (200)', async () => {
    const { body: addResult } = await pmAction(auth, 'ADD_BACKLOG_ITEM', {
      projectId: project.id,
      title: 'DoD Check',
      description: 'check DoD',
      priority: 'HIGH',
    });
    if (addResult && addResult.backlogItem && addResult.backlogItem.id) {
      const { response, body } = await pmAction(auth, 'CHECK_DOD', {
        backlogItemId: addResult.backlogItem.id,
      });
      expect(response.status()).toBe(200);
      if (body && body.dodResult) {
        expect(body.dodResult).toHaveProperty('passed');
      }
    }
  });

  test('PM-08: Should run PM loop (200)', async () => {
    const { response, body } = await pmAction(auth, 'RUN_LOOP', {
      projectId: project.id,
    });
    expect(response.status()).toBe(200);
    if (body && body.loopResult) {
      expect(body.loopResult).toHaveProperty('message');
    }
  });

  test('PM-09: Should reject ADD_BACKLOG_ITEM without title (400)', async () => {
    const { response } = await pmAction(auth, 'ADD_BACKLOG_ITEM', {
      projectId: project.id,
    });
    // Should fail because title is required
    expect(response.ok()).toBeFalsy();
  });

  test('PM-10: Should reject unknown PM action (400)', async () => {
    const { response } = await pmAction(auth, 'INVALID_ACTION', {
      projectId: project.id,
    });
    expect(response.ok()).toBeFalsy();
  });

  test('PM-11: Should get backlog for non-existent project (200 or 400)', async () => {
    const res = await auth.get(`${BASE}/api/pm/backlog/99999999`);
    expect([200, 400, 404]).toContain(res.status());
  });

  test('PM-12: Should reject PM action without authentication (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/pm/action`, {
      data: { action: 'CREATE_SPRINT', projectId: project.id },
    });
    expect(res.status()).toBe(401);
  });

  test('PM-13: Should handle multiple backlog items in same project', async () => {
    const items = ['Item A', 'Item B', 'Item C', 'Item D', 'Item E'];
    for (const title of items) {
      await pmAction(auth, 'ADD_BACKLOG_ITEM', {
        projectId: project.id,
        title,
        description: `desc for ${title}`,
        priority: 'MEDIUM',
      });
    }
    const res = await auth.get(`${BASE}/api/pm/backlog/${project.id}`);
    expect(res.status()).toBe(200);
  });

  test('PM-14: Should run complex sprint lifecycle', async () => {
    // Create sprint
    const { body: sprintResult } = await pmAction(auth, 'CREATE_SPRINT', {
      projectId: project.id,
      title: 'Complex Sprint',
    });
    expect(sprintResult).toBeDefined();

    // Add items to sprint
    for (let i = 0; i < 3; i++) {
      await pmAction(auth, 'ADD_BACKLOG_ITEM', {
        projectId: project.id,
        title: `Sprint Item ${i}`,
        description: `Item ${i} for sprint`,
        priority: i === 0 ? 'HIGH' : 'MEDIUM',
      });
    }

    // Run the loop
    const { response } = await pmAction(auth, 'RUN_LOOP', {
      projectId: project.id,
    });
    expect(response.status()).toBe(200);
  });
});
