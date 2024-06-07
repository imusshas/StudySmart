package com.muhib.studysmart.di

import com.muhib.studysmart.data.repository.SessionRepositoryImpl
import com.muhib.studysmart.data.repository.SubjectRepositoryImpl
import com.muhib.studysmart.data.repository.TaskRepositoryImpl
import com.muhib.studysmart.domain.repositorry.SessionRepository
import com.muhib.studysmart.domain.repositorry.SubjectRepository
import com.muhib.studysmart.domain.repositorry.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSubjectRepository(
        impl: SubjectRepositoryImpl
    ): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository
}