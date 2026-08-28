package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.BreathingConstants
import com.example.ui.components.GoreunsumTopBar
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.TonalPanel
import com.example.ui.theme.GoreunsumOutlineSoft
import com.example.ui.theme.GoreunsumPrimary
import com.example.ui.theme.GoreunsumPrimaryContainer
import com.example.ui.theme.GoreunsumSurfaceVariant

@Composable
fun ComfortInputScreen(
    title: String,
    actionButtonText: String,
    selectedScore: Int?,
    onScoreSelected: (Int) -> Unit,
    onSubmit: (Int?) -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoreunsumTopBar(
                    title = "편안함 기록",
                    onBack = onBack,
                    backTestTag = "comfort_back_button"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "내 느낌을 위한 선택 기록이에요.\n건너뛰어도 괜찮아요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(34.dp))

                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .background(if (selectedScore != null) GoreunsumPrimaryContainer else GoreunsumSurfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (selectedScore != null) GoreunsumPrimary.copy(alpha = 0.28f) else GoreunsumOutlineSoft,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedScore?.toString() ?: "—",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedScore != null) GoreunsumPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (selectedScore == null) "미선택" else "10점 중",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                TonalPanel {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedScore == null) "손잡이를 움직여 선택해 주세요" else comfortLabel(selectedScore),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Slider(
                            value = (selectedScore ?: 5).toFloat(),
                            onValueChange = { value ->
                                onScoreSelected(
                                    value.toInt().coerceIn(
                                        BreathingConstants.COMFORT_MIN,
                                        BreathingConstants.COMFORT_MAX
                                    )
                                )
                            },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = if (selectedScore != null) GoreunsumPrimary else MaterialTheme.colorScheme.outline,
                                activeTrackColor = if (selectedScore != null) GoreunsumPrimary else MaterialTheme.colorScheme.outline,
                                inactiveTrackColor = GoreunsumSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("comfort_slider")
                                .semantics {
                                    contentDescription = "편안함 점수. 0은 전혀 편안하지 않음, 10은 매우 편안함. 현재 ${selectedScore ?: "미선택"}"
                                }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ScaleLabel(number = "0", meaning = "전혀 편안하지 않음", alignment = Alignment.Start)
                            ScaleLabel(number = "5", meaning = "보통", alignment = Alignment.CenterHorizontally)
                            ScaleLabel(number = "10", meaning = "매우 편안함", alignment = Alignment.End)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                PrimaryActionButton(
                    text = actionButtonText,
                    onClick = { onSubmit(selectedScore) },
                    enabled = selectedScore != null,
                    testTag = "comfort_submit_button"
                )

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("comfort_skip_button")
                ) {
                    Text(
                        text = "기록하지 않고 건너뛰기",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ScaleLabel(number: String, meaning: String, alignment: Alignment.Horizontal) {
    Column(horizontalAlignment = alignment, modifier = Modifier.widthIn(max = 104.dp)) {
        Text(number, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            meaning,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = when (alignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}

private fun comfortLabel(score: Int): String = when (score) {
    0, 1, 2 -> "지금은 편안함이 적게 느껴져요"
    3, 4 -> "조금 덜 편안하게 느껴져요"
    5 -> "보통으로 느껴져요"
    6, 7 -> "조금 편안하게 느껴져요"
    else -> "편안하게 느껴져요"
}
