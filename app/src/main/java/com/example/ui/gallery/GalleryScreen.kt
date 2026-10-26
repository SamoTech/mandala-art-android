package com.example.ui.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.generator.MandalaLibrary
import com.example.model.Difficulty
import com.example.model.MandalaCategory
import com.example.model.SavedArtwork
import com.example.ui.components.MandalaCard

import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.ads.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    initialCategory: MandalaCategory?,
    savedArtworks: Map<String, SavedArtwork>,
    outlineColor: Long,
    isDesignUnlocked: (String) -> Boolean,
    onSelectMandala: (String) -> Unit,
    onRequestUnlockBonus: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf(initialCategory ?: MandalaCategory.ALL)
    }

    val allDesigns = remember { MandalaLibrary.designs }

    val filteredList = remember(selectedCategory) {
        if (selectedCategory == MandalaCategory.ALL) {
            allDesigns
        } else {
            allDesigns.filter {
                it.category == selectedCategory ||
                (selectedCategory == MandalaCategory.EASY && it.difficulty == Difficulty.EASY) ||
                (selectedCategory == MandalaCategory.MEDIUM && it.difficulty == Difficulty.MEDIUM) ||
                (selectedCategory == MandalaCategory.HARD && it.difficulty == Difficulty.HARD)
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gallery_screen_root"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = stringResource(R.string.gallery_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${allDesigns.size} Original Mandalas",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.testTag("btn_gallery_back")
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

                    // Category filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MandalaCategory.entries.forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        text = category.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("gallery_filter_${category.name.lowercase()}")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        bottomBar = {
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 900.dp)
                    .testTag("gallery_grid")
            ) {
                items(filteredList, key = { it.id }) { design ->
                    val isLocked = !isDesignUnlocked(design.id)
                    MandalaCard(
                        design = design,
                        savedArtwork = savedArtworks[design.id],
                        outlineColor = outlineColor,
                        isLocked = isLocked,
                        onSelect = {
                            if (isLocked) {
                                onRequestUnlockBonus(design.id)
                            } else {
                                onSelectMandala(design.id)
                            }
                        },
                        onToggleFavorite = { isFav ->
                            onToggleFavorite(design.id, isFav)
                        }
                    )
                }
            }
        }
    }
}
