package com.muhib.studysmart.domain.repositorry

import com.muhib.studysmart.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {

    suspend fun upsertSubject(subject: Subject)

    fun getTotalSubjectCount(): Flow<Int>

    fun getTotalGoalHours(): Flow<Float>

    suspend fun getSubjectById(subjectId: Int): Subject?

    suspend fun deleteSubject(subjectId: Int)

    fun getAllSubjects(): Flow<List<Subject>>
}