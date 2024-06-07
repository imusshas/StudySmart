package com.muhib.studysmart.presentation.subject

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhib.studysmart.domain.model.Subject
import com.muhib.studysmart.domain.model.Task
import com.muhib.studysmart.domain.repositorry.SessionRepository
import com.muhib.studysmart.domain.repositorry.SubjectRepository
import com.muhib.studysmart.domain.repositorry.TaskRepository
import com.muhib.studysmart.presentation.navArgs
import com.muhib.studysmart.util.SnackbarEvent
import com.muhib.studysmart.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navArgs: SubjectScreenNavArgs = savedStateHandle.navArgs()


    private val _state = MutableStateFlow(SubjectState())

    val state = combine(
        _state,
        taskRepository.getUpcomingTasksForSubject(navArgs.subjectId),
        taskRepository.getCompletedTasksForSubject(navArgs.subjectId),
        sessionRepository.getRecentTenSessionsForSubject(navArgs.subjectId),
        sessionRepository.getTotalSessionsDurationBySubject(navArgs.subjectId)
    ) { state, upcomingTasks, completedTasks, recentSessions, totalSessionsDuration ->
        state.copy(
            upcomingTasks = upcomingTasks,
            completedTasks = completedTasks,
            recentSessions = recentSessions,
            studiedHours = totalSessionsDuration.toHours()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SubjectState()
    )

    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    init {
        fetchSubject()
    }

    fun onEvent(event: SubjectEvent) {
        when (event) {
            is SubjectEvent.OnSubjectCardColorChange -> {
                _state.update {
                    it.copy(subjectCardColors = event.color)
                }
            }

            is SubjectEvent.OnSubjectNameChange -> {
                _state.update {
                    it.copy(subjectName = event.name)
                }
            }

            is SubjectEvent.OnGoalStudyHoursChange -> {
                _state.update {
                    it.copy(goalStudyHours = event.hours)
                }
            }

            SubjectEvent.UpdateSubject -> updateSubject()
            SubjectEvent.DeleteSubject -> deleteSubject()
            is SubjectEvent.OnTaskIsCompleteChange -> updateTask(event.task)
            SubjectEvent.DeleteSession -> deleteSession()
            is SubjectEvent.OnDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(
                        session = event.session
                    )
                }
            }
            SubjectEvent.UpdateProgress -> {
                val goalStudyHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f
                _state.update {
                    it.copy(
                        progress = (state.value.studiedHours / goalStudyHours).coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    private fun deleteSession() = viewModelScope.launch {
        try {
            state.value.session?.let {
                sessionRepository.deleteSession(it)

                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        "Session deleted successfully",
                    )
                )
            }
        } catch (e: Exception) {
            _snackbarEventFlow.emit(
                SnackbarEvent.ShowSnackbar(
                    "Couldn't delete session. ${e.message}",
                    SnackbarDuration.Long
                )
            )
        }
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        try {
            taskRepository.upsertTask(
                task = task.copy( isComplete = !task.isComplete)
            )

            if (task.isComplete) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar("Saved in upcoming tasks.")
                )
            } else {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar("Saved in completed tasks.")
                )
            }
        } catch (e: Exception) {
            _snackbarEventFlow.emit(
                SnackbarEvent.ShowSnackbar(
                    "Couldn't update task. ${e.message}",
                    SnackbarDuration.Long
                )
            )
        }
    }

    private fun deleteSubject() = viewModelScope.launch {
        try {
            val currentSubjectId = state.value.currentSubjectId
            if (currentSubjectId != null) {
                withContext(Dispatchers.IO) {
                    subjectRepository.deleteSubject(subjectId = currentSubjectId)
                }
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(message = "Subject deleted successfully.")
                )
                _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
            } else {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(message = "No Subject to delete")
                )
            }
        } catch (e: Exception) {
            _snackbarEventFlow.emit(
                SnackbarEvent.ShowSnackbar(
                    message = "Couldn't delete subject. ${e.message}",
                    duration = SnackbarDuration.Long
                )
            )
        }

    }

    private fun updateSubject() = viewModelScope.launch {
        try {
            subjectRepository.upsertSubject(
                Subject(
                    subjectId = state.value.currentSubjectId,
                    name = state.value.subjectName,
                    goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                    colors = state.value.subjectCardColors.map { it.toArgb() }
                )
            )

            _snackbarEventFlow.emit(
                SnackbarEvent.ShowSnackbar(
                    message = "Subject updated successfully."
                )
            )

        } catch (e: Exception) {
            _snackbarEventFlow.emit(
                SnackbarEvent.ShowSnackbar(
                    message = "Couldn't update subject. ${e.message}",
                    duration = SnackbarDuration.Long
                )
            )
        }
    }


    private fun fetchSubject() = viewModelScope.launch {
        subjectRepository.getSubjectById(navArgs.subjectId)?.let { subject: Subject ->
            _state.update { subjectState ->
                subjectState.copy(
                    subjectName = subject.name,
                    goalStudyHours = subject.goalHours.toString(),
                    subjectCardColors = subject.colors.map { Color(it) },
                    currentSubjectId = subject.subjectId
                )
            }
        }
    }

}