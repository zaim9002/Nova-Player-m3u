package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChannelEntity
import com.example.data.entity.FavoriteItemEntity
import com.example.data.entity.StreamType
import com.example.ui.components.ChannelCard
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.GlassHeaderBg
import com.example.ui.theme.ImmersiveBgBrush
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FavoritesScreen(
    favoritesList: List<FavoriteItemEntity>,
    onFavoriteClick: (FavoriteItemEntity) -> Unit,
    onRemoveFavorite: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val filteredFavorites = remember(favoritesList, selectedTab) {
        val type = when (selectedTab) {
            0 -> StreamType.LIVE
            1 -> StreamType.MOVIE
            else -> StreamType.SERIES
        }
        favoritesList.filter { it.streamType == type }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBgBrush)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = GlassHeaderBg,
            contentColor = NeonIndigo,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonIndigo
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("قنوات البث", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) NeonIndigo else TextSecondary) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("الأفلام", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) NeonIndigo else TextSecondary) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("المسلسلات", fontWeight = FontWeight.Bold, color = if (selectedTab == 2) NeonIndigo else TextSecondary) }
            )
        }

        if (filteredFavorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد عناصر في قائمة المفضلة لهذه الفئة.",
                    color = TextSecondary,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredFavorites) { item ->
                    val ch = ChannelEntity(
                        id = item.channelId,
                        playlistId = item.playlistId,
                        name = item.title,
                        logo = item.logo,
                        streamUrl = item.streamUrl,
                        streamType = item.streamType,
                        groupTitle = item.groupTitle ?: "المفضلة",
                        isFavorite = true
                    )

                    ChannelCard(
                        channel = ch,
                        onClick = { onFavoriteClick(item) },
                        onFavoriteToggle = { onRemoveFavorite(item.channelId) }
                    )
                }
            }
        }
    }
}
