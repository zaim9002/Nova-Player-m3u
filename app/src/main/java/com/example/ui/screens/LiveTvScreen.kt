package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChannelEntity
import com.example.ui.components.ChannelCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.ImmersiveBgBrush
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveTvScreen(
    channels: List<ChannelEntity>,
    groups: List<String>,
    onChannelClick: (ChannelEntity) -> Unit,
    onFavoriteToggle: (ChannelEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("الكل") }
    var isGridView by remember { mutableStateOf(true) }

    val filteredChannels = remember(channels, searchQuery, selectedGroup) {
        channels.filter { channel ->
            val matchesGroup = selectedGroup == "الكل" || channel.groupTitle == selectedGroup
            val matchesSearch = searchQuery.isEmpty() ||
                    channel.name.contains(searchQuery, ignoreCase = true) ||
                    channel.groupTitle.contains(searchQuery, ignoreCase = true)
            matchesGroup && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBgBrush)
    ) {
        // Search & View Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن قناة...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonIndigo) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = GlassCardBg,
                    unfocusedContainerColor = GlassCardBg
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlassCardBg)
                    .border(1.dp, GlassBorderGradient, RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "تغيير العرض",
                    tint = NeonIndigo
                )
            }
        }

        // Category Groups Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedGroup == "الكل",
                    onClick = { selectedGroup = "الكل" },
                    label = { Text("الكل (${channels.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonIndigo,
                        selectedLabelColor = Color.White,
                        containerColor = GlassCardBg,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            items(groups) { group ->
                val count = remember(channels, group) { channels.count { it.groupTitle == group } }
                FilterChip(
                    selected = selectedGroup == group,
                    onClick = { selectedGroup = group },
                    label = { Text("$group ($count)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonIndigo,
                        selectedLabelColor = Color.White,
                        containerColor = GlassCardBg,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد قنوات مطابقة للبحث.",
                    color = TextSecondary,
                    fontSize = 15.sp
                )
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChannels) { ch ->
                        ChannelCard(
                            channel = ch,
                            onClick = { onChannelClick(ch) },
                            onFavoriteToggle = { onFavoriteToggle(ch) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChannels) { ch ->
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
