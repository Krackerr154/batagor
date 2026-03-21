# Running Tests - Quick Reference

## Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Gradle 8.0+

## Run All Tests
```bash
./gradlew :app:testDebugUnitTest
```

## Run Specific Test Class
```bash
# Repository tests
./gradlew :app:testDebugUnitTest --tests "*.TaskRepositoryTest"

# ViewModel tests
./gradlew :app:testDebugUnitTest --tests "*.TaskViewModelTest"

# DeadlineHelper tests
./gradlew :app:testDebugUnitTest --tests "*.DeadlineHelperTest"

# Worker tests
./gradlew :app:testDebugUnitTest --tests "*.DeadlineNotificationWorkerTest"
```

## Run Specific Test Method
```bash
./gradlew :app:testDebugUnitTest --tests "*.TaskRepositoryTest.testSaveInsertNewTask_ReturnsPositiveId"
```

## Run Tests with Coverage
```bash
./gradlew :app:testDebugUnitTest jacocoTestReport
```

## View Test Results
After running tests, open:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

## Test Organization

```
app/src/test/java/com/geraldarya/studenttasks/
├── data/
│   └── TaskRepositoryTest.kt          (9 tests - repository CRUD)
├── ui/
│   └── TaskViewModelTest.kt           (11 tests - filtering, sorting, state)
└── worker/
    ├── DeadlineNotificationWorkerTest.kt  (15 tests - worker behavior)
    └── utils/
        └── DeadlineHelperTest.kt      (18 tests - urgency logic)
```

## Common Issues & Solutions

### Issue: "No tests found"
**Solution**: Clean and rebuild
```bash
./gradlew clean
./gradlew :app:testDebugUnitTest
```

### Issue: "Mockito cannot mock final classes"
**Solution**: Already configured in `testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")`

### Issue: "CoroutineTest timeout"
**Solution**: Tests use `StandardTestDispatcher` and `advanceUntilIdle()` for deterministic execution

### Issue: "Android dependencies not found"
**Solution**: Robolectric is configured for worker tests that need Context

## Test Maintenance Guidelines

### When Adding New Repository Methods
1. Add test to `TaskRepositoryTest.kt`
2. Mock the DAO behavior
3. Verify both success and error cases
4. Use `runTest` for coroutines

### When Adding New ViewModel Features
1. Add test to `TaskViewModelTest.kt`
2. Use `MutableStateFlow` to control repository emissions
3. Test state flow output with `advanceUntilIdle()`
4. Verify UI state transformations

### When Modifying Worker Logic
1. Extract pure functions to `DeadlineHelper` if possible
2. Add tests to `DeadlineHelperTest.kt` for pure logic
3. Document behavior in `DeadlineNotificationWorkerTest.kt`
4. Use fixed timestamps for deterministic tests

## Test Patterns

### Repository Test Pattern
```kotlin
@Test
fun testMethodName() = runTest {
    // Given: setup mock behavior
    whenever(mockDao.method()).thenReturn(expectedValue)

    // When: call repository method
    val result = repository.method()

    // Then: verify behavior
    verify(mockDao).method()
    assertEquals(expectedValue, result)
}
```

### ViewModel Test Pattern
```kotlin
@Test
fun testMethodName() = runTest {
    // Given: setup flow emissions
    taskFlow.value = testData
    advanceUntilIdle()

    // When: trigger action
    viewModel.doSomething()
    advanceUntilIdle()

    // Then: verify state
    val state = viewModel.uiState.value
    assertEquals(expectedState, state)
}
```

### Helper Test Pattern
```kotlin
@Test
fun testMethodName() {
    // Given: fixed inputs
    val input = 1000L

    // When: call pure function
    val result = Helper.method(input)

    // Then: verify output
    assertEquals(expectedOutput, result)
}
```

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run Unit Tests
  run: ./gradlew :app:testDebugUnitTest

- name: Upload Test Results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: test-results
    path: app/build/reports/tests/
```

### Pre-commit Hook Example
```bash
#!/bin/bash
./gradlew :app:testDebugUnitTest --quiet
if [ $? -ne 0 ]; then
    echo "Tests failed! Commit rejected."
    exit 1
fi
```

## Debugging Tests

### Enable Test Logging
Add to `app/build.gradle.kts`:
```kotlin
tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
```

### Run Single Test with Stack Trace
```bash
./gradlew :app:testDebugUnitTest --tests "*.TestClass.testMethod" --stacktrace
```

### Debug in Android Studio
1. Open test file
2. Click gutter icon next to test method
3. Select "Debug 'testMethod()'"
4. Set breakpoints as needed

## Test Performance

Average execution times (reference):
- TaskRepositoryTest: ~200ms
- TaskViewModelTest: ~300ms
- DeadlineHelperTest: ~100ms
- DeadlineNotificationWorkerTest: ~400ms

**Total suite**: ~1 second (deterministic, no flakiness)

## Coverage Goals

- **Repository**: 100% of public methods
- **ViewModel**: 100% of public methods and state transformations
- **Worker Logic**: 100% of urgency determination and validation
- **Edge Cases**: Boundaries, null values, empty collections

## Next Steps for Test Expansion

If expanding test coverage in future milestones:

1. **Integration Tests** (app/src/androidTest/)
   - Room database end-to-end
   - Worker execution with real WorkManager
   - Notification display

2. **UI Tests** (Compose UI tests)
   - Screen navigation
   - User interactions
   - Form validation

3. **Performance Tests**
   - Large dataset handling
   - Flow emission performance
   - Database query optimization

---

**Last Updated**: Milestone 2 completion
**Test Count**: 53 tests
**Coverage**: Repository (9), ViewModel (11), Helper (18), Worker (15)
