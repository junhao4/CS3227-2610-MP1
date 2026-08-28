Date: 2026-08-27
Time: 20:11:25 +0800
Timestamp source: repository environment time captured when the logging draft was prepared; exact issue-planning-session start time is unknown.
Task: Plan a GitHub Issues decomposition of MoneyMap into thin vertical slices without creating GitHub issues.
Status: completed
Interaction range: user requested a GitHub issue plan – temporary issue-plan handoff verified
Evidence sources: conversation, AGENTS.md, specs/MP1-requirements.md, specs/ProductSpecification.md, specs/GitHub-Issue-Plan-draft.md, GitHub official documentation search results
Related logs:
- logs/2026-08-25-190344-budget-tracker-product-interview.md
- logs/2026-08-25-192238-budget-tracker-prototyping.md
- logs/2026-08-26-104535-budget-tracker-progress-bar-debugging.md
- logs/2026-08-26-221327-budget-tracker-requirements-follow-up.md
- logs/2026-08-27-191851-commit-and-push.md
Human verification: pending

## Interaction history

### 1. Request for vertical-slice issue planning

- User asked whether the product specification could be broken into thin vertical slices and represented in GitHub Issues using ready/blocked tags and native dependency relationships to form a DAG.
- AI response: Recommended a MoneyMap MVP parent issue with independently demonstrable vertical slices, native sub-issue hierarchy, native blocking dependencies, and a small status-label system.
- Research: Consulted official GitHub documentation for issue dependencies and sub-issues.
- Resulting action: Produced an initial issue-plan proposal without creating or editing GitHub issues.

### 2. Product specification as the source of truth

- User correction: “use product spec as the source of truth u dont have to copy the info into the prompt”.
- AI response: Accepted specs/ProductSpecification.md as the single source of truth. Issues should reference relevant sections and contain only delivery-specific information.
- Resulting rule: If an issue exposes ambiguity or conflicts with the specification, stop and ask instead of inventing a requirement.

### 3. Simplifying statuses and dependencies

- User decision: Use only status:ready, status:blocked, and status:done; do not initially create type, area, or priority labels; and use native GitHub dependencies for blocking information.
- AI response: Removed additional labels from the plan and made native dependencies authoritative rather than duplicating Blocked by text.
- Resulting rules: ready means blockers are complete; blocked means a native blocker is incomplete; done means acceptance criteria and tests are complete.

### 4. Dry-run plan

- User asked for the breakdown and plan before pushing to GitHub.
- AI proposed a MoneyMap MVP parent and eleven slices: production shell; persistence; typed categories and Uncategorised fallback; transaction creation; transaction history; edit/delete; monthly budgets; dashboard; export/import; validation/accessibility/acceptance; and documentation.
- AI proposed native dependency relationships between those slices and presented the plan as a dry run.
- Resulting action: No GitHub issue or label was created.

### 5. Durable handoff and reusable prompt

- User noted context pressure and requested a temporary Markdown handoff under specs.
- AI created specs/GitHub-Issue-Plan-draft.md containing source-of-truth rules, the parent and slices, statuses, native dependencies, acceptance summaries, issue-body policy, and the creation/verification sequence.
- User requested that the updated prompt also be placed in the handoff file.
- AI added a reusable prompt requiring ProductSpecification.md as authoritative, only three status labels, native dependencies, a dry-run DAG, explicit approval before GitHub changes, and no implementation or code push during issue creation.

## Work and verification

- Proposed, approved, and executed actions:
  - Researched current GitHub issue hierarchy and dependency capabilities using official documentation.
  - Designed an eleven-slice vertical decomposition derived from ProductSpecification.md.
  - Simplified the workflow to three status labels.
  - Made native GitHub dependencies authoritative.
  - Created specs/GitHub-Issue-Plan-draft.md.
  - Added the updated reusable planning prompt.
- Files changed:
  - specs/GitHub-Issue-Plan-draft.md
- Checks and observed results:
  - Read ProductSpecification.md before preparing the plan.
  - Re-read the handoff file after writing.
  - Confirmed it contains the source-of-truth rule, hierarchy, DAG, statuses, slice definitions, issue-body policy, and prompt.
  - Confirmed no GitHub issues, labels, or dependencies were modified.
  - Confirmed no application code was changed.
- Errors, limitations, or remaining uncertainty:
  - Issue numbers do not exist because creation was intentionally deferred.
  - The dependency plan should be checked against the live GitHub repository before creation.
  - ProductSpecification.md remains marked Draft pending final human review.
  - Application-relative persistence still requires careful implementation.

## Reflection notes

- What the AI did well:
  - Kept the product specification separate from the delivery plan.
  - Accepted the source-of-truth correction.
  - Reduced the status system to three labels.
  - Used native GitHub dependencies as the authoritative DAG.
  - Produced a durable handoff document.
- What the AI did poorly:
  - The first issue-plan response contained more labels and duplicated dependency information than necessary.
  - The first prompt copied too much product information instead of relying on ProductSpecification.md.
  - Several user corrections were needed before the simpler workflow was reached.
- Human judgement required:
  - Choosing ProductSpecification.md as the source of truth.
  - Choosing only ready, blocked, and done statuses.
  - Rejecting unnecessary labels and duplicated dependency text.
  - Deferring issue creation until the dry-run plan is reviewed.
- How prompts or approach evolved:
  - The request evolved from creating a GitHub DAG into a controlled dry-run workflow.
  - The user required the product specification, minimal labels, native dependencies, and a durable handoff.
- Prompting versus manual work:
  - AI decomposed the specification, checked dependency ordering, researched GitHub capabilities, and created the handoff.
  - Human judgement removed redundant issue content and avoided over-engineering.
- What to do differently next time:
  - Start from ProductSpecification.md immediately.
  - Propose the smallest status system first.
  - Use native platform relationships whenever they provide the needed structure.
  - Validate issue granularity and dependency direction before creating issues.
  - Update the handoff as soon as workflow rules change.

Human verification: approved
