Date: 2026-09-01
Time: 08:50:22 +0800
Timestamp source: Repository machine local clock when the logging request was
processed; this is the best available timestamp for this follow-up session.
Task: Perform a repository-wide Issue #14 MVP audit using an explicit checklist,
then investigate whether the checklist-driven workflow is an AI backpressure
mechanism.
Status: ongoing
Interaction range: Repository-wide code-quality audit request – backpressure
terminology research and logging request
Evidence sources: Conversation, AGENTS.md, specs/MP1-requirements.md,
.agents/skills/code-review/SKILL.md, repository source and diffs, Gradle,
Checkstyle, Javadoc, prototype-verification output, reviews/issue-14.md, and
Lucas F. Costa’s article “Backpressure is all you need”
Related logs:
- logs/2026-08-31-104722-static-analysis-and-dev-guide-review.md
Human verification: approved

## Interaction history

### 1. Turning a broad audit into a checklist

- Exact prompt excerpt: “u help me check those things u listed, make a checklist
  to check each 1 by 1 and tick them off when done”.
- AI response: Treated the request as a full repository audit rather than a
  check of only the active Java file. It made a checklist covering build,
  automated tests, JavaFX smoke checks, static analysis, Java style,
  documentation, architecture, error handling, test design, and release
  artifacts.
- User correction or decision: The user explicitly requested visible,
  one-by-one completion tracking rather than an unstructured review.
- Resulting action: Created a structured review plan and completed the
  applicable checks individually before reporting the audit outcome.

### 2. Evidence-backed audit execution

- AI response: Used the checklist as a completion gate: a check was reported as
  complete only after command output or repository inspection supplied evidence.
- User correction or decision: None recorded during execution.
- Resulting action: Ran
  `./gradlew --no-daemon check build verifyPrototypes javadoc shadowJar
  --console=plain`, `git diff --check`, a repository-wide Java line-length
  scan, source/dependency searches, and manual inspection of changed code,
  public documentation, design boundaries, exception handling, and tests.

### 3. Audit findings and handoff

- AI response: Reported that the build, configured JUnit suite, JavaFX smoke
  checks, Checkstyle, prototype FXML loading, Javadoc generation, packaging,
  whitespace check, and Java 120-character scan passed. It found no Blocker,
  High, or Medium finding.
- User correction or decision: The user initially asked to fix the remaining
  low-risk work, then paused that change to understand and preserve the
  prompting lesson first.
- Resulting action: Updated `reviews/issue-14.md` as the canonical review
  artifact. The report records CR-14-5:
  - `Transaction` still uses `S$` in its maximum-amount validation message;
  - `CategoryController.cardMoney` retains an unreachable `S$` conversion
    branch although `SgdFormatter` already always returns `$`.

### 4. Researching and correcting the strategy label

- Exact prompt excerpts:
  - “i feel like this checklist idea is some prompting strategy, like if
    provides backpressure or smth, help me do some research on this”
  - “im talking abt asking it to write the checklist is backpressure in ai
    prompting”
  - “can u search online backpressure in ai prompting”
  - “whats this then
    https://www.lucasfcosta.com/blog/backpressure-is-all-you-need”
- AI response: Initially distinguished checklist generation from technical
  systems backpressure and described it as structured task decomposition.
- User correction or decision: The user supplied Lucas F. Costa’s article,
  which applies backpressure to AI-assisted software development through
  tests, linting, reviews, and other gates that return unfinished work to the
  agent.
- Resulting action: Corrected the terminology:
  - asking an agent to write a checklist is plan-first structured
    decomposition;
  - using that checklist with checks/review that block completion or return
    unfinished work is a checklist-driven backpressure loop in the article’s
    AI-development sense.

## Work and verification

- Proposed, approved, and executed actions:
  - performed the full repository quality audit;
  - maintained a checklist/plan so audit categories were completed one by one;
  - updated the Issue #14 review report;
  - researched the meaning of backpressure in AI prompting and coding-agent
    workflows;
  - did not yet implement CR-14-5 because the user paused the fix.
- Files or external systems changed:
  - Changed by this session: `reviews/issue-14.md`.
  - Inspected existing working-tree follow-up changes:
    `specs/ProductSpecification.md`,
    `src/main/java/cs3227/moneymap/CategoryController.java`,
    `src/main/java/cs3227/moneymap/DataAndSettingsController.java`, and
    `src/main/java/cs3227/moneymap/domain/ApplicationState.java`.
  - No dependencies, commits, pushes, GitHub changes, or application-code fixes
    were made in this session.
- Checks and observed results:
  - Full Gradle quality/build/package command passed.
  - Checkstyle passed for main, test, smoke, and prototype source sets.
  - Application, transaction, category, dashboard, data/settings, and recovery
    JavaFX smoke checks passed.
  - Prototype verification loaded all configured FXML resources.
  - Javadoc and fat-JAR generation passed.
  - `git diff --check` passed.
  - No Java source line exceeded 120 characters.
  - Manual boundary inspection confirmed controllers use the service layer,
    persistence stays behind `DataRepository`, and the service saves candidate
    state before publishing it.
- Errors, limitations, or remaining uncertainty:
  - The Issue #14 report remains `FINDINGS` because CR-14-5 is open.
  - Automated checks do not prove rendered layout, native-dialog behaviour,
    keyboard focus, or the complete end-to-end manual audit.
  - The checklist is prompt-enforced rather than a hard runtime hook; it is
    useful only when paired with specific checks and evidence.

## Reflection notes

### Strategies used and evidence

| Strategy | How it was used | Observed effect | Limitation |
| --- | --- | --- | --- |
| Retrieval-grounded prompting | The audit loaded repository instructions, MP1 requirements, the code-review skill, current report, source, tests, diffs, and the supplied backpressure article. | Findings were tied to actual repository evidence rather than generic code-quality advice. | Retrieved context still required human interpretation of product decisions and finding severity. |
| Plan-first structured decomposition | The user asked for a checklist that would be checked and ticked one item at a time. | The vague request to check code quality became explicit checks for build, tests, JavaFX smoke tests, static analysis, style, architecture, error handling, test design, and documentation. | A checklist is only a plan unless completion requires evidence. |
| Checklist-driven backpressure | Each item was marked complete only after tool output or source inspection. Untested or unresolved work prevented a clean audit conclusion. | Passing Gradle and Checkstyle did not end the audit; later consistency inspection found the two obsolete `S$` remnants recorded as CR-14-5. | The loop is prompt-enforced rather than a hard runtime hook, so it depends on the skill/workflow continuing to require the checks. |
| Tool-grounded verification | Gradle, Checkstyle, FXML/prototype checks, Javadoc generation, source searches, and `git diff --check` were used as evidence. | The report clearly separates passed automated evidence from manual checks that remain necessary. | Automated checks cannot establish rendered layout quality, native-dialog behaviour, or complete keyboard/platform usability. |
| Human corrective feedback | The user challenged the initial narrow explanation of “backpressure” and supplied Lucas F. Costa’s article. | The terminology was corrected: asking for a checklist is structured planning, while the checklist plus checks that return unfinished work is a backpressure loop in the article’s AI-development sense. | This is not yet a complete Reflexion-style correction cycle because CR-14-5 has not been fixed and independently re-verified. |

### What the AI did well or poorly

- The AI continued beyond passing automated checks and found low-risk
  source-consistency issues through a broader audit.
- The initial explanation of “backpressure” was too narrow because it used only
  the conventional streaming/queue definition. The user’s source and challenge
  caused a correction.
- The final explanation distinguished the strategy honestly: checklist
  generation is plan-first decomposition; checklist-driven checks that return
  unfinished work create the backpressure loop.

### Human judgement required

- The user identified that the supplied article used “backpressure” in a
  broader AI-assisted software-development sense and requested that the
  terminology be corrected.
- The user chose to pause the small code cleanup so the prompting lesson could
  be researched and preserved.
- The user must still decide when CR-14-5 is fixed and perform the final manual
  MVP audit; passing automated checks is insufficient.

### How the prompts or approach evolved

1. A broad request to assess repository quality became a request for a complete
   checklist.
2. The checklist became an evidence-backed completion gate: entries were ticked
   only after commands or inspection supplied evidence.
3. The user recognised a possible connection to backpressure and requested
   research.
4. The initial terminology was challenged with a relevant source and refined
   into a checklist-driven backpressure-loop explanation.

### Prompting versus manual work

- The prompting strategy helped organise a broad audit and preserve a visible
  record of which checks had actually been completed.
- The quality signal came from executable tools and repository inspection, not
  from the model merely claiming to have checked the work.
- Manual judgement remained necessary for product-scope decisions, terminology
  accuracy, prioritisation of findings, and GUI/platform acceptance checks.

### What to do differently next time

- Start a cross-cutting audit or release prompt with an explicit checklist and
  require evidence for every completed entry.
- Use the phrase “checklist-driven, evidence-backed backpressure loop” only
  when tests, review, or another gate can return unfinished work for
  correction.
- Keep the loop proportionate: run focused checks during ordinary feature work
  and reserve the full repository/manual audit for milestone or release stages.
