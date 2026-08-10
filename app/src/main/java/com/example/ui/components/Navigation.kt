package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.GlassHeaderBg
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PrimaryNeonGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "الرئيسية", Icons.Default.Home),
    LIVE("live", "مباشر", Icons.Default.LiveTv),
    MOVIES("movies", "أفلام", Icons.Default.Movie),
    SERIES("series", "مسلسلات", Icons.Default.Tv),
    FAVORITES("favorites", "المفضلة", Icons.Default.Star),
    HISTORY("history", "السجل", Icons.Default.History),
    EPG("epg", "دليل EPG", Icons.Default.CalendarMonth)
}

@Composable
fun NovaBottomNavigation(
    currentRoute: String,
    onNavigate: (NavDestination) -> Unit
) {
    NavigationBar(
        containerColor = GlassHeaderBg,
        tonalElevation = 12.dp,
        modifier = Modifier.border(
            width = 1.dp,
            brush = GlassBorderGradient,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        )
    ) {
        NavDestination.values().take(5).forEach { dest ->
            val isSelected = currentRoute == dest.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.title,
                        tint = if (isSelected) NeonIndigo else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = dest.title,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = NeonIndigo.copy(alpha = 0.25f)
                )
            )
        }
    }
}

@Composable
fun NovaTvSidebar(
    currentRoute: String,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(GlassHeaderBg)
            .border(
                width = 1.dp,
                brush = GlassBorderGradient,
                shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
            )
            .padding(vertical = 24.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(PrimaryNeonGradient)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nova Player",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        NavDestination.values().forEach { dest ->
            val isSelected = currentRoute == dest.route
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) PrimaryNeonGradient else androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color.Transparent, Color.Transparent)
                        )
                    )
                    .tvFocusGlow(shapeRadius = 14.dp)
                    .clickable { onNavigate(dest) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = dest.icon,
                    contentDescription = dest.title,
                    tint = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = dest.title,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
