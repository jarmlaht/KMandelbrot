---
name: implement-task
description: Implement a GitHub task issue, read analysis, draft plan, and implement
disable-model-invocation: true
arguments: task_number
argument-hint: "[task-number]"
---

## Role
- You are a senior Kotlin/Android developer responsible for implementing the assigned task
  - Implement exactly what the task requires
  - Avoid scope creep
  - Avoid unnecessary abstractions
  - Avoid unrelated changes

## Rules (Non-negotiable)
- To look up an Android, AndroidX, Kotlin, or Gradle API or convention: use `WebSearch` for the official documentation matching the versions in `gradle/libs.versions.toml` and the SDK levels in `app/build.gradle.kts`. This project uses AGP 9, which differs from older examples; heed deprecation notices.
- If a dependency appears missing: read `gradle/libs.versions.toml` and `app/build.gradle.kts` to confirm what is declared. Do not add dependencies without approval; when approved, add them through the version catalog.
- On any lint, compile, test, or build failure: read the error message, fix only what the error indicates.

## Steps

### Read the task issue and analysis
```bash
gh issue view $task_number --json number,title,body,comments
```
- The analysis comment posted by `/analyze-task` contains key decisions and constraints. Implementation must reflect those decisions.

### Load project context
- Read `CLAUDE.md` and explore the existing codebase.
- Every decision in the implementation must comply with `CLAUDE.md`.

### Draft implementation plan
- Create `tmp/` if it does not exist.
- Using `plan-template.md` as the structure, write the detailed plan to `tmp/task-$task_number-plan.md`.
- Fill in every section: list every file to be created or modified with the specific changes, and use code blocks to show exact changes (function signatures, class skeletons, manifest or Gradle snippets) so the user can review precisely what will be implemented.
- Ask the user to review and edit the file directly, then confirm approval in chat.

**Approval Gate: Wait for explicit user approval before proceeding.**

### Checkout feature branch
```bash
git checkout main
git pull origin main
git checkout feature/task-$task_number
```
- If the checkout fails because the branch does not exist, run:
```bash
git checkout -b feature/task-$task_number
```
- Never branch from another feature branch.

### Implement
- Follow all standards in `CLAUDE.md`.
- Implement the approved plan exactly.
- Do not deviate from the approved implementation plan without asking for approval again.
- Add or update unit tests for any pure Kotlin logic that changed.
- After implementation, run lint, the unit tests, and the debug build (which compiles the Kotlin sources):
```bash
./gradlew lintDebug testDebugUnitTest assembleDebug
```
- Verify the change in the running app on a connected device or emulator, in both portrait and landscape:
```bash
./gradlew installDebug
```
- If no device or emulator is available, say so and ask the user to verify in the running app.

### Self Review

Before requesting user approval:

- Review all modified files.
- Remove dead code, debug code, and unused imports.
- Ensure only task-related files have been modified.
- Verify the implementation matches the approved plan.
- Ask the user to review the implementation and make any manual edits.

**Approval Gate: Wait for explicit user approval before committing.**

### Commit and push (after user approval)
- Stage only the changed files.
- Commit following the Git section of `CLAUDE.md`: Conventional Commits format, with the required `Co-Authored-By` trailer.
- Push the branch:
```bash
git push -u origin feature/task-$task_number
```
