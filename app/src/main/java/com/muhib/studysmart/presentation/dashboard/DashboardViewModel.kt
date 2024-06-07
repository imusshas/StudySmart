package com.muhib.studysmart.presentation.dashboard

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhib.studysmart.domain.model.Session
import com.muhib.studysmart.domain.model.Subject
import com.muhib.studysmart.domain.model.Task
import com.muhib.studysmart.domain.repositorry.SessionRepository
import com.muhib.studysmart.domain.repositorry.SubjectRepository
import com.muhib.studysmart.domain.repositorry.TaskRepository
import com.muhib.studysmart.util.SnackbarEvent
import com.muhib.studysmart.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = combine(
        _state,
        subjectRepository.getTotalSubjectCount(),
        subjectRepository.getTotalGoalHours(),
        subjectRepository.getAllSubjects(),
        sessionRepository.getTotalSessionsDuration()
    ) { state, subjectCount, goalHours, subjects, totalSessionDuration ->
        state.copy(
            totalSubjectCount = subjectCount,
            totalGoalStudyHours = goalHours,
            subjects = subjects,
            totalStudiedHours = totalSessionDuration.toHours()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )


    val tasks: StateFlow<List<Task>> = taskRepository.getAllUpcomingTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentSessions: StateFlow<List<Session>> = sessionRepository.getRecentFiveSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )



    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()




    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnSubjectNameChange -> {
                _state.update {
                    it.copy(subjectName = event.name)
                }
            }

            is DashboardEvent.OnGoalStudyHoursChange -> {

                _state.update {
                    it.copy(goalStudyHours = event.hours)
                }
            }

            is DashboardEvent.OnSubjectCardColorChange -> {

                _state.update {
                    it.copy(subjectCardColors = event.color)
                }
            }

            is DashboardEvent.OnDeleteSessionButtonClick -> {

                _state.update {
                    it.copy(session = event.session)
                }
            }

            DashboardEvent.SaveSubject -> saveSubject()
            DashboardEvent.DeleteSession -> deleteSession()
            is DashboardEvent.OnTaskIsCompleteChange -> updateTask(event.task)
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

            _snackbarEventFlow.emit(
                SnackbarEvent.ShowSnackbar("Saved in completed tasks.")
            )

        } catch (e: Exception) {
            _snackbarEventFlow.emit(
                SnackbarEvent.ShowSnackbar(
                    "Couldn't update task. ${e.message}",
                    SnackbarDuration.Long
                )
            )
        }
    }

    private fun saveSubject() = viewModelScope.launch {
       try {
           subjectRepository.upsertSubject(
               subject = Subject(
                   name = state.value.subjectName,
                   goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                   colors = state.value.subjectCardColors.map { it.toArgb() }
               )
           )

           _state.update {
               it.copy(
                   subjectName = "",
                   goalStudyHours = "",
                   subjectCardColors = Subject.subjectCardColors.random()
               )
           }

           _snackbarEventFlow.emit(
               SnackbarEvent.ShowSnackbar("Subject saved successfully")
           )

       } catch (e: Exception) {
           _snackbarEventFlow.emit(
               SnackbarEvent.ShowSnackbar(
                   "Couldn't save subject. ${e.message}",
                   SnackbarDuration.Long
               )
           )
       }
    }
}