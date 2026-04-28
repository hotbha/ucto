# Portal & CLI Design

## Web Portal
- Dashboard: Requirements → Agents → Outputs.
  - Single source of truth with status cards for Requirements, Design, Development, Compliance, Deployment.
  - Agent outputs: AI-generated screen mockups, editable requirements, traceability links, approval buttons.
  - For each output: Include a "What this means" explanation in plain language for non-technical users.
- Tier-specific UI: Free, Startup, Growth, Enterprise.
  - Free: Basic requirements capture, 1 project, limited agent runs.
  - Startup: Full BA agent, screen generation, basic audit logs.
  - Growth: All agents, compliance checks, advanced audit filters.
  - Enterprise: Custom integrations, priority support, full traceability matrix.
- Onboarding Flow:
  1. Signup/Login via Google/Facebook or email.
  2. Project creation with requirements input.
  3. BA interrogation and clarification.
  4. Screen preview and approval with feedback capture.
  5. Development, testing, deployment.
- Zoho Integration: SMTP + SMS OTP.
- Audit Logs: Visual timeline with filters for agent type, requirement changes, test results. Sensitive data masked for compliance.
- Solutions Architect recommendations surfaced for founders.
- Help text/tooltips: Explain agents, traceability, and why BA is the communication channel.

## VS Code Extension
### Commands
- `UCTO: Init Project`
- `UCTO: Run Sprint`
- `UCTO: Trigger Agent`
- `UCTO: Deploy`

### Features
- Integrated terminal CLI (`ucto init`, `ucto sprint`, `ucto deploy`).
- Traceability Panel: Side panel showing audit logs + outputs.
- CI/CD Hooks: Auto-generate GitHub Actions YAML.

## Manifest
See `package.json` for extension metadata and commands.
# Portal & CLI Design

[← Back to README](README.md) | Related: [ucto_playbook.md](ucto_playbook.md), [traceability_matrix.md](traceability_matrix.md)

