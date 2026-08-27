Date: 2026-08-26
Time: 22:13:27 +0800
Timestamp source: repository environment time captured when the logging draft was prepared; exact resumed-interview start time is unknown.
Task: Resume and complete the Socratic requirements interview for MoneyMap while maintaining a living product specification.
Status: completed
Interaction range: interview resumed after the approved specification draft – logging request
Evidence sources: conversation, AGENTS.md, specs/MP1-requirements.md, specs/ProductSpecification.md, prior interview/prototype/debugging logs, repository diffs and verification commands
Related logs:
- logs/2026-08-25-190344-budget-tracker-product-interview.md
- logs/2026-08-25-192238-budget-tracker-prototyping.md
- logs/2026-08-26-104535-budget-tracker-progress-bar-debugging.md
- logs/2026-08-26-180900-product-specification-draft.md
Human verification: pending

## Interaction history

### 1. Resuming from the living specification

- Exact prompt excerpt: “ok carry on”
- AI response: Treated specs/ProductSpecification.md as the current decision source and resumed from the remaining unresolved decisions rather than restarting the interview.
- Recommendation: Keep category assignment internally valid and ask what should happen if an uncategorised category is deleted.
- Resulting issue: The user questioned whether uncategorised categories should instead be permanent fallbacks.

### 2. Permanent uncategorised fallback categories

- User prompt: “do u think it would be better if uncategorised is a permanent category meaning cannot delete and is the default if nothing is chosen”
- AI recommendation: Make Uncategorised Income and Uncategorised Expense permanent system categories that are automatically assigned when no category is selected.
- User follow-up: “can just call it uncategorised? or would it be better to have a blank category”
- AI recommendation: Display the shared label Uncategorised, but store separate internal fallback categories for Income and Expense. Reject a blank category because blank values look like missing or corrupt data and complicate filtering and reporting.
- Resulting decision: Use one user-facing label, Uncategorised, with separate internal Income and Expense fallback categories.

### 3. Category names across transaction types

- User prompt: “does this mean there cant be user made same category for expense and income?”
- AI response: Explained that category type is part of category identity, so the same name can exist once under Income and once under Expense.
- Recommendation: Allow duplicate names across different types, but enforce case-insensitive uniqueness within each type and show categories in type-specific contexts.
- User decision: “oh ok do this”
- Resulting action: Updated the specification to record type-specific category identity and same-name support across Income and Expense.

### 4. Starter categories and validation

- AI recommendation: Provide a small starter set: Income—Salary, Allowance, Gift, Other, Uncategorised; Expense—Food, Transport, Bills, Shopping, Entertainment, Health, Education, Other, Uncategorised.
- User decision: “ok”
- The AI later asked about category-name validation. The user correctly noted that this had been raised earlier.
- AI correction: Acknowledged that category validation was a pending confirmation rather than a new question.
- User decision: “ok follow”
- Resulting decision: Accept required names; trim leading and trailing whitespace; reject blank names; limit names to 40 characters; enforce case-insensitive uniqueness within the same type; and allow spaces and ordinary punctuation.
- Resulting action: Updated the specification to remove the category-validation TBD.

### 5. Budget availability and zero budgets

- AI recommendation: Distinguish an unset budget from an explicit zero budget.
- User clarification: “i do want to allow not setting budget tho”
- AI response: Specified that a category can have no budget, which should display No budget, while its expenses still count toward overall spending.
- AI follow-up recommendation: Allow an explicit S$0.00 budget as a deliberate “no spending permitted” budget, while omitting percentage calculations for zero budgets.
- User decision: “ok”
- Resulting decisions: Budgets are optional per expense category and calendar month; an unset budget displays No budget; unbudgeted expenses count toward overall spending; unbudgeted categories have no percentage-used or over-budget calculation; zero budgets are valid; zero spending is within a zero budget; positive spending exceeds a zero budget; and budget amounts support at most two decimal places and cannot be negative.

### 6. Persistence format and recovery

- AI recommendation: Use a versioned JSON file with atomic saves.
- User decision: “ok”
- Resulting decision: Automatic local persistence uses versioned JSON. The application writes a temporary file and replaces the previous valid file only after a successful save.
- AI recommendation for malformed or unsupported files: do not overwrite the invalid file; show a clear error; preserve it under a recoverable name; start with a safe empty state; allow recovery through backup import; and migrate older supported versions automatically.
- User decision: “ok”
- Resulting action: Added the persistence format and recovery policy to the specification.

### 7. Batching independent questions

- User prompt: “also u can give me a few qns at once and the corresponding recs if these qns are not dependent on each other”
- AI response: Agreed to batch independent decisions while keeping dependent decisions sequential.
- Resulting process change: The interview was accelerated by grouping independent choices.

### 8. Import, storage location, accessibility, and testing

- AI recommendations: Import should replace current data after explicit confirmation; persistence should use the operating-system application-data directory; the MVP should provide baseline keyboard and accessibility support; and automated domain/service tests, an FXML/navigation smoke test, and manual GUI acceptance testing should be used while a specialised GUI framework remains out of scope.
- User decision: “follow recs except qn2”
- Resulting decisions: Import replaces current data after confirmation; baseline accessibility is required; colour is not the only budget-state signal; automated domain/service tests plus FXML smoke tests and focused manual GUI testing are sufficient; and a specialised GUI-testing framework is out of scope for now.

### 9. Relative persistence and product naming

- User prompt: “store relative, moneymap”
- AI response: Compared application-relative storage with the operating-system application-data directory. Explained that a fixed application-adjacent data directory is easier to inspect and test for this coursework, provided it is not based on an arbitrary current working directory.
- Resulting decisions: Product name is MoneyMap; local persistence uses a clearly defined data/ directory relative to the application; and implementation must not rely on an arbitrary current working directory.
- Resulting action: Replaced Pocketwise with MoneyMap in the specification and documented the relative data/ location.

### 10. Progress thresholds and completion

- AI recommendation: Use green below 80%, yellow from 80% to 100%, and red above 100%.
- User decision: “i think 70% and 90%?, yes”
- Interpretation recorded: green below 70%, yellow from 70% through 90%, red above 90%.
- User also accepted the proposed complete acceptance-testing and documentation scope.
- Resulting decisions: The progress thresholds are green below 70%, yellow from 70% through 90%, and red above 90%; the acceptance checklist covers first launch, transactions, validation, dates, fallback categories, category lifecycle, budgets, summaries, filtering, persistence, malformed-file recovery, export/import, and accessibility; and documentation describes MoneyMap as a focused, manual, local-first student budget tracker distinct from other applications’ interfaces and specialised features.
- Resulting action: Removed remaining product-decision TBD markers from the specification and marked it ready for final review.

### 11. Specification consistency correction

- During verification before logging, the AI found two stale phrases: “subject to the unresolved category decision” and “A likely starter set”.
- Resulting action: Corrected them to reflect that category assignment and the starter list had already been settled.
- No product decision changed; this was a documentation consistency correction.

## Work and verification

- Proposed, approved, and executed actions:
  - Resumed the interview using the living specification as the current decision source.
  - Replaced the two type-specific user-facing uncategorised labels with the shared label Uncategorised while retaining separate internal fallback categories.
  - Settled same-name category support across Income and Expense.
  - Settled category-name validation and starter categories.
  - Settled optional budgets, zero budgets, and unbudgeted-category reporting.
  - Settled versioned JSON persistence and malformed-file recovery.
  - Settled replacement import behaviour.
  - Settled baseline accessibility and testing scope.
  - Settled relative persistence and renamed the product to MoneyMap.
  - Settled the 70%/90% progress-bar thresholds.
  - Settled the acceptance-testing and documentation scope.
  - Updated the living product specification after each meaningful decision cluster.
- Files changed:
  - specs/ProductSpecification.md
- Checks and observed results:
  - Inspected the specification before and after updates.
  - Searched for stale TBD, Pocketwise, and outdated category wording.
  - Confirmed the specification contains the requested sections.
  - Confirmed no remaining product-decision TBD markers remain.
  - Confirmed the document states that it is ready for final review and remains marked Status: Draft.
  - No production application functionality was implemented.
- Errors, limitations, or remaining uncertainty:
  - The AI repeated the category-validation question after it had already been raised earlier. The user identified this, and the AI corrected the process by treating it as a pending confirmation rather than a new question.
  - The AI initially suggested protecting ordinary default categories, then revised the model so common starter categories are ordinary editable categories while only the two Uncategorised fallback categories are permanent.
  - The exact operating-system-independent behaviour of an application-relative path will need to be handled carefully during implementation; the specification requires a fixed data/ directory beside the application, not an arbitrary working-directory path.
  - The product specification still needs a final human review before its status is changed from Draft to final.
  - Production code, user guide, developer guide, and reflection document have not yet been implemented or updated.

## Reflection notes

- What the AI did well:
  - Used the living product specification to continue the interview instead of restarting from the older chronological log.
  - Adapted the interview format when the user requested several independent questions at once.
  - Explained the modelling trade-off between a blank category and a permanent Uncategorised fallback.
  - Preserved type safety while allowing the same user-facing category name under Income and Expense.
  - Distinguished an unset budget from an explicit zero budget.
  - Converted the user’s product decisions into explicit validation, persistence, accessibility, and testing rules.
  - Updated the specification incrementally instead of waiting until the very end.
  - Detected and corrected stale wording during final verification.
- What the AI did poorly:
  - It initially repeated a previously raised category-validation question without clearly identifying it as a pending confirmation.
  - It temporarily introduced an automatic-recreation idea for uncategorised categories before the user rejected it.
  - It briefly left stale TBD markers and wording in the specification after decisions had been settled, requiring a later consistency pass.
  - It sometimes treated a short “ok” as acceptance of a broad recommendation; this was workable in context but should be made explicit when multiple sub-decisions are bundled.
- Human judgement required:
  - Choosing permanent fallback categories rather than allowing blank category values.
  - Choosing the shared Uncategorised display label with separate internal Income and Expense categories.
  - Allowing same-name categories across transaction types.
  - Choosing application-relative persistence over the operating-system application-data directory for the coursework.
  - Selecting MoneyMap as the product name.
  - Choosing the 70% and 90% progress thresholds.
  - Accepting the scope of the final acceptance tests and documentation.
- How the prompts or approach evolved:
  - The user changed the workflow from one question at a time to batching independent questions.
  - The user corrected the AI when a previously raised question was repeated.
  - The process evolved from historical logs as the main reference to a living product specification as the current decision ledger.
  - The interview moved from broad product scope to precise domain semantics, persistence recovery, accessibility, testing, and final documentation.
- Prompting versus manual work:
  - AI assistance was effective for presenting trade-offs, identifying edge cases, updating the specification, and checking for stale decisions.
  - Human judgement remained essential for deciding the product’s semantics, acceptable complexity, storage trade-off, naming, and visual thresholds.
  - The user’s corrections prevented the specification from accumulating contradictory fallback-category and persistence rules.
- What to do differently next time:
  - Before asking a question, search the living specification for both settled and pending decisions.
  - Label each question as new, pending confirmation, or already settled.
  - When a recommendation contains multiple sub-decisions, enumerate them and confirm each one explicitly.
  - Run a consistency check for stale TBD markers and old product names after every major specification update.
  - Keep implementation-specific path assumptions explicit so that “relative to the application” is not accidentally implemented as “relative to the current working directory”.

Human verification: approved
