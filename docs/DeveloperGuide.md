# MoneyMap Developer Guide

## Build and checks

MoneyMap is a Java 25 JavaFX application built with the Gradle 9.1.0 wrapper.
Install Java 25, verify it with `java -version`, then run the authoritative
project checks from the repository root:

```text
./gradlew clean check build verifyPrototypes javadoc
```

`check` runs the JUnit suite and both production smoke executables:

- `verifyApplication` starts the production assembly, checks the stage, title,
  scene, and stylesheet, and opens all four navigation destinations.
- `verifyTransactionUi` loads the real Transactions FXML with a temporary data
  directory and verifies the list-first hierarchy, progressive form
  disclosure, focus transitions, type-compatible categories, valid creation,
  validation feedback, fallback assignment, newest-first history, combined
  history filtering, note search, empty results, filter reset, JSON save,
  reload, and visible rows.

`verifyPrototypes` separately verifies that all eight exploratory prototype
FXML resources still load. It does not test production behavior.

The current JUnit suite contains 50 tests covering the domain, validation,
service, path-resolution, and JSON-persistence rules. Useful focused commands
are:

```text
./gradlew test --tests 'cs3227.moneymap.domain.*'
./gradlew test --tests 'cs3227.moneymap.service.*'
./gradlew test --tests 'cs3227.moneymap.persistence.*'
./gradlew verifyTransactionUi
./gradlew verifyApplication
```

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
| JavaFX presentation | `MoneyMapApp` assembles dependencies and creates the stage. `ApplicationController` owns shell navigation. `TransactionController`, `transactions.fxml`, and `moneymap.css` implement the list-first transaction workflow. `SgdFormatter` formats display values. |
| Application/service | `TransactionService` loads state, supplies type-compatible categories, resolves fallback categories, creates transactions, and exposes non-mutating transaction-history queries. `DataRepository` is its persistence boundary. |
| Domain | `Transaction`, `TransactionType`, `MoneyAmount`, `Category`, `StarterCategoryCatalog`, and `ApplicationState` hold immutable data and enforce core invariants. |
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

1. The Transactions screen initially presents the real list and a single Add
   action. The form is progressively disclosed and focus moves to its first
   control.
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

## Domain and validation decisions

Money amounts are stored as `BigDecimal` through `MoneyAmount` and normalized
to two decimal places. The form parser accepts trimmed plain decimal strings
that begin with a digit and have zero, one, or two fractional digits. It rejects
negative-form input, excessive precision, currency symbols, grouping commas,
scientific notation, `.50`, and `1.`. Display formatting is centralized and
uses the fixed `S$` prefix with grouping and two decimal places.

Transaction dates are required. The form defaults to `LocalDate.now(clock)`;
the domain imposes no past or future restriction. Notes are normalized from
null to an empty string and limited to 200 Unicode code points.

The starter catalog creates nine Expense categories and five Income
categories. Starter IDs are deterministically derived from type and name. The
two `Uncategorised` entries have distinct identities and are marked as
permanent fallbacks. Category selectors are type-specific, while the service
also enforces compatibility so invalid controller or persisted input cannot
bypass the rule.

Editing, deletion, custom category management, budgets, and Dashboard
calculations are intentionally outside this increment. Transaction-history
filtering and note search are implemented.

## JSON persistence and recovery

`JsonDataRepository` currently writes schema version `1`. The saved document
contains all Issue #3 state: categories and transactions, including stable IDs,
types, exact amount strings, ISO dates, category references, fallback flags,
and notes. Future budget or category-lifecycle fields are not implemented in
this schema increment.

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

Loading validates the schema version, required lists and fields, UUID and enum
values, duplicate IDs, amount/date/note invariants, fallback categories, and
transaction category references. Malformed data, an unsupported version, or an
invalid relationship causes the main file to be moved to a free recoverable
name beginning with `moneymap.json.corrupt`. MoneyMap then exposes a recovery
warning and starts with the seeded safe state. In-app import and restoration
are not implemented, so the preserved file must be retained for manual
investigation or a later recovery feature.

## Testing strategy and evidence

The 50 JUnit tests use specification-based equivalence partitions and boundary
values:

- amount tests cover zero, positive values, scales zero through two, whitespace,
  large exact values, negative forms, scale three, malformed and formatted
  input, and null;
- transaction tests cover past, present, and future dates, missing dates,
  optional notes, 200/201-code-point boundaries, and category compatibility;
- catalog tests cover the exact starter sets and two distinct permanent
  fallbacks;
- service tests cover injected today, type-specific categories, Income
  creation, automatic fallback, mismatch rejection, save invocation,
  save-before-publish failure behavior, newest-first history ordering, each
  history filter, case-insensitive note search, combined queries, blank-query
  handling, empty results, and non-mutation;
- path tests cover configured development and packaged-JAR bases; and
- JSON tests cover first launch, versioned round-trip, malformed data,
  unsupported future versions, valid-main orphan temporary files, recovery of
  a valid temporary first save, preservation of an invalid temporary first
  save, invalid-date recovery from main and temporary files, and preservation
  of a valid file after a failed temporary write.

`TransactionUiSmokeTest` complements those tests at the integration level. It
checks the visible history controls, newest-first displayed results, combined
filters, case-insensitive note search, the no-results state, and Clear filters
in addition to the transaction-creation workflow. It does not replace manual
inspection: JavaFX resource lookup and focus-owner assertions cannot prove the
rendered layout is visually correct on every platform.

## Instructions for Manual Testing

Use a disposable directory so the test cannot alter personal data.

### Startup and navigation

1. Run `./gradlew clean build`.
2. Copy `build/libs/MoneyMap.jar` to an otherwise empty writable directory.
3. From that directory, run `java -jar MoneyMap.jar`.
4. Confirm the title is `MoneyMap — Student Budget Tracker` and Dashboard opens
   initially.
5. Open all four navigation destinations and confirm only Transactions exposes
   implemented financial behavior.

### Progressive transaction form and starter categories

1. Open Transactions and confirm the transaction list appears before the form
   area and the form is initially hidden.
2. Select **＋ Add transaction** and confirm the form appears below the list and
   focus moves to **Type**.
3. Switch between Income and Expense and confirm the selector shows exactly the
   type-compatible starter categories documented in the User Guide.
4. Enter unfinished values, select **Cancel**, and confirm no row is added and
   the form collapses.

### Valid and invalid transactions

1. Save Expense transactions with `0`, `12`, `12.3`, and `12.34`; confirm each
   displays two decimal places.
2. Save an Income transaction and confirm its amount has a plus sign and uses
   Income styling.
3. Try `-0.01`, `-0.00`, `1.234`, `S$1.00`, `1,000.00`, `.50`, `1.`, and blank
   input. Confirm each is rejected with text feedback and creates no row.
4. Save transactions dated in the past, today, and the future.
5. Save one transaction with an empty note and one with a 200-character note.
   Confirm a 201-character note is rejected.
6. Save one Income and one Expense without selecting a category. Confirm each
   displays `Uncategorised`; switching type must never expose a category of the
   other type.
7. Confirm a validation error leaves the form open and a successful save
   collapses it.

### Review and locate transaction history

1. Save transactions across at least two months, with both Income and Expense
   types, distinct categories, and distinguishable notes.
2. Confirm the transaction list is ordered with the latest date first.
3. Select each **Month**, **Type**, and **Category** filter separately and
   confirm only matching records remain visible.
4. Enter a word from a note in **Search notes**, then repeat using a different
   letter case. Confirm the same matching records remain visible.
5. Combine a month, type, category, and matching note query. Confirm every
   displayed row matches all active criteria.
6. Enter text that is absent from every note. Confirm the list shows **No
   matching transactions** and does not show the no-transactions message.
7. Select **Clear filters**. Confirm all saved rows return, then restart the
   application and confirm the same records remain saved.

### Restart persistence

1. Close MoneyMap after saving at least two distinct transactions.
2. Confirm `data/moneymap.json` exists beside `MoneyMap.jar`.
3. Reopen MoneyMap and return to Transactions.
4. Confirm the saved amounts, dates, categories, and notes are still displayed.

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
3. Confirm Add moves focus into the disclosed form and Cancel returns focus to
   Add.
4. Resize the window to its minimum and confirm the list, form, and navigation
   remain reachable and usable.

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
