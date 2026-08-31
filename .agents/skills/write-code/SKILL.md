---
name: write-code
description: Implement a scoped repository issue or code change with focused test design and verification. Use for feature or bug implementation, not documentation, review, logging, commits, pushes, or GitHub updates.
---

# Write code

Implement the smallest complete slice that satisfies the approved request.

## Context-specific guidance

Read only the reference that applies:

- [Java coding standard](references/java-coding-standard.md) for Java changes;
- [testing](references/testing.md) when adding or changing tests;
- [design](references/design.md) when changing package, domain, service,
  persistence, UI, or presentation boundaries.

## Before editing

1. Read repository instructions and the applicable issue, specification, relevant
   code, and existing tests.
2. State the requested scope, explicit non-goals, assumptions, ambiguities, and
   any requirement conflict that needs user direction.
3. Derive suitable valid, invalid, boundary, error, and state-transition cases.
4. Choose the appropriate test level: unit, integration, smoke, or manual.
5. State the focused and full verification commands.

Use Red–Green–Refactor when it materially improves confidence:

- add or adjust a focused test that demonstrates the missing behaviour;
- run it and preserve the Red result where feasible;
- implement the smallest behaviour that makes it Green;
- refactor only when it improves clarity without expanding scope.

If the user explicitly asks to approve a plan before editing, wait for that
approval. Otherwise, proceed after identifying the scope and test approach.

## Implementation

- Keep product behaviour defined by the issue and specification; do not infer
  new features from past issues, logs, prototypes, or examples.
- Preserve unrelated working-tree changes.
- Do not add dependencies without approval.
- Keep validation, persistence, UI, and presentation responsibilities separated
  when the existing architecture supports that separation.
- Add or update tests at the level selected above. Do not substitute structural
  smoke evidence for behaviour it does not exercise.

## Verification and handoff

Run focused checks first, then the configured full regression checks. Also run
relevant format, resource, package, or static-analysis checks when available.

Report:

- implementation and tests changed;
- Red–Green–Refactor evidence, or why it did not apply;
- commands and observed results;
- manual checks still required;
- assumptions, limitations, and unverified platform behaviour.

## Out of scope

Do not update documentation, independently review, write interaction logs,
commit, push, tag releases, or modify GitHub unless the user separately asks.
