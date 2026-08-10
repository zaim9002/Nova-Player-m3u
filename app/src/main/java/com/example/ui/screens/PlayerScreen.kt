package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.example.data.entity.ChannelEntity
import com.example.player.NovaPlayerManager
import com.example.player.PlayerState
import com.example.ui.components.tvFocusGlow
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    playerManager: NovaPlayerManager,
    currentChannel: ChannelEntity,
    allChannels: List<ChannelEntity>,
    onChannelChange: (ChannelEntity) -> Unit,
    onFavoriteToggle: (ChannelEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val playerState by playerManager.playerState.collectAsState()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val currentPos by playerManager.currentPosition.collectAsState()
    val totalDuration by playerManager.duration.collectAsState()
    val audioTracks by playerManager.audioTracks.collectAsState()
    val speed by playerManager.playbackSpeed.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showChannelDrawer by remember { mutableStateOf(false) }

    // Auto hide controls after 5s
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            if (!showChannelDrawer) {
                showControls = false
            }
        }
    }

    // Play stream on start
    LaunchedEffect(currentChannel) {
        playerManager.playStream(
            url = currentChannel.streamUrl,
            isLive = currentChannel.streamType == com.example.data.entity.StreamType.LIVE
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) {
                        // Swipe Up -> Next Channel
                        val currentIdx = allChannels.indexOfFirst { it.id == currentChannel.id }
                        if (currentIdx != -1 && currentIdx < allChannels.size - 1) {
                            onChannelChange(allChannels[currentIdx + 1])
                        }
                    } else if (dragAmount > 50) {
                        // Swipe Down -> Prev Channel
                        val currentIdx = allChannels.indexOfFirst { it.id == currentChannel.id }
                        if (currentIdx > 0) {
                            onChannelChange(allChannels[currentIdx - 1])
                        }
                    }
                }
            }
            .clickable { showControls = !showControls }
    ) {
        // Media3 Android PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.getPlayer()
                    useController = false // Custom overlay controls
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Indicator
        if (playerState is PlayerState.Buffering) {
            CircularProgressIndicator(
                color = NeonBlue,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
            )
        }

        // Error Banner
        if (playerState is PlayerState.Error) {
            val errorMsg = (playerState as PlayerState.Error).message
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(DarkSurface.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تعذر تشغيل البث",
                    color = ErrorRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = { playerManager.retry() },
                    modifier = Modifier.background(NeonBlue, RoundedCornerShape(10.dp))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "إعادة المحاولة", tint = DarkBg)
                }
            }
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                // Top Header Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentChannel.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentChannel.groupTitle,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // PiP Button
                    IconButton(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                activity?.enterPictureInPictureMode()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.PictureInPictureAlt, contentDescription = "Picture in Picture", tint = TextPrimary)
                    }

                    // Favorite Toggle
                    IconButton(onClick = { onFavoriteToggle(currentChannel) }) {
                        Icon(
                            imageVector = if (currentChannel.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "المفضلة",
                            tint = if (currentChannel.isFavorite) WarningYellow else TextPrimary
                        )
                    }
                }

                // Center Play/Pause & Channel Switch
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IconButton(
                        onClick = {
                            val currentIdx = allChannels.indexOfFirst { it.id == currentChannel.id }
                            if (currentIdx > 0) {
                                onChannelChange(allChannels[currentIdx - 1])
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(DarkSurface.copy(alpha = 0.8f), RoundedCornerShape(26.dp))
                    ) {
                        Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "القناة السابقة", tint = TextPrimary)
                    }

                    IconButton(
                        onClick = { playerManager.togglePlayPause() },
                        modifier = Modifier
                            .size(68.dp)
                            .background(NeonBlue, RoundedCornerShape(34.dp))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "تشغيل/إيقاف",
                            tint = DarkBg,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val currentIdx = allChannels.indexOfFirst { it.id == currentChannel.id }
                            if (currentIdx != -1 && currentIdx < allChannels.size - 1) {
                                onChannelChange(allChannels[currentIdx + 1])
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(DarkSurface.copy(alpha = 0.8f), RoundedCornerShape(26.dp))
                    ) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "القناة التالية", tint = TextPrimary)
                    }
                }

                // Bottom Controls Overlay
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    if (totalDuration > 0) {
                        Slider(
                            value = currentPos.toFloat(),
                            onValueChange = { playerManager.seekTo(it.toLong()) },
                            valueRange = 0f..totalDuration.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonBlue,
                                activeTrackColor = NeonBlue,
                                inactiveTrackColor = DarkSurfaceVariant
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Channel Drawer Button
                        IconButton(
                            onClick = { showChannelDrawer = !showChannelDrawer }
                        ) {
                            Icon(imageVector = Icons.Default.Tv, contentDescription = "قائمة القنوات", tint = NeonBlue)
                        }

                        // Speed
                        IconButton(
                            onClick = {
                                val nextSpeed = when (speed) {
                                    1.0f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 2.0f
                                    else -> 1.0f
                                }
                                playerManager.setSpeed(nextSpeed)
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextPrimary)
                                Text(text = "${speed}x", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // In-Player Channel Drawer Overlay
        if (showChannelDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .align(Alignment.CenterStart)
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "القنوات المتاحة (${allChannels.size})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allChannels) { ch ->
                            val isSelected = ch.id == currentChannel.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
                                    .clickable {
                                        onChannelChange(ch)
                                        showChannelDrawer = false
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ch.name,
                                    color = if (isSelected) NeonBlue else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
