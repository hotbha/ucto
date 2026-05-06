# Screen Review Workflow

## Screen Generation
- Input: Requirements clarified by BA
- UI/UX Expert Agent uses generative AI to create:
  - Wireframes (low-fidelity)
  - Mockups (high-fidelity)
  - Design specs (JSON/YAML)
- **For MVP**: Generate for English only. Localization is Phase 2.

## Screen Presentation
- Portal integration: "Your App Screens" tab
- Screens linked to requirements
- Interactive preview embedded (Flutter web)
- Customer can view, comment, approve, or request changes

## Approval Quorum and Authority
- **Voting authority**: Customer (Founder role) only
- **BA role**: Facilitates the review process, presents screens, communicates feedback — but does not approve
- **Single reviewer**: For MVP, the project Founder is the sole approver
- **Phase 2**: Multi-signer approval (multiple Founders)

## Rejection Granularity
- **Per-screen granularity**: Customer can approve, reject, or request changes on **individual screens** within a batch
- **Batch-level gate**: Development starts only when ALL screens in a batch reach FINAL_APPROVED
- **Percentage-based gate**: If ≥80% of screens in a batch are FINAL_APPROVED and <20% are REJECTED, the batch can proceed. Rejected screens are re-generated as a follow-up mini-batch

## Revision Cycle Limits
- **Maximum 3 revision rounds** per screen (CHANGES_REQUESTED → SCREENS_GENERATED)
- After round 3: screen auto-escalates to UCTO Admin for resolution
- **Time limit**: Customer has 7 calendar days to review. After 7 days, BA may auto-approve or escalate.

## Feedback Loop
- Customer feedback → BA
- BA updates BRD, UCD, TCD, UI/UX specs, tech stack automatically
- Audit logs record every change

## Screen Status State Machine

```
DRAFT → SCREENS_GENERATED → SUBMITTED_FOR_REVIEW → IN_REVIEW → APPROVED
                                                              → REJECTED
                                                              → CHANGES_REQUESTED → SCREENS_GENERATED (max 3 rounds)
                                                                                                    → ESCALATED (after 3rd round)

APPROVED (all screens in batch) → FINAL_APPROVED → Development begins
```

See [state_machines.md](state_machines.md) for the complete formal state machine with all transitions and actors.

## Approval Gates
| Gate | Check | Who Validates | When |
|------|-------|--------------|------|
| **Gate 1** | Screens approved by customer | Customer (Founder) | Before development |
| **Gate 2** | Privacy and accessibility compliance | Compliance Agent | After Gate 1 |
| **Gate 3** | UI flows match approved screens | Tester Agent | After Gate 2 |

All three gates must pass for FINAL_APPROVED status.

## Invalid Transitions
- DRAFT → APPROVED (screens must be generated and submitted first)
- SCREENS_GENERATED → IN_REVIEW (BA must submit first)
- Any → FINAL_APPROVED without Compliance and Tester passing

[← Back to README](README.md) | Related: [agent_guidelines.md](agent_guidelines.md), [ucto_playbook.md](ucto_playbook.md), [state_machines.md](state_machines.md)
