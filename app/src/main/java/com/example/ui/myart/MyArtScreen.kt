package com.example.ui.myart

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.generator.MandalaLibrary
import com.example.model.MandalaDesign
import com.example.model.SavedArtwork
import com.example.ui.components.MandalaCard
import com.example.ui.theme.PinkAccent
import com.example.util.ImageSharer

enum class MyArtFilter(val title: String) {
    ALL("All Saved"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    FAVORITES("Favorites")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyArtScreen(
    savedArtworks: Map<String, SavedArtwork>,
    outlineColor: Long,
    onSelectMandala: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onDeleteArtwork: (String) -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(MyArtFilter.ALL) }
    var artworkToDelete by remember { mutableStateOf<MandalaDesign?>(null) }

    val allDesigns = remember { MandalaLibrary.designs }

    // Filter matching saved artworks
    val filteredDesigns = remember(savedArtworks, selectedFilter) {
        allDesigns.filter { design ->
            val saved = savedArtworks[design.id]
            when (selectedFilter) {
                MyArtFilter.ALL -> saved != null && (saved.coloredRegions.isNotEmpty() || saved.isFavorite)
                MyArtFilter.IN_PROGRESS -> saved != null && saved.percentCompleted > 0f && saved.percentCompleted < 0.99f
                MyArtFilter.COMPLETED -> saved != null && saved.percentCompleted >= 0.99f
                MyArtFilter.FAVORITES -> saved?.isFavorite == true
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_art_screen_root"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.my_art_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.testTag("btn_my_art_back")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Sub-tabs: All, In Progress, Completed, Favorites
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MyArtFilter.entries.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = filter.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("my_art_filter_${filter.name.lowercase()}")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            if (filteredDesigns.isEmpty()) {
                // Friendly Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "No saved mandalas in this view",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.my_art_empty),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onNavigateToGallery,
                        colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_empty_start_coloring")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Browse Gallery",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 165.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 900.dp)
                        .testTag("my_art_grid")
                ) {
                    items(filteredDesigns, key = { it.id }) { design ->
                        val saved = savedArtworks[design.id]

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                MandalaCard(
                                    design = design,
                                    savedArtwork = saved,
                                    outlineColor = outlineColor,
                                    onSelect = { onSelectMandala(design.id) },
                                    onToggleFavorite = { isFav ->
                                        onToggleFavorite(design.id, isFav)
                                    }
                                )

                                // Action Row: Continue, Share, Delete
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Continue / Edit button
                                    TextButton(
                                        onClick = { onSelectMandala(design.id) },
                                        modifier = Modifier.testTag("btn_continue_${design.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Continue",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                ImageSharer.exportAndShare(
                                                    context = context,
                                                    design = design,
                                                    coloredRegions = saved?.coloredRegions ?: emptyMap(),
                                                    outlineColor = outlineColor
                                                )
                                            },
                                            modifier = Modifier.size(36.dp).testTag("btn_share_${design.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = PinkAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { artworkToDelete = design },
                                            modifier = Modifier.size(36.dp).testTag("btn_delete_${design.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
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

    // Delete confirmation dialog
    artworkToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { artworkToDelete = null },
            title = {
                Text(text = "Delete Saved Artwork?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Are you sure you want to remove your colored progress for \"${target.title}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteArtwork(target.id)
                        artworkToDelete = null
                        Toast.makeText(context, "Artwork removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_artwork")
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { artworkToDelete = null },
                    modifier = Modifier.testTag("btn_cancel_delete_artwork")
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}
