# Project Guidelines

## Scope and Product Intent
- Build and maintain StudentTaskManager as a practical student planner app.
- Keep the app fast, reliable, and simple to use for deadline-driven task management.
- Prioritize maintainability and predictable behavior over over-engineered abstractions.

## Tech and Structure
- Platform: Android app module (`:app`) with Kotlin.
- UI: Jetpack Compose + Material 3.
- Navigation: Navigation Compose.
- Persistence: Room (entity, DAO, database, converters, repository).
- Background work: WorkManager with `CoroutineWorker`.

Package responsibilities:
- `data`: persistence and repository abstractions.
- `domain`: enums and domain taxonomy.
- `ui`: view model, app shell, screens, and reusable components.
- `notifications`: channel and notification helpers.
- `worker`: background deadline checking and reminder dispatch.

## Architecture Priorities
- Preserve layered boundaries:
- UI should delegate business actions to the view model.
- View model should coordinate state and repository calls.
- Repository and DAO should own persistence concerns.
- Keep state reactive using Flow and lifecycle-aware collection.
- Favor incremental architectural changes unless explicitly requested.

When adding complexity:
- Prefer extracting small, composable units first.
- Introduce a use-case layer only when view model logic becomes difficult to maintain.

## Build and Test
- Use Android Gradle workflow and Java/Kotlin target 17.
- Standard local commands:
- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:connectedDebugAndroidTest` (when device/emulator is available)

Before finalizing changes:
- Run at least debug build and relevant tests for touched areas.
- Fix compile errors and high-confidence regressions introduced by the change.

## Data and Domain Rules
- Keep Room schema changes explicit and migration-safe when version is incremented.
- Avoid breaking enum persistence semantics without migration planning.
- Keep task sorting and filtering logic deterministic and easy to test.
- Prefer repository methods that encode business intent over ad hoc queries in UI layer.

## Notification and Worker Rules
- Deadline reminders must be meaningful, timely, and non-spammy.
- Preserve or improve deduplication strategy for repeated periodic runs.
- Handle worker failures defensively and avoid crash-prone assumptions.
- Keep notification channel and payload behavior backward compatible where possible.

## UI and UX Conventions
- Keep Compose components small, readable, and preview/test-friendly.
- Maintain clear state ownership and unidirectional data flow.
- Validate task input on the client side with user-visible feedback.
- Respect accessibility basics: semantic labels, readable contrast, touch target sizes, and keyboard/accessibility service compatibility.

## Performance and Reliability
- Minimize unnecessary recomposition and avoid heavy logic in composables.
- Keep background work intervals and query ranges intentional and documented.
- Avoid expensive database or date transformations on hot UI paths when avoidable.

## Security and Privacy
- Do not log sensitive user content unnecessarily.
- Do not hardcode secrets or credentials in source files.
- Request runtime permissions only when needed and handle denial paths gracefully.

## Code Conventions
- Prefer descriptive naming over abbreviations in domain and UI code.
- Keep files focused; avoid large multi-responsibility classes.
- Add concise comments only where logic is non-obvious.
- Preserve existing style and public APIs unless the task requires change.

## Planning Alignment
- Use this milestone order when proposing substantial work:
- Milestone 1: foundation hardening (validation, worker resilience).
- Milestone 2: tests for repository/view model/worker logic.
- Milestone 3: notification UX and dedup improvements.
- Milestone 4: task edit/detail completeness and sort enhancements.
- Milestone 5: release readiness (accessibility, performance, smoke checks).

## Documentation Practice
- Keep instruction files concise and policy-focused.
- Put design details and evolving decisions in project docs such as architecture and plan documents.
- When behavior changes, update relevant documentation in the same change where practical.