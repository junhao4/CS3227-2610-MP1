Date: 2026-08-30
Time: 14:31:22 +0800
Timestamp source: Repository machine local clock when the logging request was processed
Task: Reflect on whether the first issue implementation used plain zero-shot prompting and evaluate the result
Status: completed
Interaction range: Issue-#2 implementation request – zero-shot prompting reflection request
Evidence sources: Conversation, GitHub issue #2, specs/MP1-requirements.md, specs/ProductSpecification.md, .agents/skills/code-review/references/review-criteria.md, implementation diff, Gradle output, documentation update diff
Related logs: logs/2026-08-29-225552-code-review-skill.md; logs/2026-08-29-144109-update-documentation-skill.md; logs/2026-08-29-144759-update-documentation-skill-refactor.md
Human verification: approved

## Interaction history

### 1. Issue-#2 implementation prompt

- Exact user prompt: “implement gh issue 2 using tdd where appropriate, read https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/index.html and apply the best practices. follow this coding standards `https://se-education.org/guides/conventions/java/`”
- User-provided context: `review-criteria.md` was attached with the implementation request.
- AI response: Inspected the repository instructions, requirements, product specification, existing prototype scaffold, build configuration, the linked GitHub issue, and the two requested external references before implementing the feature.
- User correction or decision: None recorded for the implementation prompt.
- Resulting action: Treated the issue acceptance criteria as the implementation contract and created a separate production JavaFX shell.

### 2. Implementation and verification

- User prompt: The implementation prompt requested TDD where appropriate and application of the supplied software-engineering and Java-coding references.
- AI response: Added a production package, production FXML resources, production CSS, a navigation controller, a production application entry point, a smoke test, and setup/developer documentation. Existing prototype code and resources were preserved separately.
- User correction or decision: None recorded.
- Resulting action: Ran the Gradle build and smoke checks.
- Evidence: The first compilation failed because the smoke test attempted to invoke private controller navigation methods. The methods were changed to package-visible and the checks were rerun successfully.

### 3. Documentation reconciliation

- User prompt: The user later invoked `/update-documentation` with the review-criteria context.
- AI response: Reviewed the current guides against the verified implementation and identified that the User Guide could imply future functionality was already available. The Developer Guide also needed clearer smoke-test limitations and an acknowledgements section.
- User correction or decision: None recorded.
- Resulting action: Updated both guides to describe the current release as a shell-only release and to distinguish automated FXML verification from manual visual and accessibility verification.

### 4. Zero-shot reflection request

- Exact user prompt: “/logging for this 1st issue i wanted to just use plain zero shot prompting for the implementation did i do that? how was the result?”
- AI response: Classified the implementation prompt as retrieval-augmented zero-shot prompting rather than strictly plain zero-shot prompting. The prompt supplied a task and authoritative references but no examples or worked implementation. The result was assessed as successful within issue #2’s scope, with an initial testability failure and remaining manual-verification limitations.
- User correction or decision: None recorded.
- Resulting action: Prepared this log and waited for approval.

## Work and verification

- Prompting classification:
  - The implementation request was zero-shot because it did not provide examples, demonstrations, or a sample solution.
  - It was not plain zero-shot in the strictest sense because it included issue-specific requirements, external reference URLs, attached review criteria, and repository context.
  - The most accurate label is retrieval-augmented zero-shot prompting with tool-assisted implementation and verification.

- Resulting implementation:
  - Added the production `cs3227.moneymap` package.
  - Added production JavaFX shell navigation.
  - Added Dashboard, Transactions, Categories and Budgets, and Data and Settings destinations.
  - Kept prototype code and resources under `cs3227.prototype` and the existing prototype resource directories.
  - Added `verifyApplication` and retained `verifyPrototypes`.
  - Added shell setup, developer, and manual-testing documentation.

- Checks and observed results:
  - `./gradlew clean build verifyApplication verifyPrototypes` passed after correcting controller-method visibility.
  - `git diff --check` passed.
  - The production smoke test loaded the production shell and all four destinations.
  - The prototype smoke test loaded all existing prototype FXML resources.
  - The documentation update was verified with `./gradlew build verifyApplication verifyPrototypes`.

- What worked:
  - The prompt was concise and allowed the agent to inspect the repository and retrieve relevant requirements.
  - The issue and references helped constrain the implementation to the production shell.
  - The resulting structure separated exploratory prototypes from production code.
  - The smoke test provided executable evidence for the main acceptance criterion.

- What did not work perfectly:
  - The first implementation had a controller testability problem that was only discovered during compilation.
  - The first documentation version did not make the shell-only status explicit enough.
  - No conventional unit-test framework was configured, so TDD was applied mainly through an executable acceptance smoke test and correction after the initial failed build.
  - Visual correctness, complete keyboard behaviour, and accessibility were not proven automatically.

- Remaining uncertainty:
  - The prompt did not explicitly specify the production package names, resource directories, controller visibility, or exact smoke-test structure. These were inferred from the issue, product specification, and repository guidance.
  - Manual GUI verification of focus order, visual layout, and resizing remains necessary.
  - The JavaFX restricted-native-access warning and module-name warning remain non-blocking.

## Reflection notes

- What the AI did well or poorly:
  - It successfully implemented the requested shell within the issue’s scope.
  - It used the supplied references to avoid prematurely implementing transactions, categories, budgets, persistence, or import/export.
  - It caught and corrected a testability problem through the build.
  - It initially made documentation claims that required clarification, showing that generated documentation needed a separate reconciliation pass.
  - It did not fully satisfy strict plain-zero-shot conditions because the task included substantial retrieved repository and external-reference context.

- Human judgement required:
  - Determining the correct prompting classification.
  - Distinguishing “zero-shot” from “plain zero-shot” and from retrieval-augmented prompting.
  - Deciding that a JavaFX smoke test was sufficient for the shell issue while acknowledging what it could not prove.
  - Verifying that empty-state wording did not overclaim unavailable application behaviour.
  - Deciding not to add test dependencies without explicit approval.

- How the prompts or approach evolved:
  - The initial implementation prompt was intentionally concise and delegated interpretation to the agent.
  - Repository inspection and retrieval of issue/specification/reference material supplied the missing implementation detail.
  - Verification introduced a corrective iteration after compilation failed.
  - A later documentation pass corrected the distinction between implemented shell behaviour and planned financial features.
  - The reflection request evaluated the prompt strategy rather than changing the implementation.

- Prompting versus manual work:
  - Retrieval-augmented context came from the GitHub issue, repository specifications, supplied review criteria, CS2103/T textbook, and SE-EDU Java standard.
  - These references guided scope, architecture separation, testing, documentation, and coding style.
  - Engineering judgement was still required for scope control, architecture boundaries, testability, documentation accuracy, and interpreting verification results.

- What to do differently next time:
  - Keep the concise zero-shot style if rapid exploration is desired, but explicitly state the intended production boundary and verification contract.
  - Ask for an acceptance-test outline before implementation so controller testability is considered earlier.
  - State which behaviours must remain out of scope to reduce inferred assumptions.
  - Separate automated structural checks from required manual UI checks in the initial implementation request.
  - Record the prompting classification accurately as retrieval-augmented zero-shot rather than plain zero-shot.
