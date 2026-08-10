package com.example.data.repository

import android.content.Context
import com.example.data.db.NovaDatabase
import com.example.data.entity.ChannelEntity
import com.example.data.entity.EpgProgramEntity
import com.example.data.entity.FavoriteItemEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.PlaylistType
import com.example.data.entity.StreamType
import com.example.data.entity.WatchHistoryEntity
import com.example.data.parser.M3uParser
import com.example.data.parser.XtreamParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit

class IPTVRepository(private val db: NovaDatabase) {

    private val playlistDao = db.playlistDao()
    private val channelDao = db.channelDao()
    private val historyDao = db.watchHistoryDao()
    private val favoriteDao = db.favoriteDao()
    private val epgDao = db.epgDao()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val watchHistory: Flow<List<WatchHistoryEntity>> = historyDao.getWatchHistory()
    val favoriteChannels: Flow<List<ChannelEntity>> = channelDao.getFavoriteChannels()
    val allFavorites: Flow<List<FavoriteItemEntity>> = favoriteDao.getAllFavorites()

    fun getChannelsByStreamType(playlistId: Long, streamType: StreamType): Flow<List<ChannelEntity>> {
        return if (playlistId > 0) {
            channelDao.getChannelsByStreamType(playlistId, streamType)
        } else {
            channelDao.getAllChannelsByStreamType(streamType)
        }
    }

    fun getGroupsByStreamType(streamType: StreamType): Flow<List<String>> {
        return channelDao.getGroupsByStreamType(streamType)
    }

    fun getFavoritesByType(streamType: StreamType): Flow<List<FavoriteItemEntity>> {
        return favoriteDao.getFavoritesByType(streamType)
    }

    fun searchChannels(query: String): Flow<List<ChannelEntity>> {
        return channelDao.searchChannels(query)
    }

    suspend fun importM3uUrl(name: String, url: String): Long = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("فشل الاتصال بالرابط (${response.code})")

        val inputStream = response.body?.byteStream() ?: throw Exception("استجابة فارغة")

        val playlistEntity = PlaylistEntity(
            name = name,
            type = PlaylistType.M3U,
            url = url
        )

        val playlistId = playlistDao.insertPlaylist(playlistEntity)
        val channels = M3uParser.parse(inputStream, playlistId)

        saveChannelsBatch(playlistId, channels)
        playlistId
    }

    suspend fun importM3uFile(name: String, inputStream: InputStream): Long = withContext(Dispatchers.IO) {
        val playlistEntity = PlaylistEntity(
            name = name,
            type = PlaylistType.M3U,
            url = "file_local"
        )

        val playlistId = playlistDao.insertPlaylist(playlistEntity)
        val channels = M3uParser.parse(inputStream, playlistId)

        saveChannelsBatch(playlistId, channels)
        playlistId
    }

    suspend fun importXtreamCodes(
        name: String,
        serverUrl: String,
        username: String,
        password: String
    ): Long = withContext(Dispatchers.IO) {
        val playlistEntity = PlaylistEntity(
            name = name,
            type = PlaylistType.XTREAM,
            serverUrl = serverUrl,
            username = username,
            password = password
        )

        val playlistId = playlistDao.insertPlaylist(playlistEntity)
        val parser = XtreamParser(serverUrl, username, password)
        val channels = parser.authenticateAndFetchChannels(playlistId)

        saveChannelsBatch(playlistId, channels)
        playlistId
    }

    private suspend fun saveChannelsBatch(playlistId: Long, channels: List<ChannelEntity>) {
        val liveCount = channels.count { it.streamType == StreamType.LIVE }
        val movieCount = channels.count { it.streamType == StreamType.MOVIE }
        val seriesCount = channels.count { it.streamType == StreamType.SERIES }

        // Batch insert in chunks of 500
        channels.chunked(500).forEach { chunk ->
            channelDao.insertChannels(chunk)
        }

        val updatedPlaylist = playlistDao.getPlaylistById(playlistId)?.copy(
            channelCount = liveCount,
            movieCount = movieCount,
            seriesCount = seriesCount,
            lastUpdated = System.currentTimeMillis()
        )
        if (updatedPlaylist != null) {
            playlistDao.updatePlaylist(updatedPlaylist)
        }
    }

    suspend fun toggleFavorite(channel: ChannelEntity) = withContext(Dispatchers.IO) {
        val newFavState = !channel.isFavorite
        channelDao.updateFavoriteState(channel.id, newFavState)

        if (newFavState) {
            favoriteDao.insertFavorite(
                FavoriteItemEntity(
                    streamType = channel.streamType,
                    channelId = channel.id,
                    playlistId = channel.playlistId,
                    title = channel.name,
                    logo = channel.logo,
                    streamUrl = channel.streamUrl,
                    groupTitle = channel.groupTitle
                )
            )
        } else {
            favoriteDao.deleteFavoriteByChannelId(channel.id)
        }
    }

    suspend fun recordWatchHistory(channel: ChannelEntity, positionMs: Long = 0, totalDurationMs: Long = 0) = withContext(Dispatchers.IO) {
        historyDao.insertOrUpdateHistory(
            WatchHistoryEntity(
                channelId = channel.id,
                channelName = channel.name,
                logo = channel.logo,
                streamUrl = channel.streamUrl,
                streamType = channel.streamType,
                positionMs = positionMs,
                totalDurationMs = totalDurationMs,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelsByPlaylist(playlistId)
        epgDao.deleteEpgByPlaylist(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        channelDao.clearAll()
        playlistDao.clearAll()
        historyDao.clearHistory()
        favoriteDao.clearFavorites()
    }

    fun getEpgPrograms(tvgId: String): Flow<List<EpgProgramEntity>> {
        return epgDao.getProgramsForChannel(tvgId)
    }

    suspend fun getCurrentEpgProgram(tvgId: String): EpgProgramEntity? = withContext(Dispatchers.IO) {
        epgDao.getCurrentProgram(tvgId)
    }
}
