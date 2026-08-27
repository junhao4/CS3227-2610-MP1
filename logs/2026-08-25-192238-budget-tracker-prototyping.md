# AI-assisted session: budget tracker JavaFX prototyping

Date: 2026-08-25
Time: 19:22:38 +0800
Timestamp source: repository environment time captured after prototype verification; exact task start time is unknown.
Task: Create disposable FXML alternatives for the student budget tracker design settled in the previous interview.
Status: completed
Interaction range: prototype request – 2026-08-25 19:22:38 +0800
Evidence sources: conversation, previous interview log, `AGENTS.md`, `specs/MP1-requirements.md`, repository files, Gradle output
Related logs: `logs/2026-08-25-190344-budget-tracker-product-interview.md`
Human verification: approved

## Interaction history

### 1. Resume prototyping and dependency boundary

- Exact prompt excerpt: “ok carry on with the prototyping”.
- AI response: Resumed from the saved handoff, inspected available Java and Gradle tooling, and found Java 25.0.3, Gradle 9.0.0, and a JavaFX-enabled JDK. The repository had no JavaFX or Gradle setup.
- AI recommendation: Add the smallest runnable Gradle/JavaFX prototype scaffold with static FXML only, no persistence or production domain logic.
- User decision: “ok”, approving the minimal Gradle/JavaFX prototype scaffold and dependencies.
- Resulting action: Created a Gradle JavaFX prototype project and began implementing the disposable screens.

### 2. Java 25 requirement and Gradle compatibility

- Initial implementation: Configured the project to compile as Java 25 and used the installed JavaFX modules.
- Verification result: Gradle 9.0.0 running on Java 25 failed with `Unsupported class file major version 69`.
- Temporary diagnostic: Running the prototype with the installed Java 17 JavaFX JDK compiled successfully, but this was not accepted as the project configuration.
- Exact user correction: “wait we must use java25 its the proj req”.
- AI response: Confirmed Java 25 must remain the project toolchain and researched Gradle’s official compatibility matrix.
- Resulting action: Generated a Gradle 9.1.0 wrapper, which officially supports running Gradle on Java 25, and restored `options.release = 25`.
- Verification: `./gradlew --version` reported Gradle 9.1.0 with Java 25.0.3. `./gradlew clean compileJava --no-daemon` passed.

### 3. FXML prototype implementation

- AI response: Implemented a single-window navigation shell with four areas and static prototype alternatives:
  - Dashboard A: summary-first layout.
  - Dashboard B: visual-first layout.
  - Transactions A: list/filter-first layout.
  - Transactions B: quick-entry-first layout.
  - Categories/Budgets A: budget table layout.
  - Categories/Budgets B: category-card layout.
  - Data and Settings: one simple backup/settings layout.
- Resulting files:
  - `build.gradle`
  - `settings.gradle`
  - `gradlew`, `gradlew.bat`, `gradle/wrapper/*`
  - `src/main/java/module-info.java`
  - `src/main/java/cs3227/prototype/PrototypeApp.java`
  - `src/main/java/cs3227/prototype/PrototypeController.java`
  - `src/main/java/cs3227/prototype/PrototypeSmokeTest.java`
  - `src/main/resources/fxml/*`
  - `src/main/resources/styles/prototype.css`
  - `.gitignore`
- Scope control: The prototype contains static sample values only. It has no persistence, domain model, budget calculations, import/export, or production interactions.

### 4. FXML loading error and correction

- Verification result: The first interactive run launched, but navigating to screens with literal `$` values caused FXMLLoader `Invalid path` exceptions because `$` is interpreted as an FXML expression prefix.
- AI response: Isolated the failure to literal currency symbols in FXML text values.
- Resulting action: Replaced currency symbols in static mock text with plain numeric values so the prototype remains focused on layout.
- Verification: Re-ran the smoke test and successfully loaded:
  - `/fxml/main.fxml`
  - `/fxml/dashboard-a.fxml`
  - `/fxml/dashboard-b.fxml`
  - `/fxml/transactions-a.fxml`
  - `/fxml/transactions-b.fxml`
  - `/fxml/categories-a.fxml`
  - `/fxml/categories-b.fxml`
  - `/fxml/settings.fxml`

## Work and verification

- Proposed, approved, and executed actions:
  - Proposed a minimal JavaFX/Gradle scaffold; user approved it.
  - Used Java 25 as required by the project.
  - Upgraded the project wrapper to Gradle 9.1.0 for Java 25 compatibility.
  - Created all approved static FXML alternatives.
  - Added a `verifyPrototypes` smoke-test task.
  - Added `.gitignore` entries for `.gradle/`, `build/`, and `.idea/`.
- Files or external systems changed: Prototype/build files listed above; no issues, commits, or pushes.
- Checks and observed results:
  - `./gradlew clean compileJava --no-daemon`: passed under Java 25.
  - `./gradlew clean verifyPrototypes --no-daemon`: passed under Java 25 and loaded all eight FXML resources.
  - `./gradlew run --no-daemon`: launched the JavaFX application under Java 25.
  - A first interactive navigation attempt exposed the literal-dollar FXML bug, which was corrected.
- Errors, limitations, or remaining uncertainty:
  - Gradle 9.0.0 is not suitable for running on Java 25; the project now uses the Gradle 9.1.0 wrapper.
  - JavaFX emitted a restricted-native-access warning at runtime; the prototype still launched and this warning does not affect the static UI exploration.
  - The automated smoke test validates FXML loading but not visual quality or click-by-click usability.
  - The screenshot attempt captured the active Safari desktop rather than the JavaFX window, so no reliable screenshot-based visual evaluation was completed.
  - The prototype uses plain numeric mock amounts without currency symbols because of the FXML expression parsing issue; currency presentation remains a design decision for the real implementation.

## Reflection notes

- What the AI did well or poorly:
  - Useful: checked the actual repository and installed tooling before creating the scaffold.
  - Useful: preserved the Java 25 requirement after the user corrected the temporary Java 17 diagnostic path.
  - Useful: added a smoke test that loads every prototype resource.
  - Useful: diagnosed the FXML `$` expression issue from the runtime stack trace.
  - Limitation: initially tried Gradle 9.0.0 with Java 25, which failed due to Gradle compatibility.
  - Limitation: the first FXML prototype run was not fully verified interactively before the `$` issue surfaced.
- Human judgement required:
  - Approving the dependency/scaffold change.
  - Enforcing Java 25 as a project requirement.
  - Deciding that disposable static prototypes were appropriate before production implementation.
  - Later comparing the alternative layouts and selecting the final screen designs.
- How prompts or approach evolved:
  - The work moved from product interview to prototype implementation only after the user explicitly approved the scaffold.
  - The user corrected the Java-version assumption, changing the build strategy to Java 25 plus Gradle 9.1.0.
- Prompting versus manual work:
  - The AI performed repository inspection, build setup, FXML implementation, error diagnosis, and smoke verification.
  - The user supplied the project constraint and approval boundaries.
  - Human visual comparison of the alternatives remains outstanding.
- What to do differently next time:
  - Check the Gradle/Java compatibility matrix before selecting a build version.
  - Use FXML-safe mock text from the start.
  - Verify each navigable prototype screen before declaring the prototype visually ready.
  - Obtain human feedback on the alternatives before settling the production UI.

Human verification: approved
