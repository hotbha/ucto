# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 01_auth.test.js >> AUTH-01: Auth API Module — Black-Box Functional Tests >> AUTH-14: Should reject refresh with invalid token (401)
- Location: 01_auth.test.js:123:3

# Error details

```
Error: expect(received).toBe(expected) // Object.is equality

Expected: 401
Received: 429
```

# Test source

```ts
  27  |     expect(res.ok()).toBeFalsy();
  28  |     expect(res.status()).toBe(400);
  29  |   });
  30  | 
  31  |   test('AUTH-03: Should reject registration with missing required fields (400)', async ({ request }) => {
  32  |     const res = await request.post(`${BASE}/api/auth/register`, {
  33  |       data: { email: '', password: '', role: '', name: '' },
  34  |     });
  35  |     // Spring Boot @Valid should return 400
  36  |     expect(res.ok()).toBeFalsy();
  37  |   });
  38  | 
  39  |   test('AUTH-04: Should reject registration with invalid email format (400)', async ({ request }) => {
  40  |     const res = await request.post(`${BASE}/api/auth/register`, {
  41  |       data: { email: 'not-an-email', password: 'TestPass123!', role: 'DEVELOPER', name: 'Bad Email' },
  42  |     });
  43  |     expect(res.ok()).toBeFalsy();
  44  |     expect(res.status()).toBe(400);
  45  |   });
  46  | 
  47  |   test('AUTH-05: Should register UCTO_ADMIN role successfully (201)', async ({ request }) => {
  48  |     const { response } = await registerUser(request, { role: 'UCTO_ADMIN', name: 'Admin User' });
  49  |     expect(response.status()).toBe(201);
  50  |   });
  51  | 
  52  |   test('AUTH-06: Should register FOUNDER role successfully (201)', async ({ request }) => {
  53  |     const { response } = await registerUser(request, { role: 'FOUNDER', name: 'Founder User' });
  54  |     expect(response.status()).toBe(201);
  55  |   });
  56  | 
  57  |   // ──── Login ────
  58  | 
  59  |   test('AUTH-07: Should login with valid credentials (200)', async ({ request }) => {
  60  |     const { email, password } = await registerUser(request);
  61  |     const { response, body } = await loginUser(request, email, password);
  62  |     expect(response.status()).toBe(200);
  63  |     expect(body).toHaveProperty('accessToken');
  64  |     expect(body).toHaveProperty('refreshToken');
  65  |     expect(body.user.email).toBe(email);
  66  |   });
  67  | 
  68  |   test('AUTH-08: Should reject login with wrong password (401)', async ({ request }) => {
  69  |     const { email } = await registerUser(request);
  70  |     const res = await request.post(`${BASE}/api/auth/login`, {
  71  |       data: { email, password: 'WrongPassword999!' },
  72  |     });
  73  |     expect(res.status()).toBe(401);
  74  |   });
  75  | 
  76  |   test('AUTH-09: Should reject login for non-existent user (401)', async ({ request }) => {
  77  |     const res = await request.post(`${BASE}/api/auth/login`, {
  78  |       data: { email: 'nonexistent_' + Date.now() + '@test.ucto', password: 'SomePass123!' },
  79  |     });
  80  |     expect(res.status()).toBe(401);
  81  |   });
  82  | 
  83  |   // ──── OAuth ────
  84  | 
  85  |   test('AUTH-10: Should complete OAuth login with Google (200)', async ({ request }) => {
  86  |     const res = await request.post(`${BASE}/api/auth/oauth`, {
  87  |       data: { provider: 'google', token: 'simulated_google_token' },
  88  |     });
  89  |     expect(res.status()).toBe(200);
  90  |     const body = await res.json();
  91  |     expect(body).toHaveProperty('accessToken');
  92  |     expect(body).toHaveProperty('refreshToken');
  93  |     expect(body).toHaveProperty('user');
  94  |     expect(typeof body.accessToken).toBe('string');
  95  |     expect(body.accessToken.length).toBeGreaterThan(0);
  96  |   });
  97  | 
  98  |   test('AUTH-11: Should reject OAuth with missing fields (400/401)', async ({ request }) => {
  99  |     const res = await request.post(`${BASE}/api/auth/oauth`, { data: {} });
  100 |     expect(res.ok()).toBeFalsy();
  101 |   });
  102 | 
  103 |   test('AUTH-12: Should reject OAuth with invalid provider (400/401)', async ({ request }) => {
  104 |     const res = await request.post(`${BASE}/api/auth/oauth`, {
  105 |       data: { provider: 'invalid_provider', token: 'some_token' },
  106 |     });
  107 |     expect(res.ok()).toBeFalsy();
  108 |   });
  109 | 
  110 |   // ──── Token Refresh ────
  111 | 
  112 |   test('AUTH-13: Should refresh tokens with valid refresh token (200)', async ({ request }) => {
  113 |     const { refreshToken } = await registerAndLogin(request);
  114 |     const res = await request.post(`${BASE}/api/auth/refresh`, {
  115 |       data: { refreshToken },
  116 |     });
  117 |     expect(res.status()).toBe(200);
  118 |     const body = await res.json();
  119 |     expect(body).toHaveProperty('accessToken');
  120 |     expect(body).toHaveProperty('refreshToken');
  121 |   });
  122 | 
  123 |   test('AUTH-14: Should reject refresh with invalid token (401)', async ({ request }) => {
  124 |     const res = await request.post(`${BASE}/api/auth/refresh`, {
  125 |       data: { refreshToken: 'invalid_token_12345' },
  126 |     });
> 127 |     expect(res.status()).toBe(401);
      |                          ^ Error: expect(received).toBe(expected) // Object.is equality
  128 |   });
  129 | 
  130 |   // ──── OTP ────
  131 | 
  132 |   test('AUTH-15: Should send OTP to valid phone number (200)', async ({ request }) => {
  133 |     const { accessToken } = await registerAndLogin(request);
  134 |     const res = await request.post(`${BASE}/api/auth/otp/send`, {
  135 |       data: { phoneNumber: '+919876543210' },
  136 |       headers: { Authorization: `Bearer ${accessToken}` },
  137 |     });
  138 |     // OTP sending may fail for real SMS, but should still return 200 with a message
  139 |     const body = await res.json();
  140 |     expect(body).toHaveProperty('message');
  141 |   });
  142 | 
  143 |   test('AUTH-16: Should reject OTP verify with invalid code (400)', async ({ request }) => {
  144 |     const { accessToken } = await registerAndLogin(request);
  145 |     const res = await request.post(`${BASE}/api/auth/otp/verify`, {
  146 |       data: { phoneNumber: '+919876543210', otp: '000000' },
  147 |       headers: { Authorization: `Bearer ${accessToken}` },
  148 |     });
  149 |     expect(res.status()).toBe(400);
  150 |   });
  151 | 
  152 |   // ──── JWT Validation ────
  153 | 
  154 |   test('AUTH-17: JWT should have valid structure (3 parts, exp > iat)', async ({ request }) => {
  155 |     const { accessToken } = await registerAndLogin(request);
  156 |     const parts = accessToken.split('.');
  157 |     expect(parts.length).toBe(3);
  158 |     const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString());
  159 |     expect(payload).toHaveProperty('sub');
  160 |     expect(payload).toHaveProperty('userId');
  161 |     expect(payload).toHaveProperty('role');
  162 |     expect(payload).toHaveProperty('iat');
  163 |     expect(payload).toHaveProperty('exp');
  164 |     expect(payload.exp).toBeGreaterThan(payload.iat);
  165 |   });
  166 | });
  167 | 
```