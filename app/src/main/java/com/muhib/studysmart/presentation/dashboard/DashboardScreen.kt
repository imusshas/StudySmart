package com.muhib.studysmart.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhib.studysmart.R
import com.muhib.studysmart.domain.model.Session
import com.muhib.studysmart.domain.model.Subject
import com.muhib.studysmart.domain.model.Task
import com.muhib.studysmart.presentation.components.AddSubjectDialog
import com.muhib.studysmart.presentation.components.CountCard
import com.muhib.studysmart.presentation.components.DeleteDialog
import com.muhib.studysmart.presentation.components.SubjectCard
import com.muhib.studysmart.presentation.components.studySessionsList
import com.muhib.studysmart.presentation.components.taskList
import com.muhib.studysmart.presentation.destinations.SessionScreenRouteDestination
import com.muhib.studysmart.presentation.destinations.SubjectScreenRouteDestination
import com.muhib.studysmart.presentation.destinations.TaskScreenRouteDestination
import com.muhib.studysmart.presentation.subject.SubjectScreenNavArgs
import com.muhib.studysmart.presentation.task.TaskScreenNavArgs
import com.muhib.studysmart.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@RootNavGraph(start = true)
@Destination
@Composable
fun DashBoardScreenRoute(
    navigator: DestinationsNavigator
) {

    val viewModel: DashboardViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val tasks = viewModel.tasks.collectAsStateWithLifecycle().value
    val recentSessions = viewModel.recentSessions.collectAsStateWithLifecycle().value

    DashboardScreen(
        state = state,
        tasks = tasks,
        recentSessions = recentSessions,
        snackbarEvent = viewModel.snackbarEventFlow,
        onEvent = viewModel::onEvent,
        onSubjectCardClick = { subjectId ->
            subjectId?.let { id ->
                val navArg = SubjectScreenNavArgs(id)
                navigator.navigate(SubjectScreenRouteDestination(navArgs = navArg))
            }
        },
        onStartSessionButtonClick = {
            navigator.navigate(SessionScreenRouteDestination())
        },
        onTaskCardClick = { taskId ->
            val navArg = TaskScreenNavArgs(taskId = taskId, subjectId = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        }
    )
}

@Composable
private fun DashboardScreen(
    state: DashboardState,
    tasks: List<Task>,
    recentSessions: List<Session>,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onEvent: (DashboardEvent) -> Unit,
    onSubjectCardClick: (Int?) -> Unit,
    onTaskCardClick: (Int?) -> Unit,
    onStartSessionButtonClick: () -> Unit,
) {

    val isAddSubjectDialogOpen = rememberSaveable { mutableStateOf(false) }
    val isDeleteSessionDialogOpen = rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    
    
    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest {event ->
            when(event) {
                is SnackbarEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackbarEvent.NavigateUp -> { /* DO NOTHING */ }
            }
        }
    }


    AddSubjectDialog(
        onDismissRequest = { isAddSubjectDialogOpen.value = false },
        onConfirmButtonClicked = {
            onEvent(DashboardEvent.SaveSubject)
            isAddSubjectDialogOpen.value = false
        },
        isOpen = isAddSubjectDialogOpen.value,
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
        selectedColors = state.subjectCardColors,
        onSubjectNameChange = { onEvent(DashboardEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(DashboardEvent.OnGoalStudyHoursChange(it)) },
        onColorChange = { onEvent(DashboardEvent.OnSubjectCardColorChange(it)) },
    )

    DeleteDialog(
        onDismissRequest = { isDeleteSessionDialogOpen.value = false },
        onConfirmButtonClicked = {
            onEvent(DashboardEvent.DeleteSession)
            isDeleteSessionDialogOpen.value = false
        },
        title = "Delete Session?",
        bodyText = "Are you sure you want to delete this session?" +
                "Your studied hours will be reduced by this session time. This action can not be undone.",
        isOpen = isDeleteSessionDialogOpen.value
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            DashBoardScreenTopAppBard()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                CountCardSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    subjectCount = state.totalSubjectCount,
                    studiedHours = state.totalStudiedHours.toString(),
                    goalHours = state.totalGoalStudyHours.toString()
                )
            }
            item {
                SubjectCardSection(
                    onAddIconClick = { isAddSubjectDialogOpen.value = true },
                    onSubjectCardClick = onSubjectCardClick,
                    subjects = state.subjects,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(
                    onClick = onStartSessionButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 20.dp)
                ) {
                    Text(text = "Start Study Session")
                }
            }

            taskList(
                sectionTitle = "UPCOMING TASKS",
                emptyListText = "You don't have any upcoming tasks.\n" +
                        "Click the + button in subject screen to add new task.",
                tasks = tasks,
                onCheckBoxClick = { onEvent(DashboardEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            studySessionsList(
                sectionTitle = "RECENT STUDY SESSIONS",
                emptyListText = "You don't have any recent study session.\n" +
                        "Start a study session to begin recording your progress.",
                sessions = recentSessions,
                onDeleteIconClick = {
                    onEvent(DashboardEvent.OnDeleteSessionButtonClick(it))
                    isDeleteSessionDialogOpen.value = true
                }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashBoardScreenTopAppBard() {

    CenterAlignedTopAppBar(
        title = {
            Text(text = "StudySmart", style = MaterialTheme.typography.headlineMedium)
        }
    )
}

@Composable
private fun CountCardSection(
    modifier: Modifier = Modifier,
    subjectCount: Int,
    studiedHours: String,
    goalHours: String,
) {
    Row(modifier = modifier) {
        CountCard(
            headingText = "Subject Count",
            count = "$subjectCount",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            headingText = "Studied Hours",
            count = studiedHours,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            headingText = "Goal Study Hours",
            count = goalHours,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubjectCardSection(
    onAddIconClick: () -> Unit,
    onSubjectCardClick: (Int?) -> Unit,
    subjects: List<Subject>,
    modifier: Modifier = Modifier,
    emptyListText: String = "You don't have any subject.\nClick + button to add new subject."
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUBJECTS",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
            IconButton(onClick = onAddIconClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add subjects")
            }
        }

        if (subjects.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.book), contentDescription = "Books",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = emptyListText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(subjects) { subject ->
                SubjectCard(
                    subjectName = subject.name,
                    gradientColors = subject.colors.map { Color(it) },
                    onClick = {
                        onSubjectCardClick(subject.subjectId)
                    }
                )
            }
        }
    }
}






























