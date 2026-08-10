package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.ChannelDao
import com.example.data.dao.EpgDao
import com.example.data.dao.FavoriteDao
import com.example.data.dao.PlaylistDao
import com.example.data.dao.WatchHistoryDao
import com.example.data.entity.ChannelEntity
import com.example.data.entity.EpgProgramEntity
import com.example.data.entity.FavoriteItemEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.PlaylistType
import com.example.data.entity.StreamType
import com.example.data.entity.WatchHistoryEntity

class Converters {
    @TypeConverter
    fun fromPlaylistType(value: PlaylistType): String = value.name

    @TypeConverter
    fun toPlaylistType(value: String): PlaylistType = try {
        PlaylistType.valueOf(value)
    } catch (e: Exception) {
        PlaylistType.M3U
    }

    @TypeConverter
    fun fromStreamType(value: StreamType): String = value.name

    @TypeConverter
    fun toStreamType(value: String): StreamType = try {
        StreamType.valueOf(value)
    } catch (e: Exception) {
        StreamType.LIVE
    }
}

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        WatchHistoryEntity::class,
        FavoriteItemEntity::class,
        EpgProgramEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NovaDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun epgDao(): EpgDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_player_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
