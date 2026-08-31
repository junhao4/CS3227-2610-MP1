---
name: implement
description: Implement a scoped MoneyMap issue through code, documentation, and review, with targeted follow-up loops when review finds problems.
---

# Implement

Before choosing an issue, use the repository's configured GitHub CLI when the
request refers to a GitHub issue or asks for the next unblocked issue:

```bash
gh auth status
gh issue view <number> --json number,title,state,labels,body,comments
```

Use `gh api graphql` when native GitHub dependency relationships are needed.
Treat the issue body and native dependency data as the source for scope and
blocking status. If the CLI is unavailable or unauthenticated, report that
constraint and use only clearly available local evidence; do not invent issue
dependencies.

For the requested issue, run these skills in order:

1. [write-code](../write-code/SKILL.md)
2. [update-documentation](../update-documentation/SKILL.md)
3. [code-review](../code-review/SKILL.md)

Use the code-review report as the handoff:

- If it requests implementation changes, run `write-code` for only the
  identified files or behaviour.
- If it requests documentation changes only, run `update-documentation` for
  only the identified documentation or diagrams.
- After either follow-up, run `code-review` again for only that affected part.
- Repeat until the review reports no required follow-up, or stop and report a
  blocker requiring user input.

Preserve the existing skills' scope, verification requirements, and approval
boundaries. Do not commit, push, log the interaction, or modify GitHub unless
the user separately requests those actions.
