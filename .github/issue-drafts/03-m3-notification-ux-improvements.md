# Title
Milestone 3: Notification UX Improvements (dedup + actionable reminders)

# Labels
`milestone:m3`, `type:enhancement`, `area:notifications`, `area:worker`, `priority:high`

# Body
## Summary
Improve reminder quality by preventing repeated spammy notifications and making reminders more actionable and useful.

## Why
Periodic worker runs can repeatedly notify for the same tasks, creating notification fatigue.

## Scope
- Add deduplication strategy for recurring worker runs.
- Improve notification copy and grouping behavior.
- Add tap action that opens app contextually.

## Checklist
- [ ] Define dedup strategy (for example: last-notified timestamp per task + window).
- [ ] Persist dedup metadata safely.
- [ ] Skip notification when task was already notified within dedup window.
- [ ] Refine urgency messaging for 24-hour and 3-day buckets.
- [ ] Add notification intent/pending intent to open relevant app screen.
- [ ] Validate behavior across repeated worker executions.
- [ ] Keep channel ID and backward compatibility intact.
- [ ] Document notification behavior changes.

## Acceptance Criteria
- [ ] Same task is not repeatedly notified inside the configured dedup window.
- [ ] Notifications remain clear, timely, and actionable.
- [ ] Existing users keep compatible notification channel behavior.

## Verification
- [ ] Manual test with repeated simulated worker runs.
- [ ] Manual test for tap action and app navigation.
- [ ] Confirm no duplicate notifications for unchanged task state.

## Out of Scope
- Full user-configurable notification preferences.
- Task edit/detail feature (Milestone 4).
