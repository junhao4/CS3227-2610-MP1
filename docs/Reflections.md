# Reflections on AI-assisted Software Engineering

> Where the interaction logs preserve only a prompt excerpt and a summary, this
> reflection labels longer wording as a faithful reconstruction rather than an
> exact quotation. The original interaction logs remain unchanged.

## Introduction

The fundamental challenge in this MP was alignment: could the agent understand
what I intended and write code in the way I wanted without me micromanaging
every aspect of the work? A huge advantage of coding agents are because of their
speed, but that speed only helps if I can balance  which tasks require human in the loop and which task can be done by the agent itself. The question became how to prompt the agent
so that its output stayed reliable and aligned with my intentions while leaving as much as possible to the agent.

The agent needed to understand the intended user experience, the product’s
scope and non-goals, the reasoning behind domain decisions, the coding and
testing standards, and the points at which it should stop and ask me instead of
making an assumption. If a mismatch was found while we were still discussing
the requirement, it was relatively cheap to correct. If the same mismatch was
found after it had been encoded in code, tests, documentation, and later
features, the correction became broader and more expensive.

This became my main way of understanding the Lecture 0–Lecture 3 material.
Lecture 0 emphasised
engineering the artefacts and the process, rather than treating the AI as a
magic coding shortcut. Lecture 1 provided the prompting techniques for communicating
intent and structuring the agent’s work, including exploration, structured
context, retrieval-augmented prompting, zero-shot prompting, and structured
multi-step reasoning. Lecture 2 showed how testing and code review could check whether
the implementation actually matched the requirements. Lecture 3 made the role of
artefacts explicit: they are the interface between humans and agents, allowing
work to be observable, reproducible, and safe across a longer agentic workflow.

Here are three techniques I used.
The first is the Socratic requirements interview, where I used questions to
discover intent. The second is the structured Issue #3 COT/TDD prompt with retrieval from online sources, where I
gave the agent targeted context and asked it to make its implementation plan
visible. The third covers the prototype and manual-verification prompts I used
for the UI. External retrieval and prototyping appeared whenever a particular
stage raised a new uncertainty.

## 1. The Socratic requirements interview and the context-pressure problem

My first substantial prompt was a requirements-interview request. I asked the
AI to read the MP1 requirements, consider “a single-user JavaFX personal budget
tracker”, and “grill me relentlessly about every consequential product,
domain, implementation, and testing decision, one question at a time.” I also
wanted the AI to give a recommendation with each question.

### Why I formulated the prompt this way

I did not want to start implementation from a vague app idea. MP1 gave me the
freedom to define the product, but that freedom created a risk: I might leave
important domain decisions implicit and allow the AI to fill in the gaps. I
wanted the agent to identify what needed to be decided, ask me about it, and
present useful options without becoming the owner of the product definition.

The one-question-at-a-time format was intentional. It made each decision
visible and gave me a chance to accept, reject, or modify the recommendation.
Requiring a recommendation meant the agent had to contribute analysis rather
than merely ask me an unstructured sequence of questions. In retrospect, this
was a Lecture 1 exploration-mode prompt and also an early attempt to design the
information flow between the human and the agent.

### What the agent produced and where it diverged

The interview helped break the product into decisions about the primary user,
categories, budgets, transactions, dates, editing and deletion, persistence,
backup, reporting, history filters, and the application’s main areas. It also
helped identify features that should be deferred, such as wallets and
transfers, rather than implemented partially.

However, the agent did not always distinguish between a recommendation and a
settled requirement. It initially recommended disallowing future-dated
transactions, but I decided that future dates should be allowed. It proposed
possibilities around automatically recreating uncategorised categories, which I
rejected. During the prototype discussion, it temporarily omitted one of the
application areas that had already been discussed, and I had to point out the
inconsistency.

These were not failures in the form of invalid Java or an obviously impossible
design. They were plausible assumptions. That made them alignment failures:
the agent could produce a reasonable product, but not necessarily the product
I intended. The AI was useful for exposing the decision space, but I had to
remain responsible for the choices. I decided that investments, loans, and
credit cards would be labels rather than specialised financial-account
features, and I chose the scope and semantics of the MVP.

### How alignment failed across sessions

The more serious problem appeared when the interview continued across multiple
context sessions. At one point, the AI repeated questions that had already been
discussed. I saw both sides of the failure. The AI had forgotten, or was no
longer reliably using, earlier decisions. At the same time, I had not yet
created a sufficiently clear durable artefact that could restore those
decisions to a later context.

At this point I wrote that the problem was “not writing a
intermediate product spec or some other durable logs”. I could have answered the
repeated questions again, but that would only have patched the immediate
conversation. Instead, I told the agent to write what had already been decided
and the future questions into `ProductSpecification.md` before continuing.

The result was a live list that the agent and I could work through together. It
recorded settled decisions, remaining questions, and the current state of the
product. The specification became a shared alignment surface and a source of
context for later sessions. I stopped treating it as documentation that could
wait until the end.

### Why this connects to Lecture 3

This is the clearest Lecture 3 example in my project. The specification acted like a
Continuity Pack because it preserved the current state across sessions. It
also contained elements of a Mission Brief because it captured product goals,
boundaries, and intended behaviour. The process of working through the list
with the agent resembled a Workflow Runbook. Progress was recorded through
explicit decisions, which were easier to use than an increasingly unreliable
transcript.

The experience also reflected Lecture 3’s Context Paradox. Adding more conversation
did not necessarily improve the agent’s understanding. Once the context became
large, earlier decisions became harder to use reliably. A smaller, deliberate
artefact containing the current decisions was more useful than the entire
history of the conversation.

### Verification and remaining limitations

The new specification improved the workflow, but I did not treat it as proof
that alignment was now complete. I checked it against the previous interview
and prototype logs, removed stale `TBD` markers as decisions were settled, and
corrected inconsistent wording. A later review also found that the progress
threshold wording had drifted to 70%/90% even though the final decision was
50%/80%. That inconsistency was corrected in the specification and checked
against the User Guide and implementation.

The specification made alignment visible and recoverable, but it could still
contain an outdated or incorrect version of the project’s state. I therefore
had to compare it with the human decisions and the final product.

### What I would do differently

I would create the living specification before the requirements interview
became long. I would distinguish settled decisions, pending questions,
assumptions, and rejected alternatives from the beginning. I would also check
the specification before asking a new question, so that the agent would not
reopen something that had already been resolved.

This experience convinced me that a well-designed conversation is not enough
to carry intent across changing contexts. A durable artefact has to preserve
the decisions once they have been made.

**Evidence:**
`logs/2026-08-25-190344-budget-tracker-product-interview.md`,
`logs/2026-08-26-180900-product-specification-draft.md`, and
`logs/2026-08-26-221327-budget-tracker-requirements-follow-up.md`.

## 2. Targeted context, retrieval, and structured COT for implementation

Once the product specification and issue acceptance criteria existed, my
prompting changed. I was no longer asking the agent to help define the whole
product. I was asking it to implement one bounded slice using the context
appropriate to that stage.

### The context was deliberately stage-specific

I learned that giving the agent more information was not automatically better.
The relevant context depended on the task. During specification, the useful
context was the MP1 requirements, product goals, previous decisions, and
unresolved questions. During implementation, it was the current GitHub issue,
the relevant sections of `ProductSpecification.md`, related source files,
existing tests, coding conventions, and acceptance criteria. During code
review, it was the diff, issue scope, requirements, review standards, test
output, documentation, and dependencies.

I used retrieval-augmented prompting throughout the project. I supplied or
directed the agent to retrieve links to the CS2103/T software-engineering and
testing material, Java coding standards, GitHub documentation, official
OpenJFX documentation, and later research about backpressure. These links were
not included just to make the prompts look sophisticated. They supplied
knowledge that was relevant to the current stage and that the repository did
not necessarily contain.

For example, the first implementation prompt was recorded as:

> “implement gh issue 2 using tdd where appropriate, read
> https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/index.html
> and apply the best practices. follow this coding standards
> `https://se-education.org/guides/conventions/java/`”

That prompt used external retrieval to align the implementation with course
engineering practices and Java conventions. For the JavaFX popup problem, local
reasoning did not resolve why the New category dialog would not close. I then
asked:

> “still cannot why?? can u seaerch online”

The agent retrieved official OpenJFX Dialog documentation. That gave the later
fix a framework-specific basis and stopped us from relying on increasingly
confident local speculation.

### The longer Issue #3 COT prompt

For Issue #3, I deliberately changed from the direct baseline style to a
structured multi-step test-first prompt. The original full prompt was not
preserved verbatim in the log. The log preserves the opening as an exact
excerpt:

> “Implement GitHub issue #3 only using zero-shot structured multi-step
> Chain-of-Thought prompting and TDD.”

The rest of the prompt was something like the following. This is a
reconstruction from the interaction log, the Issue #2 context, and the
contemporaneous prompting plan, not an exact quotation, sadly my logging skill did not catch this.

> Implement GitHub issue #3 only using zero-shot structured multi-step
> Chain-of-Thought prompting and TDD.
>
> First, read the same project context used for Issue #2: `AGENTS.md`, the MP1
> requirements, the Issue #3 description, the relevant `ProductSpecification`
> sections, the relevant source code and tests, the build configuration, and
> the supplied review criteria. Also read the CS2103/T software-engineering
> textbook at
> `https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/index.html`
> and apply its relevant best practices. Follow the Java coding standard at
> `https://se-education.org/guides/conventions/java/`. Treat the issue and
> approved specification as the product authority.
>
> **Step 1 — Understand the requirements:** identify the acceptance criteria
> and business rules in scope, note any ambiguity or conflict, and identify
> the files and architectural boundaries that are relevant to the issue.
>
> **Step 2 — Design the tests:** derive valid and invalid equivalence
> partitions, boundary values, and error cases; choose the appropriate test
> level for each behaviour; produce a requirements-to-tests table and a
> proposed test list; and state the Red–Green–Refactor order.
>
> **Step 3 — Implement and verify:** after approval, demonstrate meaningful
> failing tests, implement the minimum complete change to make them pass, run
> focused and full checks, refactor while green, and report the red/green
> evidence and any limitations. Stop for unresolved requirement conflicts.
> Do not update documentation, independently review, commit, push, or change
> GitHub unless separately instructed.

The important evidence is that the
prompt had a three-step structure, explicitly reused the Issue #2 reading
context, required the agent to expose its requirements and test plan before
editing, and then required test-first implementation and verification.

I formulated the prompt this way because I wanted the agent’s proposed path to
be visible before it changed code. I was testing whether explicit
requirements-to-tests reasoning would make the implementation more reliable,
not asking the agent to expose private internal thoughts. The useful output
was observable: a requirements-to-tests mapping, test partitions, boundaries,
test-level choices, implementation order, verification commands, and Red–
Green evidence.

### What worked

The structured prompt helped the agent derive cases for amounts, dates, notes,
category types, and persistence. It also made the TDD sequence concrete. Tests
were added before the relevant production APIs existed, so focused compilation
failures provided genuine Red evidence. After implementation, the tests became
green and the application smoke checks supplied another layer of verification.

This connected Lecture 1 and Lecture 2 in a practical way. Lecture 1 gave me a
way to structure the agent’s analysis, while Lecture 2 gave me criteria for
judging the resulting tests.
They needed to trace to requirements, cover valid and invalid partitions,
include boundaries and error handling, and detect real defects.

### What did not work

The prompt did not make the agent’s reasoning complete. The initial review
missed invalid persisted-date recovery, Java line-length and Javadoc rules,
the runtime-data ignore rule, and a stale test count in the Developer Guide. I
had to add those findings and require follow-up checks. The structured prompt
made requirements and test design more visible, but it left several review
dimensions insufficiently covered.

This corrected my expectations about COT. A long, explicit procedure can reduce
omission risk, but it cannot guarantee that the procedure is complete or that
the agent will interpret every requirement correctly.
The review findings showed why Lecture 2 insists that humans still inspect generated
code for intent, quality, architecture, dependencies, and AI-specific errors.

### How the workflow evolved

The Issue #3 process became the model for a reusable `write-code` skill. The
skill preserved the useful sequence: retrieve the current issue and
specification, state scope and ambiguity, design tests and edge cases, use
Red–Green–Refactor where appropriate, implement the smallest complete slice,
run focused and full verification, and report evidence and limitations.

Turning the Issue #3 process into a reusable skill changed my workflow at both
the Lecture 0 and Lecture 3 levels. A lesson from one implementation session became a durable
workflow artefact for later sessions. The first omissions also showed that the
review workflow needed explicit coding-standard and documentation checks.

### What I would do differently

The main change I would make to the COT prompt is to add a completion checklist
that the agent must actively tick off to give the agent more backpressure. The biggest weakness was that naming the
steps did not stop the agent from skipping some of the required work. For
example, it did not notice the 120-character line
limit and Javadoc requirements in the Java coding standard until I pointed out
the omission during review. A checklist would turn the procedure into a set
of visible obligations. That would make it harder for the agent to acknowledge
the procedure and then partially forget it.

I would add something like this after the numbered procedure:

> **Completion checklist — mark each item `[x]` only after performing it and
> give the evidence beside the item:**
>
> - [ ] Read `AGENTS.md`, the MP1 requirements, the issue, the relevant
>   `ProductSpecification` sections, source files, tests, review criteria,
>   the CS2103/T textbook, and the SE-EDU Java coding standard.
> - [ ] Mapped every acceptance criterion to an implementation decision and a
>   test or an explicitly documented reason why a test is not appropriate.
> - [ ] Covered valid and invalid equivalence partitions, boundary values,
>   malformed persisted data, and error-recovery cases.
> - [ ] Completed the Red–Green–Refactor sequence and recorded the relevant
>   failing and passing test evidence.
> - [ ] Checked the changed Java files against the 120-character line limit,
>   required Javadocs, naming conventions, and other applicable coding rules.
> - [ ] Ran focused tests, full checks, and the relevant application smoke or
>   manual checks, and reported any limitations.
>
> Do not report the task as complete while any item is unchecked. If an item
> cannot be completed, stop and explain the blocker or ask for clarification.

This would provide a form of procedural backpressure. The checklist would
make omissions visible and require the agent to attach evidence to each claim,
while the stop condition would keep it from silently moving from planning to
implementation with unfinished steps. The checklist would not make the agent
perfectly reliable, but it would show me exactly which part of the workflow had
been skipped. I would still separate functional
correctness, code quality, documentation consistency, and manual GUI
acceptance into visible review gates instead of assuming that one successful
test command represented all of them.

Targeted retrieval and structured COT helped most when they produced artefacts
I could inspect. More context and a step-by-step instruction did not, by
themselves, make the agent reliable. The useful endpoint was evidence in the
form of tests, decisions, and checks that I could examine.

**Evidence:**
`logs/2026-08-30-211654-issue-3-zero-shot-tdd.md`,
`logs/2026-08-31-093028-issue-4-reusable-skill-tdd.md`,
`logs/2026-08-30-143122-issue-2-zero-shot-reflection.md`,
`logs/2026-08-31-224608-issue-11-backup-export.md`, and
`logs/2026-08-31-230707-issue-12-replacement-import.md`.

## 3. Prototyping as repeated alignment and verification

The third major example was the way I repeatedly used prototypes and manual
feedback to align the visible product with what I actually wanted. I used the
prototype as a low-cost place to check whether the agent and I had the same
understanding of the interface before committing to production code.

### Why I kept prototyping

The product interview and later prototype sessions explored alternative layouts
for the Dashboard, Transactions, Categories and Budgets, and Data and Settings
areas. I used static, disposable screens so that I could compare information
hierarchy and interaction direction before committing those choices to
production controllers, services, tests, and documentation.

I asked the AI to produce or revise a layout, inspected the result,
gave visual feedback, and chose whether to continue with that direction. This
made it cheaper to reject a design and gave me a more concrete way to
communicate user-experience intent than abstract text alone.

### UI decisions

The category-management screen shows how I used prompting during UI design.
After the first implementation exposed too many controls at once, I asked:

> “i feel like the ui is getting very cluttered, can we use some progressive
> disclosure for the ui here, tell me your plan first”

The AI proposed replacing the inline Rename, Archive/Restore, Reassign, and
Delete controls with one **Manage** action and a contextual dialog. I approved
the plan with:

> “ok do it”

This interaction is relevant to my prompting process because I separated design
discussion from implementation. I described the problem, asked for a plan, and
then decided whether to approve the proposed direction before any changes were
made. The resulting interface used progressive disclosure: common category
information stayed visible, while the less frequent management actions appeared
through **Manage**. I still had to judge whether this was more compact and
discoverable than a separate page or more navigation, and whether it kept
destructive actions from dominating every category row.

### Visual debugging and the limits of automated checks

The progress-bar problem showed the limits of relying on the agent’s static
analysis. I reported:

> “is there a bug? i only see the food bar as the green one, the other bars use
> some other kind of styling, it looks more default and its light blue”

The AI initially suspected insufficient CSS specificity and proposed selectors
for warning and over-budget states. The FXML smoke test passed, but the visual
problem remained. I then asked for a version marker:

> “im still seeing the same thing, to make sure im not on the old one can u add
> a changing version number on the top right of the first page”

This prompted a second kind of context check. We investigated the difference
between source resources under `src/main/resources` and generated resources
under `build/resources/main`, because I was unsure whether I was seeing stale
output. I then described the controlled experiment I was performing:


When all bars were changed to use only the common `budget-progress` class,
they rendered consistently. I followed up with:

> “ok when i change to all green everything works, when u had extra styleClass
> it broke does that help u indentigy”

I meant identify here

This experiment provided stronger evidence than the AI’s initial CSS
explanation. It showed that the common control and common CSS worked, and that
the inconsistent behaviour was introduced by the additional state classes or
their state-specific CSS. The AI’s role became more useful after I supplied
the comparison: it could interpret the result, inspect the relevant FXML and
CSS, and help narrow the likely cause.

### Retrieval when local reasoning failed

The New category popup produced a similar pattern later. I reported that the
close button did not work, then clarified that I was referring to the New
category popup and that successful category creation also failed to close it.
After several rounds of local inspection, I asked:

> “still cannot why?? can u seaerch online”

The agent retrieved official OpenJFX Dialog documentation. The resulting fix
used an explicit native Dialog lifecycle with close and hidden-event cleanup.
Later smoke testing confirmed that Cancel, native window close, and successful
creation closed the popup.

This showed why retrieval needs to match the stage of the work. Earlier in
the project, the relevant context was product intent and prototype layout. At
this point, the uncertainty was framework-specific, so the missing context was
official JavaFX lifecycle behaviour. The link was valuable because it addressed
the actual uncertainty instead of adding general software-engineering advice.

### What prototyping taught me

Here, prompting was less effective than manual work. The AI could inspect
source files, propose hypotheses, and run
repeatable resource-loading tests. It could not reliably see the rendered
JavaFX window or decide whether the visual result matched my preference. My
manual observation and controlled experiments were therefore not an optional
final polish; they were part of the evidence needed to diagnose the problem.

This clarified the Lecture 2 distinction between different verification claims.
An FXML smoke test can establish that a resource loads. It cannot establish
that a progress bar is rendered correctly, that a button has the right visual
weight, or that a native dialog behaves correctly on every platform. Automated
tests, source inspection, retrieved documentation, and manual acceptance each
answer different questions.

This limitation affected the review workflow as well. At every review, I told
the agent to finish by stating exactly what I still needed to check manually.
I later made this expectation durable in the reusable `write-code` skill:
under “Verification and handoff”, it explicitly requires the agent to report
“manual checks still required”, together with its results, limitations, and
unverified platform behaviour. The `code-review` skill reinforced the same
handoff by requiring the completed review to tell me exactly which manual
checks remained.

The resulting handoff separated evidence that the agent could obtain from
source inspection or automated checks—such as compilation, resource loading,
and controller behaviour—from evidence that required me to operate and look
at the application myself. The manual list included visual layout and spacing,
visible focus, keyboard flow, native dialog behaviour, persistence through the
actual UI, and platform-specific behaviour where relevant.

A review saying “all tests passed” could otherwise sound like a complete
acceptance claim. The specific manual-check list made the boundary explicit:
the agent identified what its tools could not prove, while I performed those
checks and recorded what I observed. Manual verification therefore became part
of the human-agent alignment loop, rather than an afterthought reserved for
when the implementation appeared broken.

In the future I would treat visual feedback as a first-class
development input instead of assuming that a passing structural test meant the
prototype was ready.

**Evidence:**
`logs/2026-08-25-192238-budget-tracker-prototyping.md`,
`logs/2026-08-26-104535-budget-tracker-progress-bar-debugging.md`,
`logs/2026-08-31-151154-new-category-popup-retrieval-debugging.md`, and
`logs/2026-08-31-122549-issue-7-category-deletion-and-progressive-management.md`.



## Conclusion

My main lesson from MP1 is that AI-assisted software engineering is an
alignment problem as much as a code-generation problem. The agent has to
understand the human’s intentions, the product boundaries, the current stage
of work, the relevant standards, and the evidence required for completion.


My role became broader than writing less code or approving generated code. I had to apply the right prompting strategies to shape how the agent interacted with the project: decide what it needed
to know, preserve important decisions, create checks that could fail, question
confident but unsupported claims, and judge when the evidence was sufficient.
The agent accelerated many parts of the work, but the result still depended on
maintaining alignment from intention through artefacts and implementation to
verification.
