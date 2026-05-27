// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject } = require('./helpers');

const BASE = BASE_URL;

test.describe('SCR-06: Screen API Module — Black-Box Functional Tests', () => {

  let auth, project;

  test.beforeAll('Setup: Register user and create project', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Screen Tester' });
    auth = authContext(request, user.accessToken);
    const { body: proj } = await createProject(auth, 'Screen Project', 'For screen tests', 'STARTUP');
    project = proj;
  });

  let screenId;

  test('SCR-01: Should create a screen (201)', async () => {
    const res = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'LOGIN',
        storageUrl: 'https://storage.example.com/screens/login.png',
        mimeType: 'image/png',
      },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body).toHaveProperty('id');
    expect(body.status).toBe('PENDING');
    expect(body.revisionCount).toBe(0);
    screenId = body.id;
  });

  test('SCR-02: Should create a screen with requirementId (201)', async () => {
    const res = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'DASHBOARD',
        storageUrl: 'https://storage.example.com/screens/dashboard.png',
        mimeType: 'image/png',
        requirementId: 1,
      },
    });
    expect(res.status()).toBe(201);
  });

  test('SCR-03: Should get screens by project (200)', async () => {
    const res = await auth.get(`${BASE}/api/screens/project/${project.id}`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
    expect(body.length).toBeGreaterThanOrEqual(1);
  });

  test('SCR-04: Should approve a PENDING screen (200)', async () => {
    // Create a fresh screen for approval
    const createRes = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'APPROVABLE',
        storageUrl: 'https://storage.example.com/approvable.png',
        mimeType: 'image/png',
      },
    });
    const screen = await createRes.json();

    const res = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
      data: { status: 'APPROVED', feedback: 'Looks good!' },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('APPROVED');
    expect(body).toHaveProperty('approvedBy');
    expect(body).toHaveProperty('approvedAt');
  });

  test('SCR-05: Should request changes on a PENDING screen (200)', async () => {
    const createRes = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'CHANGEABLE',
        storageUrl: 'https://storage.example.com/changeable.png',
        mimeType: 'image/png',
      },
    });
    const screen = await createRes.json();

    const res = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
      data: { status: 'CHANGES_REQUESTED', feedback: 'Please update colors' },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CHANGES_REQUESTED');
    expect(body.revisionCount).toBe(1);
  });

  test('SCR-06: Should reject APPROVED when not PENDING (400)', async () => {
    // Create, approve, then try to approve again
    const createRes = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'ALREADY_APPROVED',
        storageUrl: 'https://storage.example.com/done.png',
        mimeType: 'image/png',
      },
    });
    const screen = await createRes.json();
    await auth.put(`${BASE}/api/screens/${screen.id}/status`, { data: { status: 'APPROVED' } });

    // Try to approve again
    const res = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
      data: { status: 'APPROVED' },
    });
    expect(res.status()).toBe(400);
  });

  test('SCR-07: Should enforce max 3 revisions on CHANGES_REQUESTED (400)', async () => {
    // Create screen
    const createRes = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'MAX_REVISIONS',
        storageUrl: 'https://storage.example.com/revisions.png',
        mimeType: 'image/png',
      },
    });
    const screen = await createRes.json();

    // Request changes 3 times to exhaust revisions
    for (let i = 0; i < 3; i++) {
      const res = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
        data: { status: 'CHANGES_REQUESTED', feedback: `Revision ${i + 1}` },
      });
      if (i < 2) {
        expect(res.status()).toBe(200);
      } else {
        // Third time should fail with max revisions
        expect(res.status()).toBe(400);
        const body = await res.json();
        expect(body).toHaveProperty('error');
        expect(body.error).toContain('Maximum revision limit');
      }
    }
  });

  test('SCR-08: Should reject invalid status transition (400)', async () => {
    const createRes = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'INVALID_TRANSITION',
        storageUrl: 'https://storage.example.com/invalid.png',
        mimeType: 'image/png',
      },
    });
    const screen = await createRes.json();

    // Try REJECTED -> APPROVED (REJECTED needs to be PENDING first)
    await auth.put(`${BASE}/api/screens/${screen.id}/status`, { data: { status: 'REJECTED' } });
    const res = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
      data: { status: 'APPROVED' },
    });
    expect(res.status()).toBe(400);
  });

  test('SCR-09: Should reject invalid status value (400)', async () => {
    const createRes = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'BAD_STATUS',
        storageUrl: 'https://storage.example.com/bad.png',
        mimeType: 'image/png',
      },
    });
    const screen = await createRes.json();

    const res = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
      data: { status: 'INVALID_STATUS' },
    });
    expect(res.status()).toBe(400);
  });

  test('SCR-10: Should return 404 for non-existent screen', async () => {
    const res = await auth.put(`${BASE}/api/screens/99999999/status`, {
      data: { status: 'APPROVED' },
    });
    expect(res.status()).toBe(404);
  });

  test('SCR-11: Should reject screen creation without auth (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'UNAUTHED',
        storageUrl: 'https://example.com/x.png',
        mimeType: 'image/png',
      },
    });
    expect(res.status()).toBe(401);
  });

  test('SCR-12: Should create screen with mimeType image/jpeg (201)', async () => {
    const res = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'SETTINGS',
        storageUrl: 'https://storage.example.com/settings.jpeg',
        mimeType: 'image/jpeg',
      },
    });
    expect(res.status()).toBe(201);
  });
});
