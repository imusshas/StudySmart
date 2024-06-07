package com.muhib.studysmart.di

import android.app.Application
import androidx.room.Room
import com.muhib.studysmart.data.local.AppDatabase
import com.muhib.studysmart.data.local.SessionDao
import com.muhib.studysmart.data.local.SubjectDao
import com.muhib.studysmart.data.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        application: Application
    ): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "studysmart.db").build()

    @Provides
    @Singleton
    fun provideSubjectDao(
        database: AppDatabase
    ): SubjectDao = database.subjectDao()

    @Provides
    @Singleton
    fun provideTaskDao(
        database: AppDatabase
    ): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideSessionDao(
        database: AppDatabase
    ): SessionDao = database.sessionDao()
}