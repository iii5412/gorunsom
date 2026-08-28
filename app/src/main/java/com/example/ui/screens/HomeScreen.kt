package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.BreathMark
import com.example.ui.components.GoreunsumWordmark
import com.example.ui.components.PrimaryActionButton
import com.example.ui.theme.GoreunsumInhale
import com.example.ui.theme.GoreunsumInhaleContainer
import com.example.ui.theme.GoreunsumExhale
import com.example.ui.theme.GoreunsumExhaleContainer
import com.example.ui.theme.GoreunsumPrimaryContainer

@Composable
fun HomeScreen(
    onStartBreathing: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GoreunsumPrimaryContainer.copy(alpha = 0.42f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            GoreunsumExhaleContainer.copy(alpha = 0.26f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoreunsumWordmark(compact = true)
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "설정",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(54.dp))

                    BreathMark(
                        size = 196.dp,
                        color = GoreunsumInhale,
                        secondaryColor = GoreunsumExhale,
                        contentDescription = "고른숨 호흡 안내",
                        modifier = Modifier.testTag("home_breath_mark")
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "지금, 숨을 고르게",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "크게 들이마시려고 애쓰지 않아도 괜찮아요.\n2분 동안 편안한 리듬을 함께 따라가요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PhaseColorKey(
                            title = "들이쉬기",
                            duration = "2초",
                            icon = Icons.Default.ArrowUpward,
                            color = GoreunsumInhale,
                            containerColor = GoreunsumInhaleContainer,
                            modifier = Modifier.weight(1f)
                        )
                        PhaseColorKey(
                            title = "내쉬기",
                            duration = "4초",
                            icon = Icons.Default.ArrowDownward,
                            color = GoreunsumExhale,
                            containerColor = GoreunsumExhaleContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(42.dp))

                    PrimaryActionButton(
                        text = "호흡 시작",
                        onClick = onStartBreathing,
                        icon = Icons.Default.PlayArrow,
                        testTag = "start_breathing_button"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TextButton(
                        onClick = onOpenHelp,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("help_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.size(7.dp))
                        Text(text = "지금 도움이 더 필요하신가요?", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun PhaseColorKey(
    title: String,
    duration: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(7.dp))
            Text(
                text = "$title  $duration",
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
