# Code Review Report

## Status

- Target: Issue #7 — S6: Safely delete and reassign categories
- Scope: permanent deletion of unused ordinary categories, reassignment before
  deletion, fallback protection, persistence, JavaFX controls, and guides.
- Overall: PASS
- Independent human review: READY

## Checks

| Area | Status | Command/evidence |
| --- | --- | --- |
| Compilation | Passed | `./gradlew check javadoc --console=plain` compiled all production, test, and smoke sources. |
| Tests | Passed | The full JUnit suite (49 tests) passed; focused service, JSON, and domain tests cover deletion, reassignment, rejection, reload, and direct fallback protection. |
| Smoke/acceptance testing | Passed | `verifyCategoryUi` exercised FXML controls, reassignment, successful deletion, blocked deletion feedback, fallback controls, and reload. `verifyApplication` and `verifyTransactionUi` passed. |
| Static analysis | Passed | Checkstyle passed for main, test, smoke, and prototype sources; a 120-character scan of changed Java sources found no violations. |
| Packaging/resources | Passed | `./gradlew shadowJar --console=plain` passed; the JAR contains the category FXML, controller, and service classes. |
| Specification validation | Passed | Acceptance criteria are mapped to service/domain tests, persistence reload, and JavaFX smoke evidence. `CR-7-1` was fixed and directly tested. |
| Documentation consistency | Passed | The User Guide and Developer Guide now match the Manage dialog, and both test-count claims match the 49-test suite. No diagram change materially clarifies this local lifecycle extension. |

## Next actions

- Rerun write-code skill: NO
- Rerun update-documentation skill: NO
- Rerun code-review skill: NO
- Reason: `CR-7-2` was corrected and targeted guide/diff checks passed.

## Agent handoff

- Follow-up required: NO
- Current implementation/review state: progressive-disclosure UI and its guides
  are consistent; all applicable automated checks pass.
- Next responsible skill or human: independent human reviewer, if desired.
- Priority and finding addressed: `CR-7-1` and `CR-7-2` fixed.
- Exact files and line locations: `CategoryController.manageCategory`,
  `CategoryUiSmokeTest`, and affected User and Developer Guide sections.
- Required change or investigation: none.
- Constraints and out-of-scope work: domain/service/persistence behaviour,
  budgets, transaction editing, and GitHub state remain unchanged by this UI
  refinement.
- Verification commands: `./gradlew check javadoc shadowJar --console=plain`,
  packaged-resource check, guide scan, and `git diff --check` passed.
- Manual checks: native-dialog layout, focus, and keyboard traversal.
- Success criteria: met; category rows expose one Manage entry point and every
  guide instruction follows it accurately.
- Documentation impact: User and Developer Guides updated and rechecked.
- Required next rerun: none.
- Blocker or decision needed: none.

## Findings

- `CR-7-2` — [Medium] Guides retain pre-disclosure instructions and an
  inconsistent test count — Status: Fixed
  - Category: Documentation consistency
  - Location: `UserGuide.md` category-management introduction; `DeveloperGuide.md`
    testing-strategy opening sentence.
  - Requirement: feature documentation must precisely match the product used by
    peer testers.
  - Evidence: the first review found direct-action instructions and conflicting
    49/62 counts. The User Guide now routes Rename, Archive, and Restore through
    **Manage**; both Developer Guide counts state 49, matching the test scan.
  - Expected / actual: all instructions must start from **Manage**, and all test
    counts must state 49; both conditions now hold.
  - Reproduction or inspection path: guide scan verified the corrected wording;
    `rg -n '@Test' src/test/java | wc -l` returns 49.
  - Impact: resolved; peer testers receive the actual interaction path and
    non-contradictory testing evidence.
  - Likely cause / confidence: fixed with high confidence by the focused
    documentation-only correction and rerun.
  - Fix direction and affected responsibility: completed in the User and
    Developer Guides.
  - Tests to add/update: none; guide/diff verification completed.
  - Documentation impact: completed.
  - Verification after fixing: targeted guide review and `git diff --check`
    passed.

- `CR-7-1` — [Medium] Domain deletion permits permanent fallback removal — Status: Fixed
  - Category: Correctness / domain invariant
  - Location: `ApplicationState.withoutCategory`
  - Requirement: both `Uncategorised` fallbacks are never deletable.
  - Evidence: the first review found that the public method checked only
    transaction references and presence. It now rejects `permanentFallback()` at
    the domain boundary before creating a candidate state.
  - Expected / actual: deletion must reject either fallback; direct calls now
    throw `IllegalArgumentException` and retain the 14-category starter state.
  - Reproduction or inspection path: `ApplicationStateTest` retrieves both
    starter fallbacks and asserts `withoutCategory` rejects each.
  - Impact: resolved; future callers cannot bypass the service guard through
    the public domain state API.
  - Likely cause / confidence: fixed with high confidence by the direct guard
    and focused red–green regression test.
  - Fix direction and affected responsibility: completed in `ApplicationState`.
  - Tests to add/update: completed in `ApplicationStateTest`.
  - Documentation impact: the corrected User and Developer Guide workflow and
    test-count claims now accurately reflect the final UI and suite.
  - Verification after fixing: focused domain test and full
    `./gradlew check javadoc --console=plain` passed.

## Positive observations

- Reassignment uses immutable candidate state and persists it before publication.
- The service rejects same, incompatible, archived, and fallback source
  categories without saving.
- Used-category deletion gives a concrete reassignment next step and leaves the
  transaction reference intact.
- The JavaFX smoke test covers actual confirmation-dialog paths rather than
  only checking that the controls exist.

## Gaps and limitations

- Automated JavaFX evidence cannot establish the rendered layout, platform
  native-dialog appearance, or complete keyboard traversal on every target OS.

## Manual checks for user

- In **Categories and Budgets**, use Tab to reach a row's **Manage** button.
  Confirm visible focus and activation with Space on macOS, or Enter/Space on
  Windows and Linux.
- Open **Manage** for an unused, used, archived, and `Uncategorised` category.
  Check that the dialog exposes only the appropriate actions, that used-category
  Delete is visibly disabled with an explanation, and that the fallback message
  is clear.
- Reassign a used category through the choice and confirmation dialogs, then
  reopen **Manage** and delete it. Check native-dialog layout, text feedback,
  and category-row layout at the supported window sizes.

## Conclusion

The Issue #7 category-management work, including this progressive-disclosure
refinement, passes independent review. The remaining checks are manual
acceptance checks rather than defects.
