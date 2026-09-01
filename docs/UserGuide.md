# MoneyMap User Guide

MoneyMap is a local JavaFX desktop application for recording Income and Expense
transactions in Singapore dollars. The current build provides transaction
creation, transaction-history review, category management, monthly expense
budgets, Dashboard summaries, automatic local persistence, complete local
backup export, and confirmed replacement import inside the production shell.

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

- **Dashboard** — monthly summary, budget-versus-spending states, and recent
  activity for a selected month.
- **Transactions** — records and displays real Income and Expense
  transactions.
- **Categories and Budgets** — creates, renames, archives, restores,
  reassigns, and deletes custom Income and Expense categories, and configures
  monthly budgets for Expense categories.
- **Data and Settings** — exports a complete local backup and imports a valid
  backup after replacement confirmation.

Click a navigation control, or press Tab until it has focus. On macOS, activate
a focused control with Space. On Windows and Linux, use Enter or Space.

## Use the Dashboard

The Dashboard is a monthly view. Its month selector initially shows the current
month. Select another available month to refresh the title, spending summary,
budget rows, Quick totals, and recent activity together.

- The large summary card compares all Expense transactions in the selected
  month with that month's total planned budget. **Left to spend** is the
  non-negative difference between them. If no Expense budgets are effective in
  that month, the card says so instead of presenting a percentage.
- **Budget vs spending** shows only Expense categories with an effective budget
  for the selected month. Each row shows the category's spent amount beside its
  budget and a progress bar: green is up to 50% used, yellow is above 50% up to
  80%, and red is above 80% or over budget. Expense transactions in categories
  without a budget still count in Expenses and Net balance, but have no budget
  row or progress bar.
- **Quick totals** shows Income, Expenses, the total planned budget, and Net
  balance for the selected month. Net balance is Income minus Expenses.
- **Recent activity** lists up to the three most recent transactions in the
  selected month. It is a summary; use **Transactions** when you need the full
  history, filters, or editing controls.

## Record a transaction

1. Open **Transactions**.
2. Select **＋ Add transaction** above **Your transactions**. The ledger is
   replaced by a focused form and keyboard focus moves to **Type**.
3. Select **Income** or **Expense**.
4. Enter the amount without `$` or grouping commas, for example `0`, `12`,
   `12.3`, or `12.34`.
5. Keep today's date or select a past or future date.
6. Optionally select a category. The list contains only categories compatible
   with the selected transaction type.
7. Optionally enter a note of up to 200 characters.
8. Select **Save transaction**.

After a successful save, the form closes and the transaction appears in the
list. Income amounts use a plus sign and Expense amounts use a minus sign; both
are displayed as SGD with exactly two decimal places, for example `+$600.00`
and `−$8.50`.

Select **Cancel** or **← Back to transactions** to discard the unfinished form
without saving a transaction and return to the ledger. Your current search and
filter selections remain available when you return.

## Edit or delete a transaction

Open **Transactions**, then choose the **⋯** menu at the end of the
transaction's row. Choose **Edit transaction** or **Delete transaction**.

- **Edit** opens the same labelled form with the saved amount, date, type,
  category, and note. Change any field, then select **Save changes**. The
  normal transaction rules still apply: changing the type replaces the
  category choices with compatible categories, and an invalid value keeps the
  correction form open without changing the saved transaction. If the saved
  category has since been archived, it remains selected for that historical
  record; you can retain it or choose a current compatible category instead.
- **Delete** opens a standard confirmation dialog identifying the transaction.
  Select **Cancel** to leave it unchanged, or select **OK** to permanently
  remove it. Deleted transactions cannot be restored by MoneyMap.

Edits and confirmed deletions are saved automatically and remain in effect
after MoneyMap restarts.

## Review and find transactions

Open **Transactions** to review saved records. The list is ordered by date with
the newest transactions first. Each date is shown once above a card containing
that day's transactions. Notes wrap within their row; a transaction without a
note shows **No note**. Use the **Find transactions** controls above the list
to narrow the displayed records:

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
| Amount | Zero through `9999999.99`, as a plain decimal beginning with a digit and containing at most two decimal places | Blank, negative, negative-form zero, values greater than `9999999.99`, more than two decimal places, letters, `$`, commas, scientific notation, `.50`, and `1.` are rejected. |
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

Open **Categories and Budgets** and select **＋ New category** to reveal the
form for an ordinary category for either Income or Expense:

1. Select the category type.
2. Enter a name, such as `Investments`, `Loans`, or `Credit Cards`.
3. Select **Create category**.

Names are trimmed before saving, must contain 1 to 40 characters, and must be
unique without regard to letter case within the selected type. The same name
may be used once for Income and once for Expense. Blank, overlong, or
duplicate names are rejected with visible feedback and are not saved. A new
category appears as a card in the current-category view and in the compatible category
choices on the Transactions screen. Category names have no specialised
account, investment, loan, or credit-card behavior.

The category-card grid scrolls vertically when it is taller than the window, so
every saved category remains reachable without changing the window layout.

## Manage, reassign, or delete a category

Open **Categories and Budgets** and select **Manage** beside an ordinary
category. Choose **Rename**, enter a new name, and confirm the standard dialog.
Names are trimmed, must contain 1 to 40 characters, and must be unique without
regard to letter case within the category type. Choose **Archive** and confirm
when you want to stop using a category for new transactions. It remains attached
to its historical transactions, is absent from the new-transaction category
selector, and remains available when filtering transaction history.

The normal category view shows active categories as cards. Select
**＋ New category** only when you need the creation form. Select
**View archived categories** to open the separate archived-category view. It
does not show the creation form. Select **Manage**, then **Restore**, to return
an archived category to active use and the matching new-transaction selector.
If an active category of the same type already uses the same name, rename the
archived category first, then restore it.

Each category card shows current-month spending and any applied budget. Expense
cards include a compact progress bar; it is green through 50% used, yellow above
50% through 80%, and red above 80% or when spending exceeds the budget. Income
cards remain more compact because they do not have budget progress. Each card
has one labelled **Manage** control, keeping lifecycle actions compact. Select
it to reveal only the actions that make sense for that category. An unused
ordinary category offers **Rename**,
**Archive** or **Restore**, and **Delete**. A used ordinary category also offers
**Reassign**, while **Delete** is disabled with an explanation until all of its
transactions have been reassigned.

To remove an unused category, select **Manage**, then **Delete**, and confirm.
This permanently removes the category. To remove a used category, select
**Manage**, then **Reassign**; choose an active category of the same type
(including `Uncategorised` if appropriate) and confirm. The affected
transaction history then shows the replacement category. Select **Manage** and
**Delete** afterwards if you no longer need the original category.

The two `Uncategorised` fallback categories open an explanation that they are
permanent rather than showing management actions. They cannot be renamed,
archived, restored, deleted, or used as the source of reassignment. They remain
valid reassignment destinations for their matching transaction type. Category
management actions provide visible feedback when an operation is invalid. All
changes are saved automatically and remain after restarting MoneyMap.

## Set a monthly expense budget

Open **Categories and Budgets** and select **Manage** on an Expense category.
Choose **Set budget** or **Edit budget** in the category's management dialog.
MoneyMap opens a full-screen editor for that one category; the all-category
budget list is not shown in this focused flow. Income categories do not offer
budget controls.

1. Use the labelled **Month** controls to move one calendar month backward or
   forward. The selected month defaults to the current month.
2. The editor shows the effective budget for that month and keeps the
   **Recurring from this month onward** and **one-time override** values
   separate.
3. Select **Set** or **Change** beside exactly the value you want to update.
   A saved value also has a **Remove** action. If a recurring value is scheduled
   for a later month, **Remove** is also available while viewing an earlier
   month, so you can clear that later value from the selected month onward.
   After removal, that scope's **Remove** action disappears. Select
   **← Back to categories** to leave the editor; the category cards are
   refreshed immediately to show the saved or removed budget.
4. Enter a plain SGD amount, such as `0`, `50`, or `250.00`, without `$` or
   grouping commas, then select **Save budget**.

A recurring amount applies from the selected month onward until a later
recurring version is set or it is removed. Changing it does not rewrite earlier
months, so past Dashboard reports retain their previous budget value. An amount
saved for one month takes priority for that month only, without changing the
recurring amount. The category detail keeps both values visible so this
relationship is clear before you edit either one. Select **Remove** beside a
recurring value to stop it from the selected month onward, or beside a
selected-month value to delete only that budget scope. Removing a month-only
override reveals the recurring value again when one is configured.

Long valid amounts remain supported without changing the card layout. A card
may shorten an unusually long summary with an ellipsis; open that category's
focused editor to inspect or change the complete amount.

Leaving a category without either kind of saved entry means it has no budget.
An explicit `$0.00` budget is different: it permits no spending, but MoneyMap
still lets you record expense transactions. A blank, negative, or
more-than-two-decimal amount is rejected with visible feedback and does not
change the saved budget.

Budgets are saved automatically and remain available after restarting
MoneyMap. Category and budget cards show the available spending progress and
over-budget state. The Dashboard presents the selected month’s income,
expenses, net balance, budget states, and up to three recent transactions.

On Dashboard, use the month dropdown to switch reporting months. It defaults
to the current month; changing it refreshes all summaries and activity together.

## Automatic local persistence

MoneyMap saves every successful transaction, category change, and budget change
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
you need to investigate it or restore a separate valid backup.

## Export a complete backup

Open **Data and Settings** and select **Export backup…**. Choose a writable
file name and location in the standard save dialog; use a `.json` file name.
MoneyMap confirms the selected file name after a successful export.

The backup is a versioned JSON document containing all current categories,
including archived state, transactions, budgets, and the metadata needed for a
future restoration. Exporting does not change the live `data/moneymap.json`
file or the data currently shown in MoneyMap. If the location cannot be
written, MoneyMap displays clear text feedback and keeps the current local data
unchanged. Do not select the active `data/moneymap.json` file as the backup
destination.

## Import and replace a backup

Open **Data and Settings** and select **Import backup…**. Choose a valid
MoneyMap `.json` backup in the standard open dialog. MoneyMap validates the
entire backup before asking whether to replace the current data. Select
**Replace data** only when you are ready: all current categories, archived
states, transactions, and budgets are replaced by the backup; nothing is
merged.

Select **Cancel** in the confirmation dialog to keep the current data. A
malformed, invalid, or incompatible backup is rejected with text feedback and
does not change the data currently shown in MoneyMap or its local data file.

## Keyboard and accessibility

- Transaction-entry and history-filter controls have visible labels and
  accessible descriptions.
- Tab follows the visual control order through the list-first screen and the
  disclosed form.
- **＋ Add transaction** switches from the ledger to the focused form and moves
  focus to **Type**.
- **Cancel** or **← Back to transactions** discards the form without saving and
  returns focus to **＋ Add transaction**.
- The budget editor's month arrows have accessible previous- and next-month
  labels, and **← Back to categories** returns from the focused budget screen.
- **Export backup…** and **Import backup…** have visible labels and accessible
  descriptions. Their native file dialogs use normal platform keyboard
  controls; the replacement dialog provides confirmation and cancellation.
- Validation is communicated with text, not colour alone.
- On macOS, use Space to activate a focused ordinary button. On Windows and
  Linux, use Enter or Space. **Save transaction** is the form's default action.

Keyboard focus order and focus transitions are covered structurally by the
automated JavaFX smoke test. The Issue #3 keyboard workflow was also manually
checked on macOS. Visible focus indication and complete Windows/Linux keyboard
interaction still require manual checking on those target platforms.

## Current scope and limitations

The current build supports recording, reviewing, filtering, and searching
Income and Expense transactions, managing categories, configuring monthly
Expense budgets, Dashboard summaries, local persistence, complete backup
export, and confirmed replacement import. It does not yet support:

- wallets, transfers, accounts, bank synchronisation, or specialised
  investment, loan, or credit-card behavior; or
- cloud synchronisation, authentication, or multi-currency values.

Data and Settings does not support merge import, cloud synchronization, or
automatically importing bank data.
