package com.muhib.studysmart.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: () -> Unit,
    title: String,
    bodyText: String,
    isOpen: Boolean
) {


    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = onConfirmButtonClicked,
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = title) },
            text = {
                Text(text = bodyText)
            }
        )
    }
}