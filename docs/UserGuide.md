# MoneyMap User Guide

## Setup

1. Install Java 25 and confirm that `java -version` reports version 25.
2. Download or copy `MoneyMap.jar` into a folder of your choice.
3. Open a terminal in that folder.

The JAR includes JavaFX. You do not need the source project, Gradle, or a
separate JavaFX installation to run it.

## Launch

Run:

```text
java -jar MoneyMap.jar
```

The MoneyMap window opens with the Dashboard empty state selected. Keep the
terminal open while using the application.

The packaged JAR contains JavaFX support for 64-bit Windows, 64-bit Linux, and
Apple silicon macOS. The packaged launch has been verified on Apple silicon
macOS; Windows and Linux remain unverified for this release.

## Navigate the application shell

Click a navigation control on the left, or press Tab until it has focus. On
macOS, activate the focused control with Space. On Windows and Linux, use Enter
or Space. Each control opens its corresponding empty state:

- **Dashboard** — placeholder for monthly summaries, category spending, and
  budget status.
- **Transactions** — placeholder for income and expense records.
- **Categories and Budgets** — placeholder for categories and monthly spending
  limits.
- **Data and Settings** — placeholder for local data and backup tools.

Recording, editing, searching, filtering, and deleting transactions; managing
categories and budgets; persistence; import; and export are not available in
the current application shell.

## Current scope

The current release is the application shell only. It does not store or display
real financial data, and the Dashboard does not yet calculate or display
summaries. The four areas are navigation destinations that communicate where
future functionality will be added.
