package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.GlassHeaderBg
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PrimaryNeonGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopBarHeader(
    title: String = "Nova Player",
    onSearchClick: () -> Unit,
    onAddPlaylistClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassHeaderBg)
            .border(
                width = 1.dp,
                brush = GlassBorderGradient,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Logo with glowing accent frame
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryNeonGradient)
                    .padding(1.5.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nova_logo_1786326586296),
                    contentDescription = "Nova Player",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )

            // Live status dot or actions
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .tvFocusGlow(shapeRadius = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = TextPrimary)
            }

            IconButton(
                onClick = onAddPlaylistClick,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .tvFocusGlow(shapeRadius = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(NeonIndigo.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة قائمة", tint = NeonIndigo)
                }
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.tvFocusGlow(shapeRadius = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "الإعدادات", tint = TextSecondary)
            }
        }
    }
}
