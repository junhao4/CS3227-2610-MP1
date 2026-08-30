# MoneyMap Developer Guide

## Build and checks

MoneyMap is a Java 25 JavaFX application built with the Gradle 9.1.0 wrapper.
Install Java 25, verify it with `java -version`, then run the authoritative
project checks from the repository root:

```text
./gradlew clean build verifyPrototypes javadoc
```

`build` runs `check`, which includes `verifyApplication`. That production smoke
task starts the real application assembly, checks its stage, title, scene, and
stylesheet, verifies Dashboard as the initial destination, and activates every
navigation button while asserting the matching destination identity.
`verifyPrototypes` separately checks that all exploratory prototype FXML
resources still load. Neither smoke workflow proves visual correctness,
complete keyboard interaction, focus behaviour, resizing, or accessibility.

No conventional `src/test` suite or static-analysis plugin is configured for
this shell increment. Gradle therefore reports `test NO-SOURCE`.

## Packaging and launch

The build uses OpenJFX 17.0.7 artifacts and Shadow 9.4.3. The runtime
configuration includes JavaFX base, controls, FXML, and graphics artifacts for
64-bit Windows, 64-bit Linux, and Apple silicon macOS. Shadow 9.4.3 is pinned
because Shadow 9.5 and later require a newer Gradle version than the project's
9.1.0 wrapper.

`./gradlew clean build` produces one release JAR:

```text
build/libs/MoneyMap.jar
```

The archive is executable through the non-`Application` `Launcher` class and
bundles the JavaFX classes and native libraries. It does not depend on JavaFX
modules in the developer's JDK. To verify the peer-tester workflow from another
directory, copy only the JAR there and run:

```text
java -jar MoneyMap.jar
```

The empty-directory packaged launch was verified on Apple silicon macOS with a
Java 25 runtime containing the standard Java SE modules but no JavaFX modules.
Windows and Linux packaging contents were inspected but not executed, so those
platforms remain unverified. The classpath-based JavaFX fat-JAR pattern emits
an upstream warning that JavaFX classes were loaded from an unnamed module;
this warning did not prevent the verified launch.

## Structure

Production code is under `cs3227.moneymap`, with production FXML under
`src/main/resources/moneymap/` and production styles under
`src/main/resources/styles/moneymap.css`.

Verification and exploratory assets use separate Gradle source sets:

- `src/smoke/` contains the production startup/navigation smoke executable;
- `src/prototype/` contains the earlier static layout experiments and their
  prototype smoke executable; and
- `src/main/` contains only production classes and resources.

Production code does not depend on the prototype source set. Neither smoke nor
prototype classes or resources are included in `MoneyMap.jar`.

`Launcher` provides the executable JAR entry point and delegates to
`MoneyMapApp`, which creates the single window. `ApplicationController` owns
shell navigation and loads one destination into the centre of the
`BorderPane`. Each destination currently presents an honest empty state until
its feature is implemented.

## Instructions for Manual Testing

1. Run `./gradlew clean build`.
2. Copy `build/libs/MoneyMap.jar` to an otherwise empty directory.
3. From that directory, run `java -jar MoneyMap.jar`.
4. Confirm the window title is `MoneyMap — Student Budget Tracker`.
5. Confirm Dashboard is selected initially.
6. Activate each left navigation control with a mouse.
7. Use Tab to focus each control, confirm the focus indication is visible and
   follows the visual order.
8. Activate each focused control with Space on macOS. On Windows and Linux, use
   Enter or Space.
9. Confirm each control opens the matching labelled empty-state view.
10. Resize the window down to its minimum and confirm the navigation remains
   visible and usable.

The manual steps above cover visible labels, navigation, focus order, and basic
window layout. No transaction, category, budget, persistence, import, or export
behaviour is implemented by the production shell yet.

## Acknowledgements

- The production shell follows the JavaFX application structure and software
  engineering practices described in the
  [CS2103/T software engineering textbook](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/index.html).
- JavaFX setup and the non-`Application` launcher follow the
  [SE-EDU JavaFX tutorial](https://se-education.org/guides/tutorials/javaFxPart1.html).
- Executable fat-JAR packaging uses the
  [Shadow Gradle plugin](https://gradleup.com/shadow/) following the
  [SE-EDU JAR guide](https://se-education.org/guides/tutorials/jar.html).
- Java naming, layout, and accessibility-oriented labelling decisions were
  checked against the
  [SE-EDU Java coding standard](https://se-education.org/guides/conventions/java/).
- The visual direction reuses design decisions from the project’s own
  [MoneyMap product specification](../specs/ProductSpecification.md) and
  existing prototype resources. No external source code or external visual
  assets were copied.
