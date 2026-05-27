// @ts-check
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: '.',
  testMatch: ['*.spec.js', '*.test.js'],
  timeout: 30000,
  expect: {
    timeout: 10000,
  },
  use: {
    baseURL: 'http://localhost:8080',
    extraHTTPHeaders: {
      'Content-Type': 'application/json',
    },
  },
  reporter: [
    ['list'],
    ['json', { outputFile: 'test-results.json' }],
  ],
  // Run 1 worker at a time to avoid rate limiting (429)
  workers: 1,
  retries: 2,
  maxFailures: 0,
});
