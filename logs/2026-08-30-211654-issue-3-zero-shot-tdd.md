Date: 2026-08-30
Time: 21:16:54 +08
Timestamp source: Current Singapore local time when the log draft was created;
the exact original Issue #3 prompt time is unavailable in the conversation record.
Task: Implement GitHub Issue #3 — Record income and expense transactions — with
zero-shot structured multi-step Chain-of-Thought prompting and TDD, then address
review findings.
Status: completed
Interaction range: Initial Issue #3 implementation prompt – Issue #3 follow-up
fixes and final verification
Evidence sources: Conversation history, GitHub Issue #3, Product Specification,
implementation/tests/guides, review report, Gradle results, and Git checks
Related logs: none
Human verification: approved

## Interaction history

### 1. Structured zero-shot Chain-of-Thought and TDD request

- Exact prompt excerpt: “Implement GitHub issue #3 only using zero-shot
  structured multi-step Chain-of-Thought prompting and TDD.” The prompt required
  reading the issue, specifications, existing code/tests, and course guidance;
  mapping acceptance criteria; deriving partitions and boundaries; choosing test
  levels; producing a requirements-to-tests table and Red–Green–Refactor order;
  then waiting for approval before editing.
- AI response: Read the requested sources and presented the requested analysis,
  test plan, implementation order, and verification commands before editing.
- User correction or decision: The user clarified that documentation would be
  done later, then approved implementation.
- Resulting action: Implemented Issue #3 transaction creation using TDD:
  validation, typed starter categories and `Uncategorised` fallbacks, service
  orchestration, application-relative versioned JSON persistence, Transactions
  FXML/controller behavior, tests, and JavaFX smoke checks.

### 2. UI direction and progressive disclosure

- Faithful prompt paraphrase: The user asked to restore the selected
  Transactions A list-first hierarchy while preserving real Issue #3 behavior,
  then preferred Add transaction to progressively disclose the form rather than
  keeping it permanently visible.
- AI response: Proposed and implemented a list-first Transaction screen with a
  hidden form revealed by **＋ Add transaction**.
- User correction or decision: The user confirmed that interaction direction
  and later manually verified the Issue #3 macOS keyboard workflow.
- Resulting action: Retained real validation, persistence, labels, category
  compatibility, focus transitions, and list-first/progressive-disclosure UI.

### 3. Initial review and persistence recovery

- Faithful prompt paraphrase: The user requested a code review and then asked
  to fix its findings.
- AI response: The first review found `CR-3-001`: an interruption after the
  first temporary JSON write could delete the only saved transaction on startup.
- User correction or decision: The user approved the fix while deferring
  documentation until later.
- Resulting action: Added Red tests, then recovered valid no-main temporary
  JSON into the main data file and preserved invalid temporary JSON as corrupt
  content. Focused and full verification passed.

### 4. User-added review findings the initial review missed

- Exact prompt excerpt: “nope there are i added stuff”.
- AI response: Inspected the updated review report and found the user had added
  `CR-3-003` through `CR-3-006`.
- User correction or decision: The user had to expand the review because the
  initial assistant review failed to identify invalid persisted-date recovery,
  four 120-character Java-line violations, the missing `/data/` ignore rule,
  and missing private-method header comments.
- Resulting action: Added Red tests reproducing invalid-date failures; handled
  `DateTimeException` as recoverable persisted data; added main and temporary
  invalid-date regression tests; wrapped long lines; ignored `/data/` without
  inspecting or deleting it; added concise private-method contracts; updated the
  Developer Guide to 46 tests; and marked all review findings fixed.

## Work and verification

- Proposed, approved, and executed actions: The user approved implementation
  after up-front analysis, approved the review fixes, and approved this log.
  The work did not add dependencies, commit, push, or modify GitHub.
- Files or external systems changed: Production Java/JavaFX code, tests, smoke
  tests, build configuration, User Guide, Developer Guide, `.gitignore`, and
  `reviews/issue-3.md`. GitHub Issue #3 was read but not changed. The local
  `data/` runtime directory was not inspected, changed, or deleted.
- Checks and observed results:
  - Focused persistence tests failed first for the intended Red cases and
    passed after implementation.
  - `./gradlew clean check build verifyPrototypes javadoc --console=plain`
    passed after the final fixes, including all 46 JUnit tests, production and
    Transactions UI smoke checks, packaging, prototype loading, and Javadoc.
  - `git diff --check` passed.
  - A source-wide Java line-length scan found no line over 120 characters.
  - `git check-ignore -v data` confirmed `/data/` is ignored.
- Errors, limitations, or remaining uncertainty: JavaFX smoke checks cannot
  prove visual layout, screen-reader behavior, visible focus, or native keyboard
  interaction on every platform. The keyboard workflow was manually confirmed
  on macOS; Windows and Linux remain unverified. Static analysis and coverage
  reporting are not configured. The feature is still uncommitted and untagged.

## Reflection notes

- What the AI did well or poorly: The structured prompt constrained work to
  Issue #3 requirements and made test design explicit. TDD exposed two
  persistence defects through focused failures. However, the initial review was
  incomplete: the user had to add `CR-3-003` through `CR-3-006` because the AI
  missed the invalid-date recovery hole, style-limit violations, missing
  runtime-data ignore rule, and missing private-method comments. This was a
  review-quality gap requiring user intervention, not merely extra feedback.
- Human judgement required: The user chose progressive disclosure, manually
  verified macOS keyboard behavior, deferred then approved documentation work,
  and provided essential review oversight by identifying omissions.
- How the prompts or approach evolved: The initial prompt required analysis
  before editing. Later prompts refined UI interaction, documentation timing,
  review scope, and repository hygiene. Review findings became explicit Red
  tests and verified behavior.
- Prompting versus manual work: Prompting helped derive partitions, test levels,
  and incremental implementation. Manual testing remained necessary for macOS
  keyboard behavior and UI preference. Independent review and direct probes
  were necessary to find malformed-date recovery outside the initial test set.
- What to do differently next time: Include malformed values for every
  persisted primitive—dates, IDs, enums, amounts, and relationships—in the
  initial persistence matrix. Run a coding-standard line-length scan and inspect
  `.gitignore` before the first review. Use an explicit pre-review checklist and
  do not declare a review clean until every checklist result is inspected.
