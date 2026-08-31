# MoneyMap Developer Guide

## Build and checks

MoneyMap is a Java 25 JavaFX application built with the Gradle 9.1.0 wrapper.
Install Java 25, verify it with `java -version`, then run the authoritative
project checks from the repository root:

```text
./gradlew clean check build verifyPrototypes javadoc
```

`check` runs the Checkstyle gates, JUnit suite, and all production smoke
executables:

- `checkstyleMain`, `checkstyleTest`, `checkstyleSmoke`, and
  `checkstylePrototype` enforce the configured mechanical Java conventions
  across every Java source set.

- `verifyApplication` starts the production assembly, checks the stage, title,
  scene, and stylesheet, and opens all four navigation destinations.
- `verifyTransactionUi` loads the real Transactions FXML with a temporary data
  directory and verifies the list-first hierarchy, progressive form
  disclosure, focus transitions, type-compatible categories, valid creation,
  validation feedback, fallback assignment, newest-first history, combined
  history filtering, note search, empty results, filter reset, date-grouped
  cards, no-note and wrapped-note presentation, the overflow action menu,
  maximum-amount feedback, editing, confirmed and cancelled deletion, JSON
  save, reload, and visible rows.
- `verifyCategoryUi` loads the real Categories and Budgets FXML with a temporary
  data directory and verifies custom category creation, normalization,
  duplicate validation, type scoping, visible feedback, the configured vertical
  scroll container, active/archived-view switching, rename/archive/restore
  management-dialog progressive disclosure, reassignment/deletion confirmation
  flows, fallback protection, recurring and month-only budget saving,
  month-stepper navigation and current-month defaulting,
  explicit-zero and invalid-amount feedback, budget removal and scope
  preservation, stable category-card sizing and progress visibility,
  blocked-deletion feedback, archived-category filtering, and reload.
- `verifyDataAndSettingsUi` loads the real Data and Settings FXML with a
  temporary data directory. It verifies the visible and accessible export
  control, complete-state backup round-trip, success feedback, failure
  feedback, and preservation of current local data after a failed export.

`verifyPrototypes` separately verifies that all nine exploratory prototype
FXML resources still load. It does not test production behavior.

The current JUnit suite covers the domain, validation,
service, path-resolution, and JSON-persistence rules. Useful focused commands
are:

```text
./gradlew test --tests 'cs3227.moneymap.domain.*'
./gradlew test --tests 'cs3227.moneymap.service.*'
./gradlew test --tests 'cs3227.moneymap.persistence.*'
./gradlew verifyTransactionUi
./gradlew verifyCategoryUi
./gradlew verifyDataAndSettingsUi
./gradlew verifyApplication
```

The Checkstyle configuration is in `config/checkstyle/checkstyle.xml` and uses
the Gradle Checkstyle tool at version 10.26.1. It enforces UTF-8 source files,
no tabs, a 120-character hard line limit, explicit imports, naming conventions,
braces, whitespace, and unused-import checks. The `javadoc` task and review
checklist remain responsible for the project’s header-comment requirements and
for higher-level CS2103/T guidance such as KISS, SLAP, cohesion, and
readability, which cannot be assessed reliably by this ruleset alone.

An FXML or JavaFX smoke test does not prove visual correctness, complete
keyboard behavior, or platform accessibility. Those properties remain part of
manual acceptance testing.

## Packaging and launch

The build uses OpenJFX 17.0.7 artifacts and Shadow 9.4.3. The runtime
configuration includes JavaFX base, controls, FXML, and graphics artifacts for
64-bit Windows, 64-bit Linux, and Apple silicon macOS. Gson 2.13.1 provides
strict JSON mapping. JUnit Jupiter 5.10.0 and JUnit Platform Launcher 1.10.0 are
test-only dependencies.

`./gradlew clean build` produces the executable fat JAR:

```text
build/libs/MoneyMap.jar
```

The archive uses the non-`Application` `Launcher` entry point and bundles
JavaFX, Gson, and the required platform-native libraries. To verify the
peer-tester workflow, copy only the JAR to a writable empty directory and run:

```text
java -jar MoneyMap.jar
```

The packaged launch has been verified on Apple silicon macOS with Java 25.
Windows and Linux artifacts are packaged but have not been executed. The
classpath-based JavaFX fat-JAR pattern can emit an upstream unnamed-module
warning; it did not prevent the verified macOS launch.

After the first successful transaction, packaged state is stored relative to
the JAR rather than the process working directory:

```text
application-folder/
├── MoneyMap.jar
└── data/
    └── moneymap.json
```

During development, `ApplicationDirectoryResolver` searches upward from the
compiled code location for the project `build.gradle`. The
`moneymap.baseDir` JVM property can instead provide an explicit development or
test base directory. Tests always inject temporary base directories and do not
read or overwrite user data.

## Architecture

Production code is separated into presentation, application/service, domain,
and persistence responsibilities:

| Area | Main responsibilities |
| --- | --- |
| JavaFX presentation | `MoneyMapApp` assembles dependencies and creates the stage. `ApplicationController` owns shell navigation. `TransactionController`, `CategoryController`, and `DataAndSettingsController` connect their respective FXML workflows to the service. `SgdFormatter` formats display values. |
| Application/service | `TransactionService` loads state, supplies type-compatible categories, validates and creates or updates transactions and custom categories, resolves fallback categories, safely removes transactions, reassigns or deletes categories, configures monthly expense budgets, calculates category spending and budget state, exposes non-mutating transaction-history queries, and delegates complete-state backup export. `DataRepository` is its persistence boundary. |
| Domain | `Transaction`, `TransactionType`, `MoneyAmount`, `Category`, `Budget`, `StarterCategoryCatalog`, and `ApplicationState` hold immutable data and enforce core invariants. |
| Persistence | `ApplicationDirectoryResolver` chooses a stable application base. `JsonDataRepository` maps state to versioned JSON and performs load, validation, atomic replacement, and corrupt-file recovery. |

The dependency direction is presentation → service → domain. Persistence
implements the service-owned `DataRepository` interface and maps persistence
DTOs to domain objects. `MoneyMapApp` is the composition root that supplies the
concrete JSON repository, system clock, and UUID generator. Production code
does not depend on the prototype source set.

Project source sets are:

- `src/main/` — production classes, FXML, and CSS;
- `src/test/` — JUnit domain, service, and persistence tests;
- `src/smoke/` — executable production JavaFX integration checks; and
- `src/prototype/` — static exploratory layouts and their resource smoke test.

Only `src/main/` and runtime dependencies are included in `MoneyMap.jar`.

### Transaction creation flow

1. The Transactions screen initially presents the real ledger, its search and
   filter controls, and a single Add action in the ledger toolbar above the
   daily cards. Add or Edit swaps that content for a focused in-page form, and
   focus moves to its first control. Returning to the ledger keeps its existing
   query controls intact.
2. `TransactionController` passes the selected type, raw amount, date,
   optional category ID, and note to `TransactionService`.
3. The service resolves an omitted category to the permanent Income or Expense
   fallback, rejects an unknown or incompatible category, parses the exact
   decimal amount, and constructs the immutable transaction.
4. The service creates a candidate `ApplicationState` and asks
   `DataRepository` to save it.
5. Only after the save succeeds does the service publish the candidate as its
   in-memory state. A failed save therefore leaves the previous visible and
   persisted state unchanged.
6. The controller rerenders the real list and collapses the form. Validation or
   persistence failures leave the form open with text feedback.

The clock and UUID supplier are injected, allowing tests to use a fixed date
and deterministic transaction identity. Production uses the system clock and
random UUIDs.

### Transaction correction and deletion flow

The list groups same-date transactions into one daily card, headed by that
calendar date. A row uses **No note** for an empty optional note and allows a
long note to wrap in the available description area. Its single **⋯** menu
contains **Edit transaction** and **Delete transaction** actions. Edit reuses
the transaction form and preserves the transaction ID; on save,
`TransactionService.updateTransaction` constructs a replacement `Transaction`
using the same validation and category-resolution path as creation. It creates
an immutable candidate through `ApplicationState.withUpdatedTransaction`, saves
the candidate, and publishes it only after a successful save.

An archived category normally cannot be chosen for a new transaction or as a
replacement during an edit. The one intentional exception is the transaction's
own existing archived category: the edit form keeps that historical reference
selected so a correction to another field does not silently recategorise it.

Delete is deliberately separate from the state operation. The controller first
shows JavaFX's standard confirmation dialog. Only its **OK** result invokes
`TransactionService.deleteTransaction`; **Cancel** leaves service state and the
JSON file untouched. A confirmed deletion produces an immutable state without
the selected ID, then follows the same save-before-publish ordering. This keeps
the confirmation decision in the presentation layer while the service remains
usable from non-JavaFX callers.

### Transaction history query and display

`TransactionController` supplies the optional month, type, category ID, and
note-query values from the visible history controls to
`TransactionService.findTransactions`. The service applies every supplied
criterion and returns a new immutable result list ordered by transaction date
descending. Note matching is case-insensitive after trimming the query; null,
empty, and whitespace-only queries do not constrain the results. Querying
never changes `ApplicationState`, so clearing filters restores the existing
saved history without a persistence operation.

The controller derives selectable months from saved transaction dates and
offers all current categories as history filters. It displays a separate
no-results message when records exist but none match the active criteria. This
is a presentation distinction only; it does not alter the stored state.

### Custom category creation flow

`CategoryController` submits the selected type and raw name to
`TransactionService.createCategory`. The service strips surrounding whitespace,
validates the 40-code-point limit, rejects case-insensitive duplicates within
the selected type, constructs a non-fallback `Category`, and saves a candidate
`ApplicationState` before publishing it. Because the state is shared by
subsequently loaded views, a newly created category is available to the
compatible transaction selector without a second category-management store.

### Category lifecycle flow

`CategoryController` defaults to an active-category card view with category
creation hidden. The **＋ New category** control reveals the form, while the
archived-category view hides both. Each category card exposes only one **Manage** button; its dialog
progressively discloses valid lifecycle actions according to the category's
state. Ordinary categories offer Rename and Archive or Restore. Used ordinary
categories offer Reassign and show a disabled Delete action with the required
reassignment explanation; unused ordinary categories offer Delete. Permanent
fallbacks instead show their protected-state explanation. Rename uses a
standard text-input dialog; Archive, Reassign, and Delete use confirmation
dialogs. Reassignment first restricts choices to active categories of the same
type, then changes all transactions referring to the source category in one
candidate state. The service validates these rules and saves a candidate state
before publishing it. Restore also rejects a case-insensitive name clash with
an active category of the same type, directing the user to rename the archived
category first. `ApplicationState` rebuilds transaction category references by
stable category ID when a category changes or is reassigned, and refuses a
deletion that would leave a dangling reference. `categoriesFor` excludes
archived categories from new transaction selection. History filters use
`allCategories`, so archived categories remain discoverable there.

### Monthly budget flow

The Categories and Budgets landing view keeps category creation and lifecycle
controls visible as a responsive card grid. An Expense card's **Manage** action
opens the existing category-management dialog; choosing **Set budget** or
**Edit budget** then opens the existing full-screen budget manager with the
selected category's detail editor and the all-category list hidden. The
editor's labelled month stepper defaults to the service clock's current month
and controls the effective-budget context. **← Back to categories** is the
editor's only exit; there is no redundant editor Close action.

The panel displays the recurring-from-this-month-onward and selected-month
values independently. Its **Set** or **Change** actions reveal only the plain
amount input for the value the user chose; the category and month are fixed by
the focused editor context. `TransactionService.recurringBudgetFor` and
`monthOnlyBudgetFor` support this presentation without exposing mutable state.
When a value exists, the editor also exposes **Remove** for that scope. The
editor also exposes recurring removal when the selected month has no active
value but an active recurring version is scheduled later, because removal
covers that later version from the selected month onward. Removing a month-only
value leaves the recurring scope intact. Removing a recurring value creates a
stop marker from the selected month onward, removes later active versions, and
retains earlier recurring history.
After a save or removal, `CategoryController.showCategories()` re-renders the
category cards before returning to the landing view, so the displayed budget
state is not stale. Card monetary summaries use a fixed-width label with
ellipsis overrun; this contains very large valid amounts without imposing a
new monetary cap. The focused editor remains the place to inspect the full
amount.
The service rejects Income categories and uses `MoneyAmount` validation, then
constructs an immutable `Budget` whose identity is the category UUID and either
a recurring version with an effective `YearMonth` or a `YearMonth` override.
`ApplicationState.withRecurringBudget` replaces recurring versions from the
selected month onward; `budgetFor` resolves a matching month-only value before
the latest recurring version effective for the selected month. Inactive
recurring stop markers represent the absence of a budget from their effective
month onward. Thus there is at most one effective budget per category/month.
As for transactions and category changes, the service saves the candidate state
before publishing it.

`spendingFor`, `percentageUsed`, and `isOverBudget` provide the calculation
rules used by the Dashboard budget rows. `DashboardController` derives the
selected-month income, expenses, net balance, budget states, and three most
recent transactions from the service without changing persistence boundaries.
They include all matching Expense transactions even when no budget exists;
overspending is never a transaction-validation error. Percentage is absent for
an unset or explicit-zero budget, while a zero budget is over budget as soon
as its actual spending becomes positive.

## Domain and validation decisions

Money amounts are stored as `BigDecimal` through `MoneyAmount` and normalized
to two decimal places. The form parser accepts trimmed plain decimal strings
that begin with a digit and have zero, one, or two fractional digits. It rejects
negative-form input, excessive precision, currency symbols, grouping commas,
scientific notation, `.50`, and `1.`. `Transaction` additionally rejects a
value above `9999999.99`; this is a transaction-recording bound, rather than a
general `MoneyAmount` bound, so monthly budgets retain their existing monetary
validation. Display formatting is centralized and uses the fixed `$` prefix
with grouping and two decimal places.

Transaction dates are required. The form defaults to `LocalDate.now(clock)`;
the domain imposes no past or future restriction. Notes are normalized from
null to an empty string and limited to 200 Unicode code points.

The starter catalog creates nine Expense categories and five Income
categories. Starter IDs are deterministically derived from type and name. The
two `Uncategorised` entries have distinct identities and are marked as
permanent fallbacks. Category selectors are type-specific, while the service
also enforces compatibility so invalid controller or persisted input cannot
bypass the rule. Ordinary categories may be renamed, archived, and restored.
Archived categories are retained for history but cannot be selected for new
transactions. Restoring returns a category to active selection unless its name
would clash with an active category of the same type. Permanent fallbacks cannot
be renamed, archived, or restored.

`Budget` applies only to an Expense category. It represents either a recurring
monthly version (optionally with an effective start `YearMonth`) or a fixed
`YearMonth` one-time budget. A recurring version may also be an inactive stop
marker. The state rejects dangling budget categories, Income-category budgets,
and duplicate entries within the same category/scope. A missing budget and an
explicit zero budget are separate states. The service computes exact SGD
spending with `BigDecimal`, permits spending beyond the limit, and intentionally
returns no percentage for an unset or zero budget. The Dashboard presents these
states using the approved B-style hero, budget rows, quick totals, and
three-item recent activity; its month ComboBox defaults to the service clock’s
current month and refreshes together.

Custom category creation, lifecycle management, transaction-history filtering,
and monthly budget configuration are implemented.

## JSON persistence and recovery

`JsonDataRepository` currently writes schema version `1`. The saved document
contains the current application state: starter and custom categories,
transactions, and monthly budgets, including stable IDs, types, exact amount
strings, ISO dates, category references, fallback flags, archived state, notes,
and budget category references, optional ISO months, recurrence flags, activity
flags, and exact amount strings. The schema version remains compatible with
older category records because a missing archived flag loads as false, with
pre-budget documents because a missing budget list loads as empty, and with
prior month-only budget records because a missing recurrence flag loads as
false. A missing recurring-version activity flag loads as active, preserving
legacy recurring baselines.

On first launch with neither a main nor temporary file, the repository returns
the fixed starter categories and an empty transaction list without creating a
file. A successful transaction creates `data/moneymap.json`.

For each save, the repository:

1. creates `data/` if needed;
2. serializes the complete candidate state to `moneymap.json.tmp`;
3. requests an atomic, replacing move to `moneymap.json`; and
4. falls back to a replacing move only when the filesystem does not support an
   atomic move.

The previous main file is not changed if the temporary write fails. At startup,
a valid main file always takes precedence over an orphan temporary file; the
temporary file is then removed. If no main file exists, the repository validates
the temporary file using the normal schema and domain-invariant checks. A valid
temporary file is promoted to the main file and returned with a recovery
warning. An invalid or unsupported temporary file is moved to a free name
beginning with `moneymap.json.corrupt`, produces a recovery warning, and leaves
the application with the seeded safe state.

Backup export uses the same schema mapping as local persistence. The Data and
Settings controller selects a destination through the native save dialog, then
asks `TransactionService` to export its current immutable state through
`DataRepository`. `JsonDataRepository` rejects the active data file and its
temporary file as destinations, writes only the selected backup path, and
reports an I/O failure without changing the active in-memory or local state.
Import remains deliberately out of scope for this slice.

Loading validates the schema version, required lists and fields, UUID and enum
values, duplicate IDs, amount/date/note invariants, fallback categories, and
transaction category references. Malformed data, an unsupported version, or an
invalid relationship causes the main file to be moved to a free recoverable
name beginning with `moneymap.json.corrupt`. MoneyMap then exposes a recovery
warning and starts with the seeded safe state. In-app import and restoration
are not implemented, so the preserved file must be retained for manual
investigation or a later recovery feature.

## Testing strategy and evidence

The JUnit tests use specification-based equivalence partitions and boundary
values:

- amount tests cover zero, positive values, scales zero through two, whitespace,
  large exact values, negative forms, scale three, malformed and formatted
  input, and null;
- transaction tests cover past, present, and future dates, missing dates,
  optional notes, 200/201-code-point boundaries, the `9999999.99` maximum and
  its rejection boundary, and category compatibility;
- catalog tests cover the exact starter sets and two distinct permanent
  fallbacks;
- service tests cover injected today, type-specific categories, Income
  creation, correction and deletion, automatic fallback, mismatch rejection, custom category creation
  and validation, rename/archive/restore rules and conflicts, compatible
  transaction use, save invocation, save-before-publish failure behavior,
  newest-first history ordering, each
  history filter, case-insensitive note search, combined queries, blank-query
  handling, empty results, non-mutation, recurring defaults, month-only
  overrides and their precedence, Expense-only validation, explicit zero
  budgets, unbudgeted spending, overspending, and zero-percentage omission;
- path tests cover configured development and packaged-JAR bases; and
- JSON tests cover first launch, versioned round-trip, malformed data,
  unsupported future versions, valid-main orphan temporary files, recovery of
  a valid temporary first save, preservation of an invalid temporary first
  save, invalid-date recovery from main and temporary files, recurring
  explicit-zero budget round-trip, edited/deleted-transaction round-trip, and preservation
  of a valid file after a failed temporary write, complete and starter-state
  backup export, archived-category/budget export preservation, and local-state
  preservation after an export failure.

`TransactionUiSmokeTest` complements those tests at the integration level. It
checks the visible history controls, newest-first displayed results, exact-date
groups, no-note and wrapped-note presentation, combined filters,
case-insensitive note search, the no-results state, and Clear filters in
addition to the transaction-creation workflow. It also checks the overflow
action menu, type-aware editing, maximum-amount feedback, and both standard
deletion outcomes before reloading the resulting JSON state.
`CategoryUiSmokeTest` checks
custom-category creation, validation, type scoping, active/archived switching,
restore controls, archived-category exclusion from new transactions,
month-stepper navigation, recurring default and month-only budget
configuration, removal of each budget scope,
preservation of the other scope, removal visibility after a recurring stop,
back-navigation refresh, large-amount card containment, stable
category-card heights, visible over-budget progress, the visible
monthly-default line when a one-time amount applies, and reload. These checks
do not replace manual
inspection: JavaFX resource lookup and focus-owner assertions cannot prove the
rendered layout is visually correct on every platform.
`DataAndSettingsUiSmokeTest` verifies export through the real FXML, controller,
service, and JSON repository. It covers a visibly labelled, accessible export
control; complete-data backup round-trip; successful export feedback; and
failure feedback that leaves local data unchanged. It cannot automate every
platform's native file chooser.

## Instructions for Manual Testing

Use a disposable directory so the test cannot alter personal data.

### Startup and navigation

1. Run `./gradlew clean build`.
2. Copy `build/libs/MoneyMap.jar` to an otherwise empty writable directory.
3. From that directory, run `java -jar MoneyMap.jar`.
4. Confirm the title is `MoneyMap — Student Budget Tracker` and Dashboard opens
   initially.
5. Open all four navigation destinations and confirm Transactions and
   Categories and Budgets expose the implemented financial behavior.

### Progressive transaction form and starter categories

1. Open Transactions and confirm the ledger, its search/filter band, and the
   **＋ Add transaction** action above **Your transactions** are visible while
   the form is hidden.
2. Select **＋ Add transaction** and confirm the ledger is replaced by a focused
   form, with focus on **Type**.
3. Switch between Income and Expense and confirm the selector shows exactly the
   type-compatible starter categories documented in the User Guide.
4. Enter unfinished values, select **Cancel** or **← Back to transactions**,
   and confirm no row is added and the ledger returns with its query unchanged.

### Valid and invalid transactions

1. Save Expense transactions with `0`, `12`, `12.3`, and `12.34`; confirm each
   displays two decimal places.
2. Save an Income transaction and confirm its amount has a plus sign and uses
   Income styling.
3. Save `9999999.99`, then try `10000000`; confirm the former saves and the
   latter is rejected with feedback naming the maximum.
4. Try `-0.01`, `-0.00`, `1.234`, `$1.00`, `1,000.00`, `.50`, `1.`, and blank
   input. Confirm each is rejected with text feedback and creates no row.
5. Save transactions dated in the past, today, and the future.
6. Save one transaction with an empty note and one with a 200-character note.
   Confirm a 201-character note is rejected.
7. Save one Income and one Expense without selecting a category. Confirm each
   displays `Uncategorised`; switching type must never expose a category of the
   other type.
8. Confirm a validation error leaves the form open and a successful save
   collapses it.

### Review and locate transaction history

1. Save transactions across at least two months, with both Income and Expense
   types, distinct categories, and distinguishable notes.
2. Save two transactions on one date. Confirm the transaction list is ordered
   with the latest date first, shows that date once, and contains both rows in
   one bordered daily card.
3. Confirm an empty note reads **No note**. Give another row a long note and
   confirm the text wraps without obscuring its amount or **⋯** action.
4. Select each **Month**, **Type**, and **Category** filter separately and
   confirm only matching records remain visible.
5. Enter a word from a note in **Search notes**, then repeat using a different
   letter case. Confirm the same matching records remain visible.
6. Combine a month, type, category, and matching note query. Confirm every
   displayed row matches all active criteria.
7. Enter text that is absent from every note. Confirm the list shows **No
   matching transactions** and does not show the no-transactions message.
8. Select **Clear filters**. Confirm all saved rows return, then restart the
   application and confirm the same records remain saved.

### Edit and delete transactions

1. Create an Expense transaction, open **⋯** in its row, select **Edit
   transaction**, and confirm the form contains its saved values and identifies
   itself as **Edit transaction**.
2. Change the type to Income. Confirm only Income categories are offered, then
   select one, change the amount, date, and note, and select **Save changes**.
   Confirm the row and its values update without creating a second record.
3. Attempt an invalid edited amount. Confirm the correction form remains open
   and the original saved values remain in the list after cancelling.
4. Open **⋯**, select **Delete transaction**, and confirm the standard dialog
   identifies it and its **Cancel** action leaves the row in place.
5. Open **⋯** again, select **Delete transaction**, and accept **OK**. Confirm
   only that row disappears,
   then restart MoneyMap and confirm the edit remains while the confirmed
   deletion does not return.

### Create and use custom categories

1. Open **Categories and Budgets** and confirm the type selector, name field,
   **Create category** button, and current-category list are visible.
2. Create `Investments` as an Income category and `Loans` as an Expense
   category. Confirm each appears with its type in the list.
3. Try a blank name, a name longer than 40 characters, and a case-insensitive
   duplicate within one type. Confirm each attempt shows validation feedback
   and does not add a second category.
4. Create the same display name once under Income and once under Expense.
   Open Transactions and confirm each type exposes only its compatible custom
   category.
5. Create enough categories for the list to exceed the window height. Scroll
   the Categories and Budgets page vertically and confirm every category is
   reachable.
6. Close and reopen MoneyMap. Confirm the custom categories remain available.

### Rename, archive, and restore categories

1. Create an ordinary category in **Categories and Budgets**, select
   **Manage**, then **Rename**, and confirm a valid new name in the standard
   dialog.
2. Record a transaction using the renamed category and confirm the category
   name appears in transaction history.
3. Select **Manage**, then **Archive**. Confirm the standard confirmation
   dialog, then verify the category is absent from the active view and
   new-transaction selector.
4. Select **View archived categories**, verify the creation form is hidden, and
   confirm the category appears with a **Manage** action.
5. Select **Manage**, then **Restore**, and verify the category returns to the
   active view and matching new-transaction selector. Create a same-type active
   category with the archived name and confirm restore requires the archived
   category to be renamed first.
6. Verify both `Uncategorised` fallbacks show a protected-state explanation and
   remain unchanged.
7. Restart the application and verify the restored category remains active.

### Reassign and delete categories

1. Create an ordinary Expense category, record a transaction with it, and then
   select **Manage**. Verify **Delete** is disabled and the dialog says the
   transactions must be reassigned first.
2. Select **Reassign**, choose a different active Expense category, and confirm
   the selection and confirmation dialogs. Verify the transaction history now
   shows the chosen replacement category.
3. Select **Manage** again, then select **Delete** for the now-unused source
   category and confirm. Verify it is removed from the category list,
   new-transaction selector, and history filter while the transaction remains
   valid under its replacement category.
4. Verify that **Manage** shows `Uncategorised` as protected rather than
   offering changes. Confirm it can be selected only as a destination for the
   matching Income or Expense type.
5. Restart the application and confirm the deletion and reassigned transaction
   category remain saved.

### Configure monthly expense budgets

1. Open **Categories and Budgets** and confirm category creation and lifecycle
   controls are visible.
2. Select Food's **Manage**, then **Set budget**. Confirm the full-screen
   budget manager opens for Food with the all-category list hidden. Confirm the
   labelled month stepper defaults to the current month.
3. Select the recurring-budget **Set** action, enter `300.00`, and save. Move
   the month forward, change it to `500.00`, and save. Return to the earlier
   month and confirm it still shows `300.00`; later months show `500.00`.
4. Select **Set** beside the one-time override, enter `100.00`, and save.
   Change the month and confirm the recurring value remains unchanged. Remove
   each scope separately and confirm the effective value updates.
5. Create a recurring value starting in a future month, move the editor back
   to an earlier month, and confirm **Remove** is available. Remove it and
   verify the **Remove** action disappears, the future month no longer has a
   recurring budget, and any earlier budget remains unchanged.
6. Try blank, negative, and three-decimal values. Confirm each gives text
   feedback and leaves the previous saved budget unchanged.
7. Confirm Income cards do not offer budget actions. Save Expense transactions
   after setting both a normal and zero budget; neither transaction should be
   blocked.
8. Select **← Back to categories** and confirm it returns to the category
   cards with the changed budget state visible immediately. Confirm expense
   cards retain their progress bars while income cards remain compact. Enter a
   very large valid budget and confirm the card remains within its fixed layout
   (a long summary may show an ellipsis); open the focused editor to inspect the
   complete value. Restart MoneyMap and confirm the recurring versions and
   month-only budget remain persisted.

### Restart persistence

1. Close MoneyMap after saving at least two distinct transactions.
2. Confirm `data/moneymap.json` exists beside `MoneyMap.jar`.
3. Reopen MoneyMap and return to Transactions.
4. Confirm the saved amounts, dates, categories, and notes are still displayed.

### Export a complete backup

1. In the disposable installation, create at least one transaction, custom or
   archived category, and Expense budget.
2. Open **Data and Settings**, confirm **Export backup…** has visible text and
   can receive keyboard focus, then select it.
3. In the native save dialog, choose a new writable `.json` file outside the
   active `data/` directory and complete the save.
4. Confirm MoneyMap names the exported file in its success feedback and the
   live Transactions, Categories and Budgets, and Dashboard data remain
   unchanged.
5. Attempt an export to an unwritable location or a directory. Confirm clear
   failure feedback and verify the existing `data/moneymap.json` is unchanged.
6. Do not choose the active `data/moneymap.json` file. Import and restoration
   are not available in this increment.

### Recovery from invalid local data

Perform this only in the disposable test directory.

1. Close MoneyMap and keep a separate copy of the valid JSON file.
2. Replace the contents of `data/moneymap.json` with invalid JSON.
3. Start MoneyMap.
4. Confirm a data-recovery warning appears, the application remains usable with
   no transactions and the starter categories, and the invalid main file has
   been preserved under a name beginning with `moneymap.json.corrupt`.
5. Retain or remove the disposable directory after testing. Do not replace
   personal data with the test file; in-app restore is unavailable.

### Recovery from an interrupted first save

Perform this only in the disposable test directory.

1. Save one transaction and close MoneyMap.
2. Rename `data/moneymap.json` to `data/moneymap.json.tmp` so there is no main
   file but the temporary file is complete and valid.
3. Start MoneyMap.
4. Confirm a recovery warning appears, the transaction is available, and the
   temporary file has been promoted to `data/moneymap.json`.
5. To test the invalid path separately, replace the temporary file contents
   with invalid JSON before startup. Confirm MoneyMap starts safely with no
   transactions and preserves the temporary content under a name beginning
   with `moneymap.json.corrupt`.

### Keyboard and layout

1. Use Tab to reach the navigation, history-filter, and transaction controls;
   confirm focus is visible and follows the visual order.
2. Activate ordinary focused buttons with Space on macOS or Enter/Space on
   Windows and Linux.
3. Confirm Add moves focus into the focused form and Cancel or **← Back to
   transactions** returns focus to Add.
4. Open an Expense budget editor and use Tab to reach both month arrows and
   **← Back to categories**. Confirm the arrows have visible focus and move the
   selected month by one month per activation.
5. Resize the window to its minimum and confirm the list, form, budget editor,
   and navigation remain reachable and usable.

Automated tests support the functional expectations above. The Issue #3
keyboard workflow was manually confirmed on macOS. Rendered appearance, focus
indication, resizing, and Windows/Linux keyboard behavior remain manual
acceptance items; Windows and Linux execution is still unverified.

## Acknowledgements

- The production architecture, test-level distinctions, TDD process, test-case
  partitioning, and readability guidance follow the
  [CS2103/T software engineering textbook](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/index.html).
- JavaFX setup and the non-`Application` launcher follow the
  [SE-EDU JavaFX tutorial](https://se-education.org/guides/tutorials/javaFxPart1.html).
- Executable fat-JAR packaging uses the
  [Shadow Gradle plugin](https://gradleup.com/shadow/) following the
  [SE-EDU JAR guide](https://se-education.org/guides/tutorials/jar.html).
- Versioned JSON mapping uses the
  [Gson library](https://github.com/google/gson).
- Automated unit and integration tests use
  [JUnit 5.10.0](https://junit.org/junit5/docs/5.10.0/user-guide/).
- Java naming, layout, and accessibility-oriented labelling decisions were
  checked against the
  [SE-EDU Java coding standard](https://se-education.org/guides/conventions/java/).
- The list-first Transactions visual hierarchy and styling reuse decisions from
  the project's own
  [MoneyMap product specification](../specs/ProductSpecification.md) and
  prototype resources. No external source code or external visual assets were
  copied.
