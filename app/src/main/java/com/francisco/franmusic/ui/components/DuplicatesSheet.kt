package com.francisco.franmusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.francisco.franmusic.domain.model.Song
import com.francisco.franmusic.ui.theme.LocalAppThemeStyle
import com.francisco.franmusic.ui.theme.TextMuted
import com.francisco.franmusic.ui.theme.TextPrimary
import com.francisco.franmusic.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicatesSheet(
    duplicateGroups: List<List<Song>>,
    onDismiss: () -> Unit,
    onDeleteSingleSong: (Song) -> Unit,
    onAutoCleanDuplicates: (List<List<Song>>) -> Unit
) {
    val theme = LocalAppThemeStyle.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentGroups by remember(duplicateGroups) { mutableStateOf(duplicateGroups) }

    val totalRedundantCopies = remember(currentGroups) {
        currentGroups.sumOf { (it.size - 1).coerceAtLeast(0) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header del Gestor de Duplicados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Canciones Duplicadas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (totalRedundantCopies > 0)
                            "$totalRedundantCopies copias repetidas en ${currentGroups.size} canciones"
                        else
                            "¡Biblioteca limpia!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (totalRedundantCopies > 0) theme.primaryLight else theme.accent
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = TextPrimary
                    )
                }
            }

            // Botón de Limpieza Automática Rápida
            if (totalRedundantCopies > 0) {
                Button(
                    onClick = {
                        onAutoCleanDuplicates(currentGroups)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoFixHigh,
                        contentDescription = null,
                        tint = theme.background,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Eliminar $totalRedundantCopies copias automáticamente",
                        color = theme.background,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (currentGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay canciones duplicadas en tu biblioteca.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currentGroups) { group ->
                        val firstSong = group.firstOrNull() ?: return@items

                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, theme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Título del grupo
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(theme.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!firstSong.artworkUri.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = firstSong.artworkUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.MusicNote,
                                                contentDescription = null,
                                                tint = theme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = firstSong.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${firstSong.artist} • ${group.size} copias encontradas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = theme.surfaceVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Lista de cada copia individual
                                group.forEachIndexed { index, song ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (index == 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = theme.primary.copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = "Original / Principal",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = theme.primaryLight,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text(
                                                    text = song.formattedDuration,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = TextMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = song.filePath.ifBlank { song.mediaUri },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Botón para eliminar esta copia específica
                                        IconButton(
                                            onClick = {
                                                onDeleteSingleSong(song)
                                                // Actualizar visualmente la lista localmente
                                                val updatedList = currentGroups.map { g ->
                                                    g.filter { it.id != song.id }
                                                }.filter { it.size > 1 }
                                                currentGroups = updatedList
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Eliminar esta copia",
                                                tint = Color(0xFFFF5252),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
