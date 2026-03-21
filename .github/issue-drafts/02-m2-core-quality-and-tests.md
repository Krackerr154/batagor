# Title
Milestone 2: Core Quality and Tests (repository + view model + worker)

# Labels
`milestone:m2`, `type:test`, `area:data`, `area:viewmodel`, `area:worker`, `priority:high`

# Body
## Summary
Introduce automated test coverage for the app's most critical logic paths: repository behavior, view model state handling, and worker urgency/reminder logic.

## Why
No automated test suite currently protects core task flows. This milestone establishes a stable quality baseline.

## Scope
- Add repository unit tests.
- Add `TaskViewModel` tests for filtering and write operations.
- Add worker-focused tests for urgency logic and reminder window behavior.

## Checklist
- [ ] Add/confirm unit test dependencies and test configuration.
- [ ] Add repository tests for insert, update, delete, and query behavior.
- [ ] Add view model tests for tag filter behavior and sorted output.
- [ ] Add view model tests for save/update/delete trigger behavior.
- [ ] Extract testable worker logic as needed (pure functions/helpers).
- [ ] Add worker tests for 24-hour and 3-day urgency categorization.
- [ ] Add tests for done-task exclusion and empty-result scenarios.
- [ ] Ensure deterministic tests (fixed clocks or controllable time source).

## Acceptance Criteria
- [ ] Unit tests run locally and pass consistently.
- [ ] Critical flows are covered: save, filter, status update, deadline selection.
- [ ] No flaky tests introduced.

## Verification
- [ ] Run `./gradlew :app:testDebugUnitTest`.
- [ ] Re-run tests at least once to confirm deterministic behavior.

## Out of Scope
- UX/content improvements to notifications (Milestone 3).
- Feature expansion (Milestone 4).
