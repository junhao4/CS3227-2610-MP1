---
name: close-issue
description: Push approved commits, close a completed GitHub issue, and update only the directly affected issue status labels after explicit confirmation.
---

# Close issue

Use only after the requested issue is implemented, documented, reviewed, and
committed locally. This skill does not create commits or fix findings.

## Check

1. Read `AGENTS.md`, the issue, its review report, and the relevant commit(s).
2. Verify GitHub API access independently with `gh auth status`, `gh api user`,
   and `gh issue view <number> --json number,title,state,labels,body,comments`.
   Use `gh api graphql` for native dependency relationships. These checks are
   separate from Git push authentication; report which exact operation fails
   rather than treating one failure as proof that all GitHub access is invalid.
3. Confirm the issue number, target remote and branch, clean working tree, and
   that no required review follow-up remains. A dirty tree is allowed only when
   the user identifies every dirty change as separate active work; list it in
   the preview and preserve it untouched. Otherwise stop.
4. Inspect the current issue's labels and its native GitHub dependencies.
   Identify only its direct dependent issues.
5. For each direct dependent, determine whether closing this issue leaves any
   other open blockers.

If any check is unclear or fails, stop and report it.

## Preview and approval

Before mutation, show:

- the commits and exact `git push` target;
- any approved concurrent dirty work that will remain untouched;
- the concise completion-comment body (commit refs, verification, and any
  remaining blockers);
- the current issue's labels before and after (`status:done`) and its closure;
- every direct dependent, its remaining open blockers, and proposed status:
  `status:ready` only when none remain, otherwise `status:blocked`.

Wait for explicit approval of that exact preview.

## Execute

1. Push the approved commits. If the push fails, stop.
2. Replace the current issue's status label with `status:done`, then close it.
3. Post the approved completion comment.
4. Update only the status labels of its direct dependents as approved.
5. Re-read the remote branch and every affected issue; report URLs and final
   labels.

## Rules

- Use native GitHub dependencies; a closed blocker satisfies the relationship,
  so do not delete or invent dependency edges.
- Do not create labels or change issue bodies, assignees, milestones,
  sub-issues, projects, or unrelated issues without separate approval.
- If the required status label or native dependency access is unavailable,
  stop rather than substituting labels, comments, or guesses.
- Never stage, commit, stash, discard, or include approved concurrent work in
  the push.
- On a partial failure, report exactly what was pushed or changed; never retry
  a mutation blindly or claim the issue was fully closed.
