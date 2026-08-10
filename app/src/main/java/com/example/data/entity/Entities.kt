package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlaylistType {
    M3U,
    XTREAM
}

enum class StreamType {
    LIVE,
    MOVIE,
    SERIES
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PlaylistType,
    val url: String? = null,
    val serverUrl: String? = null,
    val username: String? = null,
    val password: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val channelCount: Int = 0,
    val movieCount: Int = 0,
    val seriesCount: Int = 0,
    val isAutoUpdate: Boolean = true
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val logo: String? = null,
    val groupTitle: String = "عام",
    val streamUrl: String,
    val streamType: StreamType = StreamType.LIVE,
    val tvgId: String? = null,
    val isFavorite: Boolean = false,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val year: String? = null,
    val duration: String? = null,
    val description: String? = null,
    val rating: String? = null,
    val isHidden: Boolean = false,
    val customOrder: Int = 0
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long,
    val channelName: String,
    val logo: String? = null,
    val streamUrl: String,
    val streamType: StreamType,
    val watchedAt: Long = System.currentTimeMillis(),
    val positionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val episodeTitle: String? = null
)

@Entity(tableName = "favorites")
data class FavoriteItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val streamType: StreamType,
    val channelId: Long,
    val playlistId: Long,
    val title: String,
    val logo: String? = null,
    val streamUrl: String,
    val groupTitle: String? = null,
    val folderName: String = "الكل",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "epg_programs")
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val channelTvgId: String,
    val title: String,
    val description: String? = null,
    val startTimeMs: Long,
    val endTimeMs: Long
)
