# Title
Milestone 1: Foundation Hardening (validation + worker resilience)

# Labels
`milestone:m1`, `type:enhancement`, `area:worker`, `area:ui`, `priority:high`

# Body
## Summary
Harden core reliability by improving deadline worker resilience and adding user-visible task form validation.

## Why
Current behavior has limited validation and weak failure handling. This milestone reduces crash risk and prevents invalid task creation.

## Scope
- Add central constants for worker intervals and reminder windows.
- Improve `DeadlineNotificationWorker` defensive error handling.
- Add validation feedback to task form for invalid input.

## Checklist
- [ ] Add shared reminder constants (worker interval, lookahead window, urgency thresholds).
- [ ] Refactor worker to wrap query/processing with defensive exception handling.
- [ ] Return `Result.retry()` or `Result.failure()` intentionally based on failure type.
- [ ] Add input rules for task title and due date/time validity.
- [ ] Show validation errors inline in the form UI.
- [ ] Prevent save when validation fails.
- [ ] Confirm no regression in create-task flow.
- [ ] Update docs if behavior changes.

## Acceptance Criteria
- [ ] App builds successfully in debug.
- [ ] Worker does not crash app when data/query fails.
- [ ] Invalid task input is blocked with clear UI feedback.

## Verification
- [ ] Run `./gradlew :app:assembleDebug`.
- [ ] Manual test: create valid and invalid tasks.
- [ ] Manual test: simulate worker edge cases and verify graceful handling.

## Out of Scope
- Notification deduplication logic (Milestone 3).
- Task edit/detail route (Milestone 4).
