Date: 2026-08-31
Time: 12:25:49 +0800
Timestamp source: Repository machine local clock when this log draft was
prepared; the exact beginning of the Issue #7 interaction was not separately
captured.
Task: Implement, document, review, and refine GitHub Issue #7 safe category
deletion and reassignment, including progressive disclosure for category
management.
Status: completed
Interaction range: “[$implement] issue 7” – logging request after the approved
Manage-dialog refinement and final review
Evidence sources: Conversation; GitHub CLI issue and dependency checks; Git
diff; Issue #7 review report; Gradle test, Checkstyle, JavaFX smoke-test,
Javadoc, packaging, and resource-check output
Related logs:
- logs/2026-08-31-115254-issue-6-category-lifecycle-and-review-handoff.md
- logs/2026-08-31-101500-issue-5-custom-categories.md
Human verification: approved

## Interaction history

### 1. Retrieve and scope Issue #7

- Exact prompt excerpt: “[$implement] issue 7”
- AI response: Loaded the implementation workflow, checked the configured
  GitHub CLI independently, and retrieved Issue #7, “S6 — Safely delete and
  reassign categories.”
- User correction or decision: No scope correction was needed for the Issue #7
  acceptance criteria. The issue required permanent deletion only for unused
  ordinary categories, reassignment before deleting used categories, protected
  `Uncategorised` fallbacks, persistence, accessible controls, documentation,
  and focused tests.
- Resulting action: Confirmed that Issue #7 was open and `status:ready`, its
  only native blocker was closed Issue #3, and it directly blocked open Issue
  #11. `gh auth status` reported an invalid token while `gh api user`, issue
  retrieval, and GraphQL dependency retrieval succeeded; the failure was
  reported as specific to the status check rather than incorrectly treated as a
  general GitHub-access failure.

### 2. Implement safe category deletion and reassignment

- Exact prompt excerpt or labelled faithful paraphrase: The approved Issue #7
  request to implement permanent deletion for unused categories and
  reassignment before deletion for used categories.
- AI response: Proposed preserving category identity and atomically replacing
  transaction category references before allowing deletion. The UI initially
  exposed Rename, Archive/Restore, Reassign, and Delete actions directly on
  ordinary category rows.
- User correction or decision: No separate product-scope change was requested
  at this stage.
- Resulting action:
  - Added immutable state transitions to delete an unused category and reassign
    every transaction from one category to another.
  - Added service validation for used-category deletion, same-category
    reassignment, type mismatch, archived reassignment targets, and fallback
    source protection.
  - Added confirmed Reassign and Delete JavaFX flows with text feedback.
  - Added service, persistence, and JavaFX smoke coverage for successful and
    blocked flows.

### 3. Use red–green tests and fix review findings

- AI response: Added focused tests before the corresponding service and domain
  methods existed. The first focused service run failed to compile because
  `deleteCategory` and `reassignTransactions` were intentionally absent. After
  implementation, the focused service, persistence, and JavaFX checks passed.
- User correction or decision: No correction was required.
- Resulting action: The independent review found that the public
  `ApplicationState.withoutCategory` method could delete an `Uncategorised`
  fallback if called directly, even though the service protected it. A direct
  domain test was added first and failed as expected. The state model was then
  changed to reject permanent fallback deletion. The focused domain test and
  full verification passed afterwards.

### 4. Update documentation and correct stale claims

- AI response: Updated the User Guide, Developer Guide, manual-testing
  instructions, and the canonical `reviews/issue-7.md` report to describe
  deletion, reassignment, fallback protection, persistence, and verification.
- User correction or decision: No direct correction was needed initially.
- Resulting action: A later review found two documentation inconsistencies:
  an older User Guide paragraph still described direct Rename/Archive/Restore
  actions, and the Developer Guide contained conflicting 49- and 62-test
  counts. The follow-up documentation pass corrected the workflow wording and
  made both count references accurately state 49.

### 5. Plan and approve progressive disclosure

- Exact prompt excerpt: “i feel like the ui is getting very cluttered, can we
  use some progressive disclosure for the ui here, tell me your plan first”
- AI response: Identified that four inline lifecycle controls per category row
  created visual clutter. Proposed retaining a simple row with one **Manage**
  button and revealing state-appropriate actions in a per-category dialog,
  rather than adding a new application page or navigation destination.
- User correction or decision: “ok do it”
- Resulting action: Replaced inline Rename, Archive/Restore, Reassign, and
  Delete buttons with a single labelled **Manage** button. The management dialog
  now:
  - offers Rename, Archive/Restore, and Delete for unused ordinary categories;
  - offers Rename, Archive/Restore, Reassign, and disabled Delete with a
    reassignment explanation for used ordinary categories;
  - presents Restore for archived categories where appropriate; and
  - shows a protected-state explanation rather than mutable controls for
    `Uncategorised`.

### 6. Test, document, and independently review the refinement

- AI response: Updated the JavaFX smoke test to navigate the real Manage dialog
  and its nested choice/confirmation dialogs. The first automation attempt had
  a dialog-timing problem: it attempted to accept a dialog before JavaFX had
  opened it. The test sequencing was adjusted to schedule each acceptance on
  the appropriate later event-loop turn.
- User correction or decision: No direct correction was required after the
  successful retest.
- Resulting action:
  - `verifyCategoryUi` passed with actual Manage-dialog actions, disabled
    used-category deletion, protected fallback explanation, reassignment, and
    deletion after reassignment.
  - User and Developer Guides were updated to use a Manage-first workflow.
  - The final targeted review marked both review findings fixed and reported
    overall `PASS`.

## Work and verification

- Proposed, approved, and executed actions:
  - Implemented Issue #7 deletion/reassignment behaviour across immutable
    state, service, persistence tests, JavaFX UI, and documentation.
  - Added domain-level fallback protection after independent review exposed the
    service-only guard.
  - Proposed a Manage-dialog progressive-disclosure design; the user explicitly
    approved it before the UI refinement was implemented.
  - Updated the canonical Issue #7 review report after each follow-up.
  - Did not add dependencies, commit, push, write a GitHub update, close the
    issue, or change issue labels.
- Files or external systems changed:
  - `src/main/java/cs3227/moneymap/domain/ApplicationState.java`
  - `src/main/java/cs3227/moneymap/service/TransactionService.java`
  - `src/main/java/cs3227/moneymap/CategoryController.java`
  - `src/test/java/cs3227/moneymap/domain/ApplicationStateTest.java`
  - `src/test/java/cs3227/moneymap/service/TransactionServiceTest.java`
  - `src/test/java/cs3227/moneymap/persistence/JsonDataRepositoryTest.java`
  - `src/smoke/java/cs3227/moneymap/CategoryUiSmokeTest.java`
  - `docs/UserGuide.md`
  - `docs/DeveloperGuide.md`
  - `reviews/issue-7.md`
  - GitHub was queried only; no GitHub state was changed.
- Checks and observed results:
  - Focused service tests initially produced the intended Red compilation
    result before the new API existed, then passed after implementation.
  - The direct fallback-deletion domain test first failed, then passed after
    the domain guard was added.
  - `verifyCategoryUi` passed after exercising actual management-dialog,
    reassignment, successful deletion, blocked deletion, fallback, and reload
    paths.
  - `./gradlew check javadoc shadowJar --console=plain` passed, including all
    Checkstyle source sets, all 49 JUnit tests, application startup/navigation,
    transaction and category JavaFX smoke suites, Javadocs, and packaging.
  - The packaged JAR was checked to contain the category FXML and controller.
  - `git diff --check` and a 120-character scan of changed Java files passed.
- Errors, limitations, or remaining uncertainty:
  - One new smoke-test assertion initially exceeded the 120-character style
    limit; it was wrapped and the full Checkstyle gate passed.
  - One dialog-automation attempt raised an event-loop timing exception even
    though the smoke task completed. The scheduling helper was then corrected;
    the next smoke run completed cleanly.
  - Automated JavaFX checks do not prove rendered layout, visible focus,
    screen-reader behaviour, native-dialog appearance, or complete keyboard
    behaviour on every supported platform.
  - The user has not yet reported the final manual UI checks for the Manage
    dialog, so those remain manual acceptance items rather than confirmed
    observations.

## Reflection notes

- What the AI did well or poorly:
  - The AI used layered tests to reveal two different classes of problems:
    missing API behaviour through a Red compilation result, and missing fallback
    protection through a direct domain regression test.
  - The independent review correctly prevented a service-only validation rule
    from being mistaken for a domain invariant.
  - The AI initially produced a timing bug in the JavaFX dialog smoke
    automation. It diagnosed this as event-loop ordering rather than a product
    failure and made the test deterministic.
  - The AI also missed an older guide paragraph and a second stale JUnit count
    during the first documentation pass. A later review caught and corrected
    both inconsistencies.
- Human judgement required:
  - The user identified that the fully expanded inline category controls were
    too cluttered, requested a plan before editing, and approved the
    progressive-disclosure design.
  - The user’s decision favored one **Manage** entry point and a contextual
    dialog over a separate screen or additional navigation, balancing
    discoverability with compactness.
- How the prompts or approach evolved:
  - The work began as a feature implementation request for Issue #7, moved
    through safe deletion/reassignment and fallback-invariant review, then
    shifted to a user-driven usability refinement.
  - The refinement began with a request for a plan, received explicit approval,
    and proceeded through UI/test/documentation/review stages.
  - The review process evolved from a feature-correctness check to a
    documentation-consistency check, catching pre-refinement instructions and
    stale testing evidence.
- Prompting versus manual work, when relevant:
  - Prompting helped derive state-transition cases, structure the dialog flow,
    generate layered regression coverage, and keep the User and Developer
    Guides aligned with implementation.
  - Manual engineering judgement was required to decide that progressive
    disclosure should be contextual rather than a separate page, to interpret
    JavaFX event-loop failures, and to determine that fallback protection
    belonged at the domain boundary instead of only in the service layer.
- What to do differently next time:
  - When a public domain state method is introduced, derive its invariants
    independently from the service layer instead of assuming callers always use
    the service.
  - For JavaFX nested dialogs, design smoke-test scheduling around the event
    loop from the start and separately verify that no background UI-thread
    exceptions were emitted.
  - After a workflow refinement, search the whole guide set for obsolete
    control names and numeric claims instead of updating only the most obvious
    section.
  - Keep a category row’s most common state visible and disclose infrequent or
    destructive actions contextually, then validate the resulting keyboard and
    native-dialog flow manually.

Human verification: approved
