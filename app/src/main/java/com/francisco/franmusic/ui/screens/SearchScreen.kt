package com.francisco.franmusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.domain.repository.SearchFilter
import com.francisco.franmusic.playback.PlaybackUiState
import com.francisco.franmusic.ui.components.SongListItem
import com.francisco.franmusic.ui.theme.GlassBorder
import com.francisco.franmusic.ui.theme.LocalAppThemeStyle
import com.francisco.franmusic.ui.theme.TextMuted
import com.francisco.franmusic.ui.theme.TextPrimary
import com.francisco.franmusic.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    searchQuery: String,
    searchFilter: SearchFilter,
    searchResults: List<Song>,
    playbackState: PlaybackUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (SearchFilter) -> Unit,
    onSongClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit = {},
    onAddToPlaylistClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeStyle.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Search Input
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "Buscar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Canciones, artistas, álbumes o carpetas...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = theme.primaryLight
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = TextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = theme.surfaceElevated,
                    unfocusedContainerColor = theme.surfaceElevated,
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = GlassBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips (100% Local categories)
            val filters = listOf(
                SearchFilter.ALL to "Todo",
                SearchFilter.TITLES to "Canciones",
                SearchFilter.ARTISTS to "Artistas",
                SearchFilter.ALBUMS to "Álbumes",
                SearchFilter.FOLDERS to "Carpetas",
                SearchFilter.GENRES to "Géneros",
                SearchFilter.FAVORITES to "❤️ Favoritas"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters, key = { it.first.name }) { (filter, title) ->
                    val isSelected = searchFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = theme.surfaceElevated,
                            selectedContainerColor = theme.primary,
                            labelColor = TextSecondary,
                            selectedLabelColor = theme.background
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = GlassBorder,
                            selectedBorderColor = theme.primary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Search Results List
        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Busca en tu música local",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Filtra por título, artista, álbum o carpeta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sin resultados para \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Intenta buscar por otro término o cambiar el filtro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "${searchResults.size} resultados encontrados",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                items(searchResults, key = { it.id }) { song ->
                    val isCurrent = playbackState.currentSong?.id == song.id
                    SongListItem(
                        song = song,
                        isPlaying = isCurrent && playbackState.isPlaying,
                        isCurrentSong = isCurrent,
                        onClick = { onSongClick(song) },
                        onDownloadClick = { onDownloadClick(song) },
                        onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}
