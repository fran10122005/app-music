package com.francisco.franmusic

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.ui.components.EqualizerSheet
import com.francisco.franmusic.ui.components.LyricsSheet
import com.francisco.franmusic.ui.components.MiniPlayer
import com.francisco.franmusic.ui.components.NowPlayingSheet
import com.francisco.franmusic.ui.components.SleepTimerDialog
import com.francisco.franmusic.ui.screens.AlbumDetailSheet
import com.francisco.franmusic.ui.screens.ArtistDetailSheet
import com.francisco.franmusic.ui.screens.ComposerDetailSheet
import com.francisco.franmusic.ui.screens.FolderDetailSheet
import com.francisco.franmusic.ui.screens.GenreDetailSheet
import com.francisco.franmusic.ui.screens.HomeScreen
import com.francisco.franmusic.ui.screens.LibraryScreen
import com.francisco.franmusic.ui.screens.PlaylistDetailSheet
import com.francisco.franmusic.ui.screens.SearchScreen
import com.francisco.franmusic.ui.screens.SettingsScreen
import com.francisco.franmusic.ui.screens.YearDetailSheet
import com.francisco.franmusic.ui.theme.FranMusicTheme
import com.francisco.franmusic.ui.theme.LocalAppThemeStyle
import com.francisco.franmusic.ui.theme.TextMuted
import com.francisco.franmusic.ui.theme.TextPrimary
import com.francisco.franmusic.ui.viewmodel.AppScreen
import com.francisco.franmusic.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()
            FranMusicTheme(style = currentTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val theme = LocalAppThemeStyle.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val libraryTab by viewModel.libraryTab.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val sleepTimerState by viewModel.sleepTimerState.collectAsState()
    val equalizerState by viewModel.equalizerState.collectAsState()
    val lyricsState by viewModel.lyricsState.collectAsState()
    val restoreSessionEnabled by viewModel.restoreSessionEnabled.collectAsState()

    val genres by viewModel.genres.collectAsState()
    val yearGroups by viewModel.yearGroups.collectAsState()
    val composers by viewModel.composers.collectAsState()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedYearGroup by viewModel.selectedYearGroup.collectAsState()
    val selectedComposer by viewModel.selectedComposer.collectAsState()

    var showLyricsSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var maintenanceResultMessage by remember { mutableStateOf<String?>(null) }
    var smartListType by remember { mutableStateOf<String?>(null) }

    // Request permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        if (granted) {
            viewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    Scaffold(
        containerColor = theme.background,
        bottomBar = {
            Column(modifier = Modifier.background(theme.background)) {
                // Persistent MiniPlayer
                MiniPlayer(
                    state = playbackState,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onSkipNextClick = { viewModel.skipNext() },
                    onClick = { viewModel.setNowPlayingExpanded(true) }
                )

                // Navigation Bar
                NavigationBar(
                    containerColor = theme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        Triple(AppScreen.HOME, "Inicio", Icons.Rounded.Home),
                        Triple(AppScreen.LIBRARY, "Biblioteca", Icons.Rounded.LibraryMusic),
                        Triple(AppScreen.SEARCH, "Buscar", Icons.Rounded.Search),
                        Triple(AppScreen.SETTINGS, "Ajustes", Icons.Rounded.Palette)
                    )

                    navItems.forEach { (screen, label, icon) ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) theme.primary else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) theme.primary else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = theme.surfaceElevated
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        songs = songs,
                        favoriteSongs = viewModel.favoriteSongs.collectAsState().value,
                        mostPlayedSongs = viewModel.mostPlayedSongs.collectAsState().value,
                        playbackState = playbackState,
                        isOfflineMode = true,
                        onToggleOfflineMode = { },
                        onSongClick = { viewModel.playSong(it) },
                        onDownloadClick = { },
                        onAddToPlaylistClick = { songToAddToPlaylist = it }
                    )
                }

                AppScreen.LIBRARY -> {
                    LibraryScreen(
                        currentTab = libraryTab,
                        songs = songs,
                        folders = folders,
                        albums = albums,
                        artists = artists,
                        genres = genres,
                        yearGroups = yearGroups,
                        composers = composers,
                        playlists = playlists,
                        favoriteSongs = viewModel.favoriteSongs.collectAsState().value,
                        mostPlayedSongs = viewModel.mostPlayedSongs.collectAsState().value,
                        playbackState = playbackState,
                        onTabSelected = { viewModel.setLibraryTab(it) },
                        onSongClick = { viewModel.playSong(it) },
                        onFolderClick = { viewModel.selectFolder(it) },
                        onPlayFolderClick = { viewModel.playFolder(it) },
                        onAlbumClick = { viewModel.selectAlbum(it) },
                        onArtistClick = { viewModel.selectArtist(it) },
                        onGenreClick = { viewModel.selectGenre(it) },
                        onYearGroupClick = { viewModel.selectYearGroup(it) },
                        onComposerClick = { viewModel.selectComposer(it) },
                        onPlaylistClick = { viewModel.selectPlaylist(it) },
                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                        onDownloadClick = { },
                        onAddToPlaylistClick = { songToAddToPlaylist = it },
                        onSmartListClick = { smartListType = it }
                    )
                }

                AppScreen.SEARCH -> {
                    SearchScreen(
                        searchQuery = searchQuery,
                        searchFilter = searchFilter,
                        searchResults = searchResults,
                        playbackState = playbackState,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onFilterChange = { viewModel.onSearchFilterChanged(it) },
                        onSongClick = { viewModel.playSong(it, searchResults) },
                        onDownloadClick = { },
                        onAddToPlaylistClick = { songToAddToPlaylist = it }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        currentTheme = currentTheme,
                        geminiApiKey = viewModel.getGeminiApiKey(),
                        restoreSessionEnabled = restoreSessionEnabled,
                        onThemeSelect = { viewModel.setTheme(it) },
                        onSaveApiKey = { viewModel.setGeminiApiKey(it) },
                        onToggleRestoreSession = { viewModel.setRestoreSessionEnabled(it) },
                        onOpenEqualizer = { showEqualizerSheet = true },
                        onOpenSleepTimer = { showSleepTimerDialog = true },
                        onRescanLibrary = { viewModel.rescanLibrary() },
                        onCleanMissingFiles = {
                            viewModel.cleanMissingFiles { count ->
                                maintenanceResultMessage = if (count > 0)
                                    "✅ Se eliminaron $count archivos no encontrados de la base de datos."
                                else
                                    "✅ La biblioteca está limpia, no hay archivos faltantes."
                            }
                        },
                        onDetectDuplicates = {
                            viewModel.detectDuplicates { groups ->
                                val totalDupes = groups.sumOf { it.size - 1 }
                                maintenanceResultMessage = if (totalDupes > 0)
                                    "⚠️ Se detectaron $totalDupes canciones duplicadas en ${groups.size} grupos. Revisa tu biblioteca."
                                else
                                    "✅ No se detectaron duplicados en tu biblioteca."
                            }
                        }
                    )
                }
            }
        }
    }

    // Now Playing Modal Bottom Sheet
    if (isNowPlayingExpanded && playbackState.currentSong != null) {
        NowPlayingSheet(
            state = playbackState,
            onDismiss = { viewModel.setNowPlayingExpanded(false) },
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onSkipNextClick = { viewModel.skipNext() },
            onSkipPreviousClick = { viewModel.skipPrevious() },
            onSeek = { viewModel.seekToProgress(it) },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onOpenLyricsClick = { showLyricsSheet = true },
            onOpenEqualizerClick = { showEqualizerSheet = true },
            onOpenSleepTimerClick = { showSleepTimerDialog = true },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Lyrics Modal Sheet (with AI generator)
    if (showLyricsSheet && playbackState.currentSong != null) {
        LyricsSheet(
            song = playbackState.currentSong!!,
            lyricsState = lyricsState,
            currentPositionMs = playbackState.currentPositionMs,
            onDismiss = { showLyricsSheet = false },
            onGenerateWithAi = { playbackState.currentSong?.let { viewModel.generateAiLyrics(it) } },
            onSeekToTime = { viewModel.seekToPositionMs(it) }
        )
    }

    // Equalizer Modal Sheet
    if (showEqualizerSheet) {
        EqualizerSheet(
            state = equalizerState,
            onDismiss = { showEqualizerSheet = false },
            onBandLevelChange = { band, level -> viewModel.setEqualizerBandLevel(band, level) },
            onSelectPreset = { viewModel.selectEqualizerPreset(it) },
            onToggleBassBoost = { viewModel.toggleBassBoost(it) },
            onBassBoostStrengthChange = { viewModel.setBassBoostStrength(it) }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            timerState = sleepTimerState,
            onDismiss = { showSleepTimerDialog = false },
            onStartMinutes = { viewModel.startSleepTimer(it) },
            onStartEndOfSong = { viewModel.startSleepTimerEndOfSong() },
            onCancelTimer = { viewModel.cancelSleepTimer() }
        )
    }

    // Folder Detail Sheet
    selectedFolder?.let { folder ->
        val folderSongsState by viewModel.repository.getSongsByFolder(folder.path)
            .collectAsState(initial = emptyList())
        FolderDetailSheet(
            folder = folder,
            songs = folderSongsState,
            playbackState = playbackState,
            onDismiss = { viewModel.selectFolder(null) },
            onPlayAllClick = { viewModel.playFolder(folder) },
            onSongClick = { viewModel.playSong(it, folderSongsState) },
            onDownloadClick = { },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Album Detail Sheet
    selectedAlbum?.let { album ->
        val albumSongs = songs.filter { it.album == album.title }
        AlbumDetailSheet(
            album = album,
            songs = albumSongs,
            playbackState = playbackState,
            onDismiss = { viewModel.selectAlbum(null) },
            onSongClick = { viewModel.playSong(it, albumSongs) },
            onDownloadClick = { },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Artist Detail Sheet
    selectedArtist?.let { artist ->
        val artistSongs = songs.filter { it.artist == artist.name }
        ArtistDetailSheet(
            artist = artist,
            songs = artistSongs,
            playbackState = playbackState,
            onDismiss = { viewModel.selectArtist(null) },
            onSongClick = { viewModel.playSong(it, artistSongs) },
            onDownloadClick = { },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Playlist Detail Sheet
    selectedPlaylist?.let { playlist ->
        val playlistSongs by viewModel.repository.getSongsForPlaylist(playlist.id)
            .collectAsState(initial = emptyList())
        PlaylistDetailSheet(
            playlist = playlist,
            songs = playlistSongs,
            playbackState = playbackState,
            onDismiss = { viewModel.selectPlaylist(null) },
            onSongClick = { viewModel.playSong(it, playlistSongs) },
            onDownloadClick = { },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Genre Detail Sheet
    selectedGenre?.let { genre ->
        val genreSongs by viewModel.repository.getSongsByGenre(genre.name)
            .collectAsState(initial = emptyList())
        GenreDetailSheet(
            genre = genre,
            songs = genreSongs,
            playbackState = playbackState,
            onDismiss = { viewModel.selectGenre(null) },
            onPlayAllClick = {
                if (genreSongs.isNotEmpty()) viewModel.playSong(genreSongs.first(), genreSongs)
            },
            onSongClick = { viewModel.playSong(it, genreSongs) },
            onDownloadClick = { },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Year Detail Sheet
    selectedYearGroup?.let { yearGroup ->
        val yearSongs by viewModel.repository.getSongsByYear(yearGroup.year)
            .collectAsState(initial = emptyList())
        YearDetailSheet(
            yearGroup = yearGroup,
            songs = yearSongs,
            playbackState = playbackState,
            onDismiss = { viewModel.selectYearGroup(null) },
            onPlayAllClick = {
                if (yearSongs.isNotEmpty()) viewModel.playSong(yearSongs.first(), yearSongs)
            },
            onSongClick = { viewModel.playSong(it, yearSongs) },
            onDownloadClick = { },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Composer Detail Sheet
    selectedComposer?.let { composer ->
        val composerSongs by viewModel.repository.getSongsByComposer(composer.name)
            .collectAsState(initial = emptyList())
        ComposerDetailSheet(
            composer = composer,
            songs = composerSongs,
            playbackState = playbackState,
            onDismiss = { viewModel.selectComposer(null) },
            onPlayAllClick = {
                if (composerSongs.isNotEmpty()) viewModel.playSong(composerSongs.first(), composerSongs)
            },
            onSongClick = { viewModel.playSong(it, composerSongs) },
            onDownloadClick = { },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Add To Playlist Dialog
    songToAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { songToAddToPlaylist = null },
            onPlaylistSelected = { playlist ->
                viewModel.addSongToPlaylist(playlist.id, song.id)
                songToAddToPlaylist = null
            },
            onCreateNewPlaylist = { name ->
                viewModel.createPlaylist(name)
            }
        )
    }

    // Maintenance Result Dialog
    maintenanceResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { maintenanceResultMessage = null },
            containerColor = LocalAppThemeStyle.current.surface,
            title = {
                Text(
                    text = "Mantenimiento de Biblioteca",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.francisco.franmusic.ui.theme.TextPrimary
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.francisco.franmusic.ui.theme.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { maintenanceResultMessage = null }) {
                    Text("Entendido", color = LocalAppThemeStyle.current.primary)
                }
            }
        )
    }

    // Smart List Sheet (Favorites or Most Played)
    smartListType?.let { listType ->
        val theme = LocalAppThemeStyle.current
        val smartSongs = when (listType) {
            "favorites" -> viewModel.favoriteSongs.collectAsState().value
            "mostPlayed" -> viewModel.mostPlayedSongs.collectAsState().value
            else -> emptyList()
        }
        val title = when (listType) {
            "favorites" -> "❤️ Mis Favoritas"
            "mostPlayed" -> "🔥 Más Escuchadas"
            else -> ""
        }

        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { smartListType = null },
            containerColor = theme.background
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.francisco.franmusic.ui.theme.TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                items(smartSongs) { song ->
                    com.francisco.franmusic.ui.components.SongListItem(
                        song = song,
                        isPlaying = playbackState.isPlaying,
                        isCurrentSong = playbackState.currentSong?.id == song.id,
                        onClick = {
                            viewModel.playSong(song, smartSongs)
                            smartListType = null
                        },
                        onDownloadClick = {},
                        onAddToPlaylistClick = {
                            songToAddToPlaylist = song
                            smartListType = null
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun AddToPlaylistDialog(
    playlists: List<com.francisco.franmusic.domain.model.Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (com.francisco.franmusic.domain.model.Playlist) -> Unit,
    onCreateNewPlaylist: (String) -> Unit
) {
    val theme = LocalAppThemeStyle.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        title = {
            Text(
                text = "Añadir a playlist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            if (playlists.isEmpty()) {
                Text(
                    text = "No tienes playlists creadas. Ve a la biblioteca para crear una.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                LazyColumn {
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPlaylistSelected(playlist) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FolderSpecial,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = theme.primary)
            }
        }
    )
}
