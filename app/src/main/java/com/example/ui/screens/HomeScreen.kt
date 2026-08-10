package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChannelEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.WatchHistoryEntity
import com.example.ui.components.ChannelCard
import com.example.ui.components.WelcomeCard
import com.example.ui.components.tvFocusGlow
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.ImmersiveBgBrush
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow

data class CategoryItem(
    val title: String,
    val icon: ImageVector,
    val colorStart: Color,
    val colorEnd: Color,
    val route: String
)

@Composable
fun HomeScreen(
    playlists: List<PlaylistEntity>,
    historyList: List<WatchHistoryEntity>,
    favoriteList: List<ChannelEntity>,
    recentChannels: List<ChannelEntity>,
    onNavigateRoute: (String) -> Unit,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteToggle: (ChannelEntity) -> Unit,
    onAddM3uUrl: () -> Unit,
    onAddXtream: () -> Unit,
    onSelectM3uFile: () -> Unit,
    onImportM3uUrl: ((name: String, url: String) -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    val categories = listOf(
        CategoryItem("📺 البث المباشر", Icons.Default.LiveTv, NeonIndigo, NeonBlue, "live"),
        CategoryItem("🎬 الأفلام", Icons.Default.Movie, NeonViolet, NeonPink, "movies"),
        CategoryItem("📺 المسلسلات", Icons.Default.Tv, NeonPink, Color(0xFFF43F5E), "series"),
        CategoryItem("⭐ المفضلة", Icons.Default.Star, WarningYellow, Color(0xFFD97706), "favorites"),
        CategoryItem("🕘 سجل المشاهدة", Icons.Default.History, NeonCyan, Color(0xFF0D9488), "history"),
        CategoryItem("📅 دليل البرامج EPG", Icons.Default.CalendarMonth, NeonIndigo, Color(0xFF4F46E5), "epg")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBgBrush)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        if (playlists.isEmpty()) {
            WelcomeCard(
                onAddM3uUrl = onAddM3uUrl,
                onAddXtream = onAddXtream,
                onSelectM3uFile = onSelectM3uFile,
                onAddDemoArabic = if (onImportM3uUrl != null) {
                    { onImportM3uUrl("قنوات عربية (تجريبي)", "https://iptv-org.github.io/iptv/languages/ara.m3u") }
                } else null,
                onAddDemoGlobal = if (onImportM3uUrl != null) {
                    { onImportM3uUrl("قنوات عالمية (تجريبي)", "https://iptv-org.github.io/iptv/index.m3u") }
                } else null
            )
        } else {
            // Categories Grid Section
            Text(
                text = "الأقسام الرئيسية",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.take(3).forEach { cat ->
                    CategoryCard(
                        category = cat,
                        onClick = { onNavigateRoute(cat.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.drop(3).forEach { cat ->
                    CategoryCard(
                        category = cat,
                        onClick = { onNavigateRoute(cat.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Continue Watching Row
            if (historyList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                SectionHeader("متابعة المشاهدة", onSeeAll = { onNavigateRoute("history") })

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList) { item ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .border(1.dp, GlassBorderGradient, RoundedCornerShape(16.dp))
                                .tvFocusGlow(shapeRadius = 16.dp)
                                .clickable {
                                    onChannelClick(
                                        ChannelEntity(
                                            id = item.channelId,
                                            playlistId = 0,
                                            name = item.channelName,
                                            logo = item.logo,
                                            streamUrl = item.streamUrl,
                                            streamType = item.streamType
                                        )
                                    )
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassCardBg)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = item.channelName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (item.positionMs > 0) "تابعت لمدة ${(item.positionMs / 60000)} دقيقة" else "شوهد مؤخرًا",
                                    color = NeonIndigo,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Favorites Row
            if (favoriteList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                SectionHeader("القنوات المفضلة", onSeeAll = { onNavigateRoute("favorites") })

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteList) { ch ->
                        ChannelCard(
                            channel = ch,
                            onClick = { onChannelClick(ch) },
                            onFavoriteToggle = { onFavoriteToggle(ch) },
                            modifier = Modifier.width(260.dp)
                        )
                    }
                }
            }

            // Recently Added Channels
            if (recentChannels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                SectionHeader("القنوات المضافة حديثًا", onSeeAll = { onNavigateRoute("live") })

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recentChannels.take(6).forEach { ch ->
                        ChannelCard(
                            channel = ch,
                            onClick = { onChannelClick(ch) },
                            onFavoriteToggle = { onFavoriteToggle(ch) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .border(1.dp, GlassBorderGradient, RoundedCornerShape(16.dp))
            .tvFocusGlow(shapeRadius = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassCardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            category.colorStart.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(category.colorStart.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = category.colorStart,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category.title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSeeAll() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "عرض الكل",
                color = NeonIndigo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = NeonIndigo,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
