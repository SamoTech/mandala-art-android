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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.generator.MandalaLibrary
import com.example.model.Difficulty
import com.example.model.LevelSystem
import com.example.model.MandalaDesign
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
                                    text = pluralStringResource(R.plurals.gallery_original_mandala_count, allDesigns.size, allDesigns.size),
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
                                    contentDescription = stringResource(R.string.navigation_back)
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
                                        text = stringResource(category.labelResource),
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
                val showLevelSections = selectedCategory == MandalaCategory.ALL
                val mainDesigns = if (showLevelSections) {
                    filteredList.filter { it.id !in LevelSystem.ALL_LEVEL_DESIGN_IDS }
                } else {
                    filteredList
                }

                items(mainDesigns, key = { it.id }) { design ->
                    GalleryDesignCard(
                        design = design,
                        savedArtworks = savedArtworks,
                        outlineColor = outlineColor,
                        isDesignUnlocked = isDesignUnlocked,
                        onSelectMandala = onSelectMandala,
                        onRequestUnlockBonus = onRequestUnlockBonus,
                        onToggleFavorite = onToggleFavorite
                    )
                }

                if (showLevelSections) {
                    LevelSystem.LEVEL_DESIGN_IDS.forEach { (level, ids) ->
                        item(key = "level_header_$level", span = { GridItemSpan(maxLineSpan) }) {
                            LevelPackHeader(
                                level = level,
                                completedCount = LevelSystem.completedCountInLevel(level, savedArtworks),
                                totalDesigns = ids.size,
                                isUnlocked = LevelSystem.isLevelUnlocked(level, savedArtworks)
                            )
                        }
                        items(allDesigns.filter { it.id in ids }, key = { it.id }) { design ->
                            GalleryDesignCard(
                                design = design,
                                savedArtworks = savedArtworks,
                                outlineColor = outlineColor,
                                isDesignUnlocked = isDesignUnlocked,
                                onSelectMandala = onSelectMandala,
                                onRequestUnlockBonus = onRequestUnlockBonus,
                                onToggleFavorite = onToggleFavorite
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun GalleryDesignCard(
    design: MandalaDesign,
    savedArtworks: Map<String, SavedArtwork>,
    outlineColor: Long,
    isDesignUnlocked: (String) -> Boolean,
    onSelectMandala: (String) -> Unit,
    onRequestUnlockBonus: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
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

@Composable
private fun LevelPackHeader(
    level: Int,
    completedCount: Int,
    totalDesigns: Int,
    isUnlocked: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("level_header_$level")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.gallery_level_title, level, stringResource(levelNameResource(level))),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isUnlocked) {
                        stringResource(R.string.gallery_completed_progress, completedCount, totalDesigns)
                    } else {
                        stringResource(R.string.gallery_locked_hint, LevelSystem.REQUIRED_COMPLETIONS_TO_UNLOCK, level - 1)
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.gallery_level_locked),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


private val MandalaCategory.labelResource: Int
    get() = when (this) {
        MandalaCategory.ALL -> R.string.category_all
        MandalaCategory.EASY -> R.string.category_easy
        MandalaCategory.MEDIUM -> R.string.category_medium
        MandalaCategory.HARD -> R.string.category_hard
        MandalaCategory.FLORAL -> R.string.category_floral
        MandalaCategory.GEOMETRIC -> R.string.category_geometric
        MandalaCategory.INTRICATE -> R.string.category_intricate
    }

private fun levelNameResource(level: Int): Int = when (level) {
    1 -> R.string.gallery_level_name_blossom_path
    2 -> R.string.gallery_level_name_golden_flow
    3 -> R.string.gallery_level_name_master_circle
    else -> R.string.gallery_level_title
}
