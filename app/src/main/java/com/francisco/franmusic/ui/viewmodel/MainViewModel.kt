package com.francisco.franmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francisco.franmusic.data.local.ThemePreferences
import com.francisco.franmusic.data.local.db.FranMusicDatabase
import com.francisco.franmusic.data.repository.MusicRepositoryImpl
import com.francisco.franmusic.data.source.LocalMediaSource
import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Composer
import com.francisco.franmusic.domain.model.Folder
import com.francisco.franmusic.domain.model.Genre
import com.francisco.franmusic.domain.model.Playlist
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.model.YearGroup
import com.francisco.franmusic.domain.repository.MusicRepository
import com.francisco.franmusic.domain.repository.SearchFilter
import com.francisco.franmusic.playback.EqualizerManager
import com.francisco.franmusic.playback.EqualizerUiState
import com.francisco.franmusic.playback.LyricsManager
import com.francisco.franmusic.playback.LyricsUiState
import com.francisco.franmusic.playback.PlaybackUiState
import com.francisco.franmusic.playback.PlayerManager
import com.francisco.franmusic.playback.SleepTimerState
import com.francisco.franmusic.ui.theme.AppThemeStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    LIBRARY,
    SEARCH,
    SETTINGS
}

enum class LibraryTab {
    SONGS,
    FOLDERS,
    ALBUMS,
    ARTISTS,
    GENRES,
    YEARS,
    COMPOSERS,
    PLAYLISTS
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FranMusicDatabase.getInstance(application)
    private val localSource = LocalMediaSource(application)

    val repository: MusicRepository = MusicRepositoryImpl(
        database = database,
        localSource = localSource,
        externalScope = viewModelScope
    )

    private val playerManager = PlayerManager.getInstance(application)
    private val themePreferences = ThemePreferences(application)
    private val equalizerManager = EqualizerManager.getInstance()

    private val lyricsManager = LyricsManager(application) { songId, lyricsText, insight ->
        repository.saveLyrics(songId, lyricsText, insight)
    }

    val playbackState: StateFlow<PlaybackUiState> = playerManager.uiState
    val currentTheme: StateFlow<AppThemeStyle> = themePreferences.currentTheme
    val sleepTimerState: StateFlow<SleepTimerState> = playerManager.sleepTimerState
    val equalizerState: StateFlow<EqualizerUiState> = equalizerManager.uiState
    val lyricsState: StateFlow<LyricsUiState> = lyricsManager.uiState
    val restoreSessionEnabled: StateFlow<Boolean> = playerManager.restoreSessionEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _libraryTab = MutableStateFlow(LibraryTab.SONGS)
    val libraryTab: StateFlow<LibraryTab> = _libraryTab.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    // Data streams
    val songs: StateFlow<List<Song>> = repository.songs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = repository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = repository.artists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<Folder>> = repository.folders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val genres: StateFlow<List<Genre>> = repository.genres
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yearGroups: StateFlow<List<YearGroup>> = repository.yearGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val composers: StateFlow<List<Composer>> = repository.composers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayedSongs: StateFlow<List<Song>> = repository.mostPlayedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                repository.songs.collect { songList ->
                    if (songList.isNotEmpty() && playbackState.value.currentSong == null) {
                        playerManager.restoreSessionIfEnabled(songList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchSongs(query, _searchFilter.value)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection dialog states
    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    val selectedFolder: StateFlow<Folder?> = _selectedFolder.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedGenre = MutableStateFlow<Genre?>(null)
    val selectedGenre: StateFlow<Genre?> = _selectedGenre.asStateFlow()

    private val _selectedYearGroup = MutableStateFlow<YearGroup?>(null)
    val selectedYearGroup: StateFlow<YearGroup?> = _selectedYearGroup.asStateFlow()

    private val _selectedComposer = MutableStateFlow<Composer?>(null)
    val selectedComposer: StateFlow<Composer?> = _selectedComposer.asStateFlow()

    private val _editingSong = MutableStateFlow<Song?>(null)
    val editingSong: StateFlow<Song?> = _editingSong.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setLibraryTab(tab: LibraryTab) {
        _libraryTab.value = tab
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setTheme(theme: AppThemeStyle) {
        themePreferences.setTheme(theme)
    }

    fun onPermissionGranted() {
        rescanLibrary()
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            repository.refreshLocalSongs()
        }
    }

    // Playback actions
    fun playSong(song: Song, queue: List<Song> = songs.value) {
        playerManager.playSong(song, queue)
        viewModelScope.launch {
            repository.recordSongPlayed(song.id)
            lyricsManager.loadLyrics(song)
        }
    }

    fun playFolder(folder: Folder) {
        viewModelScope.launch {
            val folderSongs = repository.getSongsByFolder(folder.path).first()
            if (folderSongs.isNotEmpty()) {
                playSong(folderSongs.first(), folderSongs)
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, !song.isFavorite)
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun skipNext() {
        playerManager.skipNext()
        playerManager.uiState.value.currentSong?.let { loadLyricsForSong(it) }
    }

    fun skipPrevious() {
        playerManager.skipPrevious()
        playerManager.uiState.value.currentSong?.let { loadLyricsForSong(it) }
    }

    fun seekToProgress(progress: Float) {
        playerManager.seekToProgress(progress)
    }

    fun seekToPositionMs(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        playerManager.startSleepTimer(minutes)
    }

    fun startSleepTimerEndOfSong() {
        playerManager.startSleepTimerEndOfSong()
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    fun setRestoreSessionEnabled(enabled: Boolean) {
        playerManager.setRestoreSessionEnabled(enabled)
    }

    // Equalizer
    fun setEqualizerBandLevel(band: Int, level: Int) {
        equalizerManager.setBandLevel(band, level)
    }

    fun selectEqualizerPreset(index: Int) {
        equalizerManager.selectPreset(index)
    }

    fun toggleBassBoost(enabled: Boolean) {
        equalizerManager.toggleBassBoost(enabled)
    }

    fun setBassBoostStrength(strength: Int) {
        equalizerManager.setBassBoostStrength(strength)
    }

    // Lyrics & AI
    fun loadLyricsForSong(song: Song) {
        viewModelScope.launch {
            lyricsManager.loadLyrics(song)
        }
    }

    fun generateAiLyrics(song: Song) {
        viewModelScope.launch {
            lyricsManager.generateWithAi(song)
        }
    }

    fun getGeminiApiKey(): String = lyricsManager.geminiApiKey

    fun setGeminiApiKey(key: String) {
        lyricsManager.geminiApiKey = key
    }

    // Search actions
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSearchFilterChanged(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    // Playlists
    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name.trim())
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun selectFolder(folder: Folder?) {
        _selectedFolder.value = folder
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun selectGenre(genre: Genre?) {
        _selectedGenre.value = genre
    }

    fun selectYearGroup(yearGroup: YearGroup?) {
        _selectedYearGroup.value = yearGroup
    }

    fun selectComposer(composer: Composer?) {
        _selectedComposer.value = composer
    }

    fun setEditingSong(song: Song?) {
        _editingSong.value = song
    }

    fun updateSongMetadata(
        songId: String,
        title: String,
        artist: String,
        album: String,
        year: Int?,
        genre: String?,
        composer: String?,
        trackNumber: Int
    ) {
        viewModelScope.launch {
            repository.updateSongMetadata(songId, title, artist, album, year, genre, composer, trackNumber)
            _editingSong.value = null
        }
    }

    fun cleanMissingFiles(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.cleanMissingFiles()
            onComplete(count)
        }
    }

    fun detectDuplicates(onComplete: (List<List<Song>>) -> Unit) {
        viewModelScope.launch {
            val duplicates = repository.detectDuplicates()
            onComplete(duplicates)
        }
    }

    fun deleteSongs(songIds: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteSongs(songIds)
            onComplete()
        }
    }
}
