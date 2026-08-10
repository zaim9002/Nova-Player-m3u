package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PlaylistEntity
import com.example.data.pref.NovaPreferences
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.ImmersiveBgBrush
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    playlists: List<PlaylistEntity>,
    prefs: NovaPreferences,
    onDeletePlaylist: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onClearAllData: () -> Unit
) {
    val scrollState = rememberScrollState()

    var isParentalEnabled by remember { mutableStateOf(prefs.isParentalEnabled) }
    var pinInput by remember { mutableStateOf(prefs.parentalPin) }
    var isAutoPlay by remember { mutableStateOf(prefs.isAutoPlay) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBgBrush)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "الإعدادات",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Playlists Management Section
        SettingsSectionHeader("إدارة قوائم التشغيل", Icons.Default.PlaylistPlay)

        if (playlists.isEmpty()) {
            Text(
                text = "لا توجد قوائم مضافة حاليًا.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            playlists.forEach { pl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, GlassBorderGradient, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = pl.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "القنوات: ${pl.channelCount} | نوع القائمة: ${pl.type.name}", color = TextSecondary, fontSize = 11.sp)
                        }

                        IconButton(onClick = { onDeletePlaylist(pl.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف القائمة", tint = ErrorRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Parental Control Section
        SettingsSectionHeader("الرقابة الأبوية (Parental Control)", Icons.Default.Security)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorderGradient, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "تفعيل الرقابة الأبوية", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isParentalEnabled,
                        onCheckedChange = {
                            isParentalEnabled = it
                            prefs.isParentalEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonIndigo)
                    )
                }

                if (isParentalEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) {
                                pinInput = it
                                prefs.parentalPin = it
                            }
                        },
                        label = { Text("تعيين رمز PIN (4 أرقام)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = GlassCardBg,
                            unfocusedContainerColor = GlassCardBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Player Options
        SettingsSectionHeader("إعدادات المشغل", Icons.Default.PlayCircle)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorderGradient, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "التشغيل التلقائي للبث", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isAutoPlay,
                        onCheckedChange = {
                            isAutoPlay = it
                            prefs.isAutoPlay = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonIndigo)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy & Data
        SettingsSectionHeader("الخصوصية والبيانات", Icons.Default.Delete)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onClearHistory,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("مسح سجل المشاهدة", fontSize = 12.sp)
            }

            Button(
                onClick = onClearAllData,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f), contentColor = ErrorRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("حذف كافة البيانات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // About & Legal Disclaimer
        SettingsSectionHeader("حول التطبيق والإخلاء القانوني", Icons.Default.Info)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorderGradient, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Nova Player v1.0.0", color = NeonIndigo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nova Player هو مشغل وسائط فقط ولا يوفر أو يستضيف أي قنوات أو محتوى. المستخدم مسؤول عن مصادر المحتوى وقوائم التشغيل التي يضيفها وعن امتلاكه الحق القانوني في استخدامها.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
