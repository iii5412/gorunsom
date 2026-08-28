package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GoreunsumTopBar
import com.example.ui.components.IconLabel
import com.example.ui.components.TonalPanel
import com.example.ui.theme.GoreunsumDanger
import com.example.ui.theme.GoreunsumDangerContainer

@Composable
fun SafetyHelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .padding(horizontal = 24.dp)
        ) {
            GoreunsumTopBar(
                title = "안전 도움",
                onBack = onBack,
                backTestTag = "help_top_back_button"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "앱보다 도움이\n먼저인 순간이 있어요",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "아래 상황에 해당하거나 스스로 위급하다고 느껴진다면 호흡 가이드를 계속하기보다 즉시 도움을 요청하세요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                TonalPanel(containerColor = GoreunsumDangerContainer.copy(alpha = 0.72f)) {
                    Column {
                        IconLabel(
                            icon = Icons.Default.LocalHospital,
                            text = "즉시 도움을 요청할 상황",
                            tint = GoreunsumDanger
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val symptoms = listOf(
                            "심하거나 갑작스러운 흉통",
                            "의식이 흐려지거나 쓰러짐",
                            "입술이나 피부의 뚜렷한 색 변화",
                            "말하기 힘들 정도의 호흡곤란",
                            "심각한 천식 또는 알레르기 반응이 의심됨",
                            "증상이 빠르게 악화되거나 계속됨"
                        )
                        symptoms.forEach { symptom ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = GoreunsumDanger,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Spacer(modifier = Modifier.width(6.dp).height(6.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = symptom,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "고른숨은 증상의 원인을 판단하거나 응급 상황을 자동으로 감지하지 않습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { open119Dialer(context) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoreunsumDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("dial_119_button")
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("119 전화 화면 열기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("help_back_button")
            ) {
                Text("이전 화면으로", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

private fun open119Dialer(context: Context) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:119") }
        )
    } catch (_: Exception) {
        Toast.makeText(context, "전화 앱을 열 수 없습니다. 직접 119에 연락해 주세요.", Toast.LENGTH_LONG).show()
    }
}
