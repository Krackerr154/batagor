# StudentTaskManager Implementation Plan

## 1. Objective
Stabilize and evolve StudentTaskManager into a reliable, testable, and user-friendly task planner with robust deadline reminders.

## 2. Current Baseline
Already implemented:
- Compose UI with list, calendar, and create-task screens.
- Room persistence for tasks.
- Tag filtering and status updates.
- Periodic WorkManager deadline notifications.

Main gaps:
- No automated tests.
- Limited validation and error handling.
- Notification deduplication and UX polish are missing.

## 3. Milestones
Issue drafts for direct GitHub issue creation:
- Milestone 1 draft: `.github/issue-drafts/01-m1-foundation-hardening.md`
- Milestone 2 draft: `.github/issue-drafts/02-m2-core-quality-and-tests.md`
- Milestone 3 draft: `.github/issue-drafts/03-m3-notification-ux-improvements.md`
- Milestone 4 draft: `.github/issue-drafts/04-m4-feature-completeness-task-management.md`
- Milestone 5 draft: `.github/issue-drafts/05-m5-release-readiness.md`

### Milestone 1: Foundation Hardening
Scope:
- Add central constants for worker intervals and reminder windows.
- Improve worker resilience (defensive failure handling).
- Add basic UI error feedback for invalid form inputs.

Acceptance criteria:
- App builds cleanly.
- Worker does not crash app on data/query failures.
- Task form blocks invalid saves with user-visible feedback.

### Milestone 2: Core Quality and Tests
Scope:
- Add unit tests for TaskRepository behavior.
- Add TaskViewModel tests for filtering and CRUD-trigger behavior.
- Add worker unit test coverage for urgency logic.

Acceptance criteria:
- Tests run in CI/local with deterministic results.
- Critical flows (save, filter, status update, deadline query) are covered.

### Milestone 3: Notification UX Improvements
Scope:
- Add deduplication strategy for repeated worker notifications.
- Improve notification content and grouping behavior.
- Add tap action to open app contextually.

Acceptance criteria:
- Same task is not repeatedly notified within configured window.
- Notifications remain meaningful and actionable.

### Milestone 4: Feature Completeness for Task Management
Scope:
- Add task details/edit route.
- Add sorting options and optional overdue section.
- Add optional completion timestamps for analytics/readability.

Acceptance criteria:
- Existing tasks can be edited from UI.
- Sort/filter operations remain responsive.
- Data model migration is backward-compatible.

### Milestone 5: Release Readiness
Scope:
- Accessibility pass (labels, touch targets, contrast checks).
- Performance pass for long task lists.
- Prepare release build checks and smoke testing checklist.

Acceptance criteria:
- No blocker issues in smoke test scenarios.
- Release variant builds successfully.

## 4. Work Breakdown by Area
### UI and UX
- Improve form validation states and helper text.
- Add empty/error states for list and calendar surfaces.
- Add edit task flow from task card interaction.

### Data and Domain
- Add migration path before schema version increments.
- Add repository APIs for richer querying if needed.
- Keep domain enums stable or map them via migration-safe strategy.

### Background and Notifications
- Persist last-notified metadata per task.
- Guard worker scheduling with clear policy documentation.
- Improve message templates based on urgency tiers.

### Tooling and Quality
- Add unit test dependencies and initial test suites.
- Add static checks and baseline lint hygiene.
- Document local verification commands in repository docs.

## 5. Proposed Timeline (4 Weeks)
Week 1:
- Milestone 1 delivery.
- Start repository/view model test scaffolding.

Week 2:
- Milestone 2 delivery.
- CI/local test routine documented.

Week 3:
- Milestone 3 delivery.
- Begin edit-task feature implementation.

Week 4:
- Milestone 4 and 5 delivery.
- Release smoke tests and sign-off.

## 6. Risks and Mitigations
Risk:
- Room schema changes can break existing installs.
Mitigation:
- Add explicit migrations and test migration paths.

Risk:
- Notification fatigue from frequent reminders.
Mitigation:
- Add dedupe windows and user-tunable reminder settings later.

Risk:
- UI regressions while adding edit/details flow.
Mitigation:
- Introduce view model tests and scenario-based manual QA checklist.

## 7. Definition of Done
A milestone is complete when:
- Code is implemented and reviewed.
- Build passes for debug and release variants.
- Relevant tests pass.
- Behavior is manually verified against acceptance criteria.
- Documentation is updated where architecture or behavior changes.
