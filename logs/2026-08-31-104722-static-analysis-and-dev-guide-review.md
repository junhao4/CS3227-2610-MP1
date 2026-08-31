Date: 2026-08-31
Time: 10:47:22 +0800
Timestamp source: Repository machine local clock when the logging request was
processed; this is the best available timestamp for the follow-up session.
Task: Configure static analysis for MoneyMap and assess whether the resulting
Developer Guide content matches CS2103/T conventions.
Status: completed
Interaction range: Static-analysis configuration request – final Developer
Guide placement recommendation and logging clarification
Evidence sources: Conversation, AGENTS.md, specs/MP1-requirements.md,
existing guides and logs, build.gradle, Checkstyle configuration, Gradle
output, CS2103/T Code Quality, Quality Assurance, Test Case Design, and
SE-EDU Java coding-standard pages, and example CS2103/T project guides
Related logs:
- logs/2026-08-31-101500-issue-5-custom-categories.md
- logs/2026-08-29-225552-code-review-skill.md
Human verification: approved

## Interaction history

### 1. Request and approval for static analysis

- Exact prompt excerpt: “can u configure the static analysis”.
- AI response: Identified that the repository had no Checkstyle, PMD,
  SpotBugs, or coverage configuration and recommended Gradle Checkstyle with a
  project-specific ruleset. The repository instruction requiring approval
  before adding dependencies was explained.
- User correction or decision: The user approved Checkstyle and requested that
  the configuration cover the relevant CS2103/T conventions.
- Resulting action: Added the Gradle Checkstyle plugin and prepared a ruleset
  for all `main`, `test`, `smoke`, and `prototype` Java source sets.

### 2. Ruleset refinement and verification

- AI response: Read the CS2103/T Code Quality and Testing guidance and the
  SE-EDU Java standard. Selected automatable rules for tabs, line length,
  imports, naming, braces, whitespace, unused imports, and Javadoc types, while
  leaving KISS, SLAP, cohesion, duplication, and accessibility to review.
- User correction or decision: None recorded.
- Resulting action: Added `config/checkstyle/checkstyle.xml`, wired four
  Checkstyle tasks into Gradle `check`, removed two unused imports identified by
  the analyzer, and updated the Developer Guide with the commands and scope.
  Unsupported or overly strict configuration elements were removed after
  Checkstyle reported initialization or legacy-property failures.
- Verification: All four Checkstyle tasks passed, followed by `./gradlew check
  javadoc --console=plain` and `git diff --check`. The full project check passed
  with JUnit and all production smoke tests.

### 3. Developer Guide placement review

- Exact prompt excerpt: “are these actually things to put in the dev guide? can
  u look at cs2103t whether they did”.
- AI response: Compared the current guide with CS2103/T guidance and example
  project documentation. Concluded that build/test/Checkstyle commands and a
  concise description of automated versus manual checks belong in the
  Developer Guide, while an exhaustive rule-by-rule explanation does not.
  Product-specific manual-testing workflows belong in the Developer Guide
  appendix, and detailed tooling can be treated as DevOps material where a
  project has a separate DevOps guide.
- User correction or decision: The user clarified that the implementation log
  should cover the Issue #5 work and asked for both logs to be written.
- Resulting action: Kept the static-analysis follow-up separate from the Issue
  #5 implementation log and recorded both sessions under `logs/`.

## Work and verification

- Proposed, approved, and executed actions: Read project requirements and
  course guidance; configured Checkstyle; tuned the ruleset to the existing
  project style; added the build gate; updated the Developer Guide; and
  compared the guide structure with CS2103/T examples. No further product
  behavior was changed in this follow-up.
- Files or external systems changed: `build.gradle`,
  `config/checkstyle/checkstyle.xml`, two unused-import removals in existing
  tests, and `docs/DeveloperGuide.md`.
- Checks and observed results:
  - `checkstyleMain`, `checkstyleTest`, `checkstyleSmoke`, and
    `checkstylePrototype` passed.
  - `./gradlew check javadoc --console=plain` passed, including JUnit,
    application smoke tests, category smoke tests, and transaction smoke tests.
  - `git diff --check` passed.
- Errors, limitations, or remaining uncertainty: An initial Checkstyle ruleset
  used incompatible Javadoc properties and an overly strict blank-line rule;
  those were removed or relaxed after executable failures. Javadoc generation
  remains enforced by the existing `javadoc` task, while higher-level quality
  principles remain manual review concerns. No separate PMD, SpotBugs, or
  coverage tool was added.

## Reflection notes

- What the AI did well or poorly: The course comparison correctly separated
  mechanical checks from design judgement and found examples where Checkstyle
  commands are documented in a DevOps/testing section. The first configuration
  attempt was too ambitious and used incompatible Checkstyle properties, but
  the tool failures were used to simplify the configuration until `check`
  passed.
- Human judgement required: The user approved the dependency/tooling change and
  clarified that the Issue #5 implementation log and static-analysis follow-up
  log should be separate.
- How the prompts or approach evolved: The work moved from “configure static
  analysis” to “cover CS2103/T conventions,” then to “verify whether all of
  those details belong in the Developer Guide,” and finally to preserving two
  coherent logs rather than merging unrelated sessions.
- Prompting versus manual work, when relevant: Prompting made the desired
  quality gates and documentation boundary explicit. Manual work was required
  to interpret Checkstyle compatibility errors, choose a non-noisy rule set,
  and compare actual CS2103/T project-guide structure.
- What to do differently next time: Start from the supported Checkstyle
  version’s property names and keep the first ruleset narrowly mechanical;
  expand rules only after the base build gate is green.
