package com.example.ui.screens

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.EngineSnapshot
import com.example.domain.model.BreathingPhase
import com.example.ui.theme.GoreunsumExhale
import com.example.ui.theme.GoreunsumInhale

@Composable
fun BreathingSessionScreen(
    snapshot: EngineSnapshot?,
    onStop: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware back button to trigger Stop Confirmation (FR-026)
    BackHandler {
        onStop()
    }

    // Keep screen on during active session (FR-025)
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    var showMenu by remember { mutableStateOf(false) }

    val phase = snapshot?.phase ?: BreathingPhase.INHALE
    val remainingFormatted = snapshot?.remainingTimeFormatted ?: "02:00"

    val targetPhaseColor = if (phase == BreathingPhase.INHALE) GoreunsumInhale else GoreunsumExhale
    val animatedPhaseColor by animateColorAsState(
        targetValue = targetPhaseColor,
        animationSpec = tween(durationMillis = 400),
        label = "phaseColor"
    )

    val phaseTitle = if (phase == BreathingPhase.INHALE) "들이쉬기" else "내쉬기"
    val phaseHelper = if (phase == BreathingPhase.INHALE) "가볍게, 편안하게" else "천천히 길게"

    // Continuous circle expansion: 0f (min size 120dp) -> 1f (max size 240dp)
    val expansion = snapshot?.circleExpansion ?: 0f
    val circleSizeDp = (130f + (110f * expansion)).dp

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Top Bar: Remaining Time and More Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "남은 시간 $remainingFormatted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .testTag("remaining_time_text")
                            .semantics {
                                contentDescription = "남은 시간 $remainingFormatted"
                            }
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("session_more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기 메뉴"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
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

                Spacer(modifier = Modifier.weight(0.7f))

                // Fixed container for the breathing animation (prevents pushing other UI elements)
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clearAndSetSemantics { /* Decorative circle animation is excluded from TalkBack per PRD */ },
                    contentAlignment = Alignment.Center
                ) {
                    // Outer subtle halo
                    Box(
                        modifier = Modifier
                            .size(circleSizeDp + 32.dp)
                            .clip(CircleShape)
                            .background(animatedPhaseColor.copy(alpha = 0.12f))
                    )

                    // Core expanding/contracting breathing circle
                    Box(
                        modifier = Modifier
                            .size(circleSizeDp)
                            .clip(CircleShape)
                            .background(animatedPhaseColor.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Direction Icon
                        Icon(
                            imageVector = if (phase == BreathingPhase.INHALE) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Phase Title and Instructions (Semantic for TalkBack)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "현재 단계: $phaseTitle. $phaseHelper"
                    }
                ) {
                    Text(
                        text = phaseTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = animatedPhaseColor
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

                // Stop Button
                OutlinedButton(
                    onClick = onStop,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("stop_session_button")
                ) {
                    Text(
                        text = "중지",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
