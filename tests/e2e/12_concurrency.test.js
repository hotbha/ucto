// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject } = require('./helpers');

const BASE = BASE_URL;

test.describe('CON-12: Concurrency & Edge Cases — Black-Box Functional Tests', () => {

  test('CON-01: Should handle rapid sequential project creation (no data corruption)', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Rapid Creator' });
    const auth = authContext(request, user.accessToken);

    const projectIds = [];
    // Create 5 projects in rapid succession
    for (let i = 0; i < 5; i++) {
      const { body } = await createProject(auth, `Rapid Project ${i}`, `created at high speed ${i}`, 'STARTUP');
      if (body && body.id) {
        projectIds.push(body.id);
      }
    }

    // All should have unique IDs
    const uniqueIds = new Set(projectIds);
    expect(uniqueIds.size).toBe(projectIds.length);

    // Verify all exist
    for (const id of projectIds) {
      const res = await auth.get(`${BASE}/api/projects/${id}`);
      expect(res.status()).toBe(200);
    }
  });

  test('CON-02: Should handle concurrent screen creation on same project', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Concurrent Screen' });
    const auth = authContext(request, user.accessToken);

    const { body: project } = await createProject(auth, 'Concurrent Screens', 'test', 'STARTUP');

    // Create multiple screens quickly
    const screenPromises = [];
    for (let i = 0; i < 5; i++) {
      screenPromises.push(auth.post(`${BASE}/api/screens`, {
        data: {
          projectId: project.id,
          type: `SCREEN_${i}`,
          storageUrl: `https://storage.example.com/screen_${i}.png`,
          mimeType: 'image/png',
        },
      }));
    }

    const results = await Promise.all(screenPromises);
    results.forEach(r => expect(r.status()).toBe(201));
  });

  test('CON-03: Should handle duplicate email registration attempts (idempotent)', async ({ request }) => {
    const email = `dup_test_${Date.now()}@test.ucto`;
    const password = 'TestPass123!';

    // First registration
    const res1 = await request.post(`${BASE}/api/auth/register`, {
      data: { email, password, role: 'DEVELOPER', name: 'Original' },
    });
    expect(res1.status()).toBe(201);

    // Duplicate registration attempts
    const res2 = await request.post(`${BASE}/api/auth/register`, {
      data: { email, password: 'DifferentPass1!', role: 'VIEWER', name: 'Duplicate' },
    });
    expect(res2.status()).toBe(400);

    // Original user should still be able to login with original password
    const loginRes = await request.post(`${BASE}/api/auth/login`, {
      data: { email, password },
    });
    expect(loginRes.status()).toBe(200);
  });

  test('CON-04: Should handle long strings in project title and description', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Long String Test' });
    const auth = authContext(request, user.accessToken);

    const longTitle = 'A'.repeat(500);
    const longDesc = 'B'.repeat(2000);

    const { response, body } = await createProject(auth, longTitle, longDesc, 'STARTUP');
    // Should either succeed (201) or reject with validation error (400)
    if (response.status() === 201) {
      expect(body.title).toBe(longTitle);
      expect(body.description).toBe(longDesc);
    } else {
      expect(response.status()).toBe(400);
    }
  });

  test('CON-05: Should handle Unicode/special characters in requirement title', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Unicode Test' });
    const auth = authContext(request, user.accessToken);

    const { body: project } = await createProject(auth, 'Unicode Test', 'testing unicode', 'STARTUP');

    const unicodeTitle = 'ユーザーログイン 🔐 ¡Hola! ñoño 你好';
    const { response, body } = await createProject(auth, unicodeTitle, 'Unicode description with emojis 🎉', 'STARTUP');
    expect(response.status()).toBe(201);
  });
});
