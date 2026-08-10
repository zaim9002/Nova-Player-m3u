package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlayerState {
    object Idle : PlayerState()
    object Buffering : PlayerState()
    object Ready : PlayerState()
    data class Error(val message: String) : PlayerState()
}

data class TrackInfo(
    val id: String,
    val name: String,
    val language: String?,
    val isSelected: Boolean
)

@OptIn(UnstableApi::class)
class NovaPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val audioTracks: StateFlow<List<TrackInfo>> = _audioTracks.asStateFlow()

    private val _subtitleTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val subtitleTracks: StateFlow<List<TrackInfo>> = _subtitleTracks.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var currentUrl: String? = null
    private var isLiveStream: Boolean = true
    private var scope = CoroutineScope(Dispatchers.Main)
    private var updateJob: Job? = null

    fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            initPlayer()
        }
        return exoPlayer!!
    }

    private fun initPlayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)
            .setUserAgent("NovaPlayer/1.0 (Android; IPTV)")

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> _playerState.value = PlayerState.Buffering
                            Player.STATE_READY -> {
                                _playerState.value = PlayerState.Ready
                                _duration.value = duration.coerceAtLeast(0L)
                            }
                            Player.STATE_ENDED -> _isPlaying.value = false
                            Player.STATE_IDLE -> _playerState.value = PlayerState.Idle
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        if (isPlaying) {
                            startPositionUpdates()
                        } else {
                            stopPositionUpdates()
                        }
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        updateTracksInfo(tracks)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        val errorMsg = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "تعذر الاتصال بالشبكة، يرجى التحقق من الاتصال."
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "تعذر العثور على البث أو انتهت صلاحيته."
                            else -> "حدث خطأ أثناء تشغيل البث: ${error.localizedMessage ?: "خطأ غير معروف"}"
                        }
                        _playerState.value = PlayerState.Error(errorMsg)
                    }
                })
            }
    }

    fun playStream(url: String, isLive: Boolean = true) {
        currentUrl = url
        isLiveStream = isLive
        val player = getPlayer()

        val uri = Uri.parse(url)
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("NovaPlayer/1.0 (Android; IPTV)")

        val mediaSource: MediaSource = if (url.contains(".m3u8") || url.contains("/live/")) {
            HlsMediaSource.Factory(httpDataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(MediaItem.fromUri(uri))
        } else {
            ProgressiveMediaSource.Factory(httpDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        }

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }

    fun retry() {
        currentUrl?.let { playStream(it, isLiveStream) }
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun seekForward(millis: Long = 10000) {
        exoPlayer?.let {
            val newPos = (it.currentPosition + millis).coerceAtMost(it.duration)
            it.seekTo(newPos)
        }
    }

    fun seekBackward(millis: Long = 10000) {
        exoPlayer?.let {
            val newPos = (it.currentPosition - millis).coerceAtLeast(0)
            it.seekTo(newPos)
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
    }

    fun selectAudioTrack(trackInfo: TrackInfo) {
        // Track selection logic using TrackSelectionOverride
        exoPlayer?.let { player ->
            val tracks = player.currentTracks
            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_AUDIO) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        if (format.id == trackInfo.id || format.label == trackInfo.name) {
                            player.trackSelectionParameters = player.trackSelectionParameters
                                .buildUpon()
                                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                .build()
                            return
                        }
                    }
                }
            }
        }
    }

    private fun updateTracksInfo(tracks: Tracks) {
        val audioList = mutableListOf<TrackInfo>()
        val subtitleList = mutableListOf<TrackInfo>()

        for (group in tracks.groups) {
            when (group.type) {
                C.TRACK_TYPE_AUDIO -> {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val name = format.label ?: format.language ?: "صوت ${audioList.size + 1}"
                        audioList.add(
                            TrackInfo(
                                id = format.id ?: "audio_$i",
                                name = name,
                                language = format.language,
                                isSelected = group.isTrackSelected(i)
                            )
                        )
                    }
                }
                C.TRACK_TYPE_TEXT -> {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val name = format.label ?: format.language ?: "ترجمة ${subtitleList.size + 1}"
                        subtitleList.add(
                            TrackInfo(
                                id = format.id ?: "sub_$i",
                                name = name,
                                language = format.language,
                                isSelected = group.isTrackSelected(i)
                            )
                        )
                    }
                }
            }
        }
        _audioTracks.value = audioList
        _subtitleTracks.value = subtitleList
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        updateJob = scope.launch {
            while (true) {
                exoPlayer?.let {
                    _currentPosition.value = it.currentPosition.coerceAtLeast(0L)
                    _duration.value = it.duration.coerceAtLeast(0L)
                }
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    fun release() {
        stopPositionUpdates()
        exoPlayer?.release()
        exoPlayer = null
    }
}
