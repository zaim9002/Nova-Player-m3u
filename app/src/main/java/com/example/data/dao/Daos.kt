package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ChannelEntity
import com.example.data.entity.EpgProgramEntity
import com.example.data.entity.FavoriteItemEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.StreamType
import com.example.data.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY id DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)

    @Query("DELETE FROM playlists")
    suspend fun clearAll()
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND streamType = :streamType AND isHidden = 0 ORDER BY customOrder ASC, name ASC")
    fun getChannelsByStreamType(playlistId: Long, streamType: StreamType): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE streamType = :streamType AND isHidden = 0 ORDER BY id DESC LIMIT 500")
    fun getAllChannelsByStreamType(streamType: StreamType): Flow<List<ChannelEntity>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE streamType = :streamType AND isHidden = 0")
    fun getGroupsByStreamType(streamType: StreamType): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 AND isHidden = 0")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE (name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%') AND isHidden = 0 LIMIT 100")
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET isFavorite = :isFav WHERE id = :channelId")
    suspend fun updateFavoriteState(channelId: Long, isFav: Boolean)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Query("DELETE FROM channels")
    suspend fun clearAll()
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 50")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE streamType = :streamType ORDER BY watchedAt DESC LIMIT 20")
    fun getHistoryByStreamType(streamType: StreamType): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE channelId = :channelId LIMIT 1")
    suspend fun getHistoryForChannel(channelId: Long): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteItemEntity>>

    @Query("SELECT * FROM favorites WHERE streamType = :streamType ORDER BY addedAt DESC")
    fun getFavoritesByType(streamType: StreamType): Flow<List<FavoriteItemEntity>>

    @Query("SELECT * FROM favorites WHERE channelId = :channelId LIMIT 1")
    suspend fun getFavoriteByChannelId(channelId: Long): FavoriteItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteItemEntity)

    @Query("DELETE FROM favorites WHERE channelId = :channelId")
    suspend fun deleteFavoriteByChannelId(channelId: Long)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}

@Dao
interface EpgDao {
    @Query("SELECT * FROM epg_programs WHERE channelTvgId = :tvgId AND endTimeMs > :now ORDER BY startTimeMs ASC LIMIT 10")
    fun getProgramsForChannel(tvgId: String, now: Long = System.currentTimeMillis()): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs WHERE channelTvgId = :tvgId AND startTimeMs <= :now AND endTimeMs >= :now LIMIT 1")
    suspend fun getCurrentProgram(tvgId: String, now: Long = System.currentTimeMillis()): EpgProgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpgPrograms(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE playlistId = :playlistId")
    suspend fun deleteEpgByPlaylist(playlistId: Long)

    @Query("DELETE FROM epg_programs WHERE endTimeMs < :expiredTime")
    suspend fun deleteExpiredEpg(expiredTime: Long)
}
