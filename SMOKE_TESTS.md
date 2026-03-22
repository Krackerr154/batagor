# Smoke Test Checklist for StudentTaskManager

This document outlines critical flows and features to verify before release.

## Critical User Flows

### 1. Task Creation Flow
- [ ] Launch app successfully
- [ ] Navigate to Create Task screen via FAB
- [ ] Fill in task title (minimum 3 characters)
- [ ] Fill in task description
- [ ] Select due date via date picker
- [ ] Select due time via time picker
- [ ] Select task tag (COURSEWORK, LAB_RESEARCH, THESIS, EXAMS, PERSONAL)
- [ ] Select task priority (HIGH, MEDIUM, LOW)
- [ ] Select task status (TODO, IN_PROGRESS, DONE)
- [ ] Tap "Save task"
- [ ] Verify task appears in list screen
- [ ] Verify task shows correct details

#### Validation Tests
- [ ] Try to save task with empty title - should show error
- [ ] Try to save task with < 3 character title - should show error
- [ ] Try to save new task with past due date - should show error (edit mode allows past dates)

### 2. Task List View Flow
- [ ] View tasks in default sort order (due date)
- [ ] Verify overdue tasks appear in "Overdue" section with warning emoji
- [ ] Verify upcoming tasks appear in "Upcoming & Completed" section
- [ ] Verify task cards show all information:
  - [ ] Title
  - [ ] Description (if present)
  - [ ] Due date and time
  - [ ] Tag and priority
  - [ ] Status
- [ ] Verify deadline color coding:
  - [ ] DONE tasks: gray (surfaceContainerLow)
  - [ ] < 24h deadline: red (errorContainer)
  - [ ] < 3 days: yellow (tertiaryContainer)
  - [ ] Otherwise: default (surfaceContainerLowest)

### 3. Task Filtering Flow
- [ ] Select "All" filter - verify all tasks shown
- [ ] Select "COURSEWORK" filter - verify only coursework tasks shown
- [ ] Select "LAB_RESEARCH" filter - verify only lab research tasks shown
- [ ] Select "THESIS" filter - verify only thesis tasks shown
- [ ] Select "EXAMS" filter - verify only exams tasks shown
- [ ] Select "PERSONAL" filter - verify only personal tasks shown
- [ ] Verify selected filter chip is highlighted
- [ ] Clear filter and verify all tasks return

### 4. Task Sorting Flow
- [ ] Select "Due Date" sort - verify tasks ordered by deadline (earliest first)
- [ ] Select "Priority" sort - verify tasks ordered HIGH > MEDIUM > LOW, then by due date
- [ ] Select "Created" sort - verify tasks ordered by creation date (newest first)
- [ ] Verify selected sort chip is highlighted
- [ ] Verify sorting persists when filtering

### 5. Task Status Update Flow
- [ ] Tap "Status" button on task card
- [ ] Verify dropdown menu appears with all statuses
- [ ] Select "IN_PROGRESS"
- [ ] Verify task status updates immediately
- [ ] Change status to "DONE"
- [ ] Verify task color changes to gray
- [ ] Verify DONE tasks are excluded from overdue section

### 6. Task Edit Flow
- [ ] Tap "Edit" button on existing task
- [ ] Verify edit screen loads with correct task data
- [ ] Verify screen title shows "Edit task"
- [ ] Modify task title
- [ ] Modify task description
- [ ] Change due date and time
- [ ] Change tag, priority, status
- [ ] Tap "Save task"
- [ ] Verify navigated back to list
- [ ] Verify task shows updated information
- [ ] Verify task creation date is preserved
- [ ] Verify task notification timestamp is preserved

### 7. Task Delete Flow
- [ ] Tap "Delete" button on task card
- [ ] Verify task is removed from list immediately
- [ ] Verify deletion persists after app restart

### 8. Calendar View Flow
- [ ] Navigate to Calendar screen via bottom navigation
- [ ] Verify header shows "Scrollable planner" and "Browse the next 6 months"
- [ ] Scroll through dates (7 days ago to 180 days forward)
- [ ] Verify dates are grouped with clear date headers
- [ ] Verify tasks appear under correct dates
- [ ] Verify "No tasks" message for empty dates
- [ ] Tap "Edit" on task in calendar
- [ ] Verify navigation to edit screen works
- [ ] Tap status button and change status
- [ ] Verify status updates immediately

### 9. Navigation Flow
- [ ] Verify bottom navigation bar shows "List" and "Calendar" tabs
- [ ] Tap "List" - verify navigates to list screen
- [ ] Tap "Calendar" - verify navigates to calendar screen
- [ ] Verify bottom bar hidden on create screen
- [ ] Verify bottom bar hidden on edit screen
- [ ] Verify FAB hidden on create screen
- [ ] Verify FAB hidden on edit screen
- [ ] Verify back navigation works from create/edit screens

### 10. Background Worker & Notifications
- [ ] Create task due within 24 hours
- [ ] Wait for background worker to run (6-hour interval)
- [ ] Verify notification appears with:
  - [ ] Title: "{task_title} is approaching"
  - [ ] Body: Urgency indicator + description
  - [ ] Urgency emoji: 🔴 for < 24h, 🟡 for < 3 days, ⚠️ for overdue
- [ ] Tap notification
- [ ] Verify app opens to main screen
- [ ] Verify notification is not resent within 6-hour deduplication window

## Edge Cases

### Empty States
- [ ] Fresh install - verify no crash with empty task list
- [ ] Delete all tasks - verify empty list handled gracefully
- [ ] Filter to tag with no tasks - verify empty result handled

### Data Validation
- [ ] Very long task title (100+ characters) - verify renders correctly
- [ ] Very long description (1000+ characters) - verify renders correctly
- [ ] Task with blank description - verify no error
- [ ] Multiple tasks with same due date - verify all render
- [ ] Task due in past (edited) - verify shows as overdue

### State Persistence
- [ ] Create task and restart app - verify task persists
- [ ] Change sort order and restart app - verify sort persists (may reset to default)
- [ ] Apply filter and restart app - verify filter persists (may reset to default)
- [ ] Edit task and restart app - verify edits persist

## Performance Checks

### Large Data Sets
- [ ] Create 50+ tasks - verify list scrolls smoothly
- [ ] Create 100+ tasks - verify no lag in UI
- [ ] Apply filter to 100+ tasks - verify filtering is instant
- [ ] Change sort order with 100+ tasks - verify sorting is instant
- [ ] Scroll calendar view with 100+ tasks - verify smooth scrolling

### Memory & Battery
- [ ] Run app for 30+ minutes - verify no memory leaks
- [ ] Background worker runs multiple times - verify no excessive battery drain
- [ ] Multiple notification deliveries - verify deduplication works

## Accessibility Checks

### Screen Reader Support
- [ ] Enable TalkBack/screen reader
- [ ] Navigate through list screen - verify all elements announced
- [ ] Interact with task card buttons - verify actions announced
- [ ] Change filters - verify filter changes announced
- [ ] Change sort order - verify sort changes announced
- [ ] Navigate calendar - verify date sections announced

### Touch Targets
- [ ] Verify all buttons are at least 48dp in height
- [ ] Verify buttons are easily tappable without mistakes
- [ ] Verify dropdown menus are accessible

### Visual Accessibility
- [ ] Verify text contrast meets WCAG guidelines
- [ ] Verify color coding includes non-color indicators (emojis, text)
- [ ] Verify UI readable with system font size increased
- [ ] Verify UI works with dark/light theme

## Build & Configuration

### Debug Build
- [ ] Run `./gradlew :app:assembleDebug`
- [ ] Verify build succeeds
- [ ] Install APK on device/emulator
- [ ] Verify app launches and functions

### Unit Tests
- [ ] Run `./gradlew :app:testDebugUnitTest`
- [ ] Verify all tests pass (53 tests expected)
- [ ] Check test coverage report

### Release Build (if configured)
- [ ] Run release build command
- [ ] Verify build succeeds
- [ ] Verify ProGuard/R8 rules are correct (if minification enabled)
- [ ] Install release APK
- [ ] Perform critical flow smoke tests

## Known Limitations & Notes

### Current Implementation
- Task list partitioning happens at composition time (overdue vs upcoming)
- Sort order and filter state may reset on app restart (stored in ViewModel, not persistent)
- Bottom navigation and FAB visibility controlled by route checking
- Database migrations: v1→v2 adds lastNotifiedAtMillis for notification dedup

### Environment Issues
- Build environment may have Maven repository access issues
- If build fails with "Plugin not found" errors, repository configuration needs fixing

### Future Improvements
- Consider making sort/filter state persistent across app restarts
- Consider adding empty state illustrations
- Consider adding task search functionality
- Consider adding task export/import
- Consider adding task categories/projects
