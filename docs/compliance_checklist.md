# Compliance Checklist

## Purpose
Define the specific DPDP (India) and GDPR (EU) compliance checks that UCTO enforces, which checks are automated vs. manual, and what UCTO assists with vs. what requires human/legal review.

## Important Disclaimer
> UCTO provides compliance *assistance* tools, not legal compliance guarantees. Automated compliance checks reduce risk but do not replace professional legal advice. Customers should consult qualified legal counsel for full compliance certification.

## DPDP (Digital Personal Data Protection Act, India) Checklist

| # | Check | Automated? | Responsible Agent | Evidence Captured |
|---|-------|-----------|-------------------|------------------|
| DPDP-01 | **Consent collection**: User must explicitly consent to data collection before signup | ✅ Automated | Compliance | Consent record in audit_logs |
| DPDP-02 | **Consent withdrawal**: User can withdraw consent and request data deletion | ✅ Automated (UI) | Compliance + BA | Audit log of deletion request |
| DPDP-03 | **Purpose limitation**: Data collected only for stated purpose (app development) | ✅ Automated | Compliance | Purpose statement in project settings |
| DPDP-04 | **Data minimization**: Only required fields collected (name, email, project details) | ✅ Automated | Compliance | Schema validation on entity creation |
| DPDP-05 | **Storage limitation**: User data retained only while account active + 30 days post-deletion | ✅ Automated | Compliance | Deletion scheduler audit log |
| DPDP-06 | **Data security**: Encryption at rest (database) and in transit (TLS) | ✅ Automated | System | Infrastructure config audit |
| DPDP-07 | **Breach notification**: System can detect and log security events | ⚠️ Manual (alert to admin) | Compliance + Admin | Security event log |
| DPDP-08 | **Grievance redressal**: Contact mechanism for user complaints | ⚠️ Manual (admin response) | BA + Admin | Support ticket log |
| DPDP-09 | **Data principal rights**: User can access, correct, delete their data | ✅ Automated (UI) | Compliance + Developer | Data access/deletion audit |
| DPDP-10 | **Consent record keeping**: Explicit record of when/how consent was obtained | ✅ Automated | Compliance | Timestamped consent in audit_logs |
| DPDP-11 | **Notice at collection**: Privacy notice displayed at data collection points | ✅ Automated (UI) | BA + UI/UX | Notice display logged |
| DPDP-12 | **Children's data**: Age verification (18+) for standalone accounts | ⚠️ Manual (declaration) | Compliance | Age declaration in signup |

## GDPR (General Data Protection Regulation, EU) Checklist

| # | Check | Automated? | Responsible Agent | Evidence Captured |
|---|-------|-----------|-------------------|------------------|
| GDPR-01 | **Lawful basis for processing**: Consent obtained, documented | ✅ Automated | Compliance | Consent audit log |
| GDPR-02 | **Right to be informed**: Privacy notice at data collection points | ✅ Automated (UI) | BA + UI/UX | Notice displayed logged |
| GDPR-03 | **Right of access**: User can export all personal data | ✅ Automated (UI) | Developer | Data export audit log |
| GDPR-04 | **Right to rectification**: User can correct inaccurate data | ✅ Automated (UI) | Developer | Correction audit log |
| GDPR-05 | **Right to erasure (right to be forgotten)**: Full account deletion | ✅ Automated | Compliance + Developer | Deletion audit log |
| GDPR-06 | **Right to restrict processing**: User can pause processing (freeze account) | ✅ Automated | Compliance | Processing restriction audit log |
| GDPR-07 | **Right to data portability**: Data export in machine-readable format (JSON) | ✅ Automated | Developer | Portability export log |
| GDPR-08 | **Right to object**: User can object to processing for specific purposes | ⚠️ Manual (BA reviews) | BA + Compliance | Objection record log |
| GDPR-09 | **Rights related to automated decision-making**: Users can request human review | ⚠️ Manual (admin review) | BA + Admin | Human review request log |
| GDPR-10 | **Data Processing Agreement (DPA)**: Documented with sub-processors | ⚠️ Manual (legal) | BA + Admin | DPA stored in artifacts |
| GDPR-11 | **Cross-border transfer**: Data stored in region-compliant infrastructure | ⚠️ Manual (infra config) | Solutions Architect | Infrastructure region config |
| GDPR-12 | **Data Protection Impact Assessment (DPIA)**: Assess risks of processing | ⚠️ Manual (admin) | Solutions Architect + Admin | DPIA document stored |

## Checks Not Yet Automated (Marked Manual)
The following checks require human or legal intervention and are **not automated by UCTO agents**:
- Breach notification to data protection authorities (DPDP-07 / GDPR gap)
- Grievance redressal (DPDP-08)
- Data Processing Agreements (GDPR-10)
- Data Protection Impact Assessment (GDPR-12)
- Cross-border transfer compliance (GDPR-11)
- Age verification beyond user declaration (DPDP-12)

## Enforcement Actions

| Check Result | Action |
|-------------|--------|
| All automated checks pass | Sprint proceeds normally |
| Any automated check fails | Sprint enters COMPLIANCE_CHECK block; detailed failure report provided |
| Manual check not yet completed | Warning shown on dashboard; does not block development but blocks deployment |
| Critical compliance failure (e.g., no consent record) | Sprint blocked; BA notified; escalation to UCTO Admin |

## UCTO's Role vs. Customer's Legal Responsibility

| UCTO Provides | Customer Must Provide |
|--------------|---------------------|
| Consent collection UI and audit trail | Review and approve legal wording of consent notices |
| Data deletion automation | Comply with deletion requests within legal timelines |
| Encryption and access controls | Ensure their usage of UCTO complies with their own regulatory obligations |
| Compliance check execution and evidence | Conduct their own DPIA and DPA with sub-processors |
| Audit log of all actions | Retain records for their own compliance reporting |
| Privacy policy template | Customize privacy policy for their specific use case |
