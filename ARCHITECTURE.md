# StudentTaskManager Architecture

## 1. System Overview
StudentTaskManager is a single-module Android app built with Kotlin, Jetpack Compose, Room, and WorkManager.

Primary goals:
- Create and manage student tasks.
- View tasks in deadline-first list and calendar-like planner views.
- Notify users about upcoming deadlines.

High-level characteristics:
- Offline-first local persistence with Room.
- Reactive UI state with Kotlin Flow and Compose.
- Background scheduling with periodic WorkManager jobs.

## 2. Module and Package Structure
Root Gradle project:
- :app (Android application module)

Core package namespace:
- com.geraldarya.studenttasks

Package responsibilities:
- data: Room entities, DAO, database, converters, repository.
- domain: enums and business taxonomy (priority, status, tag).
- ui: Compose app shell, screens, view model, and components.
- notifications: notification channel and notification publishing.
- worker: periodic deadline scan and notification trigger.

## 3. Layered Architecture
### Presentation Layer
- MainActivity initializes Compose and runtime notification permission request.
- StudentTaskApp sets up navigation, scaffold, tabs, and FAB.
- Screens:
  - ListScreen: tag-filtered, deadline-first task listing.
  - CalendarScreen: date-grouped task browsing for a fixed range.
  - TaskFormScreen: task input and save flow.
- TaskCard handles status updates and delete actions.

### State and Application Layer
- TaskViewModel exposes TaskUiState as StateFlow.
- TaskViewModel combines repository task stream and selected filter tag.
- TaskViewModel handles save, update status, and delete actions.

### Data Layer
- TaskEntity models persisted tasks.
- TaskDao defines queries and CRUD operations.
- TaskRepository provides an abstraction over DAO access.
- AppDatabase provides singleton Room database instance.
- Converters map enum values to and from persisted strings.

### Background and Notification Layer
- StudentTaskApplication creates repository and schedules periodic work.
- DeadlineNotificationWorker runs every 6 hours and queries upcoming tasks.
- NotificationHelper creates channel and posts deadline notifications.

## 4. Data Model
TaskEntity fields:
- id (Long, primary key, auto-generated)
- title (String)
- description (String)
- dueAtMillis (Long)
- tag (TaskTag)
- priority (TaskPriority)
- status (TaskStatus)
- createdAtMillis (Long, default current time)
- lastNotifiedAtMillis (Long, default 0, tracks last notification timestamp)

Domain enums:
- TaskPriority: HIGH, MEDIUM, LOW
- TaskStatus: TODO, IN_PROGRESS, DONE
- TaskTag: COURSEWORK, LAB_RESEARCH, THESIS, EXAMS, PERSONAL

## 5. Runtime Flows
### Task Creation Flow
1. User opens TaskFormScreen from FAB.
2. User enters fields and selects date/time and enums.
3. Screen calls TaskViewModel.saveTask.
4. ViewModel maps fields to TaskEntity and calls repository.save.
5. Repository inserts/updates through TaskDao.
6. Room Flow emits updated list and UI re-renders.

### Task Browsing and Filtering Flow
1. TaskViewModel observes repository.observeTasks.
2. TaskViewModel combines task list with selected TaskTag filter.
3. UI receives TaskUiState and renders list or calendar grouping.

### Deadline Notification Flow
1. WorkManager triggers DeadlineNotificationWorker every 6 hours.
2. Worker queries tasks not DONE and due within 3 days.
3. Worker applies deduplication logic:
   - Skips tasks notified within last 6 hours (dedup window).
   - Only notifies tasks that are new or outside dedup window.
4. Worker calculates urgency labels with visual indicators:
   - ⚠️ Overdue (past due date)
   - 🔴 Due within 24 hours
   - 🟡 Due within 3 days
5. NotificationHelper publishes one notification per upcoming task.
6. Notification includes tap action to open app (MainActivity).
7. Worker updates lastNotifiedAtMillis timestamp for each notified task.

## 6. Navigation Architecture
Route graph:
- list (start destination)
- calendar
- create

Navigation behavior:
- Bottom bar visible on list and calendar.
- FAB hidden on create route.
- Back from create returns to previous route.

## 7. Cross-Cutting Concerns
### Permissions
- POST_NOTIFICATIONS requested at runtime on Android 13+.

### Threading and Concurrency
- Database mutations run in viewModelScope coroutines.
- Room query streams are Flow-based and lifecycle-friendly.
- Background checks run in CoroutineWorker.

### Error Handling
- Current implementation returns WorkManager success unconditionally.
- UI save path guards only against blank title.
- No explicit user-facing error state for database or worker failures yet.

## 8. Build and Runtime Dependencies
Main technologies:
- Kotlin + Android Gradle Plugin
- Jetpack Compose + Material3
- Navigation Compose
- Room
- WorkManager
- Coroutines

Build targets:
- minSdk 26
- targetSdk 35
- compileSdk 35
- Java/Kotlin target 17

## 9. Risks and Improvement Opportunities
Current risks:
- Form validation is minimal (title and past-date only).
- No snooze or dismiss-forever options for notifications.
- Missing dedicated integration tests for worker end-to-end flow.

Addressed in Milestone 3:
- ✅ Notification deduplication prevents spam during repeated worker runs.
- ✅ Tap action allows users to open app directly from notification.
- ✅ Enhanced urgency messaging with visual indicators (emojis).
- ✅ lastNotifiedAtMillis timestamp persisted in database (migration 1→2).

Suggested next architecture increments:
- Add use-case layer for domain actions if complexity grows.
- Add editable task details route and deep-link capable navigation.
- Introduce explicit error and loading UI states.
- Add dependency injection (for example Hilt) for scalability.
- Add notification action buttons (e.g., "Mark as Done", "Snooze").
