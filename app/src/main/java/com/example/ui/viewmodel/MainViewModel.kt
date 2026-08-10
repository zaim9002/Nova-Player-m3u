package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.NovaDatabase
import com.example.data.entity.ChannelEntity
import com.example.data.entity.EpgProgramEntity
import com.example.data.entity.FavoriteItemEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.StreamType
import com.example.data.entity.WatchHistoryEntity
import com.example.data.pref.NovaPreferences
import com.example.data.repository.IPTVRepository
import com.example.player.NovaPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NovaDatabase.getDatabase(application)
    val repository = IPTVRepository(db)
    val preferences = NovaPreferences(application)
    val playerManager = NovaPlayerManager(application)

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteChannels: StateFlow<List<ChannelEntity>> = repository.favoriteChannels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFavorites: StateFlow<List<FavoriteItemEntity>> = repository.allFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val liveChannels: StateFlow<List<ChannelEntity>> = repository.getChannelsByStreamType(0, StreamType.LIVE).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val liveGroups: StateFlow<List<String>> = repository.getGroupsByStreamType(StreamType.LIVE).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val moviesList: StateFlow<List<ChannelEntity>> = repository.getChannelsByStreamType(0, StreamType.MOVIE).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val movieGroups: StateFlow<List<String>> = repository.getGroupsByStreamType(StreamType.MOVIE).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val seriesList: StateFlow<List<ChannelEntity>> = repository.getChannelsByStreamType(0, StreamType.SERIES).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val seriesGroups: StateFlow<List<String>> = repository.getGroupsByStreamType(StreamType.SERIES).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isImportLoading = MutableStateFlow(false)
    val isImportLoading: StateFlow<Boolean> = _isImportLoading.asStateFlow()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    private val _currentPlayingChannel = MutableStateFlow<ChannelEntity?>(null)
    val currentPlayingChannel: StateFlow<ChannelEntity?> = _currentPlayingChannel.asStateFlow()

    fun importM3uUrl(name: String, url: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isImportLoading.value = true
            _importError.value = null
            try {
                repository.importM3uUrl(name, url)
                _isImportLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isImportLoading.value = false
                _importError.value = "فشل تحميل القائمة: ${e.localizedMessage ?: "تأكد من صحة الرابط والاتصال"}"
            }
        }
    }

    fun importXtream(name: String, serverUrl: String, user: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isImportLoading.value = true
            _importError.value = null
            try {
                repository.importXtreamCodes(name, serverUrl, user, pass)
                _isImportLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isImportLoading.value = false
                _importError.value = "تعذر الاتصال بالخادم، تحقق من البيانات."
            }
        }
    }

    fun importM3uFile(name: String, inputStream: InputStream, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isImportLoading.value = true
            _importError.value = null
            try {
                repository.importM3uFile(name, inputStream)
                _isImportLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isImportLoading.value = false
                _importError.value = "فشل قراءة الملف: ${e.localizedMessage}"
            }
        }
    }

    fun playChannel(channel: ChannelEntity) {
        _currentPlayingChannel.value = channel
        viewModelScope.launch {
            repository.recordWatchHistory(channel)
        }
    }

    fun toggleFavorite(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
