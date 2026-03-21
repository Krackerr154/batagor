# Title
Milestone 5: Release Readiness (accessibility + performance + smoke checks)

# Labels
`milestone:m5`, `type:chore`, `area:release`, `area:accessibility`, `area:performance`, `priority:high`

# Body
## Summary
Prepare the app for release with focused accessibility review, performance checks, and a repeatable smoke test checklist.

## Why
After feature and reliability milestones, release confidence depends on quality gates and baseline verification.

## Scope
- Accessibility pass for key screens and interactions.
- Performance pass for task-heavy scenarios.
- Release build and smoke test checklist completion.

## Checklist
- [ ] Verify semantic labels for interactive UI elements.
- [ ] Validate minimum touch target sizes and readable contrast.
- [ ] Check keyboard/accessibility service compatibility where applicable.
- [ ] Measure and reduce unnecessary recompositions in hot UI paths.
- [ ] Evaluate long-list behavior and scrolling responsiveness.
- [ ] Create/update smoke test checklist for critical flows.
- [ ] Run release-oriented build checks.
- [ ] Capture and resolve blocker issues before sign-off.

## Acceptance Criteria
- [ ] No blocker findings in smoke tests.
- [ ] Release build succeeds.
- [ ] Accessibility and performance baseline checks are documented.

## Verification
- [ ] Run `./gradlew :app:assembleDebug`.
- [ ] Run `./gradlew :app:testDebugUnitTest`.
- [ ] Run release variant build command used by team workflow.

## Out of Scope
- New user-facing feature additions.
- Major architectural rewrites.
