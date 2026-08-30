Date: 2026-08-29
Time: 14:41:09 +0800
Timestamp source: repository environment time captured when the logging draft was prepared
Task: Design and create a progressive-disclosure documentation-update skill for MoneyMap
Status: completed
Interaction range: user requested an update-documentation skill – skill files created and verified
Evidence sources: conversation, AGENTS.md, specs/MP1-requirements.md, specs/ProductSpecification.md, existing repository skills, current repository status, CS2103/T Documentation chapter, CS2103/T tP Deliverables, CS2103/T UML Modeling, CS2103/T Software Architecture, SE-EDU PlantUML guide, AddressBook-Level3 Developer Guide
Related logs:
- logs/2026-08-26-180900-product-specification-draft.md
- logs/2026-08-26-221327-budget-tracker-requirements-follow-up.md
- logs/2026-08-28-093753-github-issue-plan-finalisation.md
Human verification: approved

## Interaction history

### 1. Propose an update-documentation skill

- Exact prompt excerpt: “actually before this can i have a update documentation skill u see in mp1-reqs. maybe something like this?”
- User proposed a skill that would update the User Guide, Developer Guide, diagrams, and rendered images to match implemented changes, while excluding reflections, logs, the product specification, code, tests, GitHub, and Git history.
- AI response: Agreed that the skill was useful and necessary because MP1 requires an accurate User Guide, Developer Guide, and acknowledgement of the AI-assisted development process. Recommended strengthening the skill with:
  - support for creating missing required documentation;
  - evidence classification;
  - conflict detection between issue, specification, implementation, tests, and release status;
  - manual-testing appendix checks;
  - acknowledgement checks;
  - diagram abstraction-level checks;
  - PlantUML source/rendering rules; and
  - explicit prohibition on introducing dependencies automatically.
- User decision: Before creating the skill, read the specified CS2103/T and SE-EDU references.

### 2. Research the documentation and diagram references

- Exact prompt excerpt: “first can u read this [CS2103/T Documentation chapter] ... [CS2103/T tP deliverables] ... [CS2103/T UML modeling] ... [CS2103/T Software Architecture] ... [SE-EDU PlantUML guide] ... [AddressBook-Level3 Developer Guide]”
- AI response: Read all six requested online references.
- Findings:
  - The Documentation chapter distinguishes User Guide and Developer Guide responsibilities and recommends top-down, comprehensible, minimal-but-sufficient documentation.
  - The tP deliverables page requires `docs/UserGuide.md`, `docs/DeveloperGuide.md`, accurate current-feature descriptions, an acknowledgements section, a manual-testing appendix, and `.puml` files under `docs/diagrams`.
  - The UML chapter describes selecting diagram types according to the model or interaction being explained.
  - The Architecture chapter emphasises meaningful, one-way dependencies, correct abstraction levels, and the distinction between logical layers and deployment tiers.
  - The PlantUML guide treats `.puml` files as maintainable textual sources and exported images as generated outputs.
  - The AddressBook-Level3 Developer Guide provides an example structure containing acknowledgements, setup, architecture, component descriptions, implementation, and manual-testing instructions.
- Resulting design implications:
  - The skill should not add diagrams mechanically.
  - It should check the manual-testing appendix and acknowledgements.
  - It should keep Markdown and `.puml` files canonical.
  - It should regenerate images only through an existing supported workflow.
  - External examples should guide structure without overriding MoneyMap’s specification.

### 3. Decide how external references should be loaded

- Exact prompt excerpt: “how to structure the skill to read this references”
- AI response: Recommended a source hierarchy and a reference-routing section. Project-specific files should determine MoneyMap behaviour; external references should guide documentation quality and structure. The skill should always read project files but should read external references conditionally according to the documentation change.
- Proposed routing:
  - Documentation chapter and tP deliverables: required for any User Guide or Developer Guide update.
  - UML Modeling: only for UML, modelling, or non-trivial interaction changes.
  - Software Architecture: only for architecture, package, layer, component, or dependency changes.
  - PlantUML guide: only when `.puml` files or rendered images change.
  - AddressBook-Level3: only when a Developer Guide structure or component explanation needs an example.
- User decision: “ok do that”
- Resulting action: Approved creation of a separate reference-routing file instead of placing all detailed external-reference instructions directly in `SKILL.md`.

### 4. Create the documentation skill and reference map

- AI created:
  - `.agents/skills/update-documentation/SKILL.md`
  - `.agents/skills/update-documentation/references/course-reference-map.md`
- The skill includes:
  - source hierarchy;
  - required project sources;
  - preconditions for identifying the issue and release/milestone;
  - documentation scope;
  - conflict handling;
  - User Guide rules;
  - Developer Guide rules;
  - manual-testing appendix requirements;
  - acknowledgements requirements;
  - UML and architecture checks;
  - PlantUML source and generated-image rules;
  - evidence classifications;
  - reference availability fallback;
  - verification requirements; and
  - prohibited actions.
- The reference map includes:
  - all six requested external references;
  - required versus conditional routing;
  - the intended use of each reference;
  - a reference-use reporting requirement; and
  - an availability fallback.

## Work and verification

- Proposed, approved, and executed actions:
  - Reviewed the initial update-documentation skill concept.
  - Read the requested course and external documentation references.
  - Designed progressive disclosure for external reference loading.
  - Created the new skill and separate reference map.
- Files created:
  - `.agents/skills/update-documentation/SKILL.md`
  - `.agents/skills/update-documentation/references/course-reference-map.md`
- No files were modified outside these two new skill files.
- No application code, tests, product specification, documentation guides, logs, Git history, or GitHub state was changed.
- Verification performed:
  - Re-read both newly created files.
  - Confirmed `SKILL.md` contains the source hierarchy, workflow, documentation rules, evidence rules, conflict handling, reference availability, and output requirements.
  - Confirmed the reference map contains all six requested links and conditional routing rules.
  - Confirmed both files contain no whitespace errors using `git diff --check`.
  - Confirmed the only repository changes are the two new untracked skill files.
- Observed result:
  - The skill directory contains 287 lines in `SKILL.md` and 52 lines in the reference map, for 339 lines total.
- No commit or push was performed.

## Reflection notes

- What the AI did well:
  - Connected the MP1 deliverable requirements to concrete documentation checks instead of treating documentation as a generic afterthought.
  - Used the requested course references to refine the skill’s scope and workflow.
  - Distinguished project-specific sources of truth from external documentation examples.
  - Preserved the user’s concern about context cost by routing external references conditionally.
  - Added explicit verification and conflict-handling rules so documentation cannot silently legitimise unverified behaviour.
  - Kept PlantUML source files separate from generated diagram outputs.
- What the AI did poorly:
  - The initial proposed skill referenced a `references/course-documentation-guidance.md` file that did not exist.
  - The initial design did not clearly include the required Developer Guide manual-testing appendix.
  - The initial design did not make release or milestone status explicit enough.
  - The initial design treated rendered documentation images too broadly until the PlantUML workflow was reviewed.
- Human judgement required:
  - Deciding that external references should be conditionally read rather than automatically loaded in every invocation.
  - Choosing a separate reference-routing file for progressive disclosure.
  - Keeping course references as guidance rather than allowing them to override the MoneyMap specification.
  - Approving creation of the new skill files.
- How the prompts or approach evolved:
  - The workflow began with a general request for an update-documentation skill.
  - It then moved to reference research before implementation.
  - The design was refined around course deliverables, manual testing, acknowledgements, UML abstraction, PlantUML source tracking, and context discipline.
  - The final implementation used a concise operational skill plus a separate conditional-reference map.
- Prompting versus manual work:
  - AI researched the course materials, identified documentation implications, drafted the skill structure, created the files, and verified their contents.
  - Human judgement determined that reference loading should be progressive and approved the final repository change.
- What to do differently next time:
  - Inspect the repository for referenced support files before proposing them.
  - Identify course-specific deliverables, such as the manual-testing appendix, during the first draft.
  - Decide source hierarchy and reference-routing rules before writing the main skill.
  - Keep external guidance in a routing file from the start, while retaining only operational rules in `SKILL.md`.
  - Log the skill-design and creation session immediately after approval so the workflow remains durable.

Human verification: approved
