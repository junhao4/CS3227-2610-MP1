---
name: code-review
description: Review an implemented MoneyMap change after testing and documentation updates; report actionable findings and whether follow-up skills must run.
---

# Code review

Review; do not fix. Inspect the implementation, tests, specifications,
acceptance evidence, User Guide, Developer Guide, diagrams, and Git diff.
Preserve unrelated changes. Do not edit code, tests, docs, specifications,
build files, logs, Git, or GitHub; do not install tools or dependencies.

## Truth and gates

Use this order: verified behaviour/evidence, approved issue and acceptance
criteria, `specs/ProductSpecification.md`, `specs/MP1-requirements.md`, current
guides, then textbook guidance. Project sources define behaviour.

Stop or report `BLOCKED` if the review target, release status, or required
evidence is unavailable. Never call an unavailable check passed. Do not call
unverified behaviour available.

## Workflow

1. Identify the target issue/release, diff, changed files, requirements,
   tests, docs, diagrams, and unrelated changes.
2. Read [checks](references/project-checks.md); discover and run applicable
   configured compilation, tests, smoke checks, static analysis, packaging,
   and resource checks.
3. Read [criteria](references/review-criteria.md). Validate every changed
   requirement against implementation and evidence.
4. Exercise the changed feature as a user: happy path, invalid inputs,
   boundaries, state changes, recovery, persistence, and adjacent workflows.
5. Review code quality, design, architecture, tests, regression protection,
   security where applicable, and maintainability.
6. Compare code and observed behaviour with every affected guide, command,
   example, diagram, error claim, and manual-testing instruction.
7. Read [report](references/review-report.md); record findings, limitations,
   evidence, and rerun decisions. Re-read the report and final diff.

Steps 1–3 are mandatory for a normal feature review. Narrow later steps only
when irrelevant; report what was skipped and why.

## Finding rules

Report only evidence-based findings. Distinguish defects, risks, suggestions,
and unanswered questions. Use:

- `Blocker`: build, test, launch, required acceptance, or safe release fails.
- `High`: likely functional, data-loss, security, serious regression, or major
  architecture defect.
- `Medium`: meaningful robustness, testability, maintainability, or doc defect.
- `Low`: local readability, naming, style, or cleanup issue.

Each finding must contain enough detail to fix and verify it: location,
requirement, evidence, expected/actual behaviour, reproduction or inspection
path, impact, likely cause and confidence, fix direction, files/responsibility,
tests, documentation impact, and post-fix checks.

## Rerun decisions

Report all four explicitly:

- `Rerun code-writing skill: YES` for implementation defects, unmet criteria,
  broken behaviour, data-loss/security risk, serious regression, or code-level
  defensive/design fixes; otherwise `NO`.
- `Rerun update-documentation skill: YES` for stale/missing docs, diagrams,
  commands, examples, or user claims; `AFTER CODE FIX` when code changes first;
  otherwise `NO`.
- `Rerun code-review skill: AFTER FOLLOW-UP WORK` after relevant code, tests, or
  documentation changes; otherwise `NO`.
- `Independent human review: READY` only when no unresolved Blocker/High
  findings remain and limitations are explicit; otherwise `NOT READY` or
  `BLOCKED`.

Do not update docs to describe an unverified fix.
