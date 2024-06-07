package com.muhib.studysmart.presentation.task

import com.muhib.studysmart.domain.model.Subject
import com.muhib.studysmart.util.Priority

sealed class TaskEvent {

    data class OnTitleChange(val title: String): TaskEvent()

    data class OnDescriptionChange(val description: String): TaskEvent()

    data class OnDateChange(val millis: Long?): TaskEvent()

    data class OnPriorityChange(val priority: Priority): TaskEvent()

    data class OnRelatedSubjectChange(val subject: Subject): TaskEvent()

    data object OnIsCompleteChage: TaskEvent()

    data object SaveTask: TaskEvent()

    data object DeleteTask: TaskEvent()
}