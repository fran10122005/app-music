package com.francisco.franmusic.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleMode: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

class PlayerManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var lastPersistedPositionSec: Long = -1L
    @Volatile private var sessionRestoreInProgress = false

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val stateStore = PlaybackStateStore.getInstance(context)
    val restoreSessionEnabled: Flow<Boolean> = stateStore.restoreSessionEnabledFlow

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val playlistQueue = mutableListOf<Song>()

    val sleepTimerManager = SleepTimerManager(scope) {
        controller?.pause()
        saveCurrentSession()
    }
    val sleepTimerState: StateFlow<SleepTimerState> = sleepTimerManager.state

    init {
        initController()
    }

    private fun initController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                setupPlayerListener()
                syncStateFromPlayer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    saveCurrentSession()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                _uiState.update { it.copy(isBuffering = isBuffering) }
                syncStateFromPlayer()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                sleepTimerManager.onSongCompleted()
                val mediaId = mediaItem?.mediaId
                val matchedSong = playlistQueue.find { it.id == mediaId }
                val index = playlistQueue.indexOfFirst { it.id == mediaId }
                _uiState.update {
                    it.copy(
                        currentSong = matchedSong,
                        currentQueueIndex = index,
                        durationMs = controller?.duration?.takeIf { d -> d > 0 } ?: matchedSong?.durationMs ?: 0L,
                        currentPositionMs = 0L
                    )
                }
                saveCurrentSession()
            }

            override fun onPlayerError(error: PlaybackException) {
                _uiState.update {
                    it.copy(
                        isPlaying = false,
                        errorMessage = "Error de reproducción: ${error.message}"
                    )
                }
            }
        })
    }

    private fun syncStateFromPlayer() {
        val player = controller ?: return
        val currentMediaId = player.currentMediaItem?.mediaId
        val song = playlistQueue.find { it.id == currentMediaId }
        val index = playlistQueue.indexOfFirst { it.id == currentMediaId }
        val duration = if (player.duration > 0) player.duration else (song?.durationMs ?: 0L)

        _uiState.update {
            it.copy(
                currentSong = song ?: it.currentSong,
                isPlaying = player.isPlaying,
                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = duration,
                shuffleMode = player.shuffleModeEnabled,
                repeatMode = player.repeatMode,
                queue = playlistQueue.toList(),
                currentQueueIndex = index
            )
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let { player ->
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = if (player.duration > 0) player.duration else (_uiState.value.durationMs)
                    _uiState.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = dur
                        )
                    }

                    // Save position every ~5 seconds to save battery and persist accurately
                    val currentFiveSec = pos / 5000L
                    if (currentFiveSec != lastPersistedPositionSec) {
                        lastPersistedPositionSec = currentFiveSec
                        stateStore.updatePosition(pos)
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val player = controller ?: return
        playlistQueue.clear()
        playlistQueue.addAll(queue)

        val mediaItems = queue.map { it.toMediaItem() }
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()

        _uiState.update {
            it.copy(
                currentSong = song,
                queue = queue,
                currentQueueIndex = startIndex,
                isPlaying = true,
                errorMessage = null
            )
        }
        saveCurrentSession()
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        saveCurrentSession()
    }

    fun seekTo(positionMs: Long) {
        val player = controller ?: return
        player.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
        scope.launch { stateStore.updatePosition(positionMs) }
    }

    fun seekToProgress(fraction: Float) {
        val duration = _uiState.value.durationMs
        if (duration > 0) {
            val targetMs = (duration * fraction.coerceIn(0f, 1f)).toLong()
            seekTo(targetMs)
        }
    }

    fun skipNext() {
        val player = controller ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    fun skipPrevious() {
        val player = controller ?: return
        if (player.currentPosition > 3000) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    fun toggleShuffle() {
        val player = controller ?: return
        val newShuffle = !player.shuffleModeEnabled
        player.shuffleModeEnabled = newShuffle
        _uiState.update { it.copy(shuffleMode = newShuffle) }
        saveCurrentSession()
    }

    fun toggleRepeat() {
        val player = controller ?: return
        val nextMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = nextMode
        _uiState.update { it.copy(repeatMode = nextMode) }
        saveCurrentSession()
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerManager.startTimerMinutes(minutes)
    }

    fun startSleepTimerEndOfSong() {
        sleepTimerManager.startEndOfSong()
    }

    fun cancelSleepTimer() {
        sleepTimerManager.cancel()
    }

    fun setRestoreSessionEnabled(enabled: Boolean) {
        scope.launch {
            stateStore.setRestoreSessionEnabled(enabled)
        }
    }

    fun saveCurrentSession() {
        scope.launch {
            val currentSong = _uiState.value.currentSong
            val pos = controller?.currentPosition ?: _uiState.value.currentPositionMs
            val queueIds = playlistQueue.map { it.id }
            val shuffle = controller?.shuffleModeEnabled ?: _uiState.value.shuffleMode
            val repeat = controller?.repeatMode ?: _uiState.value.repeatMode
            if (currentSong != null && queueIds.isNotEmpty()) {
                stateStore.saveSession(
                    currentSongId = currentSong.id,
                    queueIds = queueIds,
                    positionMs = pos,
                    shuffleEnabled = shuffle,
                    repeatMode = repeat
                )
            }
        }
    }

    suspend fun restoreSessionIfEnabled(allAvailableSongs: List<Song>) {
        if (allAvailableSongs.isEmpty()) return
        if (sessionRestoreInProgress) return
        sessionRestoreInProgress = true

        try {
            val isRestoreEnabled = stateStore.restoreSessionEnabledFlow.first()
            if (!isRestoreEnabled) return
            if (controller?.isPlaying == true || playlistQueue.isNotEmpty()) return

            val session = stateStore.sessionFlow.first()
            if (!session.isValid) return

            val idMap = allAvailableSongs.associateBy { it.id }
            val restoredQueue = session.queueIds.mapNotNull { idMap[it] }
            val currentSong = session.currentSongId?.let { idMap[it] }

            if (restoredQueue.isNotEmpty() && currentSong != null) {
                playlistQueue.clear()
                playlistQueue.addAll(restoredQueue)

                controller?.let { player ->
                    val mediaItems = restoredQueue.map { it.toMediaItem() }
                    val targetIndex = restoredQueue.indexOfFirst { it.id == currentSong.id }.coerceAtLeast(0)

                    player.setMediaItems(mediaItems, targetIndex, session.positionMs)
                    player.shuffleModeEnabled = session.shuffleEnabled
                    player.repeatMode = session.repeatMode
                    player.playWhenReady = false // Paused on restoration
                    player.prepare()

                    _uiState.update {
                        it.copy(
                            currentSong = currentSong,
                            currentQueueIndex = targetIndex,
                            currentPositionMs = session.positionMs,
                            durationMs = currentSong.durationMs,
                            shuffleMode = session.shuffleEnabled,
                            repeatMode = session.repeatMode,
                            queue = restoredQueue,
                            isPlaying = false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        // IMPORTANTE: Este método se llama en el hilo principal.
        // NO hacer I/O bloqueante aquí (sin openInputStream, sin readBytes).
        // Media3 carga la artwork de forma asíncrona usando solo el URI.
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)

        if (!artworkUri.isNullOrEmpty()) {
            try {
                val parsed = Uri.parse(artworkUri)
                metadataBuilder.setArtworkUri(parsed)
                // No leer bytes aquí - es I/O bloqueante en el hilo principal
            } catch (_: Exception) {
            }
        }

        val uri = if (localFilePath != null) {
            val file = File(localFilePath)
            if (file.exists()) Uri.fromFile(file) else Uri.parse(mediaUri)
        } else {
            Uri.parse(mediaUri)
        }

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    companion object {
        @Volatile
        private var INSTANCE: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
