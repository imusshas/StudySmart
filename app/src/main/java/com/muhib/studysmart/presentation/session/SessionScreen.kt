package com.muhib.studysmart.presentation.session

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhib.studysmart.presentation.components.DeleteDialog
import com.muhib.studysmart.presentation.components.SubjectListBottomSheet
import com.muhib.studysmart.presentation.components.studySessionsList
import com.muhib.studysmart.util.Constants.ACTION_SERVICE_CANCEL
import com.muhib.studysmart.util.Constants.ACTION_SERVICE_START
import com.muhib.studysmart.util.Constants.ACTION_SERVICE_STOP
import com.muhib.studysmart.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit

@Destination(
    deepLinks = [DeepLink(
        action = Intent.ACTION_VIEW, uriPattern = "study_smart://dashboard/session"
    )]
)
@Composable
fun SessionScreenRoute(
    navigator: DestinationsNavigator, timerService: StudySessionTimerService
) {
    val viewModel: SessionViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    SessionScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarEvent = viewModel.snackbarEventFlow,
        onBackButtonClick = { navigator.navigateUp() },
        timerService = timerService
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionScreen(
    state: SessionState,
    onEvent: (SessionEvent) -> Unit,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onBackButtonClick: () -> Unit,
    timerService: StudySessionTimerService
) {
    val context = LocalContext.current

    val hours = timerService.hours.value
    val minutes = timerService.minutes.value
    val seconds = timerService.seconds.value
    val currentTimerState = timerService.currentTimerState.value

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val isBottomSheetOpen = remember { mutableStateOf(false) }

    val isDeleteDialogOpen = rememberSaveable { mutableStateOf(false) }


    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest { event ->
            when (event) {
                is SnackbarEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackbarEvent.NavigateUp -> { /* DO NOTHING */
                }
            }
        }
    }

    LaunchedEffect(key1 = state.subjects) {
        val subjectId = timerService.subjectId.value
        onEvent(
            SessionEvent.UpdateSubjectIdAndRelatedSubject(
                subjectId = subjectId,
                relatedToSubject = state.subjects.find { it.subjectId == subjectId }?.name
            )
        )
    }

    SubjectListBottomSheet(
        onSubjectClick = { subject ->
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    isBottomSheetOpen.value = false
                }
                onEvent(SessionEvent.OnRelatedSubjectChange(subject))
            }
        },
        onDismissRequest = { isBottomSheetOpen.value = false },
        sheetState = sheetState,
        isOpen = isBottomSheetOpen.value,
        subjects = state.subjects
    )

    DeleteDialog(
        onDismissRequest = { isDeleteDialogOpen.value = false },
        onConfirmButtonClicked = {
            onEvent(SessionEvent.DeleteSession)
            isDeleteDialogOpen.value = false
        },
        title = "Delete Session?",
        bodyText = "Are you sure want to delete this session? This action can not be undone.",
        isOpen = isDeleteDialogOpen.value
    )




    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SessionScreenTopBar(onBackButtonClick = onBackButtonClick)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                TimerSection(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }

            item {
                RelatedToSubjectSection(
                    selectSubjectButtonClick = { isBottomSheetOpen.value = true },
                    relatedToSubject = state.relatedToSubject ?: "",
                    seconds = seconds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }

            item {
                ButtonSection(
                    startButtonClick = {
                        if (state.subjectId != null && state.relatedToSubject != null) {
                            ServiceHelper.triggerForegroundService(
                                context = context,
                                action = if (currentTimerState == TimerState.STARTED) ACTION_SERVICE_STOP else ACTION_SERVICE_START,
                            )
                            timerService.subjectId.value = state.subjectId
                        } else {
                            onEvent(SessionEvent.NotifyToUpdateSubject)
                        }
                    },
                    cancelButtonClick = {
                        ServiceHelper.triggerForegroundService(
                            context = context,
                            action = ACTION_SERVICE_CANCEL,
                        )
                    },
                    finishButtonClick = {
                        val duration = timerService.duration.toLong(DurationUnit.SECONDS)
                        onEvent(SessionEvent.SaveSession(duration))
                        if (duration >= 36) {
                            ServiceHelper.triggerForegroundService(
                                context = context,
                                action = ACTION_SERVICE_CANCEL,
                            )
                        }
                    },
                    timerState = currentTimerState,
                    seconds = seconds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }

            studySessionsList(sectionTitle = "STUDY SESSIONS HISTORY",
                emptyListText = "You don't have any recent study session.\n" + "Start a study session to begin recording your progress.",
                sessions = state.sessions,
                onDeleteIconClick = { session ->
                    onEvent(SessionEvent.OnDeleteSessionButtonClick(session))
                    isDeleteDialogOpen.value = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionScreenTopBar(
    onBackButtonClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Navigate Back")
            }
        },
        title = { Text(text = "Study Sessions", style = MaterialTheme.typography.headlineSmall) },

        )
}


@Composable
private fun TimerSection(
    hours: String, minutes: String, seconds: String, modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .border(5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )
        Row {
            AnimatedContent(
                targetState = hours,
                label = hours,
                transitionSpec = { timerTextAnimation() }
            ) { hours ->
                Text(
                    text = "$hours:",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                )
            }
            AnimatedContent(
                targetState = minutes,
                label = minutes,
                transitionSpec = { timerTextAnimation() }
            ) { minutes ->
                Text(
                    text = "$minutes:",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                )
            }
            AnimatedContent(
                targetState = seconds,
                label = seconds,
                transitionSpec = { timerTextAnimation() }
            ) { seconds ->
                Text(
                    text = seconds,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                )
            }
        }
    }
}

@Composable
private fun RelatedToSubjectSection(
    selectSubjectButtonClick: () -> Unit,
    relatedToSubject: String,
    seconds: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = "Related To Subject", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = relatedToSubject, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = selectSubjectButtonClick, enabled = seconds == "00") {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Subject"
                )
            }
        }
    }

}


@Composable
fun ButtonSection(
    cancelButtonClick: () -> Unit,
    startButtonClick: () -> Unit,
    finishButtonClick: () -> Unit,
    timerState: TimerState,
    seconds: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Button(
            onClick = cancelButtonClick,
            enabled = seconds != "00" && timerState != TimerState.STARTED,
        ) {
            Text(text = "Cancel", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
        }
        Button(
            onClick = startButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (timerState == TimerState.STARTED) Color.Red else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = when (timerState) {
                    TimerState.STARTED -> "Stop"
                    TimerState.STOPPED -> "Resume"
                    else -> "Start"
                }, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        Button(
            onClick = finishButtonClick,
            enabled = seconds != "00" && timerState != TimerState.STARTED,
        ) {
            Text(text = "Finish", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
        }
    }
}


private fun timerTextAnimation(duration: Int = 600): ContentTransform {
    return slideInVertically(animationSpec = tween(duration)) { fullHeight -> fullHeight } +
            fadeIn(animationSpec = tween(duration)) togetherWith
            slideOutVertically(animationSpec = tween(duration)) { fullHeight -> -fullHeight } +
            fadeOut(animationSpec = tween(duration))
}