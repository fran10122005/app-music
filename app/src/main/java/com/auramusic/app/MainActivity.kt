package com.auramusic.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.auramusic.app.domain.model.Playlist
import com.auramusic.app.domain.model.Song
import com.auramusic.app.ui.components.MiniPlayer
import com.auramusic.app.ui.components.NowPlayingSheet
import com.auramusic.app.ui.screens.AlbumDetailSheet
import com.auramusic.app.ui.screens.ArtistDetailSheet
import com.auramusic.app.ui.screens.DownloadsScreen
import com.auramusic.app.ui.screens.HomeScreen
import com.auramusic.app.ui.screens.LibraryScreen
import com.auramusic.app.ui.screens.PlaylistDetailSheet
import com.auramusic.app.ui.screens.SearchScreen
import com.auramusic.app.ui.theme.AuraMusicTheme
import com.auramusic.app.ui.theme.DarkBackground
import com.auramusic.app.ui.theme.DarkSurface
import com.auramusic.app.ui.theme.DarkSurfaceElevated
import com.auramusic.app.ui.theme.GlassBorder
import com.auramusic.app.ui.theme.PrimaryViolet
import com.auramusic.app.ui.theme.PrimaryVioletLight
import com.auramusic.app.ui.theme.TextMuted
import com.auramusic.app.ui.theme.TextPrimary
import com.auramusic.app.ui.theme.TextSecondary
import com.auramusic.app.ui.viewmodel.AppScreen
import com.auramusic.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AuraMusicTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val libraryTab by viewModel.libraryTab.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()

    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }

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
        containerColor = DarkBackground,
        bottomBar = {
            Column(modifier = Modifier.background(DarkBackground)) {
                // Persistent MiniPlayer
                MiniPlayer(
                    state = playbackState,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onSkipNextClick = { viewModel.skipNext() },
                    onClick = { viewModel.setNowPlayingExpanded(true) }
                )

                // Navigation Bar
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        Triple(AppScreen.HOME, "Inicio", Icons.Rounded.Home),
                        Triple(AppScreen.LIBRARY, "Biblioteca", Icons.Rounded.LibraryMusic),
                        Triple(AppScreen.SEARCH, "Buscar", Icons.Rounded.Search),
                        Triple(AppScreen.DOWNLOADS, "Descargas", Icons.Rounded.Download)
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
                                    tint = if (selected) PrimaryVioletLight else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) PrimaryVioletLight else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = DarkSurfaceElevated
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
                        playbackState = playbackState,
                        isOfflineMode = isOfflineMode,
                        onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                        onSongClick = { viewModel.playSong(it) },
                        onDownloadClick = { viewModel.downloadSong(it) },
                        onAddToPlaylistClick = { songToAddToPlaylist = it }
                    )
                }

                AppScreen.LIBRARY -> {
                    LibraryScreen(
                        currentTab = libraryTab,
                        songs = songs,
                        albums = albums,
                        artists = artists,
                        playlists = playlists,
                        playbackState = playbackState,
                        onTabSelected = { viewModel.setLibraryTab(it) },
                        onSongClick = { viewModel.playSong(it) },
                        onAlbumClick = { viewModel.selectAlbum(it) },
                        onArtistClick = { viewModel.selectArtist(it) },
                        onPlaylistClick = { viewModel.selectPlaylist(it) },
                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                        onDownloadClick = { viewModel.downloadSong(it) },
                        onAddToPlaylistClick = { songToAddToPlaylist = it }
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
                        onDownloadClick = { viewModel.downloadSong(it) },
                        onAddToPlaylistClick = { songToAddToPlaylist = it }
                    )
                }

                AppScreen.DOWNLOADS -> {
                    val downloadedSongs = songs.filter { it.isDownloaded }
                    DownloadsScreen(
                        downloadedSongs = downloadedSongs,
                        activeDownloads = downloads,
                        playbackState = playbackState,
                        onSongClick = { viewModel.playSong(it, downloadedSongs) },
                        onDeleteDownload = { viewModel.deleteDownload(it) },
                        onPlayAllClick = {
                            if (downloadedSongs.isNotEmpty()) {
                                viewModel.playSong(downloadedSongs.first(), downloadedSongs)
                            }
                        },
                        onAddToPlaylistClick = { songToAddToPlaylist = it }
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
            onDownloadClick = { viewModel.downloadSong(it) },
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
            onDownloadClick = { viewModel.downloadSong(it) },
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
            onDownloadClick = { viewModel.downloadSong(it) },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Playlist Detail Sheet
    selectedPlaylist?.let { playlist ->
        // Retrieve songs for playlist from repository stream
        val playlistSongsState by viewModel.repository.getSongsForPlaylist(playlist.id)
            .collectAsState(initial = emptyList())

        PlaylistDetailSheet(
            playlist = playlist,
            songs = playlistSongsState,
            playbackState = playbackState,
            onDismiss = { viewModel.selectPlaylist(null) },
            onSongClick = { viewModel.playSong(it, playlistSongsState) },
            onDownloadClick = { viewModel.downloadSong(it) },
            onAddToPlaylistClick = { songToAddToPlaylist = it }
        )
    }

    // Add to Playlist Selection Dialog
    songToAddToPlaylist?.let { song ->
        AlertDialog(
            onDismissRequest = { songToAddToPlaylist = null },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = "Añadir a playlist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                if (playlists.isEmpty()) {
                    Text(
                        text = "No tienes playlists aún. Crea una en la pestaña Biblioteca.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.addSongToPlaylist(playlist.id, song.id)
                                        songToAddToPlaylist = null
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FolderSpecial,
                                    contentDescription = null,
                                    tint = PrimaryViolet,
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
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { songToAddToPlaylist = null }) {
                    Text("Cerrar", color = TextMuted)
                }
            }
        )
    }
}
