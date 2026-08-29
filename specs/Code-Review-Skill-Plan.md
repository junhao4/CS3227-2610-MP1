# Code-review skill plan

## Purpose

Create a read-only Codex skill that reviews an implemented MoneyMap change
after implementation and documentation updates. The skill should combine
specification validation, user-oriented acceptance testing, automated
verification, static inspection, design review, test-quality review, and
implementation-to-documentation traceability.

The skill reports whether the next action is to:

- rerun the code-writing skill;
- rerun the update-documentation skill;
- rerun code review after follow-up work; or
- proceed to independent human review.

The skill diagnoses and reports. It does not silently modify code, tests,
documentation, specifications, build files, Git history, or GitHub state.

## Intended pipeline

```text
Implement change
    |
    v
Run available implementation checks
    |
    v
Run update-documentation
    |
    v
Run code-review
    |
    +-- PASS ------------------------------> Independent human review
    |
    +-- Implementation finding ------------> Code-writing skill
    |                                           |
    |                                           v
    |                                     Verification
    |                                           |
    |                                           v
    |                              Update-documentation if needed
    |                                           |
    |                                           v
    |                                     Code-review again
    |
    `-- Documentation-only finding --------> Update-documentation
                                                |
                                                v
                                         Code-review again if needed
```

The review must treat the current implementation and verification evidence as
the source of truth. It must not recommend updating documentation to describe
an unverified fix.

## Planned review steps

The eventual skill will run these steps in order. Each step has a distinct
purpose, evidence output, and routing consequence. The detailed rules will be
loaded from references only when the change makes them relevant.

| Step | Review activity | Main evidence/output | Reference(s) |
| --- | --- | --- | --- |
| 1 | Establish review boundary | Target issue/release, changed files, acceptance criteria, documentation scope, Git diff, unrelated changes | `review-routing.md` |
| 2 | Discover project checks | Available Gradle tasks, configured static-analysis tools, test and packaging commands | `project-checks.md` |
| 3 | Run automated verification | Compilation, tests, smoke checks, static analysis, packaging, and resource-check results classified as passed, failed, not configured, not applicable, or blocked | `project-checks.md` |
| 4 | Validate against specification | Requirement-to-implementation-to-evidence matrix; unmet, ambiguous, or unauthorised behaviour | `specification-validation.md` |
| 5 | Perform user acceptance testing | Realistic feature workflows, invalid inputs, boundaries, recovery, persistence, expected versus observed outcomes | `user-acceptance-testing.md` |
| 6 | Review code quality | Evidence-based findings about readability, complexity, naming, duplication, SLAP, exceptions, assertions, logging, and defensive coding | `cs2103t-review-criteria.md` |
| 7 | Review architecture and design | Responsibility, coupling, cohesion, dependency direction, testability, and stale design diagrams | `architecture-and-design.md` |
| 8 | Review tests and regression protection | Test-level suitability, meaningful assertions, input partitions, boundaries, state transitions, integration, and regression gaps | `test-quality.md` |
| 9 | Compare documentation with code | Claim-by-claim classification of User Guide, Developer Guide, diagrams, commands, examples, and manual-testing instructions | `documentation-consistency.md` |
| 10 | Consolidate and route findings | Severity, confidence, fix details, rerun decisions, limitations, and human-review readiness | `review-report-template.md` |

The implementation should not skip Steps 1–4 for a normal feature review.
Steps 5–9 may be narrowed only when the changed artefacts clearly make a
review area irrelevant, and the report must say which areas were not reviewed
and why.

The specification and user-acceptance steps are intentionally separate:

- specification validation checks whether the product requirement is correctly
  implemented;
- user acceptance testing checks whether a realistic user can achieve the
  intended outcome; and
- documentation consistency checks whether those verified outcomes are
  accurately described to future users and maintainers.

The code-review skill remains read-only throughout all steps. A failed step
produces a finding or limitation; it does not trigger an automatic code or
documentation edit.

## Proposed skill package

```text
.agents/skills/code-review/
|-- SKILL.md
`-- references/
    |-- cs2103t-review-criteria.md
    |-- review-report-template.md
    `-- project-checks.md
```

### `SKILL.md`

Keep the entrypoint short and operational. It should contain:

1. Purpose and read-only boundary.
2. Review inputs and stop conditions.
3. Source precedence.
4. Project check-discovery rules.
5. Specification and user-validation workflow.
6. Categories of review.
7. Finding severity and confidence rules.
8. Rerun-decision rules.
9. Required report fields.
10. Links to the conditional references.

The entrypoint should not reproduce the full CS2103/T checklist. It should
route the reviewer to the detailed criteria when that level of inspection is
needed.

### `references/cs2103t-review-criteria.md`

Organise the detailed review criteria by concern:

- readability and naming;
- long methods and deep nesting;
- complicated expressions and magic values;
- logical structure and happy-path clarity;
- SLAP and abstraction levels;
- KISS and unnecessary complexity;
- dead code, variable scope, and duplication;
- comments and documentation of non-obvious decisions;
- exceptions, assertions, logging, and defensive coding;
- responsibility and separation of concerns;
- dependency direction and architecture;
- testability and test isolation;
- unit, integration, system, acceptance, and regression testing;
- specification-to-behaviour traceability and user acceptance;
- equivalence partitions and boundary values;
- security-sensitive behaviour where applicable; and
- maintainability and release readiness.

Each checklist item should tell the reviewer what evidence to inspect and
should distinguish a confirmed problem from a review question or suggestion.

### `references/review-report-template.md`

Define the exact report schema so every finding contains enough information to
fix the problem without rediscovering it:

- severity;
- category;
- file and line location;
- affected feature or acceptance criterion;
- evidence;
- expected behaviour;
- actual behaviour;
- reproduction steps or inspection path;
- impact;
- specification and user-validation impact;
- likely cause and confidence;
- required fix direction;
- likely files or responsibilities involved;
- tests to add or update;
- documentation impact;
- verification after fixing; and
- rerun decisions.

### `references/project-checks.md`

Document how to discover and classify repository checks without assuming that
specific plugins exist. The eventual skill should prefer configured commands
and should report unavailable checks accurately.

For the current repository, the plan records these known facts:

- `build.gradle` applies the Gradle `application` plugin.
- Java compilation is configured with Java release 25 and JavaFX modules.
- A custom `verifyPrototypes` Java execution task exists.
- No Checkstyle, PMD, SpotBugs, JaCoCo, Error Prone, or equivalent plugin is
  currently configured in `build.gradle`.
- No conventional `src/test` directory was found during planning.

The implementation should rediscover the current state when it runs rather
than treating these facts as permanent.

## Source and reference routing

The code-review skill should use this source precedence:

1. Verified implementation behaviour and test results.
2. Approved issue and acceptance criteria.
3. `specs/ProductSpecification.md`.
4. `specs/MP1-requirements.md`.
5. Current User Guide and Developer Guide.
6. Routed CS2103/T references.

Project sources determine MoneyMap behaviour. The textbook supplies review
heuristics and terminology; it cannot override the product specification or
verified implementation.

The planned external references are:

| Reference | Route when | Planned use |
| --- | --- | --- |
| [Code Quality](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/codeQuality.html) | Every full code review | Readability, naming, methods, nesting, SLAP, duplication, comments, defensive coding |
| [Quality Assurance](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/qualityAssurance.html) | Every full code review | Role of code review and static analysis; verification versus validation |
| [Testing](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/testing.html) | Any test or regression review | Testing levels, testability, regression, automation, GUI limitations |
| [Test Case Design](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/testCaseDesign.html) | Any input, validation, or stateful behaviour review | Equivalence partitions, boundary values, negative cases, input combinations |
| [Software Architecture](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/architecture.html) | Any boundary, package, layer, or dependency review | Responsibilities, architecture decisions, abstraction, dependency direction |
| [Principles](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/principles.html) | Any responsibility or coupling review | SRP, separation of concerns, SOLID, DRY, YAGNI, and related principles |
| [Refactoring](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/refactoring.html) | A code smell or corrective restructuring is reviewed | Safe improvement direction and preserving behaviour while restructuring |
| [Debugging](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/debugging.html) | A runtime defect needs reproduction or isolation | Evidence-based reproduction, isolation, cause analysis, and verification |
| [Secure Software Engineering](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/security.html) | Security-sensitive behaviour is changed | Security review prompts, if the page is available and applicable |
| [tP Grading](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html) | Defining project-specific quality expectations | Logging, exceptions, assertions, defensive coding, standards, SLAP, duplication, automated analysis |

The exact Secure Software Engineering URL must be confirmed when the skill is
implemented. If a routed page is unavailable, the skill should report that
fact and continue using project sources and available references.

## Review workflow

### 1. Establish the boundary

Identify the target issue, feature, release, or commit; changed files; tests;
documentation updated by `update-documentation`; acceptance criteria; product
specification sections; Git diff; and unrelated working-tree changes.

Stop if the review target or release status is unclear.

### 2. Build an evidence baseline

Discover available Gradle tasks and configured tools. Record commands before
running them. Classify each check as:

- passed;
- failed;
- not configured;
- not applicable; or
- blocked by the environment.

Never report a check as passed merely because its tool is unavailable.

### 3. Run available checks

Use configured, non-mutating checks such as compilation, tests, smoke tests,
static analysis, packaging, resource validation, and coverage. Do not install
plugins or dependencies automatically.

Potential commands include `./gradlew tasks`, `./gradlew test`,
`./gradlew check`, `./gradlew build`, and any project-specific smoke task that
actually exists, such as `verifyPrototypes` in the current prototype project.

### 4. Validate the implementation against the specification

Compare the implementation against the approved issue, acceptance criteria,
`specs/ProductSpecification.md`, and relevant user stories. For every changed
requirement, determine whether the implementation provides the promised happy
path, rejects invalid inputs, preserves required invariants and existing data,
handles important state transitions, produces the required user-visible result
or error, and avoids unauthorised behaviour.

Use tests and direct execution as evidence. A source-level match is not enough
to establish that the correct user-visible behaviour is implemented.

### 5. Perform user-oriented acceptance testing

Exercise the changed feature as a user would, using the User Guide, Developer
Guide manual-testing appendix, acceptance criteria, and sensible exploratory
variations. Record the exact input, expected outcome, observed outcome, and
verification status.

Cover the primary workflow, important invalid inputs, boundary values, error
recovery, persistence or reload behaviour, and adjacent-feature interactions
when applicable. GUI smoke tests prove only the paths they exercise; they do
not prove complete visual correctness or accessibility.

If the environment prevents user testing, report the limitation and do not mark
the behaviour as verified.

### 6. Inspect implementation quality

Review the changed code and relevant surrounding code for readability,
complexity, abstraction levels, naming, duplication, dead code, exception
handling, assertions, logging, defensive coding, state ownership, and
responsibility placement.

### 7. Inspect architecture and design

Check responsibility boundaries, separation of concerns, coupling, cohesion,
dependency direction, testability, UI/domain/persistence boundaries, and
whether diagrams or design explanations are stale.

Do not demand every design principle mechanically. Report only evidence-based
risks or clearly justified improvement suggestions.

### 8. Inspect tests and regression protection

Check whether changed behaviour has meaningful positive, negative, boundary,
state-transition, persistence, integration, and regression coverage at a
reasonable testing level. Check determinism, meaningful assertions, test
isolation, and whether tests duplicate implementation logic.

### 9. Check documentation against code and behaviour

Perform a separate consistency pass. Compare the implementation and observed
behaviour against the User Guide, Developer Guide, manual-testing appendix,
documented commands, diagrams, examples, error descriptions, supported and
unsupported feature claims, and release status.

For each affected documented claim, classify it as accurate and verified,
inaccurate because the code differs, stale because the code changed,
incomplete because an important path is omitted, or not verifiable in the
current environment.

Do not edit documentation from this skill. State whether the
update-documentation skill must be rerun and identify the affected sections.

### 10. Produce actionable findings

Every finding must give the location, evidence, expected and actual behaviour,
impact, fix direction, test implications, documentation impact, and commands
or checks needed after fixing.

## Severity and routing rules

Use four severities:

- `Blocker`: prevents build, test, launch, required acceptance, or safe release.
- `High`: likely functional defect, data-loss risk, security issue, serious
  regression, or major architecture failure.
- `Medium`: meaningful robustness, maintainability, testability, or
  documentation problem.
- `Low`: local readability, naming, style, or cleanup issue.

The report must make these decisions explicitly:

### Rerun code-writing skill

Set to `YES` for implementation defects, unmet acceptance criteria, broken
build/runtime behaviour, data-loss or security risks, serious regressions,
major architecture problems, or missing defensive behaviour that requires code
changes. Otherwise set to `NO`.

### Rerun update-documentation skill

Set to `YES` for stale or missing documentation, diagrams, commands, manual
testing, or user-visible claims. Set to `AFTER CODE FIX` when a code change
must happen first. Set to `NO` when documentation remains accurate and the
finding has no documentation impact.

### Rerun code-review skill

Set to `AFTER FOLLOW-UP WORK` after implementation or documentation changes,
especially for Blocker or High findings and for changes affecting persistence,
validation, architecture, integration, tests, or user-visible behaviour.

### Independent human review

Set to `READY` only when required checks are complete or their limitations are
explicit, no unresolved Blocker or High findings remain, and the implementation
and documentation are consistent. Otherwise use `NOT READY` or `BLOCKED`.

## Report contract

The final report must contain:

```markdown
# Code Review Report

## Review status

- Review target:
- Issue/release:
- Review boundary:
- Overall result: PASS | FINDINGS | BLOCKED
- Independent review readiness: READY | NOT READY | BLOCKED

## Verification summary

| Area | Status | Command/evidence |
| --- | --- | --- |
| Compilation | ... | ... |
| Automated tests | ... | ... |
| Smoke tests | ... | ... |
| Static analysis | ... | ... |
| Packaging | ... | ... |
| Specification validation | ... | ... |
| User acceptance testing | ... | ... |
| Documentation consistency | ... | ... |

## Recommended next actions

- Rerun code-writing skill: YES | NO
- Rerun update-documentation skill: YES | NO | AFTER CODE FIX
- Rerun code-review skill: YES | NO | AFTER FOLLOW-UP WORK
- Independent human review: READY | NOT READY | BLOCKED
- Reason:

## Findings

### [severity] Finding title

- Category:
- Location:
- Affected change:
- Related requirement:
- Evidence:
- Expected behaviour:
- Actual behaviour:
- Reproduction or inspection path:
- Impact:
- Specification or acceptance criterion:
- Likely cause:
- Confidence:
- Required fix direction:
- Files or responsibilities likely involved:
- Tests to add or update:
- Documentation impact:
- Specification/user-testing impact:
- Verification after fixing:

## Positive observations

- ...

## Coverage and testing gaps

- ...

## Documentation consistency

- ...

## Limitations

- ...

## Review conclusion

- ...
```

## Implementation and validation phases

1. Confirm the plan and the exact skill package location.
2. Create `SKILL.md` with only shared policy, routing, workflow, and output
   rules.
3. Create the CS2103/T review-criteria reference.
4. Create the report-template reference if the report contract is too large
   for the entrypoint.
5. Create the project-checks reference with repository-specific discovery
   guidance.
6. Validate frontmatter and reference discoverability.
7. Exercise the skill against a realistic implemented change in an isolated
   temporary workspace.
8. Check that a deliberate implementation defect routes to code-writing,
   that a documentation-only defect routes to update-documentation, and that
   a clean review reports readiness for independent review.
9. Re-read the files and inspect the final diff.

## Constraints

- Do not modify project code, tests, documentation, specifications, logs, Git
  history, GitHub state, or build configuration during review.
- Do not install dependencies, plugins, or analysis tools without approval.
- Do not silently fix findings.
- Do not weaken tests or change specifications to make a review pass.
- Do not treat unavailable checks as passed.
- Do not present code smells as confirmed defects without evidence.
- Do not require every textbook principle mechanically.
- Do not report documentation as correct when the implementation is unverified.
