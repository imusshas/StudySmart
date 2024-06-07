package com.muhib.studysmart.presentation.session

import com.muhib.studysmart.domain.model.Session
import com.muhib.studysmart.domain.model.Subject

data class SessionState(
    val subjects: List<Subject> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val relatedToSubject: String? = null,
    val subjectId: Int? = null,
    val session: Session? = null
)
