# AI-assisted follow-up: progress-bar debugging and diagnosis

Date: 2026-08-26
Time: 10:45:35 +0800
Timestamp source: repository environment time captured when the logging draft was prepared; exact debugging-session start time is unknown.
Task: Diagnose and fix inconsistent JavaFX progress-bar styling in the budget-tracker prototype.
Status: ongoing
Interaction range: first progress-bar bug report – 2026-08-26 10:45:35 +0800
Evidence sources: conversation, `AGENTS.md`, `specs/MP1-requirements.md`, source/generated FXML and CSS files, Gradle verification output, user experiment
Related logs:
- `logs/2026-08-25-192238-budget-tracker-prototyping.md`
- `logs/2026-08-25-190344-budget-tracker-product-interview.md`
Human verification: approved

## Interaction history

### 1. Initial bug report

- Exact prompt excerpt: “is there a bug? i only see the food bar as the green one, the other bars use some other kind of styling, it looks more default and its light blue”
- AI response: Initially suspected that the warning and over-budget CSS selectors were not overriding JavaFX’s default `ProgressBar` skin strongly enough. The AI proposed increasing selector specificity and explicitly styling the internal `.bar`.
- Resulting action: Changed the CSS selectors to target `.progress-bar.warning-progress .bar` and `.progress-bar.over-budget .bar`, then ran the Java 25 smoke test.
- Verification: All FXML resources loaded, but the user reported that the visual problem remained.

### 2. Version marker and generated-resource confusion

- Exact prompt excerpt: “im still seeing the same thing, to make sure im not on the old one can u add a changing version number on the top right of the first page”
- AI response: Added `PROTOTYPE v0.4 · BAR-STATE-FIX` to the top-right of Dashboard A and updated the Settings version text.
- Resulting action: Added a visible build marker to `dashboard-a.fxml`, updated `settings.fxml`, and added a `.build-marker` CSS rule.
- Verification: Java 25 FXML smoke test passed.
- Related diagnosis: The user linked `build/resources/main/fxml/dashboard-a.fxml`, which prompted inspection of source and generated resources. The source file is `src/main/resources/fxml/dashboard-a.fxml`; Gradle copies it into `build/resources/main`. The two files had identical hashes, confirming that the generated resource was being copied from the current source.

### 3. User’s controlled experiment

- Exact prompt excerpt: “why when i change the code it keeps reverting to the old one after i run gradle, im trying to change the styleClass to all budget-progress to identitfy the problem”
- AI response: Explained that `build/resources/main` is generated output and that edits must be made in `src/main/resources`.
- User experiment: Changed all three Dashboard A progress bars to use only `styleClass="budget-progress"`.
- Exact follow-up prompt excerpt: “ok when i change to all green everything works, when u had extra styleClass it broke does that help u indentigy”
- Evidence from the experiment:
  - All three bars rendered consistently when they shared only `budget-progress`.
  - The common FXML control and common CSS were therefore valid.
  - The inconsistent behaviour was introduced by the additional state classes or their state-specific CSS.
- AI evaluation: Recognised that this experiment was decisive and that the previous diagnosis had not isolated the problem sufficiently.

### 4. Single-state-class experiment

- AI response: Recommended replacing compound class combinations such as `budget-progress warning-progress` and `budget-progress over-budget` with distinct state classes.
- User decision: “ok try this”
- Resulting action: Changed the prototype to use:
  - `budget-progress`
  - `budget-progress-warning`
  - `budget-progress-over`
- Updated Dashboard A, Dashboard B, Categories B, and the CSS selectors.
- Verification: Java 25 FXML smoke test passed and loaded all eight resources.
- Remaining uncertainty: The smoke test confirms FXML loading but cannot prove the final JavaFX skin visually renders correctly.

## Work and verification

- Files changed during the debugging session:
  - `src/main/resources/fxml/dashboard-a.fxml`
  - `src/main/resources/fxml/dashboard-b.fxml`
  - `src/main/resources/fxml/categories-b.fxml`
  - `src/main/resources/fxml/settings.fxml`
  - `src/main/resources/styles/prototype.css`
- Executed checks:
  - Inspected source and generated FXML/CSS.
  - Compared source and generated resource hashes.
  - Confirmed the generated resource was copied from the source.
  - Ran `./gradlew clean verifyPrototypes --no-daemon` under Java 25 after each implementation attempt.
  - All eight FXML resources loaded successfully.
- Important errors or limitations:
  - The AI’s first diagnosis focused on CSS specificity and did not immediately identify the exact cause.
  - The AI then proposed a stronger CSS fix without first performing a controlled isolation experiment.
  - The user’s all-green experiment provided the strongest evidence that the extra state class/CSS interaction was responsible.
  - The later single-state-class experiment passed structural verification, but final visual confirmation remains with the user.
  - The AI was unable to obtain a reliable screenshot of the JavaFX window; macOS screen capture repeatedly captured the desktop instead.

## Reflection notes

- What the AI did well:
  - Inspected both source and generated resources when the user suspected changes were reverting.
  - Preserved the Java 25 build requirement.
  - Added a build marker to help distinguish the current prototype from stale windows.
  - Used the user’s experiment to refine the diagnosis.
- What the AI did poorly:
  - It was overconfident in the initial CSS-specificity diagnosis.
  - It treated successful FXML loading as stronger evidence than it actually was; loading does not verify JavaFX skin rendering.
  - It proposed multiple CSS fixes before isolating whether the issue came from FXML class assignment, CSS selector matching, the JavaFX skin, or a stale process.
  - It did not independently discover the decisive all-green comparison before the user performed it.
- AI limitation demonstrated:
  - The AI could inspect static source and verify resource loading, but could not reliably observe the rendered JavaFX window in the environment.
  - Without direct visual inspection or a controlled A/B experiment, it inferred the cause from CSS structure and runtime behaviour and initially inferred incorrectly.
  - The user’s manual visual observation and experiment were necessary to narrow the fault to the additional state class/CSS interaction.
- Human judgement and verification required:
  - The user noticed that the bars differed visually even though the FXML smoke test passed.
  - The user changed all bars to the common `budget-progress` class as a controlled experiment.
  - The user interpreted the result and supplied the key causal evidence.
  - The user remained responsible for deciding whether the rendered bars actually looked correct.
- Prompting versus manual work:
  - AI-assisted work was useful for inspecting files, tracing generated resources, editing candidate CSS/FXML rules, and running repeatable smoke tests.
  - Manual experimentation was more effective for the visual diagnosis because it produced direct comparative evidence.
  - Prompting was less effective when the AI relied on a plausible CSS explanation without a controlled test.
- What to do differently next time:
  - Begin with a minimal A/B experiment: common class only versus common class plus one state class.
  - Inspect both the JavaFX node style-class list and the rendered skin before asserting a CSS cause.
  - Treat FXML loading success and visual correctness as separate verification claims.
  - Avoid presenting a likely diagnosis as established until a test distinguishes CSS matching, JavaFX skin behaviour, stale processes, and generated-resource issues.
  - Add a reliable visual testing method before relying on CSS changes for UI work.

Human verification: approved
