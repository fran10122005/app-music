package com.francisco.franmusic.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.francisco.franmusic.R
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.playback.PlaybackUiState
import com.francisco.franmusic.ui.theme.LocalAppThemeStyle
import com.francisco.franmusic.ui.theme.TextMuted
import com.francisco.franmusic.ui.theme.TextPrimary
import com.francisco.franmusic.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    state: PlaybackUiState,
    onDismiss: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenLyricsClick: () -> Unit,
    onOpenEqualizerClick: () -> Unit,
    onOpenSleepTimerClick: () -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onDeleteSong: ((Song, Boolean) -> Unit)? = null
) {
    val song = state.currentSong ?: return
    val theme = LocalAppThemeStyle.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderProgress by remember { mutableFloatStateOf(state.progress) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deletePhysicalFileChecked by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.background,
        dragHandle = null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Marca de agua sutil de FranMusic fija de fondo
            Image(
                painter = painterResource(id = R.drawable.franmusic_watermark),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .alpha(0.045f),
                contentScale = ContentScale.Fit
            )

            // Contenedor principal con distribución vertical armoniosa (evita espacio vacío abajo)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Barra Superior estilo Spotify
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Cerrar",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (song.album.isNotBlank()) "REPRODUCIENDO DEL ÁLBUM" else "REPRODUCIENDO",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.primaryLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = song.album.ifBlank { song.artist.ifBlank { "FranMusic" } },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Más opciones",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // 2. Carátula grande con esquinas redondeadas y brillo ambiental (proporción Spotify)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = theme.primary.copy(alpha = 0.5f),
                            spotColor = theme.primary
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(theme.surfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (!song.artworkUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = song.artworkUri,
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = theme.primary,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }

                // 3. Título, Artista y Botón de Favorito (Diseño natural y elegante)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (state.isPlaying) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AnimatedAudioWave(tint = theme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(song) },
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (song.isFavorite) theme.accent else TextMuted,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (song.folderName.isNotBlank() && song.folderName != "Música") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.surfaceElevated
                        ) {
                            Text(
                                text = "📁 ${song.folderName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // 4. Barra de progreso y tiempos
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (isDraggingSlider) sliderProgress else state.progress,
                        onValueChange = {
                            isDraggingSlider = true
                            sliderProgress = it
                        },
                        onValueChangeFinished = {
                            onSeek(sliderProgress)
                            isDraggingSlider = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primaryLight,
                            activeTrackColor = theme.primary,
                            inactiveTrackColor = theme.surfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentSec = ((if (isDraggingSlider) sliderProgress else state.progress) * state.durationMs / 1000).toLong()
                        val totalSec = state.durationMs / 1000

                        Text(
                            text = "%d:%02d".format(currentSec / 60, currentSec % 60),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "%d:%02d".format(totalSec / 60, totalSec % 60),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // 5. Controles Principales estilo Spotify
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Aleatorio
                    IconButton(onClick = onToggleShuffle, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (state.shuffleMode) theme.primary else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Anterior
                    IconButton(
                        onClick = onSkipPreviousClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Botón Central Play / Pause (Circular grande con degradado y glow)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, CircleShape, ambientColor = theme.primary, spotColor = theme.primary)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(theme.primary, theme.secondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isBuffering) {
                            CircularProgressIndicator(
                                color = TextPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            IconButton(
                                onClick = onPlayPauseClick,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (state.isPlaying) "Pausar" else "Reproducir",
                                    tint = theme.background,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }

                    // Siguiente
                    IconButton(
                        onClick = onSkipNextClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Siguiente",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repetir
                    IconButton(onClick = onToggleRepeat, modifier = Modifier.size(44.dp)) {
                        val icon = when (state.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        }
                        val tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) theme.primary else TextMuted
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repetir",
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 6. Barra de opciones inferiores (Acceso rápido: Letras, Ecualizador, Timer, Eliminar)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = theme.surfaceElevated,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Letras
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenLyricsClick() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Letras ✨",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Ecualizador
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenEqualizerClick() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = theme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ecualizador",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Temporizador
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenSleepTimerClick() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = theme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Timer",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Eliminar canción directamente
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Eliminar canción",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet de Opciones estilo Spotify
    if (showOptionsMenu) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsMenu = false },
            containerColor = theme.surfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!song.artworkUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = song.artworkUri,
                                contentDescription = song.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = theme.surfaceVariant.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(10.dp))

                PlayerOptionItem(
                    icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                    title = "Añadir a playlist",
                    tint = TextPrimary,
                    onClick = {
                        showOptionsMenu = false
                        onAddToPlaylistClick(song)
                    }
                )

                PlayerOptionItem(
                    icon = Icons.Rounded.Lyrics,
                    title = "Ver letras sincronizadas (IA)",
                    tint = theme.primaryLight,
                    onClick = {
                        showOptionsMenu = false
                        onOpenLyricsClick()
                    }
                )

                PlayerOptionItem(
                    icon = Icons.Rounded.GraphicEq,
                    title = "Ajustes de Ecualizador",
                    tint = theme.secondary,
                    onClick = {
                        showOptionsMenu = false
                        onOpenEqualizerClick()
                    }
                )

                PlayerOptionItem(
                    icon = Icons.Rounded.Timer,
                    title = "Temporizador de apagado",
                    tint = theme.accent,
                    onClick = {
                        showOptionsMenu = false
                        onOpenSleepTimerClick()
                    }
                )

                PlayerOptionItem(
                    icon = Icons.Rounded.DeleteOutline,
                    title = "Eliminar canción de la biblioteca",
                    tint = Color(0xFFFF5252),
                    onClick = {
                        showOptionsMenu = false
                        showDeleteConfirmDialog = true
                    }
                )
            }
        }
    }

    // Diálogo de Confirmación para Eliminar Canción
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = theme.surfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "¿Eliminar canción?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "¿Estás seguro de que deseas eliminar \"${song.title}\"? Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { deletePhysicalFileChecked = !deletePhysicalFileChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = deletePhysicalFileChecked,
                            onCheckedChange = { deletePhysicalFileChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF5252),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Eliminar también el archivo de audio del almacenamiento",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteSong?.invoke(song, deletePhysicalFileChecked)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PlayerOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

@Composable
fun AnimatedAudioWave(tint: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(20.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height(h1.dp).clip(RoundedCornerShape(2.dp)).background(tint))
        Box(modifier = Modifier.width(3.dp).height(h2.dp).clip(RoundedCornerShape(2.dp)).background(tint))
        Box(modifier = Modifier.width(3.dp).height(h3.dp).clip(RoundedCornerShape(2.dp)).background(tint))
    }
}
