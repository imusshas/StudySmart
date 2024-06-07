package com.muhib.studysmart.presentation.subject

import androidx.compose.ui.graphics.Color
import com.muhib.studysmart.domain.model.Session
import com.muhib.studysmart.domain.model.Subject
import com.muhib.studysmart.domain.model.Task

data class SubjectState(
    val currentSubjectId: Int? = null,
    val subjectName: String = "",
    val goalStudyHours: String = "",
    val subjectCardColors: List<Color> = Subject.subjectCardColors.random(),
    val studiedHours: Float = 0f,
    val progress: Float = 0f,
    val recentSessions: List<Session> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val session: Session? = null,
)
