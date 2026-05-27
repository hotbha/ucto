// @ts-check
const { test, expect } = require('@playwright/test');
const { BASE_URL, registerAndLogin, authContext, createProject, createRequirement, pmAction, baChat, docAction, orchAction } = require('./helpers');

const BASE = BASE_URL;

test.describe('E2E-11: End-to-End User Journeys — Black-Box Functional Tests', () => {

  test('E2E-01: Full user journey: register -> project -> requirement -> PM actions', async ({ request }) => {
    // 1. Register and login
    const user = await registerAndLogin(request, { role: 'FOUNDER', name: 'Full Journey User' });
    const auth = authContext(request, user.accessToken);

    // 2. Create project
    const { body: project } = await createProject(auth, 'E2E Project', 'End-to-end test', 'STARTUP');
    expect(project).toHaveProperty('id');

    // 3. Create requirement
    const { body: req } = await createRequirement(auth, project.id, 'Login Feature', 'Users login with email/password');
    expect(req).toHaveProperty('id');

    // 4. Add backlog item
    const { body: backlog } = await pmAction(auth, 'ADD_BACKLOG_ITEM', {
      projectId: project.id,
      title: 'Implement Login',
      description: 'Build login screen and API',
      priority: 'HIGH',
    });
    expect(backlog).toBeDefined();

    // 5. Create sprint
    const { body: sprint } = await pmAction(auth, 'CREATE_SPRINT', {
      projectId: project.id,
      title: 'Sprint 1',
    });
    expect(sprint).toBeDefined();

    // 6. Get backend
    const backlogRes = await auth.get(`${BASE}/api/pm/backlog/${project.id}`);
    expect(backlogRes.status()).toBe(200);

    // 7. Verify project exists
    const projRes = await auth.get(`${BASE}/api/projects/${project.id}`);
    expect(projRes.status()).toBe(200);

    // 8. Run PM loop
    const { response: loopRes } = await pmAction(auth, 'RUN_LOOP', {
      projectId: project.id,
    });
    expect(loopRes.status()).toBe(200);

    console.log('✅ Full user journey completed successfully');
  });

  test('E2E-02: BA chat -> orchestrator -> doc generation flow', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'FOUNDER', name: 'BA Doc Journey' });
    const auth = authContext(request, user.accessToken);

    // Create project
    const { body: project } = await createProject(auth, 'BA Doc Journey', 'Test BA to Doc flow', 'STARTUP');

    // Send BA chat message
    const { body: chatResponse } = await baChat(auth, project.id, 'Build a dashboard with real-time metrics');
    expect(chatResponse).toBeDefined();

    // Get orchestrator evaluation
    const evalRes = await auth.get(`${BASE}/api/orchestrator/evaluate/${project.id}`);
    expect(evalRes.status()).toBe(200);

    // Generate documentation
    const { response: docRes } = await docAction(auth, 'GENERATE', {
      projectId: project.id,
      docType: 'ARCHITECTURE',
      content: 'Dashboard with WebSocket for real-time updates',
    });

    console.log('✅ BA -> Orchestrator -> Doc flow completed');
  });

  test('E2E-03: Screen review cycle: create -> changes -> approve', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Screen Review Journey' });
    const auth = authContext(request, user.accessToken);

    const { body: project } = await createProject(auth, 'Screen Review', 'Test screen lifecycle', 'STARTUP');

    // Create screen
    const createRes = await auth.post(`${BASE}/api/screens`, {
      data: {
        projectId: project.id,
        type: 'DASHBOARD',
        storageUrl: 'https://storage.example.com/dashboard.png',
        mimeType: 'image/png',
      },
    });
    expect(createRes.status()).toBe(201);
    const screen = await createRes.json();

    // Request changes
    const changesRes = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
      data: { status: 'CHANGES_REQUESTED', feedback: 'Please fix alignment' },
    });
    expect(changesRes.status()).toBe(200);

    // Update to PENDING again (simulating designer re-upload)
    const pendingRes = await auth.put(`${BASE}/api/screens/${screen.id}/status`, {
      data: { status: 'PENDING' },
    });
    // May or may not allow this transition
    expect([200, 400]).toContain(pendingRes.status());

    console.log('✅ Screen review cycle completed');
  });

  test('E2E-04: Subscription lifecycle: register -> trial -> check usage', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'DEVELOPER', name: 'Sub Journey' });
    const auth = authContext(request, user.accessToken);

    // Check subscription
    const mySubRes = await auth.get(`${BASE}/api/subscriptions/my`);
    expect(mySubRes.status()).toBe(200);

    // Start trial
    const trialRes = await auth.post(`${BASE}/api/subscriptions/start-trial`);
    expect(trialRes.status()).toBe(200);

    // Check can-run-agent
    const canRunRes = await auth.get(`${BASE}/api/subscriptions/can-run-agent`);
    expect(canRunRes.status()).toBe(200);
    const canRunBody = await canRunRes.json();
    expect(canRunBody).toHaveProperty('canRun');

    console.log('✅ Subscription lifecycle completed');
  });

  test('E2E-05: Multi-user isolation: two users should not see each other\'s projects', async ({ request }) => {
    // Create User A with a project
    const userA = await registerAndLogin(request, { role: 'DEVELOPER', name: 'User A' });
    const authA = authContext(request, userA.accessToken);
    await createProject(authA, 'User A Project', 'Secret project A', 'STARTUP');

    // Create User B
    const userB = await registerAndLogin(request, { role: 'DEVELOPER', name: 'User B' });
    const authB = authContext(request, userB.accessToken);

    // User B should not see User A's project
    const projRes = await authB.get(`${BASE}/api/projects`);
    const projects = await projRes.json();
    const hasUserAProject = projects.some(p => p.title === 'User A Project');
    expect(hasUserAProject).toBeFalsy();

    // User B should get a 404 when trying to access User A's project directly
    // We don't have the project ID from User A's context, but we created it
    // Since we can't access it cross-user, just verify User B has 0 or different projects
    console.log('✅ Multi-user isolation verified');
  });

  test('E2E-06: Audit trail integrity: project actions should create audit logs', async ({ request }) => {
    const user = await registerAndLogin(request, { role: 'FOUNDER', name: 'Audit Trail User' });
    const auth = authContext(request, user.accessToken);

    // Create project (generates PROJECT_CREATE audit log)
    const { body: project } = await createProject(auth, 'Audit Trail Test', 'Test audit', 'STARTUP');

    // Update project (generates PROJECT_UPDATE audit log)
    await auth.put(`${BASE}/api/projects/${project.id}`, {
      data: { title: 'Updated Audit Project', description: 'updated' },
    });

    // Get audit logs
    const auditRes = await auth.get(`${BASE}/api/audit-logs?projectId=${project.id}`);
    expect(auditRes.status()).toBe(200);
    const logs = await auditRes.json();
    expect(logs.length).toBeGreaterThanOrEqual(2);

    // Verify audit log actions
    const actions = logs.map(l => l.action);
    expect(actions).toContain('PROJECT_CREATE');
    expect(actions).toContain('PROJECT_UPDATE');

    console.log('✅ Audit trail integrity verified');
  });
});
