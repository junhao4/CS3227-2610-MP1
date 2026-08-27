# AI-assisted session: budget tracker product interview

Date: 2026-08-25
Time: 19:03:44 +0800
Timestamp source: repository environment time captured when the interview was paused; exact interaction start time is unknown.
Task: Define the product design for a single-user JavaFX personal budget tracker by conducting a one-question-at-a-time requirements interview.
Status: ongoing
Interaction range: interview start – 2026-08-25 19:03:44 +0800
Evidence sources: conversation, `AGENTS.md`, `specs/MP1-requirements.md`, repository file listing, Money Manager official product pages
Related logs: none
Human verification: approved

## Interaction history

### 1. Interview format and primary user

- Exact prompt excerpt: User asked to read `specs/MP1-requirements.md`, consider “a single-user JavaFX personal budget tracker,” and “grill me relentlessly about every consequential product, domain, implementation, and testing decision, one question at a time.”
- AI response: Recommended narrowing the primary user to a university student and asking for a concrete recurring budgeting problem.
- User decision: “ok, student”.
- Resulting action: The interview was narrowed to student personal budgeting.

### 2. Recommendation with every question

- Exact prompt excerpt: “u should give me the question and your recommendation for every qn one at a time”.
- AI response: Agreed to provide a recommendation, one question, and then record the decision and consequences after each answer.
- Resulting action: All subsequent interview questions used that format.

### 3. Money Manager as reference

- Exact prompt excerpt: “ok, student, can u search the mobile app money manager shld be smth like that”.
- AI response: Reviewed Money Manager’s official listing and developer pages. Identified manual income/expense/transfer recording, category budgets, calendar and monthly views, charts, filters, recurring entries, bookmarks, multiple assets/accounts, backup/restore, and credit/debit-card features. Recommended using the basic interaction pattern as inspiration without copying the full scope.
- User decision: Continue with a student-oriented product inspired by the app.
- Resulting action: Proposed an MVP centred on manually recording transactions, category budgets, remaining balances, and monthly summaries.
- Evidence: Money Manager official listing, developer site, and help centre were consulted during the session.

### 4. Special financial concepts as categories

- Exact prompt excerpt: “investments, loans, credit cards can all be handled by allowing the user to create categories”.
- AI response: Warned that investments, loans, and credit cards have specialised account/liability semantics, but could be represented as ordinary labels if the app does not calculate balances, interest, repayment progress, or market value.
- User decision: “yes ordinary user created transaction category”.
- Resulting action: These names have no special behaviour and classify transactions only.

### 5. Transaction types, wallets, and transfers

- AI response: Initially recommended only `INCOME` and `EXPENSE` for the MVP and asked whether transfers should be out of scope.
- User correction: “i think we shld design it in a way that keeps it open for transfer and different wallets”.
- AI response: Recommended separating extensibility from current scope and modelling wallets/transfers conceptually without prematurely implementing them.
- User decision: “can record it as for a later release first”.
- Resulting action: The MVP uses one implicit personal-money pool and supports income and expense transactions only. Wallets, transfer logic, and wallet UI are deferred.

### 6. Transaction fields and category defaults

- AI response: Recommended that each MVP transaction contain an identifier, type, positive exact monetary amount, date, category, and optional note, with currency-safe exact representation rather than floating-point currency values.
- User decision: “ok”.
- AI follow-up: Recommended requiring categories for all transactions and providing a default `Uncategorised Income` category.
- User correction: “also for q6 maybe can have uncategorised expense also”.
- Resulting action: Every income and expense requires exactly one category. Default `Uncategorised Income` and `Uncategorised Expense` categories are available. Amounts are positive monetary values and notes are optional.

### 7. Category lifecycle

- AI response: Recommended allowing category creation, renaming, and archiving; preventing permanent deletion of categories used by transactions; allowing deletion of unused categories.
- User decision: “ok”.
- Resulting action: Used categories remain available for historical records but can be archived and excluded from new-transaction choices. Unused categories can be permanently deleted.

### 8. Budget periods and structure

- AI response: Recommended fixed calendar months for the MVP rather than custom month start dates or arbitrary periods.
- User decision: “ok”.
- Resulting action: Budgets and monthly summaries use calendar months.
- AI response: Recommended one explicit monthly budget amount per expense category, with income tracked separately and not automatically changing budgets.
- User decision: “ok”.
- Resulting action: The system reports budgeted amount, actual expenses, remaining amount, percentage used, and over-budget status per category.

### 9. Overspending and dates

- AI response: Recommended allowing expenses to exceed budgets and showing a negative remaining amount rather than blocking the transaction.
- User decision: “ok”.
- Resulting action: Over-budget expenses are valid and visibly marked.
- AI response: Recommended allowing today and past transaction dates only, excluding future-dated transactions.
- User correction: “no future dates is ok it makes testing easier for users i think”.
- Resulting action: Future-dated transactions are allowed. They are ordinary transactions associated with their selected calendar month and are not described as forecasts or guarantees.

### 10. Editing, deletion, and persistence

- AI response: Recommended editable transactions and deletion with confirmation, without audit history or undo.
- User decision: “ok”.
- Resulting action: Editing reuses validation; deletion is permanent after confirmation; no audit history or undo is planned for the MVP.
- AI response: Recommended automatic local persistence after successful changes and loading data at startup, with cloud sync, accounts, and bank integration out of scope.
- User decision: “ok”.
- Resulting action: The MVP is an offline, single-user application with automatic local save and reload.

### 11. Backup and reporting

- AI response: Recommended manual full-data export and import with validation and confirmation, while excluding cloud and scheduled backups.
- User decision: “ok”.
- Resulting action: Import/export is part of the MVP and must be tested for malformed or incompatible files.
- AI response: Recommended a monthly summary containing income, expenses, net balance, spending by category, category budget status, and one budget-versus-spending bar chart.
- User decision: “ok”.
- Resulting action: Advanced analytics, forecasts, and multiple chart types are out of scope.

### 12. Transaction history

- AI response: Recommended a transaction history view with newest-first sorting, month/type/category filters, and note-text search.
- User decision: “ok”.
- Resulting action: These history and filtering capabilities are part of the MVP.

### 13. Application areas and prototype exploration

- AI response: Proposed four JavaFX areas in one main window with persistent navigation: Dashboard; Transactions; Categories and Budgets; Data and Settings.
- User asked: “for q17 do u think its worth making 3 quick fxml prototype for me to view the ui?”
- Clarification: User meant “protoype different version of those screens”.
- AI response: Recommended two alternative static layouts for each of the first three areas and one simple prototype for Data and Settings.
- User decision: “ok”.
- Resulting action: Dashboard, Transactions, and Categories/Budgets each get two alternative disposable layouts; Data/Settings gets one simple layout. Prototypes use static sample data and no production business logic.
- User correction: “ok but u had 4 areas right”.
- AI response: Confirmed the fourth area and restored it to the plan.

### 14. Prototype setup and pause

- AI response: Inspected the repository and found no Java source, Gradle files, Maven files, or JavaFX setup. Explained that actual FXML prototypes would require a minimal reproducible Gradle/JavaFX scaffold and that adding dependencies requires user approval.
- User decision: “ok before we do this pause the interview, make sure the progress is saved so future agents know the current decisions and where to pick up / what needs to be done”.
- Resulting action: No prototype files, source files, build files, dependencies, issues, commits, or pushes were created during this session.

## Work and verification

- Proposed, approved, and executed actions:
  - Read `AGENTS.md` and `specs/MP1-requirements.md`.
  - Read the repository logging skill.
  - Inspected existing log naming and content.
  - Searched the web for current Money Manager features using official sources.
  - Inspected repository files for existing JavaFX/Gradle setup.
  - Conducted the product interview and recorded the decisions below.
  - Proposed this session log; user approved writing it.
- Files or external systems changed: This log file only. No source code, build files, dependencies, issues, commits, or pushes.
- Checks and observed results:
  - Confirmed the assignment requires a Java desktop app and AI-interaction logs.
  - Confirmed the repository currently has no build or JavaFX files.
  - Confirmed the product research was based on official Money Manager sources.
- Errors, limitations, or remaining uncertainty:
  - Exact persistence format and corrupt-file recovery are unresolved.
  - Currency and locale behaviour are unresolved.
  - Validation limits for amounts, dates, names, and budgets are unresolved.
  - Category-budget edge cases are unresolved.
  - UI layout selection is unresolved until prototypes are viewed.
  - Accessibility and keyboard interaction requirements are unresolved.
  - Testing levels and GUI-test scope are unresolved.
  - Assignment documentation alignment and product differentiation from Money Manager need further discussion.

## Current decisions and next pickup

### Settled decisions

- Primary user: university student.
- Core product: single-user personal budget tracker.
- Transactions: income and expense only in the MVP.
- Categories: user-created labels; investments, loans, and credit cards have no special behaviour.
- Default categories: `Uncategorised Income` and `Uncategorised Expense`.
- Category lifecycle: archive used categories; permanently delete only unused categories.
- Wallets and transfers: deferred to a later release.
- Budget period: fixed calendar months.
- Budgets: one explicit monthly budget per expense category.
- Overspending: allowed and visibly marked.
- Dates: past, present, and future dates allowed.
- Transactions: editable; deletions require confirmation; no undo or audit history.
- Persistence: automatic local save and reload.
- Backup: manual full-data export/import.
- Reporting: monthly summary and one budget-versus-spending bar chart.
- History: newest-first sorting, month/type/category filtering, and note-text search.
- UI areas: Dashboard, Transactions, Categories and Budgets, Data and Settings.
- Prototypes: two alternative static layouts for the first three areas and one simple Data and Settings layout.

### Next agent should ask about

1. Exact prototype layout alternatives and selection criteria.
2. Whether the user approves adding a minimal Gradle/JavaFX scaffold and dependencies.
3. Currency and locale behaviour.
4. Validation rules for amounts, dates, names, and budgets.
5. Persistence file format and corrupt-file recovery.
6. Whether expenses in categories without budgets appear in the dashboard.
7. Accessibility and keyboard interaction requirements.
8. Detailed testing levels and GUI-test scope.
9. Assignment documentation alignment.
10. Final product differentiation from Money Manager.

### Do not do yet

- Do not implement production functionality.
- Do not add Gradle files or JavaFX dependencies without explicit approval.
- Do not create issues, commit, or push.
- Do not write `specs/ProductSpecification.md` until the interview and prototype decisions are settled.

## Reflection notes

- What the AI did well or poorly:
  - Useful: challenged the difference between category labels and full financial-account semantics.
  - Useful: identified that wallets and transfers should be deferred rather than partially implemented.
  - Useful: supplied a recommendation with every question after the user requested that format.
  - Useful: checked the repository before recommending prototype setup.
  - Limitation: the first interview question did not include a recommendation; the user corrected the format.
  - Limitation: the AI initially recommended disallowing future dates; the user corrected that assumption.
  - Limitation: the prototype discussion briefly omitted the previously proposed fourth application area; the user caught the inconsistency and it was restored.
- Human judgement required:
  - Choosing a student as the primary user.
  - Deciding that investments, loans, and credit cards are labels rather than specialised features.
  - Choosing to defer wallets and transfers.
  - Accepting future-dated transactions.
  - Approving or rejecting each recommendation.
- How prompts or approach evolved:
  - The user changed the interview format to require a recommendation alongside every question.
  - The user introduced Money Manager as a reference point.
  - The user refined category defaults and requested multiple UI versions.
  - The user requested a durable pause point before prototype implementation.
- Prompting versus manual work:
  - The AI performed requirements decomposition, product comparison, scope challenge, and repository inspection.
  - The user made the consequential product decisions and corrected assumptions.
- What to do differently next time:
  - Maintain a visible decision ledger earlier.
  - Treat “open for future extension” separately from implemented scope.
  - Keep all previously agreed application areas visible while discussing prototypes.
  - Resolve exact prototype and dependency approval boundaries before creating files.

Human verification: approved
