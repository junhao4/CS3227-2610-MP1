---
name: commit
description: Prepare and create local commits in focused semantic groups using Conventional Commits. Use when the user wants to commit or organize commits.
---

# Commit

Create focused, reviewable commits from existing changes.

- Inspect `.gitignore`, `git status`, staged and unstaged diffs, and untracked files.
- Preserve unrelated user changes.
- Reject staging secrets, IDE state, or unapproved generated output.
- Ask before editing `.gitignore` or committing.
- This skill handles local commit creation only; history rewriting, pushing, tagging, and issue-closing are outside its scope.

## Gitignore

Inspect `.gitignore` before grouping changes.

If it is missing or incomplete:

1. identify only clearly generated, local, IDE, build, or secret files that should be ignored;
2. show the proposed entries;
3. ask for approval;
4. create or update `.gitignore`;
5. treat the change as a separate `chore` commit.

Never use `.gitignore` to hide an uncertain or user-owned file.

## Commits

Group changes by intent, not merely by file. Each commit should represent one independently valid logical outcome. Keep inseparable implementation, tests, and documentation together.

Stage explicit paths only:

```bash
git add -- path/to/file
```

Use Conventional Commits:

```text
<type>[optional scope][!]: <description>
```

- `feat` adds a feature.
- `fix` fixes a bug.
- Use `test`, `docs`, `refactor`, or `chore` for those respective changes.
- Use `!` or a `BREAKING CHANGE:` footer for breaking changes.
- Keep the description concise and specific.
- Use a scope when helpful. Write the description in the imperative mood,
  capitalize it, omit trailing punctuation, aim for 50 characters, and never
  exceed 72.
- For non-trivial commits, add a body explaining what changed and why, wrapped
  at 72 characters.

Before each commit:

- inspect the staged diff;
- run relevant checks;
- show the proposed message, staged paths, and remaining changes;
- ask for explicit confirmation immediately before `git commit`.

After approval, create only the approved commit and report its hash. Leave pushing to a separate request.
