# Usage Metering & Tier Enforcement Design

## Purpose
Define how "agent runs" are counted, billed, and enforced across UCTO subscription tiers.

## Definition of "Agent Run"

**An "agent run" is counted each time the orchestration service publishes a `trigger` event to any agent topic.** The run is considered consumed regardless of whether the agent completes successfully or fails.

### What Increments Usage
| Action | Counts as Run? | Notes |
|--------|---------------|-------|
| BA agent triggered for clarification | Yes | 1 run per trigger |
| Developer agent triggered for code generation | Yes | 1 run per trigger |
| UI/UX agent triggered for screen generation | Yes | 1 run per trigger |
| Tester agent triggered for validation | Yes | 1 run per trigger |
| Compliance agent triggered for check | Yes | 1 run per trigger |
| Architect agent triggered for review | Yes | 1 run per trigger |

### What Does NOT Increment Usage
| Action | Counts as Run? | Notes |
|--------|---------------|-------|
| Customer login | No | Auth is free |
| BA presenting screens to customer | No | Presentation is not a trigger event |
| Customer approving/rejecting screens | No | Customer action, not agent trigger |
| Viewing dashboard / audit logs | No | Read operations |
| Password reset / OTP send | No | Auth operations |
| Sprint Review | No | Ceremony, not agent work |
| Retrospective | No | Ceremony, not agent work |

### Clarifying Example
A single sprint that goes through:
1. BA clarification (1 run) → 
2. UI/UX screen generation (1 run) → 
3. Compliance check for screens (1 run) → 
4. Developer code generation (1 run) → 
5. Compliance check for code (1 run) → 
6. Tester validation (1 run) → 
7. Architect review (1 run)

**Total: 7 agent runs** for that sprint.

## Tier Counters

### Configuration
```yaml
tiers:
  free:
    agent_runs_per_month: 5
    max_projects: 1
    features: [ba_agent, screen_generation]
  startup:
    agent_runs_per_month: 50
    max_projects: 5
    features: [all_agents, basic_audit]
  growth:
    agent_runs_per_month: 200
    max_projects: 50
    features: [compliance_checks, advanced_audit]
  enterprise:
    agent_runs_per_month: -1  # unlimited
    max_projects: -1           # unlimited
    features: [custom_integrations, priority_support]
```

### Counter Model
```java
// UsageCounter entity
{
  "projectId": "proj_xyz",
  "tier": "free",
  "agentRunsUsedThisMonth": 3,
  "agentRunsRemaining": 2,
  "monthlyResetDate": "2026-05-01T00:00:00Z",
  "lastRunTimestamp": "2026-05-05T10:00:00Z"
}
```

### Monthly Reset Behavior
- All counters reset to 0 on the 1st of each month at 00:00 UTC
- Enterprise tier: checked annually (or manually configured)
- If user upgrades mid-month: new tier limits apply immediately, existing run count carries over
- If user downgrades mid-month: runs exceeding the new tier limit are blocked, but data remains accessible

## Enforcement Behavior at Limit Exhaustion

| Scenario | Behavior |
|----------|----------|
| Free tier hits 5/5 runs | **Graceful degradation.** All data remains visible. New agent triggers blocked. Upgrade CTA displayed on dashboard. Read-only access maintained. |
| Startup hits 50/50 runs | Same as above. All agent triggers blocked. Existing code, screens, logs remain viewable. |
| Growth hits 200/200 runs | Same as above. Upgrade to Enterprise prompted. |
| Enterprise (unlimited) | Never blocked. |

### Graceful Degradation Rules
1. User can still log in and access all projects
2. User can view generated screens, code, audit logs, test results
3. User cannot trigger any new agent (any `/api/agent/trigger` returns 402 Payment Required)
4. Dashboard shows: "You've used all N agent runs this month. Upgrade to continue building."
5. All existing API endpoints for read operations return 200
6. POST/PUT endpoints for agent triggers return 402
7. Existing subscriptions continue to deliver as configured

### Upgrade CTA Timing
| Agent Runs Used | CTA |
|----------------|-----|
| 3/5 (60%) | Subtle banner: "You've used 60% of your runs this month" |
| 5/5 (100%) | Prominent upgrade prompt on all pages |
| At trigger block | Full-screen interstitial with tier comparison |

## Audit Events for Metering
Every usage increment generates an audit event:
```json
{
  "eventType": "usage.run_consumed",
  "projectId": "proj_xyz",
  "agentType": "developer",
  "agentRunCount": 4,
  "tierLimit": 5,
  "remaining": 1,
  "userId": "user_abc",
  "timestamp": "2026-05-05T10:00:00Z"
}
```

## Dashboard / Reporting Fields

### Current Month View
| Field | Source |
|-------|--------|
| Tier | Subscription table |
| Agent runs used | UsageCounter.agentRunsUsedThisMonth |
| Agent runs remaining | Tier limit - used |
| Runs by agent type | Aggregated from audit_logs |
| Runs by project | Aggregated from audit_logs |
| Days until reset | Next 1st of month |

### Historical View (Startup+)
| Field | Source |
|-------|--------|
| Month | audit_logs timestamp truncated to month |
| Total runs | COUNT(*) WHERE eventType = 'usage.run_consumed' |
| Runs by agent type | GROUP BY agentType |
| Cost estimate | Runs × cost per run |

## Sample Formulas for Per-Agent-Run Cost

| Agent Type | Avg LLM Tokens | Cost per Run (USD) | Notes |
|-----------|---------------|-------------------|-------|
| BA | 2,000 (input) + 500 (output) | ~$0.005–$0.01 | Low complexity |
| Developer | 4,000 (input) + 2,000 (output) | ~$0.02–$0.05 | Code generation is token-heavy |
| Tester | 3,000 (input) + 1,000 (output) | ~$0.01–$0.03 | Moderate |
| Compliance | 2,000 (input) + 500 (output) | ~$0.005–$0.01 | Check-based |
| UI/UX | 3,000 (input) + 1,500 (output) | ~$0.015–$0.04 | Design generation |
| Solutions Architect | 2,000 (input) + 800 (output) | ~$0.01–$0.02 | Moderate |

**Average cost per agent run: ~$0.015–$0.03 USD** (assuming GPT-4-class model)

**Monthly cost estimate:**
- Free (5 runs): ~$0.075–$0.15 (subsidized)
- Startup (50 runs): ~$0.75–$1.50
- Growth (200 runs): ~$3.00–$6.00
- Enterprise (500 runs): ~$7.50–$15.00

These costs are well within gross margin targets documented in `costs.md`.
