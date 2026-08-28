package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.engine.EngineSnapshot
import com.example.domain.model.BreathingConstants
import com.example.domain.model.BreathingPhase
import com.example.ui.theme.GoreunsumExhale
import com.example.ui.theme.GoreunsumExhaleContainer
import com.example.ui.theme.GoreunsumInhale
import com.example.ui.theme.GoreunsumInhaleContainer

@Composable
fun BreathingSessionScreen(
    snapshot: EngineSnapshot?,
    onStop: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onStop() }

    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose { currentView.keepScreenOn = false }
    }

    var showMenu by remember { mutableStateOf(false) }
    val phase = snapshot?.phase ?: BreathingPhase.INHALE
    val remainingFormatted = snapshot?.remainingTimeFormatted ?: "02:00"
    val sessionProgress = ((snapshot?.elapsedActiveMs ?: 0L).toFloat() /
        BreathingConstants.SESSION_DURATION_MS.toFloat()).coerceIn(0f, 1f)
    val expansion = (snapshot?.circleExpansion ?: 0f).coerceIn(0f, 1f)

    val targetPhaseColor = if (phase == BreathingPhase.INHALE) GoreunsumInhale else GoreunsumExhale
    val phaseContainerColor = if (phase == BreathingPhase.INHALE) GoreunsumInhaleContainer else GoreunsumExhaleContainer
    val phaseColor by animateColorAsState(
        targetValue = targetPhaseColor,
        animationSpec = tween(durationMillis = 350),
        label = "phaseColor"
    )
    val phaseTitle = if (phase == BreathingPhase.INHALE) "들이쉬기" else "내쉬기"
    val phaseHelper = if (phase == BreathingPhase.INHALE) "가볍게, 편안하게" else "천천히, 조금 더 길게"
    val phaseScale = 0.70f + (0.30f * expansion)

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            phaseContainerColor.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = phaseContainerColor.copy(alpha = 0.94f),
                        modifier = Modifier.testTag("remaining_time_text")
                    ) {
                        Text(
                            text = remainingFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = phaseColor,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .semantics { contentDescription = "남은 시간 $remainingFormatted" }
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("session_more_menu_button")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "더보기 메뉴")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("도움이 필요해요") },
                                onClick = {
                                    showMenu = false
                                    onOpenHelp()
                                },
                                modifier = Modifier.testTag("session_menu_help_item")
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { sessionProgress },
                    color = phaseColor,
                    trackColor = phaseContainerColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .clearAndSetSemantics { }
                )

                Spacer(modifier = Modifier.weight(0.72f))

                Box(
                    modifier = Modifier
                        .size(308.dp)
                        .clearAndSetSemantics { },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(292.dp)
                            .border(2.dp, phaseColor.copy(alpha = 0.34f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(278.dp)
                            .graphicsLayer {
                                scaleX = phaseScale
                                scaleY = phaseScale
                            }
                            .clip(CircleShape)
                            .background(phaseContainerColor.copy(alpha = 0.96f))
                    )
                    Box(
                        modifier = Modifier
                            .size(226.dp)
                            .graphicsLayer {
                                scaleX = phaseScale
                                scaleY = phaseScale
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        phaseColor.copy(alpha = 0.72f),
                                        phaseColor
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (phase == BreathingPhase.INHALE) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "현재 단계 $phaseTitle. $phaseHelper"
                    }
                ) {
                    Text(
                        text = phaseTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = phaseColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = phaseHelper,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "숨을 억지로 크게 쉬지 않아도 괜찮아요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onStop,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = phaseColor),
                    border = BorderStroke(1.dp, phaseColor.copy(alpha = 0.58f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("stop_session_button")
                ) {
                    Text(
                        text = "잠시 멈추기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = phaseColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
