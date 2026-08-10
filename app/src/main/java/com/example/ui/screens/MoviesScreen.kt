package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChannelEntity
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.ImmersiveBgBrush
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MoviesScreen(
    movies: List<ChannelEntity>,
    groups: List<String>,
    onMovieClick: (ChannelEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("الكل") }

    val filteredMovies = remember(movies, searchQuery, selectedGroup) {
        movies.filter { m ->
            val matchesGroup = selectedGroup == "الكل" || m.groupTitle == selectedGroup
            val matchesSearch = searchQuery.isEmpty() || m.name.contains(searchQuery, ignoreCase = true)
            matchesGroup && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBgBrush)
    ) {
        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن فيلم...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonViolet) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonViolet,
                unfocusedBorderColor = DarkCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = GlassCardBg,
                unfocusedContainerColor = GlassCardBg
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        // Groups
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedGroup == "الكل",
                    onClick = { selectedGroup = "الكل" },
                    label = { Text("الكل (${movies.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet,
                        selectedLabelColor = Color.White,
                        containerColor = GlassCardBg,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            items(groups) { grp ->
                FilterChip(
                    selected = selectedGroup == grp,
                    onClick = { selectedGroup = grp },
                    label = { Text(grp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet,
                        selectedLabelColor = Color.White,
                        containerColor = GlassCardBg,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (filteredMovies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "لا تتوفر أفلام في هذه الفئة.", color = TextSecondary, fontSize = 15.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredMovies) { m ->
                    MediaPosterCard(
                        channel = m,
                        onClick = { onMovieClick(m) }
                    )
                }
            }
        }
    }
}
