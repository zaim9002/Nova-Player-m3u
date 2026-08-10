package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.entity.ChannelEntity
import com.example.ui.components.NovaBottomNavigation
import com.example.ui.components.NovaTvSidebar
import com.example.ui.components.ParentalPinDialog
import com.example.ui.components.TopBarHeader
import com.example.ui.screens.AddPlaylistScreen
import com.example.ui.screens.EpgScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.NovaPlayerTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NovaPlayerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    NovaPlayerApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun NovaPlayerApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val configuration = LocalConfiguration.current
    val isTvOrWide = configuration.screenWidthDp >= 800 || configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val playlists by viewModel.playlists.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val favoriteChannels by viewModel.favoriteChannels.collectAsState()
    val allFavorites by viewModel.allFavorites.collectAsState()
    val liveChannels by viewModel.liveChannels.collectAsState()
    val liveGroups by viewModel.liveGroups.collectAsState()
    val moviesList by viewModel.moviesList.collectAsState()
    val movieGroups by viewModel.movieGroups.collectAsState()
    val seriesList by viewModel.seriesList.collectAsState()
    val seriesGroups by viewModel.seriesGroups.collectAsState()

    val isLoading by viewModel.isImportLoading.collectAsState()
    val importError by viewModel.importError.collectAsState()
    val currentPlayingChannel by viewModel.currentPlayingChannel.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pendingChannelToPlay by remember { mutableStateOf<ChannelEntity?>(null) }

    fun checkParentalAndPlay(channel: ChannelEntity) {
        val prefs = viewModel.preferences
        val isGroupLocked = prefs.isGroupLocked(channel.groupTitle)
        if (isGroupLocked && prefs.isParentalEnabled) {
            pendingChannelToPlay = channel
            showPinDialog = true
        } else {
            viewModel.playChannel(channel)
            navController.navigate("player")
        }
    }

    if (showPinDialog) {
        ParentalPinDialog(
            correctPin = viewModel.preferences.parentalPin,
            onPinCorrect = {
                showPinDialog = false
                pendingChannelToPlay?.let {
                    viewModel.playChannel(it)
                    navController.navigate("player")
                }
            },
            onDismiss = {
                showPinDialog = false
                pendingChannelToPlay = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isTvOrWide && currentRoute != "player") {
                NovaBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { dest ->
                        navController.navigate(dest.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (isTvOrWide && currentRoute != "player") {
                    NovaTvSidebar(
                        currentRoute = currentRoute,
                        onNavigate = { dest ->
                            navController.navigate(dest.route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                playlists = playlists,
                                historyList = watchHistory,
                                favoriteList = favoriteChannels,
                                recentChannels = liveChannels,
                                onNavigateRoute = { route -> navController.navigate(route) },
                                onChannelClick = { ch -> checkParentalAndPlay(ch) },
                                onFavoriteToggle = { ch -> viewModel.toggleFavorite(ch) },
                                onAddM3uUrl = { navController.navigate("add_playlist") },
                                onAddXtream = { navController.navigate("add_playlist") },
                                onSelectM3uFile = { navController.navigate("add_playlist") },
                                onImportM3uUrl = { name, url -> viewModel.importM3uUrl(name, url) }
                            )
                        }

                        composable("live") {
                            LiveTvScreen(
                                channels = liveChannels,
                                groups = liveGroups,
                                onChannelClick = { ch -> checkParentalAndPlay(ch) },
                                onFavoriteToggle = { ch -> viewModel.toggleFavorite(ch) }
                            )
                        }

                        composable("movies") {
                            MoviesScreen(
                                movies = moviesList,
                                groups = movieGroups,
                                onMovieClick = { m -> checkParentalAndPlay(m) }
                            )
                        }

                        composable("series") {
                            SeriesScreen(
                                seriesList = seriesList,
                                groups = seriesGroups,
                                onSeriesClick = { s -> checkParentalAndPlay(s) }
                            )
                        }

                        composable("favorites") {
                            FavoritesScreen(
                                favoritesList = allFavorites,
                                onFavoriteClick = { fav ->
                                    val ch = ChannelEntity(
                                        id = fav.channelId,
                                        playlistId = fav.playlistId,
                                        name = fav.title,
                                        logo = fav.logo,
                                        streamUrl = fav.streamUrl,
                                        streamType = fav.streamType,
                                        groupTitle = fav.groupTitle ?: "المفضلة"
                                    )
                                    checkParentalAndPlay(ch)
                                },
                                onRemoveFavorite = { channelId ->
                                    val ch = liveChannels.find { it.id == channelId }
                                        ?: moviesList.find { it.id == channelId }
                                        ?: seriesList.find { it.id == channelId }
                                    if (ch != null) {
                                        viewModel.toggleFavorite(ch)
                                    }
                                }
                            )
                        }

                        composable("history") {
                            HistoryScreen(
                                historyList = watchHistory,
                                onHistoryItemClick = { item ->
                                    val ch = ChannelEntity(
                                        id = item.channelId,
                                        playlistId = 0,
                                        name = item.channelName,
                                        logo = item.logo,
                                        streamUrl = item.streamUrl,
                                        streamType = item.streamType
                                    )
                                    checkParentalAndPlay(ch)
                                },
                                onClearHistory = { viewModel.clearWatchHistory() }
                            )
                        }

                        composable("epg") {
                            EpgScreen(epgPrograms = emptyList())
                        }

                        composable("add_playlist") {
                            AddPlaylistScreen(
                                onImportM3uUrl = { name, url ->
                                    viewModel.importM3uUrl(name, url) {
                                        navController.navigate("home")
                                    }
                                },
                                onImportXtream = { name, url, user, pass ->
                                    viewModel.importXtream(name, url, user, pass) {
                                        navController.navigate("home")
                                    }
                                },
                                onImportM3uFile = { name, inputStream ->
                                    viewModel.importM3uFile(name, inputStream) {
                                        navController.navigate("home")
                                    }
                                },
                                isLoading = isLoading,
                                errorMessage = importError
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                playlists = playlists,
                                prefs = viewModel.preferences,
                                onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                onClearHistory = { viewModel.clearWatchHistory() },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }

                        composable("player") {
                            currentPlayingChannel?.let { channel ->
                                PlayerScreen(
                                    playerManager = viewModel.playerManager,
                                    currentChannel = channel,
                                    allChannels = when (channel.streamType) {
                                        com.example.data.entity.StreamType.LIVE -> liveChannels
                                        com.example.data.entity.StreamType.MOVIE -> moviesList
                                        com.example.data.entity.StreamType.SERIES -> seriesList
                                    },
                                    onChannelChange = { newCh -> viewModel.playChannel(newCh) },
                                    onFavoriteToggle = { ch -> viewModel.toggleFavorite(ch) },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
