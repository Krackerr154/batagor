package com.geraldarya.studenttasks.ui

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geraldarya.studenttasks.StudentTaskApplication
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.ui.screens.CalendarScreen
import com.geraldarya.studenttasks.ui.screens.ListScreen
import com.geraldarya.studenttasks.ui.screens.TaskFormScreen
import kotlinx.coroutines.launch

private enum class DashboardDestination(val route: String, val label: String) {
    List("list", "List"),
    Calendar("calendar", "Calendar")
}

@Composable
fun StudentTaskApp() {
    val context = LocalContext.current.applicationContext as Application
    val app = context as StudentTaskApplication
    val viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(app.repository))

    val navController = rememberNavController()
    val uiState by viewModel.uiState

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val destinations = listOf(DashboardDestination.List, DashboardDestination.Calendar)
            val currentRoute = currentDestination?.route
            val shouldShowBottomBar = currentRoute != "create" && !currentRoute?.startsWith("edit/") ?: true

            if (shouldShowBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        val icon = when (destination) {
                            DashboardDestination.List -> Icons.Default.List
                            DashboardDestination.Calendar -> Icons.Default.CalendarMonth
                        }
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val shouldShowFab = currentRoute != "create" && !currentRoute?.startsWith("edit/") ?: true
            if (shouldShowFab) {
                FloatingActionButton(onClick = { navController.navigate("create") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
            }
        }
    ) { padding ->
        AppNavHost(
            padding = padding,
            uiState = uiState,
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            navController = navController
        )
    }
}

@Composable
private fun AppNavHost(
    padding: PaddingValues,
    uiState: TaskUiState,
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    navController: androidx.navigation.NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = DashboardDestination.List.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(DashboardDestination.List.route) {
            ListScreen(
                uiState = uiState,
                onFilterChanged = viewModel::setFilter,
                onStatusChanged = viewModel::updateStatus,
                onDelete = viewModel::deleteTask,
                onEdit = { task ->
                    navController.navigate("edit/${task.id}")
                },
                onSortChanged = viewModel::setSortOrder
            )
        }
        composable(DashboardDestination.Calendar.route) {
            CalendarScreen(
                tasks = uiState.tasks,
                onStatusChanged = viewModel::updateStatus
            )
        }
        composable("create") {
            TaskFormScreen(
                onSave = viewModel::saveTask,
                onBack = onNavigateBack
            )
        }
        composable(
            route = "edit/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            val scope = rememberCoroutineScope()
            val taskState = remember { mutableStateOf<TaskEntity?>(null) }

            LaunchedEffect(taskId) {
                scope.launch {
                    taskState.value = viewModel.getTask(taskId)
                }
            }

            taskState.value?.let { task ->
                TaskFormScreen(
                    onSave = viewModel::saveTask,
                    onBack = onNavigateBack,
                    editingTaskId = task.id,
                    editingTaskTitle = task.title,
                    editingTaskDescription = task.description,
                    editingTaskDueAtMillis = task.dueAtMillis,
                    editingTaskTag = task.tag,
                    editingTaskPriority = task.priority,
                    editingTaskStatus = task.status
                )
            }
        }
    }
}
