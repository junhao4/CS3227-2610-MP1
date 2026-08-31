Date: 2026-08-31
Time: 09:30:28 +08
Timestamp source: Current Singapore local time when the log draft was created;
the exact time of the initial Issue #4 prompt is unavailable in the conversation
record.
Task: Create and apply a reusable `write-code` skill, modelled on the completed
Issue #3 interaction workflow, to implement GitHub Issue #4 transaction-history
filtering and note search; then document and review the result.
Status: ongoing
Interaction range: Request to derive `write-code` from the Issue #3 log –
Issue #4 implementation, documentation update, review fixes, and final
documentation-count correction
Evidence sources: Conversation history, Issue #3 log, `write-code` skill,
GitHub Issue #4 review evidence, Product Specification, implementation/tests,
guides, review report, Gradle results, and Git checks
Related logs: `logs/2026-08-30-211654-issue-3-zero-shot-tdd.md`
Human verification: approved

## Interaction history

### 1. Reusable workflow derived from Issue #3

- Exact prompt excerpt: “Read the completed Issue #3 interaction log and
  implementation prompt as a model for workflow structure, not product
  behaviour. Create a terse reusable `write-code` skill.”
- AI response: Proposed a skill that retrieves the scoped issue, specification,
  relevant code and tests; states scope and ambiguity; designs tests and edge
  cases; uses Red–Green–Refactor where it materially fits; implements the
  smallest complete slice; runs focused and full checks; and reports evidence
  and limitations.
- User correction or decision: The user approved creation of the skill, then
  invoked it for “the next issue”.
- Resulting action: Created `.agents/skills/write-code/` with the reusable
  workflow and focused supporting references. The skill deliberately generalises
  Issue #3's process rather than copying transaction-specific requirements.

### 2. Issue #4 structured prompting and TDD workflow

- Faithful prompt paraphrase: The user used `$write-code` for the next GitHub
  issue, which was Issue #4: transaction-history review, filters, and note
  search.
- AI response: Applied the reusable workflow: read the issue, product
  specification, relevant production code and tests, identified scope and
  ambiguities, designed focused service and JavaFX smoke tests, presented the
  proposed Red–Green–Refactor order, and waited for approval before editing.
- User correction or decision: The user approved implementation and later
  clarified that documentation should be handled separately.
- Resulting action: Implemented list-first transaction-history filtering by
  month, type, category, and case-insensitive note text; combined filters;
  newest-first ordering; clear-filter behaviour; and a visible no-results
  state. The implementation preserved Issue #3's form, validation,
  persistence, accessibility-related labels, and progressive disclosure.

### 3. Review feedback improved both code and the review skill

- Exact prompt excerpts: “does code review not read the textbook? u missed the
  120 line and javadoc rules” and “ok add that to the skill as shortly as
  possible”.
- AI response: Confirmed that the prior review had missed Java coding-standard
  checks. Proposed a terse code-review skill addition requiring the routed Java
  standard, a changed-file 120-character scan, and inspection of changed public
  APIs, non-trivial private methods, and class responsibilities for concise
  Javadocs.
- User correction or decision: The user approved the skill improvement and
  instructed the agent to fix Issue #4.
- Resulting action: Updated the review skill and fixed the Issue #4 findings:
  duplicate `Uncategorised` category labels are type-qualified only where
  necessary; month selection is covered in the JavaFX smoke test; changed Java
  lines meet the 120-character limit; and required Javadocs were added.

### 4. Documentation correction after verification

- Faithful prompt paraphrase: The user asked whether documentation needed
  changing, then invoked `$update-documentation` and requested a check of only
  that change instead of a full repeat review.
- AI response: Initially concluded that user-facing documentation did not need
  exact fallback-label wording. During the rerun review, however, the agent
  found that the Developer Guide said the current JUnit suite had 49 tests
  while the verified suite had 50.
- User correction or decision: The user approved the documentation skill and
  requested only focused verification of the count correction.
- Resulting action: Updated both Developer Guide test-count references from
  49 to 50. Confirmed that no `49 JUnit tests` statement remains, the current
  test-result XML totals 50 tests, and `git diff --check` passes.

## Work and verification

- Proposed, approved, and executed actions: The user approved creation of the
  reusable skill, Issue #4 implementation after the up-front plan, fixes for
  review findings, and the documentation correction. No dependency was added.
  No commit, push, or GitHub update has been performed in this interaction
  range.
- Files or external systems changed: `.agents/skills/write-code/`, the
  code-review skill and report template, transaction controller/service,
  transaction FXML and stylesheet, focused service tests, JavaFX smoke tests,
  User Guide, and Developer Guide. The local Issue #4 review report was
  updated but remains ignored under `reviews/`. GitHub Issue #4 was read; no
  GitHub state was changed.
- Checks and observed results:
  - The intended Red smoke-test evidence initially failed because the Income
    fallback category label was not type-qualified; it passed after the
    converter change.
  - Focused `TransactionServiceTest` passed.
  - `./gradlew verifyTransactionUi` passed, verifying creation, history
    filtering, search, fallback assignment, validation, persistence, reload,
    empty state, and filter reset.
  - `./gradlew check`, `./gradlew javadoc`, `./gradlew shadowJar`, and
    `./gradlew verifyPrototypes` passed.
  - `git diff --check` passed.
  - The corrected scan over all changed Java files found no line over
    120 characters.
  - Complete test-result XML totals 50 JUnit tests; the two Developer Guide
    count claims now both state 50.
- Errors, limitations, or remaining uncertainty:
  - The first coding-standard scan command was malformed because multiple
    changed filenames were passed to `awk` as one argument. It was recognised
    and replaced with a per-file scan before the review result was recorded.
  - Automated JavaFX smoke tests do not prove complete visual layout,
    screen-reader behaviour, visible focus, or native keyboard interaction on
    every platform.
  - GitHub could not be refreshed during the final documentation task because
    the network connection was unavailable; this did not affect the local
    evidence-backed documentation correction.
  - The feature and documentation changes are uncommitted at the end of this
    log's interaction range.

## Reflection notes

- What the AI did well or poorly: Converting Issue #3's workflow into a
  reusable skill made the analysis, test design, implementation order, and
  verification expectations explicit for Issue #4. It also supported targeted
  Red–Green–Refactor evidence for the fallback-label defect. However, the
  initial code review still missed the Java 120-character and Javadoc rules;
  the user identified that omission. The agent also initially overlooked the
  Developer Guide's stale test count and only found it during the later review.
- Human judgement required: The user chose to retain the Issue #3
  list-first/progressive-disclosure interaction direction, decided that the
  Issue #3 workflow should become reusable rather than be repeated manually,
  required concise skills, identified review omissions, controlled when
  documentation should be updated, and limited the final documentation check
  to the changed claim.
- How the prompts or approach evolved: The starting approach was a detailed,
  issue-specific zero-shot structured multi-step prompt for Issue #3. It was
  generalised into a terse `write-code` skill for future scoped issues. Review
  feedback then added an explicit coding-standard gate to `code-review`,
  reducing reliance on an implicit assumption that the reviewer would inspect
  all course rules.
- Prompting versus manual work: Structured prompting was useful for making
  acceptance criteria, equivalence partitions, boundary cases, test levels,
  and Red–Green–Refactor order visible before code changed. Manual judgement
  remained essential for selecting the UI interaction model, recognising
  incomplete review coverage, and deciding the appropriate scope of follow-up
  documentation work.
- What to do differently next time: Keep the reusable skill concise, but make
  required quality gates mechanically checkable. For every review, run the
  changed-file line-length and Javadoc checks before declaring findings
  complete. For documentation, avoid hard-coded test counts where practical,
  or update every occurrence in the same change. Maintain an explicit
  distinction between transparent decision/test evidence and private model
  reasoning.
