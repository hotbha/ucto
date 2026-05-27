# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 01_auth.test.js >> AUTH-01: Auth API Module — Black-Box Functional Tests >> AUTH-17: JWT should have valid structure (3 parts, exp > iat)
- Location: 01_auth.test.js:154:3

# Error details

```
Error: registerAndLogin failed after 3 attempts for role=undefined
```

# Test source

```ts
  12  | }
  13  | 
  14  | /**
  15  |  * Generate a unique email for test isolation.
  16  |  */
  17  | function uniqueEmail(prefix = 'test') {
  18  |   return `${prefix}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}@test.ucto`;
  19  | }
  20  | 
  21  | /**
  22  |  * Retry a request function if it returns 429 (rate limited).
  23  |  * Waits 1s between retries, up to 3 attempts.
  24  |  */
  25  | async function withRetry(fn, maxRetries = 3) {
  26  |   for (let attempt = 1; attempt <= maxRetries; attempt++) {
  27  |     const result = await fn();
  28  |     if (result.response && result.response.status() === 429) {
  29  |       if (attempt < maxRetries) {
  30  |         console.log(`  ⏳ Rate limited (429), retrying in 1s (attempt ${attempt}/${maxRetries})...`);
  31  |         await sleep(1000);
  32  |         continue;
  33  |       }
  34  |     }
  35  |     return result;
  36  |   }
  37  |   // Should never reach here
  38  |   return await fn();
  39  | }
  40  | 
  41  | /**
  42  |  * Register a new user. Returns the full AuthResponse.
  43  |  */
  44  | async function registerUser(request, overrides = {}) {
  45  |   const email = overrides.email || uniqueEmail('reg');
  46  |   const payload = {
  47  |     email,
  48  |     password: overrides.password || 'TestPass123!',
  49  |     role: overrides.role || 'DEVELOPER',
  50  |     name: overrides.name || 'Test User',
  51  |     ...overrides,
  52  |   };
  53  |   // Ensure email is always our generated one if overrides doesn't change it
  54  |   if (!overrides.email) payload.email = email;
  55  | 
  56  |   const res = await request.post(`${BASE_URL}/api/auth/register`, { data: payload });
  57  |   const body = res.ok() ? await res.json() : null;
  58  |   return { response: res, body, email: payload.email, password: payload.password };
  59  | }
  60  | 
  61  | /**
  62  |  * Login with email/password. Returns the full AuthResponse.
  63  |  */
  64  | async function loginUser(request, email, password) {
  65  |   const res = await request.post(`${BASE_URL}/api/auth/login`, {
  66  |     data: { email, password },
  67  |   });
  68  |   const body = res.ok() ? await res.json() : null;
  69  |   return { response: res, body };
  70  | }
  71  | 
  72  | /**
  73  |  * Register + login in one step, with retry for rate limiting.
  74  |  * Returns auth tokens and user info.
  75  |  */
  76  | async function registerAndLogin(request, overrides = {}) {
  77  |   for (let attempt = 1; attempt <= 3; attempt++) {
  78  |     const { email, password, response: regRes } = await registerUser(request, overrides);
  79  |     
  80  |     // If rate limited, wait and retry
  81  |     if (regRes.status() === 429) {
  82  |       console.log(`  ⏳ Register rate limited (429), retrying in 1s (attempt ${attempt}/3)...`);
  83  |       await sleep(1000);
  84  |       continue;
  85  |     }
  86  | 
  87  |     // Login
  88  |     const { body, response: loginRes } = await loginUser(request, email, password);
  89  |     
  90  |     if (loginRes.status() === 429) {
  91  |       console.log(`  ⏳ Login rate limited (429), retrying in 1s (attempt ${attempt}/3)...`);
  92  |       await sleep(1000);
  93  |       continue;
  94  |     }
  95  | 
  96  |     if (loginRes.ok() && body) {
  97  |       return {
  98  |         accessToken: body.accessToken,
  99  |         refreshToken: body.refreshToken,
  100 |         email: body.user?.email || email,
  101 |         userId: body.user?.id,
  102 |         role: body.user?.role,
  103 |         body,
  104 |       };
  105 |     }
  106 | 
  107 |     // If registration succeeded but login failed (unlikely), we should try again
  108 |     if (attempt < 3) {
  109 |       await sleep(500);
  110 |     }
  111 |   }
> 112 |   throw new Error(`registerAndLogin failed after 3 attempts for role=${overrides.role}`);
      |         ^ Error: registerAndLogin failed after 3 attempts for role=undefined
  113 | }
  114 | 
  115 | /**
  116 |  * Create an authenticated request context (reuses headers).
  117 |  */
  118 | function authContext(request, accessToken) {
  119 |   return {
  120 |     get: async (url, options = {}) =>
  121 |       request.get(url, {
  122 |         ...options,
  123 |         headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
  124 |       }),
  125 |     post: async (url, options = {}) =>
  126 |       request.post(url, {
  127 |         ...options,
  128 |         headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
  129 |       }),
  130 |     put: async (url, options = {}) =>
  131 |       request.put(url, {
  132 |         ...options,
  133 |         headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
  134 |       }),
  135 |     delete: async (url, options = {}) =>
  136 |       request.delete(url, {
  137 |         ...options,
  138 |         headers: { ...options.headers, Authorization: `Bearer ${accessToken}` },
  139 |       }),
  140 |   };
  141 | }
  142 | 
  143 | /**
  144 |  * Create a project. Returns the project object.
  145 |  */
  146 | async function createProject(authReq, title, description, tier) {
  147 |   const res = await authReq.post(`${BASE_URL}/api/projects`, {
  148 |     data: { title: title || 'Test Project', description: description || 'A test project', tier: tier || 'STARTUP' },
  149 |   });
  150 |   const body = res.ok() ? await res.json() : null;
  151 |   return { response: res, body };
  152 | }
  153 | 
  154 | /**
  155 |  * Create a requirement. Returns the requirement object.
  156 |  */
  157 | async function createRequirement(authReq, projectId, title, description) {
  158 |   const res = await authReq.post(`${BASE_URL}/api/requirements`, {
  159 |     data: { projectId: String(projectId), title: title || 'Test Requirement', description: description || 'A test requirement' },
  160 |   });
  161 |   const body = res.ok() ? await res.json() : null;
  162 |   return { response: res, body };
  163 | }
  164 | 
  165 | /**
  166 |  * Execute a PM action.
  167 |  */
  168 | async function pmAction(authReq, action, data = {}) {
  169 |   const res = await authReq.post(`${BASE_URL}/api/pm/action`, {
  170 |     data: { action, ...data },
  171 |   });
  172 |   const body = res.ok() ? await res.json() : null;
  173 |   return { response: res, body };
  174 | }
  175 | 
  176 | /**
  177 |  * Execute a Doc action.
  178 |  */
  179 | async function docAction(authReq, action, data = {}) {
  180 |   const res = await authReq.post(`${BASE_URL}/api/docs/action`, {
  181 |     data: { action, ...data },
  182 |   });
  183 |   const body = res.ok() ? await res.json() : null;
  184 |   return { response: res, body };
  185 | }
  186 | 
  187 | /**
  188 |  * Execute an Orchestrator action.
  189 |  */
  190 | async function orchAction(authReq, action, data = {}) {
  191 |   const res = await authReq.post(`${BASE_URL}/api/orchestrator/action`, {
  192 |     data: { action, ...data },
  193 |   });
  194 |   const body = res.ok() ? await res.json() : null;
  195 |   return { response: res, body };
  196 | }
  197 | 
  198 | /**
  199 |  * Send a BA chat message.
  200 |  */
  201 | async function baChat(authReq, projectId, message) {
  202 |   const res = await authReq.post(`${BASE_URL}/api/ba/chat`, {
  203 |     data: { projectId, message },
  204 |   });
  205 |   const body = res.ok() ? await res.json() : null;
  206 |   return { response: res, body };
  207 | }
  208 | 
  209 | module.exports = {
  210 |   BASE_URL,
  211 |   sleep,
  212 |   uniqueEmail,
```