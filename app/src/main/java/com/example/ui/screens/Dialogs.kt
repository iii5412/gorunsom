package com.example.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoreunsumDanger
import com.example.ui.theme.GoreunsumPrimary

@Composable
fun StopConfirmDialog(
    onResume: () -> Unit,
    onConfirmStop: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "세션을 끝낼까요?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "지금까지의 시간은 중단된 세션으로 기록됩니다.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = GoreunsumPrimary),
                modifier = Modifier.testTag("dialog_resume_button")
            ) {
                Text("계속하기")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onConfirmStop,
                modifier = Modifier.testTag("dialog_stop_confirm_button")
            ) {
                Text(
                    text = "세션 끝내기",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun BackgroundResumeDialog(
    onResume: () -> Unit,
    onEndSession: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "세션을 다시 시작할까요?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "잠시 자리를 비우셨어요. 이어서 진행하거나 끝낼 수 있습니다.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = GoreunsumPrimary),
                modifier = Modifier.testTag("bg_dialog_resume_button")
            ) {
                Text("이어서 하기")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onEndSession,
                modifier = Modifier.testTag("bg_dialog_end_button")
            ) {
                Text(
                    text = "세션 끝내기",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "기록 삭제",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "저장된 모든 세션 기록을 삭제할까요? 삭제한 기록은 복구할 수 없습니다.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = GoreunsumDanger),
                modifier = Modifier.testTag("dialog_confirm_delete_button")
            ) {
                Text("모두 삭제")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_delete_button")
            ) {
                Text(
                    text = "취소",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
