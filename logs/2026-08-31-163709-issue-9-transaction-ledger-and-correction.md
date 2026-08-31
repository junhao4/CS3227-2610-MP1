Date: 2026-08-31
Time: 16:37:09 +0800
Timestamp source: Repository machine local clock when this log draft was
prepared; the exact beginning of the Issue #9 interaction was not separately
captured.
Task: Implement, document, review, and iteratively refine GitHub Issue #9 —
edit and delete transactions — including a date-grouped ledger, compact
actions, transaction maximum, and focused Add/Edit workflow.
Status: completed
Interaction range: “[$implement] issue 9 aka S8” – final logging request after
the approved transaction-toolbar placement refinement
Evidence sources: Conversation; `AGENTS.md`; project specifications; Git diff;
Issue #9 review report; Gradle test, Checkstyle, JavaFX smoke-test, JavaDocs,
build, resource, and prototype-verification output
Related logs:

- `logs/2026-08-31-151154-new-category-popup-retrieval-debugging.md`
- `logs/2026-08-31-122549-issue-7-category-deletion-and-progressive-management.md`

Human verification: approved

## Interaction history

### 1. Implement transaction correction and confirmed deletion

- Exact prompt excerpt: “[$implement] issue 9 aka S8”
- AI response: Scoped Issue #9 as editable amount, date, type, category, and
  note; deliberate permanent deletion with confirmation; persistence;
  accessible controls; documentation; and layered tests.
- User correction or decision: The user later expanded the issue with
  ledger-presentation and transaction-entry refinements rather than treating
  edit/delete as only a service feature.
- Resulting action:
  - Added immutable transaction replacement and removal operations to
    `ApplicationState`.
  - Added `TransactionService` update and delete operations using the existing
    save-before-publish persistence pattern.
  - Added JavaFX edit and standard delete-confirmation flows.
  - Added service, JSON-persistence, domain, and JavaFX smoke coverage.

### 2. Resolve archived-category correction safety

- AI response: Independent review found that a transaction could lose its
  archived historical category during an unrelated edit.
- User correction or decision: No additional product decision was required
  because this contradicted the need to keep historical transactions meaningful.
- Resulting action:
  - Kept a transaction’s own archived category selected when editing it.
  - Allowed the service to retain only that historical value while continuing to
    reject other archived categories for new selection or replacement.
  - Added service and JavaFX smoke coverage for retained archived categories
    and deliberate type/category replacement.
  - Recorded the resolved review finding as `CR-9-001`.

### 3. Design a less cluttered transaction ledger

- Exact prompt excerpts: “work thru how the ui for this page can be improved
  with me”; “wait i tot grp dates mean all the transaction with the same date
  only show the date once”; “yes copy exactly this prototype”
- AI response: Proposed a transaction-history layout based on daily date
  headings, a boxed same-day hierarchy, compact row metadata, and progressive
  disclosure for less-frequent controls.
- User correction or decision: The user clarified that date grouping must show
  the date once for all transactions recorded on that date, with a visually
  nested card rather than repeated row dates.
- Resulting action:
  - Added a static `transactions-c.fxml` exploratory prototype and registered
    it with prototype-resource verification.
  - Implemented a production daily ledger using real transaction data, grouped
    by exact date in newest-first order.
  - Added readable headings such as `31 Aug 2026`.
  - Kept search visible while hiding less-frequent filters behind
    **Filter & sort**.

### 4. Refine row actions, notes, and amount boundary

- Exact prompt excerpt: “edit and delete should be combined into 1 button,
  long notes should wrap i think, probably should have a max amount per
  transaction also like 10 million or smth. also if no note what do u think it
  shld show”
- AI response: Recommended one compact **⋯** menu per row, a neutral
  **No note** placeholder, wrapping note text, and a transaction-only cap of
  `S$9,999,999.99`.
- User correction or decision: “go”
- Resulting action:
  - Replaced direct Edit/Delete row buttons with one **⋯** menu containing
    **Edit transaction** and **Delete transaction**.
  - Made long transaction notes wrap and used **No note** for an absent
    optional note.
  - Added the maximum in `Transaction`, not `MoneyAmount`, so budget amounts
    retain their existing validation rules.
  - Wrote and ran a boundary test confirming `9999999.99` is accepted and
    `10000000` is rejected.
  - Added UI feedback coverage for the over-limit input.
  - Independent review later removed one obsolete smoke-test helper left over
    from the older direct-button UI; this was recorded as resolved `CR-9-002`.

### 5. Move Add/Edit into a focused in-page form

- Exact prompt excerpts: “before this move the add transaction to the right,
  also the section where we add shld it be another page? instead of at the
  bottom?”; “is it fighting for space with filter tho”; “ok do it”
- AI response: Recommended retaining Transactions as one navigation destination
  while swapping between the ledger and a focused Add/Edit form inside that
  page. This avoids permanently placing a long form below the history and
  avoids adding a separate top-level page.
- User correction or decision: The user agreed that Add should not compete with
  filters and approved the focused in-page form.
- Resulting action:
  - Added mutually exclusive `transactionLedger`, `transactionHistoryHeader`,
    and `transactionForm` presentation states.
  - **＋ Add transaction** and Edit hide the ledger and show a focused form with
    **← Back to transactions**.
  - Back and Cancel discard incomplete form values and return to the existing
    ledger without rebuilding or clearing its query controls.
  - Added JavaFX smoke assertions for initial ledger visibility, focused form
    activation, keyboard focus on Type, and returning through Back.
  - Updated the User Guide, Developer Guide, manual-testing instructions, and
    Issue #9 review report.

### 6. Correct Add-action placement using user visual feedback

- Exact prompt excerpts: “add transactions did not move to the right of the
  page”; “or where would be better, currently its in a weird position”; “ok do
  it”; “now its akwardly stuck to your transaction with no spacing”
- AI response: Diagnosed first that the page-header layout did not stretch
  sufficiently. After reconsidering the visual hierarchy with the user,
  proposed a dedicated ledger toolbar instead: search/filter above, then
  **Your transactions** on the left and **＋ Add transaction** on the right,
  immediately above the daily cards.
- User correction or decision: The user reported that the initial toolbar
  spacer still left the button awkwardly attached to the heading.
- Resulting action:
  - Moved **＋ Add transaction** out of the page title and into the ledger
    toolbar.
  - Added a smoke assertion that the toolbar belongs to the ledger and appears
    before the transaction cards.
  - Replaced the non-expanding label spacer with a flexible JavaFX `Region`,
    allowing the button to occupy the toolbar’s right edge.
  - Re-ran the transaction UI smoke test and whitespace validation successfully.
  - Updated documentation and the canonical review report to describe the
    ledger-toolbar location rather than a top-right page-header location.

## Work and verification

- Proposed, approved, and executed actions:
  - Implemented Issue #9 update/delete behaviour with immutable state and
    persistence safety.
  - Preserved archived historical category references during correction.
  - Created a daily-ledger presentation with exact-date grouping, wrapped
    notes, an explicit no-note state, and compact overflow actions.
  - Added a practical transaction-entry ceiling without changing budget
    validation.
  - Replaced the permanently below-ledger form with a focused in-page Add/Edit
    view.
  - Moved the Add action through two user-informed layout iterations to a
    ledger toolbar with a flexible spacer.
  - Updated the User Guide, Developer Guide, manual-test appendix, and
    canonical review report.
  - Did not add dependencies, commit, push, publish, close, or otherwise
    modify GitHub issues, labels, or dependencies.
- Files or external systems changed:
  - `src/main/java/cs3227/moneymap/domain/ApplicationState.java`
  - `src/main/java/cs3227/moneymap/domain/Transaction.java`
  - `src/main/java/cs3227/moneymap/service/TransactionService.java`
  - `src/main/java/cs3227/moneymap/TransactionController.java`
  - `src/main/resources/moneymap/transactions.fxml`
  - `src/main/resources/styles/moneymap.css`
  - `src/test/java/cs3227/moneymap/domain/TransactionTest.java`
  - `src/test/java/cs3227/moneymap/service/TransactionServiceTest.java`
  - `src/test/java/cs3227/moneymap/persistence/JsonDataRepositoryTest.java`
  - `src/smoke/java/cs3227/moneymap/TransactionUiSmokeTest.java`
  - `src/prototype/` transaction-ledger prototype resources and checks
  - `docs/UserGuide.md`
  - `docs/DeveloperGuide.md`
  - `reviews/issue-9.md`
  - GitHub was queried only; no GitHub state was changed.
- Checks and observed results:
  - The maximum-boundary domain test was added first; it passed after the
    transaction-specific guard was implemented.
  - The first focused-form smoke-test run failed as intended because
    `transactionLedger` did not yet exist in the FXML. The test passed after the
    focused ledger/form states were implemented.
  - Focused transaction domain, service, and JavaFX UI checks passed.
  - `./gradlew check build javadoc verifyPrototypes` passed, including JUnit,
    Checkstyle, production startup/navigation, transaction/category UI smoke
    tests, JavaDocs, packaging, and all nine prototype FXML resources.
  - `git diff --check` passed.
  - A 120-character scan of changed Java files returned no violations.
  - After the final flexible-toolbar-spacer correction,
    `verifyTransactionUi` passed again.
- Errors, limitations, or remaining uncertainty:
  - Gradle initially could not access its existing user-level wrapper-cache
    lock inside the restricted environment. Project verification succeeded
    after limited permission was granted for Gradle’s existing cache.
  - The initial layout reasoning did not match the user’s rendered experience
    twice: first the Add action did not visibly reach the intended page
    position, and then it sat too close to **Your transactions**. User visual
    feedback identified both problems.
  - Automated JavaFX smoke tests verify FXML structure and exercised behaviour,
    but cannot fully prove visual spacing, window-size responsiveness, popup
    placement, screen-reader wording, native-dialog rendering, or complete
    keyboard behaviour on every target platform.

## Reflection notes

- What the AI did well or poorly:
  - The AI implemented layered domain, service, persistence, UI,
    documentation, and review coverage for a feature that began as edit/delete
    but expanded into interaction design.
  - The independent review correctly found a historical-category data-integrity
    risk and a smaller obsolete test helper, preventing both from being hidden
    by passing happy-path tests.
  - The AI did poorly by treating source-level layout intent as enough evidence
    for visual placement. The user had to report that the Add button was not
    positioned as intended and later that it was visually attached to the
    toolbar label.
- Human judgement required:
  - The user clarified the meaning of transaction date grouping and selected a
    boxed same-day hierarchy.
  - The user chose one **⋯** menu over direct row actions, decided that
    **No note** was preferable to an empty description, and selected a
    transaction-only amount ceiling.
  - The user recognised that permanent form placement at the bottom of the
    ledger was cluttered and chose a focused in-page form over a new top-level
    navigation page.
  - The user’s direct visual feedback drove the final action-toolbar placement
    and spacer correction.
- How the prompts or approach evolved:
  - The feature moved from standard transaction correction/deletion into a
    collaborative redesign of the history screen.
  - A static prototype clarified the desired daily grouping, but production
    still required user feedback to match hierarchy and spacing.
  - The design evolved from direct row actions to one overflow menu, from a
    permanently below-list form to a focused in-page view, and from a
    page-header Add action to a ledger-toolbar action.
- Prompting versus manual work, when relevant:
  - Prompting helped identify state transitions, choose the appropriate
    validation boundary, generate tests, structure documentation, and propose
    alternative UI arrangements.
  - Automated testing established exact-date grouping, query preservation,
    form switching, validation, persistence, and resource loading.
  - Manual observation remained essential for judging whether the control’s
    actual placement, whitespace, and visual hierarchy matched the intended
    design.
- What to do differently next time:
  - For visible UI placement changes, validate the rendered page before
    presenting the source layout as complete; do not infer that a flexible
    layout node will visually expand as intended.
  - Use explicit JavaFX `Region` spacers for horizontally distributed toolbar
    content rather than relying on a label to consume remaining space.
  - Keep prototypes as concrete acceptance references, then compare production
    against each major hierarchy and interaction decision.
  - Add focused smoke assertions for view-state switching and node hierarchy,
    while keeping a short manual checklist for visual spacing and
    platform-native interactions.
  - Update all affected guide wording after a control moves, including user
    steps, accessibility notes, manual tests, and review evidence.

Human verification: approved
