package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GoreunsumWordmark
import com.example.ui.components.IconLabel
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.TonalPanel
import com.example.ui.theme.GoreunsumDangerContainer
import com.example.ui.theme.GoreunsumPrimaryContainer

@Composable
fun SafetyOnboardingScreen(
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GoreunsumPrimaryContainer.copy(alpha = 0.5f),
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
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(34.dp))

                    GoreunsumWordmark()

                    Spacer(modifier = Modifier.height(42.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    ) {
                        Text(
                            text = "처음 한 번만 확인해 주세요",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "안전하게 사용하기 위한\n짧은 안내예요",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "고른숨의 웰니스 목적과, 앱보다 즉시 도움을 먼저 요청해야 하는 순간을 알려드릴게요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    TonalPanel {
                        Column {
                            IconLabel(icon = Icons.Default.HealthAndSafety, text = "웰니스 호흡 가이드")
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "고른숨은 만 18세 이상 성인의 명상 전 준비와 일상적 긴장 완화를 위한 웰니스 앱이며 의료기기가 아닙니다. 질환이나 건강 상태를 진단·치료·치유·예방하지 않으며 의료인의 진료나 의학적 조언을 대신하지 않습니다.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TonalPanel(containerColor = GoreunsumDangerContainer.copy(alpha = 0.72f)) {
                        Column {
                            IconLabel(
                                icon = Icons.Default.LocalHospital,
                                text = "위급할 때는 즉시 도움 요청",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "심하거나 갑작스러운 호흡곤란, 흉통, 의식 저하 등 위급한 증상이 있거나 위급하다고 느껴진다면 앱 사용보다 119 또는 의료기관의 도움을 우선하세요.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "회원가입 없이 사용하며 세션 기록은 기기에 저장돼요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                PrimaryActionButton(
                    text = "확인하고 시작",
                    onClick = onAccept,
                    testTag = "onboarding_confirm_button"
                )

                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }
}
