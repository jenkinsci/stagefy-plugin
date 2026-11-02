# Contributing

[한국어](CONTRIBUTING.ko.md)

Issues and pull requests are welcome.

## Setup

- JDK 17+ and Maven 3.9+
- `mvn hpi:run` starts a development Jenkins with the plugin loaded
- `mvn test` (or `make test`) runs the tests; `mvn verify` adds static analysis

## Project layout

- `src/main/java/.../StagefyGlobalVariable.java` registers the `stagefy` global variable.
- `src/main/resources/io/jenkins/plugins/stagefy/` holds the Groovy sources (`core/` stages and steps, `util/` helpers). They are loaded by Jenkins at runtime, not compiled by Maven, so they are only exercised through the tests.
- `src/test/java/.../StagefyDslTest.java` runs pipelines with `JenkinsRule` and asserts on the build log.

## Making a change

1. Write a failing test in `StagefyDslTest` first (a pipeline that runs a YAML stage and checks the log or result).
2. Implement the change.
3. Run `mvn verify` (tests and static analysis) and make sure it passes.
4. Update `README.md` (and `README.ko.md`) when behaviour or YAML syntax changes, and add an entry under *Unreleased* in `CHANGELOG.md`.
5. Keep pull requests focused on one change.

## Submitting a pull request

1. Fork the repository and create a branch from `main` (for example `fix/yaml-cache`). Never commit directly to `main`.
2. Commit using the [Angular commit convention](https://github.com/angular/angular/blob/main/contributing-docs/commit-message-guidelines.md): `<type>(<optional scope>): <subject>`.
   - Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.
   - Subject: imperative, lower-case, no trailing period.
   - Breaking changes: add a `BREAKING CHANGE:` footer (or `!` after the type/scope).
3. Push the branch and open a pull request against `main`. Describe what changed and why, and link any related issue.
4. Make sure `mvn verify` passes; the Jenkins `Jenkinsfile` build runs the same checks.

## Code notes

- Groovy classes that live in a pipeline must be `Serializable`.
- Report user errors with `jenkins.error(...)` and include the stage/template name in the message.
- Treat YAML values as untrusted when they reach a shell; see the security notes in the README.
