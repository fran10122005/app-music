# AuraMusic 🎵

Aplicación de música moderna para Android desarrollada en **Kotlin**, **Jetpack Compose** y **Material 3**, con arquitectura limpia, soporte de biblioteca local (MediaStore), streaming online con catálogo legal de dominio público / Creative Commons, descargas explícitas con **WorkManager**, caché LRU con **Media3 ExoPlayer**, persistencia reactiva en **Room** y reproducción en segundo plano mediante **Media3 Session**.

---

## 🌟 Características Principales

1. **Biblioteca Local y Metadatos (MediaStore)**:
   - Lectura de música local del almacenamiento con metadatos completos: título, artista, álbum, duración y carátula.
   - Manejo de permisos según la versión de Android: `READ_MEDIA_AUDIO` y `POST_NOTIFICATIONS` en Android 13+ (API 33+) y `READ_EXTERNAL_STORAGE` en versiones anteriores.
   - Navegación agrupada por **Canciones**, **Álbumes**, **Artistas** y **Playlists**.

2. **Fuentes Online Legales**:
   - Interfaz extensible `MusicSource` con operaciones desacopladas: `getSongs()`, `getAlbums()`, `getArtists()`, `search()`, `getStreamUrl()`.
   - `LocalMediaSource`: lectura eficiente mediante MediaStore.
   - `LegalDemoMediaSource`: catálogo 100% legal de obras maestras clásicas de dominio público y licencias Creative Commons (Chopin, Beethoven, Vivaldi, Bach, Mozart, Satie) con metadatos reales y streaming directo.
   - Arquitectura lista para conectar servidores autoalojados (Subsonic, Navidrome, Jellyfin) o proveedores con API Key mediante DataStore o BuildConfig sin elusión de DRM ni scraping ilegal.

3. **Reproducción Moderna en Segundo Plano (Media3)**:
   - `PlaybackService` basado en `MediaSessionService` de AndroidX Media3.
   - Notificación multimedia del sistema con controles nativos (Play/Pause, Anterior/Siguiente, barra de progreso interactiva) y compatibilidad con Bluetooth/auriculares y desconexión (Becoming Noisy).
   - Cola de reproducción inteligente, soporte para aleatorio (*shuffle*) y repetición (*repeat* off, all, one).
   - Mini reproductor persistente y pantalla modal expandible "Reproduciendo ahora" con carátula con relieve, slider de búsqueda y controles completos.

4. **Modo Offline, Caché y Descargas**:
   - **Caché de streaming**: `Media3Cache` con política LRU (`LeastRecentlyUsedCacheEvictor` de 100MB) para evitar descargas repetidas de canciones escuchadas.
   - **Descargas explícitas con WorkManager**: `DownloadWorker` descarga audio directamente al almacenamiento seguro de la app (`context.filesDir/downloads`), con notificación de progreso en tiempo real.
   - **Registro en Room**: persistencia del estado (`QUEUED`, `DOWNLOADING`, `COMPLETED`, `FAILED`), progreso porcentual y bytes transferidos.
   - **Modo Offline**: interruptor en la pantalla de inicio que filtra automáticamente la biblioteca mostrando exclusivamente pistas locales y descargadas.

5. **Diseño y Experiencia de Usuario (Material 3)**:
   - Tema oscuro elegante (Dark Velvet Obsidian) con acentos violeta eléctrico y cian neón.
   - Barra de navegación inferior: **Inicio**, **Biblioteca**, **Buscar**, **Descargas**.
   - Búsqueda en tiempo real con chips de filtro: Todo, Dispositivo, En Línea (CC), Descargadas.
   - Creación y gestión de listas de reproducción personalizadas.

---

## 🏗️ Arquitectura del Proyecto

El proyecto sigue una arquitectura limpia pragmática organizada en capas:

```
app/src/main/java/com/auramusic/app/
├── AuraMusicApp.kt                 # Application class
├── MainActivity.kt                 # Single Activity Compose con Scaffold y navegación
├── domain/
│   ├── model/                      # Modelos puros: Song, Album, Artist, Playlist, DownloadInfo
│   └── repository/                 # Interfaces de repositorio y filtros
├── data/
│   ├── local/db/
│   │   ├── AuraDatabase.kt         # Base de datos Room
│   │   ├── dao/                    # SongDao, PlaylistDao, DownloadDao
│   │   └── entity/                 # SongEntity, PlaylistEntity, PlaylistSongCrossRef, DownloadEntity
│   ├── source/
│   │   ├── MusicSource.kt          # Abstracción de fuentes
│   │   ├── LocalMediaSource.kt     # MediaStore local
│   │   └── LegalDemoMediaSource.kt # Catálogo online legal
│   └── repository/
│       └── MusicRepositoryImpl.kt  # Implementación unificada
├── playback/
│   ├── Media3Cache.kt              # SimpleCache LRU de Media3
│   └── PlayerManager.kt            # Controlador de MediaController y StateFlow reactivo
├── service/
│   └── PlaybackService.kt          # MediaSessionService para segundo plano
├── download/
│   ├── DownloadWorker.kt           # CoroutineWorker para descargas con notificación
│   └── DownloadManagerHelper.kt    # Encolado en WorkManager
└── ui/
    ├── theme/                      # Color, Type, Theme (Material 3)
    ├── components/                 # MiniPlayer, NowPlayingSheet, SongListItem
    ├── screens/                    # HomeScreen, LibraryScreen, SearchScreen, DownloadsScreen, DetailSheets
    └── viewmodel/                  # MainViewModel (MVVM reactivo con StateFlows)
```

---

## 🚀 Requisitos y Compilación

- **JDK**: Java 17 o 21 (Eclipse Adoptium Temurin recomendado).
- **Android SDK**: `compileSdk = 35`, `minSdk = 24`, `targetSdk = 34`.
- **Gradle**: 8.14.3 con Version Catalog (`gradle/libs.versions.toml`).

### Comandos de Terminal

- **Compilar Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```

- **Ejecutar Pruebas Unitarias**:
  ```bash
  ./gradlew test
  ```

- **Ejecutar Lint**:
  ```bash
  ./gradlew lint
  ```

---

## 🔒 Legalidad y Fuentes Online

AuraMusic cumple estrictamente con las políticas de derechos de autor:
- No incluye scraping ni descarga de servicios protegidos por DRM o términos restrictivos (YouTube, Spotify, etc.).
- Las pistas online provienen de grabaciones de dominio público licenciadas bajo Creative Commons (CC BY / CC0) de archivos abiertos como Musopen e Internet Archive.
- Si se desea conectar un servidor propio (Navidrome / Subsonic), basta con implementar `MusicSource` configurando el host y las credenciales en DataStore o BuildConfig.
