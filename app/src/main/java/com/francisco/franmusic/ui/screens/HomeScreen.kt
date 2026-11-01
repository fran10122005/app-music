package com.francisco.franmusic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.francisco.franmusic.R
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.playback.PlaybackUiState
import com.francisco.franmusic.ui.components.SongListItem
import com.francisco.franmusic.ui.theme.LocalAppThemeStyle
import com.francisco.franmusic.ui.theme.TextMuted
import com.francisco.franmusic.ui.theme.TextPrimary
import com.francisco.franmusic.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun HomeScreen(
    songs: List<Song>,
    favoriteSongs: List<Song> = emptyList(),
    mostPlayedSongs: List<Song> = emptyList(),
    playbackState: PlaybackUiState,
    isOfflineMode: Boolean = true,
    onToggleOfflineMode: () -> Unit = {},
    onSongClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit = {},
    onAddToPlaylistClick: (Song) -> Unit,
    onPlayAllShuffle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeStyle.current

    // Saludo dinámico según la hora del día
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 5..11 -> "Buenos días"
        in 12..19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    val recentSongs = remember(songs) { songs.take(12) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Header Principal
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "FranMusic Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tu música en FranMusic",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.primaryLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Accesos Rápidos estilo Spotify (Favoritas, Más Escuchadas, Modo Aleatorio)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickShortcutCard(
                        title = "Favoritas",
                        subtitle = "${favoriteSongs.size} pistas",
                        icon = Icons.Rounded.Favorite,
                        iconTint = theme.accent,
                        onClick = {
                            if (favoriteSongs.isNotEmpty()) {
                                onSongClick(favoriteSongs.first())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickShortcutCard(
                        title = "Top Reproducidas",
                        subtitle = "${mostPlayedSongs.size} pistas",
                        icon = Icons.Rounded.TrendingUp,
                        iconTint = theme.primary,
                        onClick = {
                            if (mostPlayedSongs.isNotEmpty()) {
                                onSongClick(mostPlayedSongs.first())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (onPlayAllShuffle != null || songs.isNotEmpty()) {
                        QuickShortcutCard(
                            title = "Aleatorio",
                            subtitle = "Toda la música",
                            icon = Icons.Rounded.Shuffle,
                            iconTint = theme.secondary,
                            onClick = {
                                if (onPlayAllShuffle != null) {
                                    onPlayAllShuffle()
                                } else if (songs.isNotEmpty()) {
                                    onSongClick(songs.shuffled().first())
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Sección: 🔥 Las Más Escuchadas (si tiene pistas reproducidas)
        if (mostPlayedSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 Las Más Escuchadas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${mostPlayedSongs.size} canciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(mostPlayedSongs.take(10), key = { "most_${it.id}" }) { song ->
                        FeaturedSongCard(
                            song = song,
                            isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 3. Sección: ❤️ Canciones Favoritas (si tiene favoritas)
        if (favoriteSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❤️ Tus Favoritas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${favoriteSongs.size} canciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favoriteSongs.take(10), key = { "fav_${it.id}" }) { song ->
                        FeaturedSongCard(
                            song = song,
                            isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 4. Sección: ✨ Agregadas Recientemente
        if (recentSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ Agregadas Recientemente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentSongs, key = { "recent_${it.id}" }) { song ->
                        FeaturedSongCard(
                            song = song,
                            isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 5. Sección: 🎵 Tu Música (Lista limpia de canciones)
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎵 Tu Música",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${songs.size} pistas",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (songs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron canciones en tu biblioteca.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        items(songs.take(30), key = { it.id }) { song ->
            SongListItem(
                song = song,
                isPlaying = playbackState.isPlaying,
                isCurrentSong = playbackState.currentSong?.id == song.id,
                onClick = { onSongClick(song) },
                onDownloadClick = { onDownloadClick(song) },
                onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun QuickShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeStyle.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = theme.surfaceElevated,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeaturedSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeStyle.current

    Box(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surface),
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
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Play badge button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = theme.background,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
