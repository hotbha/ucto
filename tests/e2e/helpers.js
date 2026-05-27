// @ts-check
/**
 * Shared helper functions for UCTO black-box E2E tests.
 * These tests treat the system as a black box — only API contracts matter.
 */

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

/** Sleep for ms */
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Generate a unique email for test isolation.
 */
function uniqueEmail(prefix = 'test') {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}@test.ucto`;
}

/**
 * Retry a request function if it returns 429 (rate limited).
 * Waits 1s between retries, up to 3 attempts.
 */
async function withRetry(fn, maxRetries = 3) {
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    const result = await fn();
    if (result.response && result.response.status() === 429) {
      if (attempt < maxRetries) {
        console.log(`  ⏳ Rate limited (429), retrying in 1s (attempt ${attempt}/${maxRetries})...`);
        await sleep(1000);
        continue;
      }
    }
    return result;
  }
  // Should never reach here
  return await fn();
}

/**
 * Register a new user. Returns the full AuthResponse.
 */
async function registerUser(request, overrides = {}) {
  const email = overrides.email || uniqueEmail('reg');
  const payload = {
    email,
    password: overrides.password || 'TestPass123!',
    role: overrides.role || 'DEVELOPER',
    name: overrides.name || 'Test User',
    ...overrides,
  };
  // Ensure email is always our generated one if overrides doesn't change it
  if (!overrides.email) payload.email = email;

  const res = await request.post(`${BASE_URL}/api/auth/register`, { data: payload });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body, email: payload.email, password: payload.password };
}

/**
 * Login with email/password. Returns the full AuthResponse.
 */
async function loginUser(request, email, password) {
  const res = await request.post(`${BASE_URL}/api/auth/login`, {
    data: { email, password },
  });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body };
}

/**
 * Register + login in one step, with retry for rate limiting.
 * Returns auth tokens and user info.
 */
async function registerAndLogin(request, overrides = {}) {
  for (let attempt = 1; attempt <= 3; attempt++) {
    const { email, password, response: regRes } = await registerUser(request, overrides);
    
    // If rate limited, wait and retry
    if (regRes.status() === 429) {
      console.log(`  ⏳ Register rate limited (429), retrying in 1s (attempt ${attempt}/3)...`);
      await sleep(1000);
      continue;
    }

    // Login
    const { body, response: loginRes } = await loginUser(request, email, password);
    
    if (loginRes.status() === 429) {
      console.log(`  ⏳ Login rate limited (429), retrying in 1s (attempt ${attempt}/3)...`);
      await sleep(1000);
      continue;
    }

    if (loginRes.ok() && body) {
      return {
        accessToken: body.accessToken,
        refreshToken: body.refreshToken,
        email: body.user?.email || email,
        userId: body.user?.id,
        role: body.user?.role,
        body,
      };
    }

    // If registration succeeded but login failed (unlikely), we should try again
    if (attempt < 3) {
      await sleep(500);
    }
  }
  throw new Error(`registerAndLogin failed after 3 attempts for role=${overrides.role}`);
}

/**
 * Create an authenticated request context (reuses headers).
 */
function authContext(request, accessToken) {
  return {
    get: async (url, options = {}) =>
      request.get(url, {
        ...options,
        headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
      }),
    post: async (url, options = {}) =>
      request.post(url, {
        ...options,
        headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
      }),
    put: async (url, options = {}) =>
      request.put(url, {
        ...options,
        headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
      }),
    delete: async (url, options = {}) =>
      request.delete(url, {
        ...options,
        headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
      }),
  };
}

/**
 * Create a project. Returns the project object.
 */
async function createProject(authReq, title, description, tier) {
  const res = await authReq.post(`${BASE_URL}/api/projects`, {
    data: { title: title || 'Test Project', description: description || 'A test project', tier: tier || 'STARTUP' },
  });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body };
}

/**
 * Create a requirement. Returns the requirement object.
 */
async function createRequirement(authReq, projectId, title, description) {
  const res = await authReq.post(`${BASE_URL}/api/requirements`, {
    data: { projectId: String(projectId), title: title || 'Test Requirement', description: description || 'A test requirement' },
  });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body };
}

/**
 * Execute a PM action.
 */
async function pmAction(authReq, action, data = {}) {
  const res = await authReq.post(`${BASE_URL}/api/pm/action`, {
    data: { action, ...data },
  });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body };
}

/**
 * Execute a Doc action.
 */
async function docAction(authReq, action, data = {}) {
  const res = await authReq.post(`${BASE_URL}/api/docs/action`, {
    data: { action, ...data },
  });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body };
}

/**
 * Execute an Orchestrator action.
 */
async function orchAction(authReq, action, data = {}) {
  const res = await authReq.post(`${BASE_URL}/api/orchestrator/action`, {
    data: { action, ...data },
  });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body };
}

/**
 * Send a BA chat message.
 */
async function baChat(authReq, projectId, message) {
  const res = await authReq.post(`${BASE_URL}/api/ba/chat`, {
    data: { projectId, message },
  });
  const body = res.ok() ? await res.json() : null;
  return { response: res, body };
}

module.exports = {
  BASE_URL,
  sleep,
  uniqueEmail,
  registerUser,
  loginUser,
  registerAndLogin,
  authContext,
  createProject,
  createRequirement,
  pmAction,
  docAction,
  orchAction,
  baChat,
};
