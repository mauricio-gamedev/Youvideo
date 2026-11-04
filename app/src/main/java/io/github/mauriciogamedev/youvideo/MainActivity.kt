package io.github.mauriciogamedev.youvideo

import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import io.github.mauriciogamedev.youvideo.BuildConfig
import io.github.mauriciogamedev.youvideo.data.provider.DefaultMediaProviders
import io.github.mauriciogamedev.youvideo.domain.provider.MediaFormatting
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderId
import io.github.mauriciogamedev.youvideo.domain.provider.ProviderMedia
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.github.mauriciogamedev.youvideo.ui.theme.YouvideoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YouvideoTheme {
                YouvideoApp(lifecycleOwner = this@MainActivity)
            }
        }
    }
}

private enum class Destination(
    val label: String,
    val icon: ImageVector
) {
    HOME("Início", Icons.Outlined.Home),
    EXPLORE("Explorar", Icons.Outlined.Search),
    DOWNLOADS("Downloads", Icons.Outlined.Download),
    LIBRARY("Biblioteca", Icons.Outlined.Bookmarks)
}

private enum class ProviderFilter {
    ALL,
    PEERTUBE,
    INTERNET_ARCHIVE,
    YOUTUBE
}

@Composable
private fun YouvideoApp(lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    var selectedDestination by rememberSaveable { mutableStateOf(Destination.HOME) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedLocalVideoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedRemoteMedia by remember { mutableStateOf<ProviderMedia?>(null) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Playback can still use the temporary grant for this app session.
            }
            selectedLocalVideoUri = uri.toString()
        }
    }

    if (selectedRemoteMedia != null) {
        RemoteVideoPlayerScreen(
            media = selectedRemoteMedia!!,
            onClose = { selectedRemoteMedia = null }
        )
    } else if (selectedLocalVideoUri != null) {
        LocalVideoPlayerScreen(
            uri = Uri.parse(selectedLocalVideoUri),
            lifecycleOwner = lifecycleOwner,
            onClose = { selectedLocalVideoUri = null }
        )
    } else {
        Scaffold(
            containerColor = Color(0xFF07090D),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF10141B),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = selectedDestination == destination,
                            onClick = { selectedDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedDestination) {
                    Destination.HOME -> HomeScreen(
                        onExplore = { selectedDestination = Destination.EXPLORE },
                        onLibrary = { selectedDestination = Destination.LIBRARY },
                        onOpenMedia = { videoPicker.launch(arrayOf("video/*")) }
                    )

                    Destination.EXPLORE -> ExploreScreen(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onOpenMedia = { selectedRemoteMedia = it }
                    )

                    Destination.DOWNLOADS -> EmptyFeatureScreen(
                        title = "Seus downloads",
                        description = "Quando uma fonte compatível autorizar o download, seus arquivos e transferências aparecerão aqui.",
                        icon = Icons.Outlined.Download
                    )

                    Destination.LIBRARY -> LibraryScreen(
                        onOpenMedia = { videoPicker.launch(arrayOf("video/*")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onExplore: () -> Unit,
    onLibrary: () -> Unit,
    onOpenMedia: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = Color(0xFF253B68)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFBBD0FF),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Youvideo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Seu espaço para assistir",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9EA9BB)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Configurações",
                    tint = Color(0xFFBBC5D5)
                )
            }
        }

        item {
            Card(
                onClick = onExplore,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF17233A))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tudo começa por aqui",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Descubra conteúdos de fontes compatíveis e organize sua experiência em um só lugar.",
                        color = Color(0xFFC5D2E9),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF86A9FF)
                    ) {
                        Text(
                            text = "Explorar conteúdos",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            color = Color(0xFF07152F),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Card(
                onClick = onOpenMedia,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111923))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF253B68),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.PlayCircleOutline,
                                contentDescription = null,
                                tint = Color(0xFFBBD0FF),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reproduzir vídeo do celular",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Escolha um arquivo e assista no player do Youvideo.",
                            color = Color(0xFF98A3B4),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeading(
                    title = "Sua central multimídia",
                    subtitle = "A base para reunir suas fontes e arquivos."
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Search,
                        title = "Explorar",
                        detail = "Encontrar conteúdo",
                        onClick = onExplore
                    )
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Bookmarks,
                        title = "Biblioteca",
                        detail = "Organizar depois",
                        onClick = onLibrary
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeading(
                    title = "Continue de onde parou",
                    subtitle = "O histórico será integrado à biblioteca em uma próxima etapa."
                )
                EmptyStateCard(
                    icon = Icons.Outlined.PlayCircleOutline,
                    title = "Histórico ainda vazio",
                    description = "Os vídeos selecionados nesta versão são reproduzidos localmente. O histórico persistente virá depois."
                )
            }
        }

        item {
            Text(
                text = "Versão inicial • Recursos em desenvolvimento",
                color = Color(0xFF6F7B8D),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ExploreScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenMedia: (ProviderMedia) -> Unit
) {
    val providers = remember {
        DefaultMediaProviders.create(BuildConfig.YOUTUBE_API_KEY)
    }
    val youtubeConfigured = providers.any { it.id == ProviderId.YOUTUBE }

    var sourceFilter by rememberSaveable { mutableStateOf(ProviderFilter.ALL) }
    var submittedQuery by rememberSaveable { mutableStateOf("") }
    var searchVersion by rememberSaveable { mutableStateOf(0) }
    var results by remember { mutableStateOf<List<ProviderMedia>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchVersion, sourceFilter) {
        if (searchVersion == 0) return@LaunchedEffect

        val selectedProviders = providers.filter { provider ->
            when (sourceFilter) {
                ProviderFilter.ALL -> true
                ProviderFilter.PEERTUBE -> provider.id == ProviderId.PEERTUBE
                ProviderFilter.INTERNET_ARCHIVE -> provider.id == ProviderId.INTERNET_ARCHIVE
                ProviderFilter.YOUTUBE -> provider.id == ProviderId.YOUTUBE
            }
        }

        if (selectedProviders.isEmpty()) {
            results = emptyList()
            hasSearched = true
            errorMessage = if (sourceFilter == ProviderFilter.YOUTUBE && !youtubeConfigured) {
                "O conector do YouTube precisa de uma chave oficial da YouTube Data API."
            } else {
                "Nenhuma fonte está disponível para este filtro."
            }
            return@LaunchedEffect
        }

        isLoading = true
        hasSearched = true
        errorMessage = null

        val attempts = coroutineScope {
            selectedProviders.map { provider ->
                async {
                    provider to runCatching {
                        provider.search(submittedQuery, limit = 12)
                    }
                }
            }.awaitAll()
        }

        results = attempts
            .flatMap { attempt -> attempt.second.getOrDefault(emptyList()) }
            .distinctBy { it.stableKey }

        val failures = attempts.mapNotNull { it.second.exceptionOrNull()?.localizedMessage }
        errorMessage = when {
            results.isNotEmpty() && failures.isNotEmpty() ->
                "Algumas fontes não responderam. Os resultados disponíveis foram mantidos."
            results.isEmpty() && failures.isNotEmpty() ->
                failures.first()
            results.isEmpty() ->
                "Nenhum resultado encontrado nessas fontes. Tente outra busca."
            else -> null
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Explorar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Busque em fontes abertas e reproduza pelo player de cada serviço.",
            color = Color(0xFF9EA9BB),
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            },
            placeholder = { Text("Buscar vídeos, filmes ou aulas") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = sourceFilter == ProviderFilter.ALL,
                onClick = { sourceFilter = ProviderFilter.ALL },
                label = { Text("Todas") }
            )
            FilterChip(
                selected = sourceFilter == ProviderFilter.PEERTUBE,
                onClick = { sourceFilter = ProviderFilter.PEERTUBE },
                label = { Text("PeerTube") }
            )
            FilterChip(
                selected = sourceFilter == ProviderFilter.INTERNET_ARCHIVE,
                onClick = { sourceFilter = ProviderFilter.INTERNET_ARCHIVE },
                label = { Text("Internet Archive") }
            )
            FilterChip(
                selected = sourceFilter == ProviderFilter.YOUTUBE,
                onClick = { sourceFilter = ProviderFilter.YOUTUBE },
                label = { Text("YouTube") }
            )
        }

        Button(
            onClick = {
                submittedQuery = query.trim()
                searchVersion += 1
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = query.trim().length >= 2 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color(0xFF07152F),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            } else {
                Icon(Icons.Outlined.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (isLoading) "Buscando..." else "Buscar")
        }

        if (!youtubeConfigured) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF17233A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "YouTube: configuração pendente",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "O conector oficial será ativado quando YOUTUBE_API_KEY estiver configurada. A reprodução usa o player incorporado do YouTube.",
                        color = Color(0xFFC5D2E9),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                color = if (results.isEmpty()) MaterialTheme.colorScheme.error else Color(0xFFB8C7E3),
                style = MaterialTheme.typography.bodySmall
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (results.isEmpty() && !isLoading) {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.Search,
                        title = if (hasSearched) "Nenhum conteúdo disponível" else "Pronto para explorar",
                        description = if (hasSearched) {
                            errorMessage ?: "Tente novamente com outros termos."
                        } else {
                            "Digite pelo menos dois caracteres e escolha as fontes que deseja pesquisar."
                        }
                    )
                }
            }

            items(results, key = { it.stableKey }) { media ->
                Card(
                    onClick = { onOpenMedia(media) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111923))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = media.providerName,
                            color = Color(0xFF9DBAFF),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = media.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!media.creator.isNullOrBlank()) {
                            Text(
                                text = media.creator,
                                color = Color(0xFF98A3B4),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val duration = MediaFormatting.durationLabel(media.durationSeconds)
                        if (duration.isNotBlank()) {
                            Text(
                                text = duration,
                                color = Color(0xFF7F8DA3),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteVideoPlayerScreen(
    media: ProviderMedia,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val webView = remember(media.playbackUrl) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }
    }

    DisposableEffect(webView, media.playbackUrl) {
        if (media.providerId == ProviderId.YOUTUBE) {
            // YouTube requires the app identity in the WebView Referer header.
            webView.loadUrl(
                media.playbackUrl,
                mapOf("Referer" to "https://io.github.mauriciogamedev.youvideo")
            )
        } else {
            webView.loadUrl(media.playbackUrl)
        }
        onDispose {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.providerName,
                    color = Color(0xFF9DBAFF),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = media.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            factory = { webView }
        )

        Text(
            text = "Reprodução pelo player incorporado da fonte. Os controles e a atribuição do serviço são mantidos.",
            color = Color(0xFF98A3B4),
            style = MaterialTheme.typography.bodySmall
        )

        Button(
            onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(media.sourceUrl))
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Abrir na fonte original")
        }
    }
}

@Composable
private fun EmptyFeatureScreen(
    title: String,
    description: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        EmptyStateCard(
            icon = icon,
            title = "Ainda está vazio",
            description = description
        )
    }
}

@Composable
private fun LibraryScreen(onOpenMedia: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Sua biblioteca",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        EmptyStateCard(
            icon = Icons.Outlined.FolderOpen,
            title = "Sua biblioteca começa aqui",
            description = "Abra vídeos armazenados no seu dispositivo. Favoritos, histórico e playlists serão adicionados depois."
        )
        Button(
            onClick = onOpenMedia,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text("Abrir vídeo do dispositivo")
        }
    }
}

@Composable
private fun LocalVideoPlayerScreen(
    uri: Uri,
    lifecycleOwner: LifecycleOwner,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    var playbackError by remember(uri) { mutableStateOf<String?>(null) }

    val player = remember(uri) {
        ExoPlayer.Builder(context.applicationContext)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(uri))
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playbackError = "Não foi possível reproduzir este arquivo. Tente outro formato de vídeo."
            }
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    DisposableEffect(lifecycleOwner, player) {
        var resumeWhenStarted = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    resumeWhenStarted = player.isPlaying
                    player.pause()
                }
                Lifecycle.Event.ON_START -> {
                    if (resumeWhenStarted) {
                        player.play()
                        resumeWhenStarted = false
                    }
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = "Player do Youvideo",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reprodução local",
                    color = Color(0xFF98A3B4),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        val exoPlayer = player
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = true
                    this.player = exoPlayer
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            }
        )

        if (playbackError != null) {
            Text(
                text = playbackError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Use os controles do player para pausar, avançar ou ajustar a reprodução.",
                color = Color(0xFF98A3B4),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF98A3B4)
        )
    }
}

@Composable
private fun FeatureTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121923))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9DBAFF),
                modifier = Modifier.size(25.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail,
                    color = Color(0xFF98A3B4),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11151C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF202B40),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF9DBAFF),
                        modifier = Modifier.size(27.dp)
                    )
                }
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = Color(0xFF98A3B4),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
