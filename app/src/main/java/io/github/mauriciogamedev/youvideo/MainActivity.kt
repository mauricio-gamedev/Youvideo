package io.github.mauriciogamedev.youvideo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mauriciogamedev.youvideo.ui.theme.YouvideoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YouvideoTheme {
                YouvideoApp()
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

@Composable
private fun YouvideoApp() {
    var selectedDestination by rememberSaveable { mutableStateOf(Destination.HOME) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

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
                    onLibrary = { selectedDestination = Destination.LIBRARY }
                )

                Destination.EXPLORE -> ExploreScreen(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                Destination.DOWNLOADS -> EmptyFeatureScreen(
                    title = "Seus downloads",
                    description = "Quando uma fonte compatível autorizar o download, seus arquivos e transferências aparecerão aqui.",
                    icon = Icons.Outlined.Download
                )

                Destination.LIBRARY -> EmptyFeatureScreen(
                    title = "Sua biblioteca",
                    description = "Favoritos, histórico e playlists ficarão organizados neste espaço.",
                    icon = Icons.Outlined.FolderOpen
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onExplore: () -> Unit,
    onLibrary: () -> Unit
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
                    subtitle = "Seu histórico aparecerá aqui quando a reprodução estiver disponível."
                )
                EmptyStateCard(
                    icon = Icons.Outlined.PlayCircleOutline,
                    title = "Ainda não há reproduções",
                    description = "O player e o histórico serão adicionados nas próximas etapas."
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
    onQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Explorar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Pesquise e descubra conteúdos de provedores compatíveis.",
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
            placeholder = { Text("Buscar vídeos ou canais") }
        )
        EmptyStateCard(
            icon = Icons.Outlined.Search,
            title = "Fontes ainda não conectadas",
            description = "A busca será ativada quando os primeiros conectores autorizados forem implementados."
        )
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
