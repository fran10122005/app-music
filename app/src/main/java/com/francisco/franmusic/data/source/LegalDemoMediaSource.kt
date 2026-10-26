package com.francisco.franmusic.data.source

import com.francisco.franmusic.domain.model.Album
import com.francisco.franmusic.domain.model.Artist
import com.francisco.franmusic.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LegalDemoMediaSource : MusicSource {

    override val sourceId: String = "legal_demo"
    override val displayName: String = "Catálogo Libre (CC / Dominio Público)"

    // Catálogo 100% legal de obras maestras de dominio público y licencias abiertas (Musopen / Wikimedia Commons / Free Music Archive)
    private val legalCatalog = listOf(
        Song(
            id = "legal_chopin_nocturne",
            title = "Nocturne in E-flat major, Op. 9, No. 2",
            artist = "Frédéric Chopin",
            album = "Romantic Classics",
            durationMs = 272000L,
            trackNumber = 1,
            artworkUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
            mediaUri = "https://ia800504.us.archive.org/33/items/ChopinNocturneOp.9No.2/ChopinNocturneOp9No2.mp3",
            isOnline = true,
            isDownloaded = false,
            mimeType = "audio/mpeg",
            sourceId = sourceId
        ),
        Song(
            id = "legal_beethoven_moonlight",
            title = "Moonlight Sonata (Adagio sostenuto)",
            artist = "Ludwig van Beethoven",
            album = "Piano Masterpieces",
            durationMs = 315000L,
            trackNumber = 2,
            artworkUri = "https://images.unsplash.com/photo-1520523839898-50712825e3a7?w=500&q=80",
            mediaUri = "https://ia800108.us.archive.org/11/items/MoonlightSonata_755/Beethoven-MoonlightSonata.mp3",
            isOnline = true,
            isDownloaded = false,
            mimeType = "audio/mpeg",
            sourceId = sourceId
        ),
        Song(
            id = "legal_vivaldi_spring",
            title = "The Four Seasons: Spring (La Primavera)",
            artist = "Antonio Vivaldi",
            album = "The Four Seasons",
            durationMs = 216000L,
            trackNumber = 1,
            artworkUri = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&q=80",
            mediaUri = "https://ia800301.us.archive.org/15/items/VivaldiTheFourSeasonsSpring/01_Spring_mvt_1_Allegro.mp3",
            isOnline = true,
            isDownloaded = false,
            mimeType = "audio/mpeg",
            sourceId = sourceId
        ),
        Song(
            id = "legal_bach_air",
            title = "Air on the G String (BWV 1068)",
            artist = "Johann Sebastian Bach",
            album = "Baroque Treasures",
            durationMs = 295000L,
            trackNumber = 3,
            artworkUri = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=500&q=80",
            mediaUri = "https://ia800204.us.archive.org/24/items/AirOnTheGString_667/Bach-AirOnTheGString.mp3",
            isOnline = true,
            isDownloaded = false,
            mimeType = "audio/mpeg",
            sourceId = sourceId
        ),
        Song(
            id = "legal_mozart_nachtmusik",
            title = "Eine kleine Nachtmusik (Allegro)",
            artist = "Wolfgang Amadeus Mozart",
            album = "Viennese Serenades",
            durationMs = 345000L,
            trackNumber = 1,
            artworkUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
            mediaUri = "https://ia800501.us.archive.org/7/items/EineKleineNachtmusik_201306/Mozart-EineKleineNachtmusik1stMovement.mp3",
            isOnline = true,
            isDownloaded = false,
            mimeType = "audio/mpeg",
            sourceId = sourceId
        ),
        Song(
            id = "legal_satie_gymnopedie",
            title = "Gymnopédie No. 1 (Lent et douloureux)",
            artist = "Erik Satie",
            album = "Impressionist Piano",
            durationMs = 205000L,
            trackNumber = 1,
            artworkUri = "https://images.unsplash.com/photo-1445985543469-433ecba627a0?w=500&q=80",
            mediaUri = "https://ia800204.us.archive.org/12/items/GymnopedieNo1_923/ErikSatie-GymnopedieNo1.mp3",
            isOnline = true,
            isDownloaded = false,
            mimeType = "audio/mpeg",
            sourceId = sourceId
        )
    )

    override suspend fun getSongs(): List<Song> = withContext(Dispatchers.IO) {
        legalCatalog
    }

    override suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        legalCatalog.groupBy { it.album }
            .map { (albumTitle, albumSongs) ->
                val first = albumSongs.first()
                Album(
                    id = "online_album_${albumTitle.hashCode()}",
                    title = albumTitle,
                    artist = first.artist,
                    artworkUri = first.artworkUri,
                    songCount = albumSongs.size
                )
            }
    }

    override suspend fun getArtists(): List<Artist> = withContext(Dispatchers.IO) {
        legalCatalog.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                Artist(
                    id = "online_artist_${artistName.hashCode()}",
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = artistSongs.map { it.album }.distinct().size,
                    artworkUri = artistSongs.firstOrNull()?.artworkUri
                )
            }
    }

    override suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        val lower = query.lowercase().trim()
        legalCatalog.filter {
            it.title.lowercase().contains(lower) ||
            it.artist.lowercase().contains(lower) ||
            it.album.lowercase().contains(lower)
        }
    }

    override suspend fun getStreamUrl(songId: String): String? {
        return legalCatalog.find { it.id == songId }?.mediaUri
    }
}
