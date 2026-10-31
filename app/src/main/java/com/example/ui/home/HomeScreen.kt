package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.generator.MandalaLibrary
import com.example.model.MandalaCategory
import com.example.model.MandalaDesign
import com.example.model.SavedArtwork
import com.example.ui.components.MandalaCard
import com.example.ui.theme.PopGradientEnd
import com.example.ui.theme.PopGradientMid
import com.example.ui.theme.PopGradientStart
import com.example.ui.theme.PurplePrimary

import com.example.ads.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    savedArtworks: Map<String, SavedArtwork>,
    outlineColor: Long,
    isDesignUnlocked: (String) -> Boolean,
    onSelectMandala: (String) -> Unit,
    onRequestUnlockBonus: (String) -> Unit,
    onNavigateToGallery: (MandalaCategory?) -> Unit,
    onNavigateToMyArt: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredDesign = remember { MandalaLibrary.designs.first() } // Lotus Bloom
    val today = java.time.LocalDate.now()
    val dailyInspiration = remember(today) {
        MandalaLibrary.dailyInspiration(today)
    }

    val categories = listOf(
        MandalaCategory.EASY,
        MandalaCategory.MEDIUM,
        MandalaCategory.HARD,
        MandalaCategory.FLORAL,
        MandalaCategory.GEOMETRIC,
        MandalaCategory.INTRICATE
    )

    var selectedCategoryFilter by remember { mutableStateOf(MandalaCategory.FLORAL) }
    val filteredDesigns = remember(selectedCategoryFilter) {
        MandalaLibrary.designs.filter {
            it.category == selectedCategoryFilter ||
            (selectedCategoryFilter == MandalaCategory.EASY && it.difficulty == com.example.model.Difficulty.EASY) ||
            (selectedCategoryFilter == MandalaCategory.MEDIUM && it.difficulty == com.example.model.Difficulty.MEDIUM) ||
            (selectedCategoryFilter == MandalaCategory.HARD && it.difficulty == com.example.model.Difficulty.HARD)
        }
    }

    val heroGradient = Brush.linearGradient(
        listOf(PopGradientStart, PopGradientMid, PopGradientEnd)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 700.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header: Logo, Title, Settings Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo bubble with candy gradient
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PopGradientStart, PopGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.testTag("btn_home_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.home_settings),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Featured Hero Card: bold candy gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .clickable { onSelectMandala(featuredDesign.id) }
                    .testTag("hero_featured_card"),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroGradient)
                        .padding(22.dp)
                ) {
                    Column {
                        // Pill badge
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Featured Artwork",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = featuredDesign.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "A calming sacred flower pattern with ${featuredDesign.regionCount} harmonious petals.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Play Now pill button
                        Button(
                            onClick = { onSelectMandala(featuredDesign.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = PurplePrimary
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_hero_play_now")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.home_play_now),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Nav Shortcuts: All Mandalas Gallery & My Saved Art
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigateToGallery(null) }
                        .testTag("btn_explore_gallery"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PopGradientStart, PopGradientMid)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "All Mandalas",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${MandalaLibrary.designs.size} Designs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigateToMyArt() }
                        .testTag("btn_nav_my_art"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PopGradientMid, PopGradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.home_my_art),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            val count = savedArtworks.size
                            Text(
                                text = if (count == 1) "1 Artwork" else "$count Artworks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Inspiration section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.home_daily_inspiration),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tap to color",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            MandalaCard(
                design = dailyInspiration,
                savedArtwork = savedArtworks[dailyInspiration.id],
                outlineColor = outlineColor,
                onSelect = { onSelectMandala(dailyInspiration.id) },
                onToggleFavorite = { isFav ->
                    onToggleFavorite(dailyInspiration.id, isFav)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Categories Filter Section
            Text(
                text = stringResource(R.string.home_categories),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = {
                            Text(
                                text = cat.title,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.testTag("home_category_chip_${cat.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Carousel of filtered category items
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredDesigns, key = { it.id }) { design ->
                    val isLocked = !isDesignUnlocked(design.id)
                    Box(modifier = Modifier.width(220.dp)) {
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

            Spacer(modifier = Modifier.height(20.dp))

            // Adaptive Banner Ad on Home Screen
            BannerAdView(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
