# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 12_concurrency.test.js >> CON-12: Concurrency & Edge Cases — Black-Box Functional Tests >> CON-03: Should handle duplicate email registration attempts (idempotent)
- Location: 12_concurrency.test.js:56:3

# Error details

```
Error: expect(received).toBe(expected) // Object.is equality

Expected: 201
Received: 429
```

# Test source

```ts
  1   | // @ts-check
  2   | const { test, expect } = require('@playwright/test');
  3   | const { BASE_URL, registerAndLogin, authContext, createProject } = require('./helpers');
  4   | 
  5   | const BASE = BASE_URL;
  6   | 
  7   | test.describe('CON-12: Concurrency & Edge Cases — Black-Box Functional Tests', () => {
  8   | 
  9   |   test('CON-01: Should handle rapid sequential project creation (no data corruption)', async ({ request }) => {
  10  |     const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Rapid Creator' });
  11  |     const auth = authContext(request, user.accessToken);
  12  | 
  13  |     const projectIds = [];
  14  |     // Create 5 projects in rapid succession
  15  |     for (let i = 0; i < 5; i++) {
  16  |       const { body } = await createProject(auth, `Rapid Project ${i}`, `created at high speed ${i}`, 'STARTUP');
  17  |       if (body && body.id) {
  18  |         projectIds.push(body.id);
  19  |       }
  20  |     }
  21  | 
  22  |     // All should have unique IDs
  23  |     const uniqueIds = new Set(projectIds);
  24  |     expect(uniqueIds.size).toBe(projectIds.length);
  25  | 
  26  |     // Verify all exist
  27  |     for (const id of projectIds) {
  28  |       const res = await auth.get(`${BASE}/api/projects/${id}`);
  29  |       expect(res.status()).toBe(200);
  30  |     }
  31  |   });
  32  | 
  33  |   test('CON-02: Should handle concurrent screen creation on same project', async ({ request }) => {
  34  |     const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Concurrent Screen' });
  35  |     const auth = authContext(request, user.accessToken);
  36  | 
  37  |     const { body: project } = await createProject(auth, 'Concurrent Screens', 'test', 'STARTUP');
  38  | 
  39  |     // Create multiple screens quickly
  40  |     const screenPromises = [];
  41  |     for (let i = 0; i < 5; i++) {
  42  |       screenPromises.push(auth.post(`${BASE}/api/screens`, {
  43  |         data: {
  44  |           projectId: project.id,
  45  |           type: `SCREEN_${i}`,
  46  |           storageUrl: `https://storage.example.com/screen_${i}.png`,
  47  |           mimeType: 'image/png',
  48  |         },
  49  |       }));
  50  |     }
  51  | 
  52  |     const results = await Promise.all(screenPromises);
  53  |     results.forEach(r => expect(r.status()).toBe(201));
  54  |   });
  55  | 
  56  |   test('CON-03: Should handle duplicate email registration attempts (idempotent)', async ({ request }) => {
  57  |     const email = `dup_test_${Date.now()}@test.ucto`;
  58  |     const password = 'TestPass123!';
  59  | 
  60  |     // First registration
  61  |     const res1 = await request.post(`${BASE}/api/auth/register`, {
  62  |       data: { email, password, role: 'DEVELOPER', name: 'Original' },
  63  |     });
> 64  |     expect(res1.status()).toBe(201);
      |                           ^ Error: expect(received).toBe(expected) // Object.is equality
  65  | 
  66  |     // Duplicate registration attempts
  67  |     const res2 = await request.post(`${BASE}/api/auth/register`, {
  68  |       data: { email, password: 'DifferentPass1!', role: 'VIEWER', name: 'Duplicate' },
  69  |     });
  70  |     expect(res2.status()).toBe(400);
  71  | 
  72  |     // Original user should still be able to login with original password
  73  |     const loginRes = await request.post(`${BASE}/api/auth/login`, {
  74  |       data: { email, password },
  75  |     });
  76  |     expect(loginRes.status()).toBe(200);
  77  |   });
  78  | 
  79  |   test('CON-04: Should handle long strings in project title and description', async ({ request }) => {
  80  |     const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Long String Test' });
  81  |     const auth = authContext(request, user.accessToken);
  82  | 
  83  |     const longTitle = 'A'.repeat(500);
  84  |     const longDesc = 'B'.repeat(2000);
  85  | 
  86  |     const { response, body } = await createProject(auth, longTitle, longDesc, 'STARTUP');
  87  |     // Should either succeed (201) or reject with validation error (400)
  88  |     if (response.status() === 201) {
  89  |       expect(body.title).toBe(longTitle);
  90  |       expect(body.description).toBe(longDesc);
  91  |     } else {
  92  |       expect(response.status()).toBe(400);
  93  |     }
  94  |   });
  95  | 
  96  |   test('CON-05: Should handle Unicode/special characters in requirement title', async ({ request }) => {
  97  |     const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Unicode Test' });
  98  |     const auth = authContext(request, user.accessToken);
  99  | 
  100 |     const { body: project } = await createProject(auth, 'Unicode Test', 'testing unicode', 'STARTUP');
  101 | 
  102 |     const unicodeTitle = 'ユーザーログイン 🔐 ¡Hola! ñoño 你好';
  103 |     const { response, body } = await createProject(auth, unicodeTitle, 'Unicode description with emojis 🎉', 'STARTUP');
  104 |     expect(response.status()).toBe(201);
  105 |   });
  106 | });
  107 | 
```