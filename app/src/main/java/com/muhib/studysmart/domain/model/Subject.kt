package com.muhib.studysmart.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.muhib.studysmart.presentation.theme.gradient1
import com.muhib.studysmart.presentation.theme.gradient2
import com.muhib.studysmart.presentation.theme.gradient3
import com.muhib.studysmart.presentation.theme.gradient4
import com.muhib.studysmart.presentation.theme.gradient5

@Entity
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val subjectId: Int? = null,
    val name: String,
    val goalHours: Float,
    val colors: List<Int>
) {
    companion object {
        val subjectCardColors = listOf(gradient1, gradient2, gradient3, gradient4,gradient5,)
    }
}
