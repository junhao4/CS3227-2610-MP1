# Product Specification: MoneyMap

Status: Draft

## Problem Statement

University students often have irregular income, recurring expenses, and multiple spending categories, but may not have a simple way to understand where their money is going within each month.

Existing financial applications can include features such as bank synchronisation, multiple accounts, investments, loans, credit cards, recurring transactions, and advanced analytics. These features may be useful, but they can also make a personal budgeting application unnecessarily complex for a student who mainly wants to:

- record income and expenses manually;
- organise transactions into categories;
- set monthly spending limits;
- see how much has been spent and how much remains;
- review previous transactions; and
- keep a local backup of their data.

The product should therefore focus on being a small, understandable, offline-first personal budget tracker for a single university student. It should help the user build an accurate picture of their spending without requiring bank integration or specialised financial-account semantics.

## Solution

MoneyMap is a single-user JavaFX desktop application for recording and reviewing personal income and expenses.

The MVP uses manual transaction entry and a single implicit personal-money pool. It does not model separate wallets, bank accounts, credit cards, loans, investments, or transfers yet. These concepts may be added in a later release, but in the MVP they can only be represented indirectly through ordinary user-created transaction categories.

The application is divided into four main areas:

1. Dashboard
2. Transactions
3. Categories and Budgets
4. Data and Settings

The Dashboard provides a high-level view of the selected month, including income, expenses, net balance, category spending, and budget status.

The Transactions area allows the user to create, edit, delete, search, filter, and review income and expense transactions.

The Categories and Budgets area allows the user to create and manage transaction categories and define monthly budgets for expense categories.

The Data and Settings area provides local-data and backup-related functions, including export and import.

The application stores data locally and automatically saves successful changes. It is intended to work offline and does not require an account, cloud service, bank connection, or network connection.

All monetary values use Singapore dollars (SGD). The preferred display format is `S$1,240.00`, with two decimal places.

## Numbered User Stories

### Transaction management

1. As a student, I want to record an income transaction so that my available money is reflected in the application.
2. As a student, I want to record an expense transaction so that I can track where my money is being spent.
3. As a student, I want to select whether a transaction is income or expense so that the application can process it correctly.
4. As a student, I want to enter a transaction amount with up to two decimal places so that ordinary SGD amounts can be recorded accurately.
5. As a student, I want to save a transaction with an amount of `S$0.00` so that I can use zero as a placeholder when the amount is temporarily unknown.
6. As a student, I want negative amounts to be rejected so that the meaning of each transaction remains unambiguous.
7. As a student, I want a transaction date to default to today so that entering ordinary transactions is quick.
8. As a student, I want to record transactions dated in the past, present, or future so that I can maintain records and plan upcoming entries.
9. As a student, I want to assign a transaction to an appropriate category so that I can understand my spending and income by type.
10. As a student, I want to leave a transaction note so that I can record useful context such as “lunch with classmates” or “monthly phone bill”.
11. As a student, I want transaction notes to be optional so that I can record simple transactions quickly.
12. As a student, I want to edit a transaction so that I can correct an amount, date, category, note, or transaction type.
13. As a student, I want to delete a transaction after confirming the deletion so that I can remove incorrect or unwanted records.
14. As a student, I want to review my transaction history in newest-first order so that recent activity is easy to find.
15. As a student, I want to filter transactions by month so that I can focus on a particular budgeting period.
16. As a student, I want to filter transactions by income or expense so that I can review one transaction type at a time.
17. As a student, I want to filter transactions by category so that I can investigate a particular area of spending or income.
18. As a student, I want to search transaction notes so that I can find entries containing a particular word or phrase.

### Category management

19. As a student, I want the application to provide common starter categories so that I can begin recording transactions without creating every category myself.
20. As a student, I want to create my own categories so that I can organise transactions according to my personal needs.
21. As a student, I want to use ordinary categories such as “Investments”, “Loans”, or “Credit Cards” without requiring the application to implement specialised financial behaviour.
22. As a student, I want categories to have an income or expense type so that an income transaction cannot accidentally be assigned to an expense category and vice versa.
23. As a student, I want to rename a category so that category labels remain useful as my budgeting needs change.
24. As a student, I want to archive a category that is no longer active so that it is not offered for new transactions while its historical transactions remain valid.
25. As a student, I want to permanently delete an unused category so that my category list does not become cluttered.
26. As a student, I want the application to prevent deletion of a category that is still used by transactions unless those transactions are reassigned first, so that historical records do not become invalid.
27. As a student, I want a transaction without a selected category to be assigned automatically to `Uncategorised` so that I can record it quickly without losing category integrity.
28. As a student, I want `Uncategorised` to be available for both income and expense transactions so that every transaction has a valid fallback category.
29. As a student, I want the same category name to be available once under Income and once under Expense when that is useful for my budgeting system.
30. As a student, I want category names to be unique within their type so that the category selector remains unambiguous.

### Budget management

31. As a student, I want to set a monthly budget for an expense category so that I can control spending in that category.
32. As a student, I want budgets to use fixed calendar months so that budget periods are predictable and easy to understand.
33. As a student, I want to see the budgeted amount, actual spending, remaining amount, percentage used, and budget status for each budgeted expense category.
34. As a student, I want to continue recording expenses after exceeding a budget so that the application records what actually happened instead of blocking transactions.
35. As a student, I want over-budget categories to be visibly marked so that I can notice overspending quickly.
36. As a student, I want expenses in categories without an explicit budget to be handled consistently in the Dashboard and summaries.
   Expenses in categories without a budget count toward overall spending and are displayed with a `No budget` status rather than a percentage-used or over-budget calculation.

### Dashboard and reporting

37. As a student, I want to view a monthly summary of income so that I know how much money entered my records during the month.
38. As a student, I want to view a monthly summary of expenses so that I know how much I spent during the month.
39. As a student, I want to view my monthly net balance so that I can compare income and expenses.
40. As a student, I want to view spending grouped by category so that I can identify major spending areas.
41. As a student, I want to view a budget-versus-spending bar chart so that I can compare planned and actual expense amounts visually.
42. As a student, I want the Dashboard to use the selected month consistently so that the figures and chart are not misleading.

### Data and settings

43. As a student, I want the application to save changes locally after successful operations so that my records are not lost when I close the application.
44. As a student, I want the application to reload my saved data when it starts so that I can continue where I left off.
45. As a student, I want to export my complete application data manually so that I can create a backup.
46. As a student, I want to import a previous backup so that I can restore my data or move it to another local installation.
47. As a student, I want invalid or incompatible import files to be rejected clearly so that an import does not silently corrupt my data.
48. As a student, I want the application to ask for confirmation before replacing current data during an import so that I do not overwrite my records accidentally.

## Implementation Decisions

### Platform and technology

The application will be implemented as a Java desktop application using JavaFX.

The project must use Java 25 because this is a project requirement. The current prototype uses the Gradle 9.1.0 wrapper because the earlier Gradle version was incompatible with Java 25.

The production application should use a maintainable separation between the domain model, application or service logic, persistence, JavaFX controllers and views, and presentation formatting.

The prototype code is static and exploratory. Production code should not depend on hard-coded prototype values or prototype-only navigation assumptions.

### Prototype lifecycle

The current FXML files were created to compare alternative layouts. The selected layouts are:

- Dashboard A: summary-first layout;
- Transactions A: transaction-list-first layout;
- Categories and Budgets B: category-card layout; and
- Data and Settings: the simple settings layout.

The prototypes contain static sample data and do not implement production persistence, calculations, validation, or import/export behaviour.

The visual design ideas may be reused in the production application, including layout hierarchy, spacing, typography, colour palette, card structure, transaction-list presentation, category-card presentation, and rounded budget progress bars.

The production screens should be implemented as real application views connected to the domain model rather than copied wholesale from the static prototypes.

The prototype should eventually be moved or isolated under a clearly named `prototype/` structure or equivalent source set before production implementation becomes the main project. A hidden `.prototype` directory is not preferred because it is less discoverable.

### Transaction model

The MVP supports two transaction types: Income and Expense.

Wallets, transfers, separate accounts, bank accounts, credit cards, loans, and investment accounts are deferred to a later release.

Each transaction should contain, at minimum:

- a unique identifier;
- transaction type;
- non-negative monetary amount;
- date;
  - category reference; and
- optional note.

Amounts are stored as exact monetary values rather than binary floating-point values.

Amounts use SGD, support at most two decimal places, may be zero, reject negative values, do not require a warning when zero is entered, and should be displayed with two decimal places.

Refunds, reversals, negative adjustments, and linked correction transactions are out of scope for the MVP. Users correct mistakes by editing or deleting the original transaction.

Transaction dates are required, default to today, may be in the past, present, or future, and determine the calendar month used by summaries and budgets.

Notes are optional and may contain up to 200 characters. They should be searchable by note text in the transaction history.

### Currency and formatting

The fixed currency for the MVP is Singapore dollars (SGD).

The preferred user-facing format is `S$1,240.00`, with two decimal places. Currency formatting should be performed by Java code or a presentation-formatting component rather than hard-coded throughout FXML.

FXML should avoid using a raw dollar sign as the beginning of a literal attribute value because `$` can be interpreted as an FXML expression prefix. Static prototype text should use an FXML-safe representation, such as an embedded `S$` value or an XML character entity where necessary.

Multi-currency support, currency conversion, exchange rates, and locale-specific currency switching are out of scope.

### Categories

The application provides a fixed small set of common starter categories for income and expenses. The initial starter list is:

The starter set is:

Expense categories:

- Food
- Transport
- Bills
- Shopping
- Entertainment
- Health
- Education
- Other Expense
- Uncategorised

Income categories:

- Salary
- Allowance
- Gift
- Other Income
- Uncategorised

Categories have an explicit type. Income categories may be assigned only to income transactions, expense categories may be assigned only to expense transactions, and budgets may be created only for expense categories.

The user-facing fallback label is `Uncategorised`. Internally, the application stores two separate permanent system categories, one for Income and one for Expense. If the user does not select a category, the application assigns the appropriate internal fallback category automatically. The two fallback categories cannot be renamed, archived, or deleted.

User-created categories are ordinary labels. Names such as “Investments”, “Loans”, and “Credit Cards” do not trigger specialised calculations or account behaviour.

Category-name validation:

- name is required;
- leading and trailing whitespace is trimmed;
- blank names are rejected;
- maximum length is 40 characters;
- duplicate names are rejected case-insensitively within the relevant category type; and
- spaces and ordinary punctuation are allowed.

The same display name may be used once for an Income category and once for an Expense category. The category type is part of the category identity, and the interface should show categories in type-specific contexts to avoid ambiguity.

Category lifecycle:

- a category with no associated transactions may be permanently deleted;
- a category used by transactions may not be permanently deleted until all associated transactions are reassigned;
- a used category may be archived;
- archived categories remain available for historical transactions but are excluded from new-transaction selection;
- ordinary starter and user-created categories are not protected from deletion or renaming; and
- the two permanent `Uncategorised` fallback categories are protected from deletion, renaming, and archiving.

### Budgets

Budgets apply only to expense categories.

The MVP uses at most one budget per expense category per calendar month. A budget is optional. If no budget is set, the category remains usable and its expenses count toward overall spending, but the category displays `No budget` and has no percentage-used or over-budget calculation.

Budget amounts may be zero and support at most two decimal places. Negative budget amounts and values with more than two decimal places are invalid. An explicit `S$0.00` budget means that the user intends to permit no spending in that category: zero spending remains within budget, while any positive spending is over budget. Percentage-used calculations are not displayed for a zero budget.

For each budgeted category and selected month, the application should calculate and display the budgeted amount, actual expense amount, remaining amount, percentage of budget used, and budget status.

Overspending is allowed. An expense should not be blocked because it causes a category to exceed its budget.

A category that exceeds its budget should be visibly marked. The selected prototype uses rounded progress bars whose state colours communicate the budget state:

Budget progress states use these thresholds:

- green: spending is at or below 50% of the budget;
- yellow: spending is above 50% through 80% of the budget; and
- red: spending is above 80% of the budget or exceeds the budget.

### Persistence

The application automatically saves successful changes locally and reloads the saved data at startup.

Automatic local persistence uses a versioned JSON file. JSON is readable during development, can represent categories, transactions, budgets, archived states, and metadata in one structured file, and can be validated and migrated as the application evolves.

The persistence file is stored relative to the application in a clearly defined `data/` directory beside the application. For a packaged application, the expected layout is:

```text
application-folder/
├── moneymap.jar
└── data/
    └── moneymap.json
```

The application directory means the directory containing the application JAR, not an arbitrary current working directory. During development, the equivalent configured application base directory may be used so that Gradle or an IDE launch does not change the data location unexpectedly. The persistence design must also define first-launch behaviour, missing-file behaviour, malformed-file behaviour, unsupported-version behaviour, and recovery if an interrupted save occurs. Saves should be atomic: the application writes a temporary file such as `moneymap.json.tmp` and replaces the previous valid file only after the new file has been written successfully.

If `data/moneymap.json` is malformed or uses an unsupported future version, the application must not overwrite it. It should show a clear error, preserve the invalid file under a recoverable name such as `data/moneymap.json.corrupt`, and start with a safe empty in-memory state so that the application remains usable. The user can recover data by importing a valid backup. Older supported versions may be migrated automatically before loading.

The MVP does not include cloud synchronisation, user accounts, network backup, or bank integration.

### Export and import

The application supports manual full-data export and import.

The export should include all information required to restore the local application state, including categories, archived-category state, transactions, budgets, and required application metadata.

Import should validate the complete file before replacing current data. The application should reject malformed files, reject incompatible versions, report validation errors clearly, ask for confirmation before replacing current data, and avoid leaving the application in a partially imported state.

Import replaces all current data after explicit confirmation. It does not merge records. This avoids duplicate transaction identifiers, category conflicts, budget conflicts, and ambiguous archived-category behaviour.

### UI structure

The application uses one main JavaFX window with persistent navigation between the four areas: Dashboard, Transactions, Categories and Budgets, and Data and Settings.

The selected prototype layouts are Dashboard A summary-first, Transactions A list-first, Categories and Budgets B category-card-first, and the simple Data and Settings layout.

The interface should make the selected month visible when displaying monthly summaries, budgets, and charts.

The MVP provides baseline desktop accessibility: every input has a visible label, every interactive control has an understandable label or accessible description, keyboard focus can reach every interactive control, tab order follows the visual order, dialogs support standard confirmation and cancellation keys, and invalid inputs receive clear text feedback. Budget state is not communicated by colour alone; progress states also include text such as `Within budget`, `Near limit`, or `Over budget`.

## Testing Decisions

The application should be tested at multiple levels.

### Domain and validation tests

The domain and service layers should have automated tests for income creation, expense creation, positive and zero amounts, negative amounts, amounts with zero to two decimal places, amounts with more than two decimal places, required dates, past/present/future dates, optional notes, note-length limits, category-type compatibility, category creation/renaming/archiving/deletion, category deletion with and without transactions, monthly budget calculations, under-budget/near-budget/over-budget states, overspending, expenses in categories without budgets, transaction editing, and confirmed/cancelled deletion.

### Persistence tests

Persistence tests should verify saving after a successful change, loading valid data, first launch with no data file, malformed data files, unsupported file versions, interrupted or failed saves, and preservation of existing valid data after a failed save.

Persistence tests must cover the versioned JSON format and the documented recovery strategy.

### Import and export tests

Import/export tests should verify export and import of a complete valid data set, preservation of categories/archived states/transactions/budgets, malformed files, incompatible files, confirmation before replacement, no partial replacement after failed validation, and importing empty but valid data.

Import/export tests must verify replacement after confirmation and must ensure that failed validation does not partially replace current data.

### JavaFX and view tests

The JavaFX layer should be tested sufficiently to verify that all main navigation areas can be opened, selected-month changes update displayed data, transaction creation and editing update the visible list, deletion confirmation works, category and budget actions update the relevant view, validation errors are presented to the user, progress-bar states use the correct visual state, and empty states are understandable.

The existing prototype smoke test verifies that all prototype FXML resources can be loaded. It does not verify visual appearance or complete interaction behaviour.

Most behaviour is covered by automated domain and service tests. The application also uses an FXML/navigation smoke test and focused manual acceptance testing for visible layout and interaction. A specialised automated GUI-testing framework is out of scope unless later evidence shows that it is necessary.

### Manual acceptance testing

Manual acceptance testing should cover the complete primary student workflow:

1. Start the application with no existing data.
2. Review the starter categories.
3. Record income.
4. Record several expenses.
5. Assign expenses to categories.
6. Create monthly budgets.
7. Review the Dashboard.
8. Filter and search transaction history.
9. Edit and delete a transaction.
10. Archive or delete a category according to its usage.
11. Export the data.
12. Modify or clear the local data.
13. Import the exported data.
14. Confirm that the restored data is correct.

The acceptance checklist must also cover first launch with no data, starter categories, positive/zero/two-decimal and rejected negative amounts, past/present/future dates, automatic `Uncategorised` fallback, category creation/renaming/archiving/deletion rules, monthly budgets including unset and zero budgets, overspending, Dashboard summaries and chart states, transaction filtering and note search, editing and confirmed deletion, automatic persistence and restart, malformed-file recovery, export and replacement import, and keyboard and accessibility basics.

## Out of Scope

The following are out of scope for the MVP:

- multiple users;
- user accounts and authentication;
- cloud synchronisation;
- bank or card synchronisation;
- automatic transaction importing;
- separate wallets or accounts;
- transfers between wallets or accounts;
- specialised investment tracking;
- specialised loan tracking;
- specialised credit-card tracking;
- interest calculations;
- repayment schedules;
- market prices or portfolio valuation;
- multi-currency support;
- currency conversion;
- recurring transactions;
- scheduled transactions;
- negative transaction amounts;
- refund or reversal transactions;
- linked adjustment transactions;
- audit history;
- undo after deletion;
- advanced forecasting;
- financial advice;
- arbitrary budget periods;
- custom month-start dates;
- multiple chart types;
- mobile and web versions; and
- network-based features.

Investments, loans, and credit cards may still appear as ordinary user-created categories. Their names alone do not activate financial-account behaviour.

## Further Notes

### Product differentiation

The application is inspired by common personal-budgeting workflows, including transaction entry, categories, budgets, summaries, charts, and backup. It is intentionally narrower than a full financial-management application.

Its scope is differentiated by focusing on a single university student, manual local-first tracking, a small number of understandable concepts, no account or bank integration, no specialised financial-account semantics, explicit monthly category budgets, and simple transaction history and reporting.

The product should not copy another application’s exact interface, branding, assets, or complete feature set.

### Prototype findings

The prototypes were used to compare alternative layouts before production implementation.

The selected findings were:

- Dashboard A provides the preferred summary-first arrangement;
- Transactions A provides the preferred list-first arrangement;
- Categories and Budgets B provides the preferred category-card arrangement;
- the Data and Settings prototype is acceptable as the initial direction;
- rounded progress bars are preferred;
- progress-bar colours should communicate budget state; and
- visual confirmation by the user is required for UI decisions because an FXML smoke test cannot prove visual correctness.

### Known implementation lesson

During progress-bar debugging, the automated FXML smoke test passed even while the user observed incorrect visual styling. A controlled user experiment—changing all bars to the same `budget-progress` style class—provided more useful diagnostic evidence than the initial CSS-only reasoning.

This demonstrates that structural tests and visual verification are separate concerns. The production testing plan should not treat successful FXML loading as proof that the rendered interface is visually correct.

### Documentation and project process

The final product specification must be kept consistent with the implemented application, `docs/UserGuide.md`, `docs/DeveloperGuide.md`, `docs/Reflections.md`, and the interaction logs in `logs/`.

The interaction logs preserve the AI-assisted development process, including recommendations, user corrections, verification steps, and reflection points. The product specification should describe the resulting product decisions rather than reproduce the full conversation.

### Remaining decisions before implementation

The product specification is ready for final review. Its documentation should describe MoneyMap as a focused manual, local-first student budget tracker. It may be inspired by common budgeting workflows, but it must explicitly state that it does not replicate another application’s interface or specialised financial features.
