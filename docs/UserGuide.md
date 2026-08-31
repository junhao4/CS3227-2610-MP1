# MoneyMap User Guide

MoneyMap is a local JavaFX desktop application for recording Income and Expense
transactions in Singapore dollars. The current build provides transaction
creation, transaction-history review, and automatic local persistence inside
the production application shell.

## Setup

1. Install Java 25 and confirm that `java -version` reports version 25.
2. Download or copy `MoneyMap.jar` into a writable folder of your choice.
3. Open a terminal in that folder.

The JAR includes JavaFX. You do not need the source project, Gradle, or a
separate JavaFX installation to run it. MoneyMap creates its `data/` directory
beside the JAR after the first successful transaction, so the containing folder
must be writable.

## Launch

Run:

```text
java -jar MoneyMap.jar
```

The MoneyMap window opens with Dashboard selected. Keep the terminal open while
using the application.

The packaged JAR contains JavaFX support for 64-bit Windows, 64-bit Linux, and
Apple silicon macOS. Packaged launch has been verified on Apple silicon macOS;
Windows and Linux remain unverified for this build.

## Navigate MoneyMap

Use the navigation controls on the left to open an area:

- **Dashboard** — placeholder for monthly summaries, category spending, and
  budget status.
- **Transactions** — records and displays real Income and Expense
  transactions.
- **Categories and Budgets** — creates and displays custom Income and Expense
  categories. Monthly budgets are not available yet.
- **Data and Settings** — placeholder for backup and data-management tools.

Click a navigation control, or press Tab until it has focus. On macOS, activate
a focused control with Space. On Windows and Linux, use Enter or Space.

## Record a transaction

1. Open **Transactions**.
2. Select **＋ Add transaction**. The form appears below the transaction list
   and keyboard focus moves to **Type**.
3. Select **Income** or **Expense**.
4. Enter the amount without `S$` or grouping commas, for example `0`, `12`,
   `12.3`, or `12.34`.
5. Keep today's date or select a past or future date.
6. Optionally select a category. The list contains only categories compatible
   with the selected transaction type.
7. Optionally enter a note of up to 200 characters.
8. Select **Save transaction**.

After a successful save, the form closes and the transaction appears in the
list. Income amounts use a plus sign and Expense amounts use a minus sign; both
are displayed as SGD with exactly two decimal places, for example `+S$600.00`
and `−S$8.50`.

Select **Cancel** to discard the unfinished form without saving a transaction.

## Review and find transactions

Open **Transactions** to review saved records. The list is ordered by date with
the newest transactions first. Use the **Find transactions** controls above the
list to narrow the displayed records:

1. Select a **Month**, **Type**, or **Category** to show only matching
   transactions. Leaving a selector at its `All ...` prompt does not restrict
   the list.
2. Enter a word or phrase in **Search notes** to find notes containing that
   text. Searching is case-insensitive; an empty or whitespace-only search
   does not restrict the list.
3. Combine any of the controls when you need a more specific result. Every
   active condition must match.
4. Select **Clear filters** to restore the complete history.

When no saved transaction matches the active conditions, MoneyMap displays
**No matching transactions** and suggests changing or clearing the filters.
Filtering and searching change only what is displayed; they do not edit or
remove saved transactions.

### Transaction fields and validation

| Field | Accepted input | Invalid input and behavior |
| --- | --- | --- |
| Type | Income or Expense | A transaction cannot be saved without a type. |
| Amount | Zero or a positive plain decimal beginning with a digit and containing at most two decimal places | Blank, negative, negative-form zero, more than two decimal places, letters, `S$`, commas, scientific notation, `.50`, and `1.` are rejected. |
| Date | A required past, present, or future date; defaults to today | A missing date is rejected. |
| Category | An optional category matching the selected type | Changing the type clears an incompatible selection. Leaving the field empty uses the matching `Uncategorised` category. |
| Note | Empty or up to 200 characters | A note longer than 200 characters is rejected. The form displays a live character count. |

If validation fails, MoneyMap shows clear text feedback and keeps the form open
with the entered values available for correction. No transaction is added or
saved. A local-file error is handled the same way: the form remains open and
the unsaved transaction does not appear in the list.

## Starter categories

MoneyMap provides separate starter sets for Income and Expense. Changing the
transaction type changes the available category choices.

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

The two `Uncategorised` choices are distinct permanent fallback categories. If
you do not select a category, MoneyMap automatically uses the fallback matching
the transaction type.

## Create a custom category

Open **Categories and Budgets** to create an ordinary category for either
Income or Expense:

1. Select the category type.
2. Enter a name, such as `Investments`, `Loans`, or `Credit Cards`.
3. Select **Create category**.

Names are trimmed before saving, must contain 1 to 40 characters, and must be
unique without regard to letter case within the selected type. The same name
may be used once for Income and once for Expense. Blank, overlong, or
duplicate names are rejected with visible feedback and are not saved. A new
category appears in the current-category list and in the compatible category
choices on the Transactions screen. Category names have no specialised
account, investment, loan, or credit-card behavior.

The current-category list scrolls vertically when it is taller than the
window, so every saved category remains reachable without changing the window
layout.

## Automatic local persistence

MoneyMap saves every successful transaction and custom-category creation
automatically and reloads the saved state the next time it starts. For a
packaged application, the files are located beside the JAR:

```text
application-folder/
├── MoneyMap.jar
└── data/
    └── moneymap.json
```

The JSON file is versioned. MoneyMap writes a temporary file first and replaces
the main file only after the new state has been written successfully. A failed
save does not publish the unsaved transaction or replace the previous valid
file.

If MoneyMap is interrupted while creating the first saved file, it checks the
temporary file at the next startup. A valid temporary file is recovered as
`moneymap.json` and its transactions are loaded; MoneyMap displays a recovery
warning. An invalid or unsupported temporary file is preserved under a
recoverable name such as `moneymap.json.corrupt`, and MoneyMap starts with the
starter categories and no transactions.

Do not edit `moneymap.json` while MoneyMap is running. If the file is malformed
or has an unsupported version, MoneyMap preserves it under a recoverable name
such as `moneymap.json.corrupt`, displays a data-recovery warning, and starts
with no transactions and the starter categories. Keep the preserved file if
you need to investigate or recover its contents manually; in-app import and
restore are not available yet.

## Keyboard and accessibility

- Transaction-entry and history-filter controls have visible labels and
  accessible descriptions.
- Tab follows the visual control order through the list-first screen and the
  disclosed form.
- **＋ Add transaction** reveals the form and moves focus to **Type**.
- **Cancel** closes the form without saving and returns focus to
  **＋ Add transaction**.
- Validation is communicated with text, not colour alone.
- On macOS, use Space to activate a focused ordinary button. On Windows and
  Linux, use Enter or Space. **Save transaction** is the form's default action.

Keyboard focus order and focus transitions are covered structurally by the
automated JavaFX smoke test. The Issue #3 keyboard workflow was also manually
checked on macOS. Visible focus indication and complete Windows/Linux keyboard
interaction still require manual checking on those target platforms.

## Current scope and limitations

The current build supports recording, reviewing, filtering, and searching
displayed Income and Expense transactions. It does not yet support:

- editing or deleting transactions;
- renamed, archived, or deleted categories;
- budgets or Dashboard calculations;
- import or export;
- wallets, transfers, accounts, bank synchronisation, or specialised
  investment, loan, or credit-card behavior; or
- cloud synchronisation, authentication, or multi-currency values.

The three non-Transactions areas remain honest placeholders for later
increments.
