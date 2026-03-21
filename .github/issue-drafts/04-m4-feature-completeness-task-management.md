# Title
Milestone 4: Feature Completeness for Task Management (edit/detail + sorting)

# Labels
`milestone:m4`, `type:feature`, `area:ui`, `area:data`, `priority:medium`

# Body
## Summary
Complete core task management by adding task detail/edit capability and improving organization with richer sorting options.

## Why
Current flow supports creation and status updates but lacks full edit/detail lifecycle and advanced sorting.

## Scope
- Add task details/edit route.
- Add sorting options and optional overdue grouping.
- Optionally store completion timestamp if needed for readability/analytics.

## Checklist
- [ ] Add navigation route for task details/edit.
- [ ] Add UI entry point from task card/list to open selected task.
- [ ] Load task by ID and prefill editable fields.
- [ ] Save edited task changes through view model/repository.
- [ ] Add sorting controls (for example: due date, priority, created date).
- [ ] Add optional overdue section/grouping in list screen.
- [ ] If schema changes are required, add migration plan and implementation.
- [ ] Validate calendar/list behavior after edits.

## Acceptance Criteria
- [ ] Existing tasks can be edited end-to-end from the UI.
- [ ] Sorting/filtering remains responsive and deterministic.
- [ ] Any schema change is migration-safe.

## Verification
- [ ] Run `./gradlew :app:assembleDebug`.
- [ ] Manual test: edit, cancel, and save task changes.
- [ ] Manual test: sort options and overdue grouping behavior.

## Out of Scope
- Accessibility/performance release pass (Milestone 5).
