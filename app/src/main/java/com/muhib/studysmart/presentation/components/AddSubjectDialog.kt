package com.muhib.studysmart.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.muhib.studysmart.domain.model.Subject

@Composable
fun AddSubjectDialog(
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: () -> Unit,
    title: String = "Add/Update Subject",
    selectedColors: List<Color>,
    subjectName: String,
    goalHours: String,
    onColorChange: (List<Color>) -> Unit,
    onSubjectNameChange: (String) -> Unit,
    onGoalHoursChange: (String) -> Unit,
    isOpen: Boolean
) {

    val subjectNameError = rememberSaveable {
        mutableStateOf<String?>(null)
    }
    val goalHoursError = rememberSaveable {
        mutableStateOf<String?>(null)
    }

    subjectNameError.value = when {
        subjectName.isBlank() -> "Please enter subject name."
        subjectName.length < 3 -> "Subject name is too short."
        subjectName.length > 20 -> "Subject name is too long."
        else -> null
    }

    goalHoursError.value = when {
        goalHours.isBlank() -> "Please enter goal hours."
        goalHours.toFloatOrNull() == null -> "Invalid goal hours."
        goalHours.toFloat() < 1f -> "Please set at least one hour."
        goalHours.toFloat() > 1000f -> "Please set maximum of 1000 hours."
        else -> null
    }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = onConfirmButtonClicked,
                    enabled = subjectNameError.value == null && goalHoursError.value == null
                ) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = title) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Subject.subjectCardColors.forEach { colors ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.dp,
                                        if (colors == selectedColors) Color.Black else Color.Transparent,
                                        CircleShape
                                    )
                                    .background(brush = Brush.verticalGradient(colors = colors))
                                    .clickable { onColorChange(colors) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = onSubjectNameChange,
                        label = { Text(text = "Subject Name") },
                        singleLine = true,
                        isError = subjectNameError.value != null && subjectName.isNotBlank(),
                        supportingText = { Text(text = subjectNameError.value.orEmpty()) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = goalHours,
                        onValueChange = onGoalHoursChange,
                        label = { Text(text = "Goal Study Hours") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        isError = goalHoursError.value != null && goalHours.isNotBlank(),
                        supportingText = { Text(text = goalHoursError.value.orEmpty()) }
                    )
                }
            }
        )
    }
}