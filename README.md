# FranMusic 🎵

> **Reproductor de música local de alta fidelidad para Android** desarrollado con **Kotlin moderno**, **Jetpack Compose**, **Material 3**, **Media3 ExoPlayer / MediaSession**, persistencia reactiva con **Room + KSP**, ecualizador nativo y generación inteligente de letras con **Inteligencia Artificial (Google Gemini)**.

---

## 📌 Contexto del Proyecto para el Asesor de Prompts / Asistente IA

Este documento sirve como **fuente única de verdad (Ground Truth)** del proyecto para que cualquier modelo de lenguaje o asesor de prompts entienda el estado actual, el alcance técnico y las decisiones de diseño de **FranMusic**.

### Identidad y Alcance
- **Nombre de la Aplicación**: `FranMusic`
- **Application ID / Package**: `com.francisco.franmusic`
- **Enfoque Principal**: **Reproductor 100% Local y Offline**. Se han retirado intencionalmente las pantallas y lógicas de descargas y fuentes online simuladas para concentrar todos los recursos en una experiencia local impecable, ultra-rápida y visualmente impactante.
- **Plataforma Objetivo**: Android 7.0+ (API 24+ hasta API 35+ / Android 15).
- **Entorno de Compilación**:
  - `compileSdk = 35`
  - `targetSdk = 34`
  - `minSdk = 24`
  - Java 21 LTS (Temurin)
  - Gradle 8.14.3 con Version Catalog (`gradle/libs.versions.toml`)
  - Kotlin 2.0.21 con Kotlin Compose Compiler Plugin

---

## 🌟 Funcionalidades Implementadas (Features Core & Plus)

### 1. 📂 Explorador de Carpetas de Audio (Plus #1)
- Detección automática de la estructura de carpetas físicas en el dispositivo (`MediaStore.Audio.Media.DATA`).
- Modelo `Folder(path, name, songCount, artworkUri)`.
- Pestaña **Carpetas** en la Biblioteca y hoja de detalle (`FolderDetailSheet`) con opción para **"Reproducir Carpeta Completa"** con un solo toque.

### 2. ❤️ Sistema de Favoritos y Listas Inteligentes (Plus #2)
- Marcado rápido con icono de corazón animado (❤️) en el reproductor completo (`NowPlayingSheet`) y en los elementos de lista.
- Consulta en Room de `favoriteSongs` (`isFavorite = true`).
- Contador de reproducciones (`playCount`) persistido para generar automáticamente la lista inteligente de **"Más Escuchadas 🔥"**.

### 3. ⏱️ Temporizador de Apagado / Sleep Timer (Plus #3)
- `SleepTimerManager.kt` con cuenta regresiva observable mediante `StateFlow<SleepTimerState>`.
- Presets rápidos de apagado: **15 min**, **30 min**, **45 min**, **60 min**.
- Modo inteligente **"Fin de canción"**: detiene automáticamente la reproducción al terminar la pista actual.
- Control interactivo mediante `SleepTimerDialog.kt` accesible desde el reproductor y desde Ajustes.

### 4. 🎚️ Ecualizador Nativo y Refuerzo de Graves / Bass Boost (Plus #4)
- `EqualizerManager.kt` conectado al `audioSessionId` de ExoPlayer en `PlaybackService`.
- 5 bandas de ecualización de frecuencia (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz) con indicadores de ganancia en dB.
- Presets de audio: *Plano*, *Rock*, *Pop*, *Bajo Potente*, *Voz Clara*, *Electrónica*.
- Interruptor y deslizador de intensidad de **Refuerzo de Graves (Bass Boost)** (0 a 100%).
- Hoja modal interactiva `EqualizerSheet.kt`.

### 5. ✨ Visor de Letras Inteligente con IA y LRC (Plus #5)
- `LyricsManager.kt` con arquitectura híbrida en 3 niveles:
  1. **Local (.lrc / .txt)**: Detección y lectura inmediata de archivos de letras complementarios en la misma carpeta del audio.
  2. **Búsqueda Abierta**: Consulta REST en segundo plano a la API abierta de LRCLIB.
  3. **Generación con IA (Google Gemini)**: Botón con icono ✨ que consulta el modelo de lenguaje para estructurar la letra estrofa por estrofa, generar traducción si está en otro idioma e incluir un análisis o resumen temático (*AI Insight*).
  4. **Persistencia en Room**: Toda letra obtenida o generada se guarda en `SongEntity.lyrics` para acceso offline instantáneo y permanente.
- `LyricsSheet.kt`: Visor deslizante con auto-scroll sincronizado al compás de la reproducción y resaltado de la estrofa actual. Tocar cualquier línea sincronizada adelanta o retrasa la canción a ese segundo exacto.

### 6. 🎨 Motor de Estilos Visuales Dinámicos (5 Temas)
- Selector de temas en tiempo real mediante `AppThemeStyle.kt` y persistido en `ThemePreferences.kt`:
  1. **Fran Neon (Cyberpunk)**: Fondo negro puro OLED (`#080B11`) con acentos neón cian (`#00F5D4`), violeta neón y rosa.
  2. **Sunset Glow**: Fondo cálido noche (`#120814`) con acentos naranja fuego, ámbar dorado y magenta.
  3. **Midnight Blue**: Fondo navy profundo (`#060B16`) con acentos azul zafiro (`#3A86FF`) y hielo.
  4. **Emerald Groove**: Fondo bosque oscuro (`#06120B`) con acentos verde esmeralda (`#10B981`) y lima eléctrico.
  5. **Fran Violet (Original)**: Estilo clásico FranMusic con violetas y cian.
- Todos los componentes (MiniPlayer, NowPlaying, fondos, sliders y barras de navegación) se adaptan automáticamente a través de `LocalAppThemeStyle`.

### 7. 🎧 Reproducción en Segundo Plano y Notificación Multimedia (Media3)
- `PlaybackService.kt` extiende `MediaSessionService`.
- Integración con el estilo multimedia nativo de Android (`MediaStyle`): carátula expandida, controles de reproducción (Anterior, Play/Pause, Siguiente, Favorito) y la barra de progreso ondulada (*squiggled seekbar*) de Android 13/14/15.
- Gestión de `AudioFocus` (pausa automática ante llamadas telefónicas) y evento `BECOMING_NOISY` (pausa inmediata al desconectar auriculares o apagar Bluetooth).
- WakeLock local (`C.WAKE_MODE_LOCAL`) para evitar que el CPU suspenda la reproducción de audio local.

### 8. 🎯 Cola Persistente y Restauración de Sesión (Fase 1)
- `PlaybackStateStore.kt` implementado con **DataStore Preferences**:
  - Persiste: `currentSongId`, `queueIds`, `positionMs`, `shuffleEnabled`, `repeatMode` y `lastUpdated`.
  - Guardado inteligente de posición cada 5 segundos durante la reproducción para no desgastar batería.
  - Guardado en cambios de pista, pausa y en `onTaskRemoved` / `onDestroy` del servicio multimedia.
  - Al reabrir la aplicación, si la opción está activada (configurable desde Ajustes), reconstruye la cola y la pista activa posicionada exactamente donde se dejó, en estado pausado lista para play.
  - El estado sobrevive a cierres forzados de la app y reinicios del teléfono.

---

## 🏛️ Estructura del Código Fuente

```
app/src/main/java/com/francisco/franmusic/
├── FranMusicApp.kt                      # Aplicación Android base
├── MainActivity.kt                      # Actividad principal Compose con Scaffold, NavigationBar y Sheets
├── domain/
│   ├── model/
│   │   ├── Song.kt                      # Modelo Song (metadatos, folderPath, isFavorite, playCount, lyrics)
│   │   ├── Folder.kt                    # Modelo Folder (path, name, songCount, artworkUri)
│   │   ├── Album.kt                     # Modelo Album (title, artist, songCount, year, artworkUri)
│   │   ├── Artist.kt                    # Modelo Artist (name, songCount, artworkUri)
│   │   ├── Playlist.kt                  # Modelo Playlist (id, name, createdAt, songCount)
│   │   └── Lyrics.kt                    # Modelos LyricLine y LyricsData (sincronización y AI Insight)
│   └── repository/
│       ├── MusicRepository.kt           # Contrato de repositorio principal
│       └── SearchFilter.kt              # Filtros de búsqueda
├── data/
│   ├── local/
│   │   ├── ThemePreferences.kt          # Persistencia del tema visual elegido
│   │   └── db/
│   │       ├── FranMusicDatabase.kt     # Base de datos Room (versión 2)
│   │       ├── dao/
│   │       │   ├── SongDao.kt           # Consultas de canciones, carpetas, favoritos, playCount y letras
│   │       │   └── PlaylistDao.kt       # Gestión de playlists locales
│   │       └── entity/
│   │           ├── SongEntity.kt        # Entidad Room de canción
│   │           ├── PlaylistEntity.kt    # Entidad Room de playlist
│   │           └── PlaylistSongCrossRef.kt
│   ├── source/
│   │   └── LocalMediaSource.kt          # Lector de MediaStore y extracción de rutas de carpetas
│   └── repository/
│       └── MusicRepositoryImpl.kt       # Implementación reactiva de repositorio 100% local
├── playback/
│   ├── PlayerManager.kt                 # Singleton mediador de MediaController, cola y estado
│   ├── EqualizerManager.kt              # Conexión con Equalizer y BassBoost nativos de Android
│   ├── SleepTimerManager.kt             # Gestor del temporizador de apagado y cuenta regresiva
│   ├── LyricsManager.kt                 # Motor de letras (.lrc, LRCLIB y Gemini AI)
│   ├── PlaybackUiState.kt               # Estado reactivo del reproductor
│   └── PlaybackStateStore.kt            # Persistencia de sesión con DataStore Preferences
├── service/
│   └── PlaybackService.kt               # MediaSessionService para segundo plano y notificación
└── ui/
    ├── theme/
    │   ├── AppThemeStyle.kt             # Definición de los 5 temas (colores, gradientes, superficies)
    │   ├── Color.kt                     # Paleta de colores base
    │   ├── Theme.kt                     # FranMusicTheme y LocalAppThemeStyle
    │   └── Type.kt                      # Tipografía moderna
    ├── components/
    │   ├── MiniPlayer.kt                # Mini reproductor flotante con tema dinámico
    │   ├── NowPlayingSheet.kt           # Gran reproductor con onda sonora animada, acciones y carátula
    │   ├── LyricsSheet.kt               # Visor de letras sincronizadas con IA y tarjetas de análisis
    │   ├── EqualizerSheet.kt            # Panel de ecualización y refuerzo de graves
    │   ├── SleepTimerDialog.kt          # Selector y cuenta regresiva del temporizador de apagado
    │   └── SongListItem.kt              # Elemento de lista con carátula y acciones rápidas
    └── screens/
        ├── HomeScreen.kt                # Resumen de inicio con accesos rápidos y estado
        ├── LibraryScreen.kt             # Pestañas: Canciones, Carpetas, Álbumes, Artistas, Playlists
        ├── SearchScreen.kt              # Búsqueda local instantánea con filtros
        ├── SettingsScreen.kt            # Selector de temas, ajustes de IA, escaneo y utilidades
        └── DetailDialogs.kt             # Hojas de detalle para Carpeta, Álbum, Artista y Playlist
```

---

## 🛠️ Comandos de Terminal Útiles

```bash
# Compilar y validar fuentes
./gradlew compileDebugSources

# Ejecutar la suite completa de pruebas unitarias
./gradlew test

# Generar el paquete APK de desarrollo
./gradlew assembleDebug
# Ubicación del APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## 💡 Guía para el Asesor de Prompts (Prompt Guidelines)

Cuando formules nuevos requerimientos o prompts para este proyecto, ten en cuenta:
1. **Mantener el enfoque 100% Local**: No reintroducir pestañas ni dependencias de streaming online innecesarias; la prioridad es la velocidad y excelencia en la reproducción de archivos del dispositivo.
2. **Respetar el sistema de diseño reactivo**: Siempre usar `LocalAppThemeStyle.current` en componentes de Compose para que cualquier nueva pantalla responda inmediatamente al selector de los 5 temas.
3. **Persistencia en Room**: Cualquier metadato nuevo de canciones debe incluirse en `SongEntity` y `Song.kt` con valores por defecto para mantener la compatibilidad hacia atrás.
4. **Manejo de Audio**: Toda interacción con la reproducción debe pasar por `PlayerManager` y `PlaybackService` para garantizar que la notificación multimedia y el control en segundo plano sigan sincronizados.
