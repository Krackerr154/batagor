# Notification Deduplication System

## Overview
StudentTaskManager implements a deduplication strategy to prevent notification spam during repeated worker runs. This ensures users receive timely reminders without being overwhelmed by duplicate notifications for the same task.

## Strategy

### Deduplication Window
- **Window Duration**: 6 hours (same as worker interval)
- **Constant**: `ReminderConstants.DEDUP_WINDOW_MILLIS`
- **Purpose**: Prevents re-notifying the same task within a short time period

### Implementation Details

#### 1. Database Schema
TaskEntity includes a `lastNotifiedAtMillis` field:
```kotlin
data class TaskEntity(
    // ... other fields
    val lastNotifiedAtMillis: Long = 0  // 0 = never notified
)
```

**Migration**: Database version 1 → 2 adds this column with default value 0.

#### 2. Deduplication Logic
The `DeadlineHelper.shouldNotifyTask()` function determines if a task should be notified:

```kotlin
fun shouldNotifyTask(task: TaskEntity, currentMillis: Long): Boolean {
    // Never notified before → notify
    if (task.lastNotifiedAtMillis == 0L) {
        return true
    }

    // Check if outside dedup window
    val timeSinceLastNotification = currentMillis - task.lastNotifiedAtMillis
    return timeSinceLastNotification >= ReminderConstants.DEDUP_WINDOW_MILLIS
}
```

**Logic Flow**:
1. If `lastNotifiedAtMillis == 0` → **Notify** (task never notified)
2. If `currentTime - lastNotifiedAtMillis >= 6 hours` → **Notify** (outside dedup window)
3. Otherwise → **Skip** (within dedup window)

#### 3. Worker Integration
`DeadlineNotificationWorker` applies deduplication during task processing:

```kotlin
tasks.forEach { task ->
    // Validate task
    if (!DeadlineHelper.isValidForNotification(task)) {
        return@forEach
    }

    // Apply deduplication check
    if (!DeadlineHelper.shouldNotifyTask(task, now)) {
        Log.d(TAG, "Skipping recently notified task: id=${task.id}")
        return@forEach
    }

    // Determine urgency and send notification
    val urgency = DeadlineHelper.determineUrgency(task.dueAtMillis, now)
    if (urgency != null) {
        NotificationHelper.showDeadlineNotification(...)

        // Update timestamp after successful notification
        repository.updateLastNotified(task.id, now)
    }
}
```

## Behavior Examples

### Scenario 1: First Notification
- **Task**: Assignment due in 2 days
- **lastNotifiedAtMillis**: 0 (never notified)
- **Result**: ✅ Notification sent, timestamp updated

### Scenario 2: Repeated Worker Run (Within Window)
- **Task**: Same assignment
- **Worker Runs**: 2 hours after first notification
- **lastNotifiedAtMillis**: 2 hours ago
- **Result**: ⛔ Skip notification (within 6-hour window)

### Scenario 3: Repeated Worker Run (Outside Window)
- **Task**: Same assignment
- **Worker Runs**: 7 hours after first notification
- **lastNotifiedAtMillis**: 7 hours ago
- **Result**: ✅ Notification sent again, timestamp updated

### Scenario 4: Task Status Change
- **Task**: User marks task as DONE
- **Worker Runs**: Task excluded from query (status != DONE)
- **Result**: ⛔ No notification (filtered at database level)

## Edge Cases

### Concurrent Worker Runs
- **Scenario**: Multiple worker instances run simultaneously
- **Handling**: Room database handles concurrent writes atomically
- **Result**: At most one notification per task per dedup window

### Clock Changes
- **Scenario**: System time changes backward
- **Handling**: Deduplication uses elapsed time calculation
- **Result**: May allow notification if negative elapsed time detected
- **Impact**: Minimal - users prefer extra notification over missed reminder

### Database Migration
- **Scenario**: Existing tasks from v1 database
- **Handling**: Migration sets `lastNotifiedAtMillis = 0` for all existing tasks
- **Result**: All existing tasks eligible for immediate notification on first run

## Testing

### Unit Tests
`DeadlineHelperTest.kt` includes comprehensive dedup tests:
- `testShouldNotifyTask_NeverNotified_ReturnsTrue`
- `testShouldNotifyTask_NotifiedRecently_ReturnsFalse`
- `testShouldNotifyTask_NotifiedExactlyAtDedupWindow_ReturnsTrue`
- `testShouldNotifyTask_NotifiedBeyondDedupWindow_ReturnsTrue`
- `testShouldNotifyTask_NotifiedJustNow_ReturnsFalse`

### Manual Validation
To verify deduplication behavior:

1. **Create a task** due in 2 days
2. **Trigger worker** manually (first run)
   - Expected: Notification sent
3. **Trigger worker** again immediately (within 6 hours)
   - Expected: No duplicate notification
4. **Wait 6+ hours**, trigger worker again
   - Expected: Notification sent again

## Constants Reference

```kotlin
// ReminderConstants.kt
const val WORKER_INTERVAL_HOURS = 6L          // Worker runs every 6 hours
const val DEDUP_WINDOW_HOURS = 6L             // Dedup window matches interval
const val DEDUP_WINDOW_MILLIS = 6L * 60 * 60 * 1000L  // 21,600,000 ms
```

## Monitoring and Debugging

### Log Messages
Worker logs dedup decisions:
```
D/DeadlineNotificationWorker: Skipping recently notified task: id=123
D/DeadlineNotificationWorker: Worker completed successfully: sent N notifications
```

### Database Inspection
Query last notified timestamps:
```sql
SELECT id, title, lastNotifiedAtMillis, dueAtMillis
FROM tasks
WHERE status != 'DONE'
ORDER BY lastNotifiedAtMillis DESC;
```

## Future Enhancements

### Possible Improvements
1. **User Preferences**: Allow users to configure dedup window duration
2. **Snooze Feature**: Let users snooze specific tasks for custom duration
3. **Urgency-Based Windows**: Shorter dedup for urgent tasks (24h), longer for 3-day tasks
4. **Notification History**: Store full notification history for analytics
5. **Smart Dedup**: Consider task changes (title, deadline) as new notification triggers

### Backward Compatibility
- Database migration preserves all existing task data
- Notification channel ID unchanged (`student_deadlines`)
- Dedup is additive - no existing behavior broken
- Users may receive immediate notifications for old tasks after upgrade (by design)

## Related Files
- `TaskEntity.kt` - Data model with lastNotifiedAtMillis field
- `AppDatabase.kt` - Migration 1→2 implementation
- `DeadlineHelper.kt` - Pure dedup logic functions
- `DeadlineNotificationWorker.kt` - Worker integration
- `TaskDao.kt` - Database operations including updateLastNotified
- `ReminderConstants.kt` - Dedup window configuration
- `DeadlineHelperTest.kt` - Comprehensive unit tests
