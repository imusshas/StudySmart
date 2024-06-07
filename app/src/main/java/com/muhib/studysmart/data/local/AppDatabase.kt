package com.muhib.studysmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.muhib.studysmart.domain.model.Session
import com.muhib.studysmart.domain.model.Subject
import com.muhib.studysmart.domain.model.Task

@Database(
    entities = [Subject::class, Session::class, Task::class],
     version = 1
)
@TypeConverters(ColorListConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun subjectDao(): SubjectDao

    abstract fun sessionDao(): SessionDao

    abstract fun taskDao(): TaskDao
}