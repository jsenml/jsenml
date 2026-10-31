# Development

## Building

```shell
./gradlew              # default tasks: spotlessApply build
./gradlew build        # compile, test, check formatting, coverage
./gradlew spotlessApply
```

The build uses a JDK 25 toolchain (provisioned by Gradle if missing). Library main sources are compiled with
`--release 17`; tests and examples may use newer Java APIs. CI builds on Java 17, 21 and 25.

## Project layout

- `build-logic/` - convention plugins (`internal.java-library-convention`, `internal.errorprone-convention`,
  `internal.kover-convention`, `internal.publishing-convention`, `internal.idea-convention`, ...).
- `jsenml-bom/` - the BOM.
- `jsenml-core/`, `jsenml-jackson/`, `jsenml-jackson2/`, `jsenml-gson/`, `jsenml-spring-boot/` - module groups, see
  [Modules](./Modules.md).
- `examples/` - example applications, see [Examples](./Examples.md).
- `docs/` - these pages.

Versions of all dependencies are in `gradle/libs.versions.toml`.

## Code quality

- **Formatting:** Spotless with google-java-format for Java and ktfmt for Kotlin. Run `spotlessApply` instead of
  formatting by hand; `build` fails on unformatted code.
- **Static analysis:** Error Prone with NullAway. Packages are `@NullMarked` (JSpecify), nullable types are annotated
  explicitly.
- **Coverage:** Kover, aggregated in the root project.
- **Javadoc:** every public type and method is documented, including overrides.

Design and coding rules for contributors (and coding agents) are in [`AGENTS.md`][agents].

## Testing

- JUnit 6 and AssertJ; Java test methods are named `givenX_whenY_thenZ`, Kotlin ones with backticks.
- Shared codec behavior lives in contract tests of the format modules, extended by every backend, see
  [Writing a Backend](./Writing-a-Backend.md).
- Spring Boot autoconfiguration is tested with `ApplicationContextRunner`, HTTP bodies end-to-end with `RestTestClient`
  (Spring MVC) and `WebTestClient` (WebFlux).

## IntelliJ IDEA

Importing the Gradle project creates run configurations: build, format, the example applications and JUnit runs of every
module.

## CI

GitHub Actions:

- `ci.yml` - builds on Java 17, 21 and 25 on pushes and pull requests to `main` and `release-*` branches, and weekly.
  Changes in `docs/` alone do not trigger it.
- `wiki.yml` - publishes `docs/` to the wiki, see below.

Dependabot updates GitHub Actions and direct Gradle dependencies monthly, skipping major versions and the Gradle
wrapper. Auto-merge of its pull requests and the wiki publishing are turned off (`if: false`) while the repository is
private.

## Publishing

Library modules are signed and published to Maven Central with the nmcp plugin, aggregated in the root project.
Examples are not published.

## Documentation

- `README.md` - a short overview with a few minimal use cases.
- `docs/` - the full documentation, published to the GitHub wiki by the `wiki.yml` workflow: `docs/README.md` becomes
  the wiki's Home page, `_Sidebar.md` and `_Footer.md` its sidebar and footer, and `./Page.md` links are rewritten to
  wiki links. Link files outside `docs/` with absolute GitHub URLs, as the wiki has no access to the repository tree.
- Module group READMEs (`jsenml-core/README.md`, ...) - short summaries shown when browsing the repository.

The sync is one-way: every publish replaces the wiki with `docs/`. Before the first publish, create any page in the
wiki manually, as the wiki's Git repository exists only after that. Update the pages in the same change as the behavior
they describe.

[agents]: https://github.com/damianmalczewski/jsenml/blob/main/AGENTS.md
