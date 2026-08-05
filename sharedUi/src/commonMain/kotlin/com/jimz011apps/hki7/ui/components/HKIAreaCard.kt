package com.jimz011apps.hki7.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jimz011apps.hki7.data.HAArea
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.data.HKIAreaConfig
import com.jimz011apps.hki7.data.HKIRoomWidget
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.ui.RoomStatusRoles
import com.jimz011apps.hki7.ui.displayedRoomControlEntityIds
import com.jimz011apps.hki7.ui.localizedText
import com.jimz011apps.hki7.ui.resolveRoomMediaStatus
import com.jimz011apps.hki7.ui.resolveRoomStatus
import com.jimz011apps.hki7.ui.roomMediaPlayerIds
import com.jimz011apps.hki7.ui.utils.MdiIcon

/**
 * The original HKI 7 room tile extracted from RoomsScreen.
 *
 * Rendering and interaction stay in commonMain. Platform hosts only resolve the live entity list and
 * number of people in the room, which removes MainViewModel from the visual component without
 * creating a browser-specific copy.
 */
@Composable
fun HKIAreaCard(
    area: HAArea,
    config: HKIAreaConfig,
    widgets: List<HKIRoomWidget>,
    roomEntities: List<HAEntity>,
    peopleHere: Int,
    baseUrl: String,
    isEditMode: Boolean,
    canDelete: Boolean,
    isDragging: Boolean,
    isSquare: Boolean = false,
    compactTiles: Boolean = true,
    cornerRadius: Int = LocalItemCornerRadius.current,
    onDelete: () -> Unit,
    onSettings: () -> Unit,
    onClick: () -> Unit,
    onActivityClick: ((String, List<String>) -> Unit)? = null,
) {
    val appColors = LocalHKIAppColors.current
    val headerColor = remember(config.headerColor) { parseRoomHeaderColor(config.headerColor) }
    // Match HKIPage: a custom header color takes precedence over the room wallpaper/picture.
    val imageSource = if (headerColor != null) null else (config.wallpaper ?: area.picture)?.takeIf(String::isNotBlank)
    val imageUrl = imageSource?.let { if (it.startsWith("http")) it else "$baseUrl$it" }
    val roomColorScheme = MaterialTheme.colorScheme
    val generatedRoomColor = remember(
        area.area_id,
        appColors.background,
        roomColorScheme.primary,
        roomColorScheme.secondary,
        roomColorScheme.tertiary,
        roomColorScheme.primaryContainer,
        roomColorScheme.secondaryContainer,
        roomColorScheme.tertiaryContainer,
    ) {
        val palette = listOf(
            roomColorScheme.primary,
            roomColorScheme.secondary,
            roomColorScheme.tertiary,
            roomColorScheme.primaryContainer,
            roomColorScheme.secondaryContainer,
            roomColorScheme.tertiaryContainer,
        )
        val index = ((area.area_id.hashCode() % palette.size) + palette.size) % palette.size
        palette[index]
    }
    val roomAccentColor = headerColor ?: generatedRoomColor
    val roomCardColor = roomAccentColor.copy(
        alpha = if (appColors.background.luminance() < 0.5f) {
            0.45f
        } else if (roomAccentColor.luminance() < 0.35f) {
            0.28f
        } else {
            0.18f
        },
    )
    val roomCardBrush = roomCardColor.let { color ->
        Brush.verticalGradient(
            listOf(
                color.compositeOver(appColors.background),
                color.copy(alpha = color.alpha * 0.45f).compositeOver(appColors.background),
                appColors.background,
            ),
        )
    }
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "room scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSquare) Modifier.aspectRatio(1f) else Modifier.height(if (compactTiles) 112.dp else 160.dp))
            .scale(scale),
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = !isEditMode) { onClick() },
            shape = RoundedCornerShape(cornerRadius.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.elevated),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.28f)))
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(roomCardBrush),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (config.icon != "None") {
                            MdiIcon(config.icon ?: area.icon, tint = appColors.onMuted, size = 48.dp)
                        }
                    }
                }

                val mediaPlayerIds = remember(config) { config.roomMediaPlayerIds() }
                val mediaPlayers = remember(mediaPlayerIds, roomEntities) {
                    val byId = roomEntities.associateBy(HAEntity::entity_id)
                    mediaPlayerIds.map { id -> byId[id] ?: HAEntity(entity_id = id, state = "unavailable") }
                }
                val mediaSummary = remember(mediaPlayers) { resolveRoomMediaStatus(mediaPlayers) }
                val mediaStatus = mediaSummary.localizedText()
                val mediaIcon = mediaPlayerStateIcon(mediaSummary.representative)
                val displayedControlIds = remember(widgets) { displayedRoomControlEntityIds(widgets) }
                val roomSummary = remember(config, roomEntities, displayedControlIds, peopleHere) {
                    resolveRoomStatus(config, roomEntities, displayedControlIds, peopleHere)
                }
                val topIndicatorKinds = if (isEditMode) 0 else roomSummary.indicators.count {
                    it.role in ROOM_CARD_TOP_STATUS_ROLES
                }
                val bottomIndicatorKinds = if (isEditMode) 0 else roomSummary.indicators.count {
                    it.role in ROOM_CARD_BOTTOM_STATUS_ROLES
                }
                val topIndicatorPadding = when (topIndicatorKinds) {
                    0 -> 0.dp
                    1 -> 42.dp
                    2 -> 76.dp
                    else -> 112.dp
                }
                val bottomIndicatorPadding = when (bottomIndicatorKinds) {
                    0 -> 8.dp
                    1 -> 42.dp
                    2 -> 76.dp
                    else -> 116.dp
                }
                val primaryColor = if (imageUrl != null) Color.White else appColors.onSurface
                val secondaryColor = if (imageUrl != null) Color.White.copy(alpha = 0.88f) else appColors.onMuted

                Box(
                    modifier = Modifier.fillMaxSize().padding(if (compactTiles) 12.dp else 16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .padding(end = topIndicatorPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (config.icon != "None") {
                            Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.35f)) {
                                MdiIcon(
                                    config.icon ?: area.icon,
                                    modifier = Modifier.padding(if (compactTiles) 6.dp else 8.dp),
                                    tint = Color.White,
                                    size = if (compactTiles) 16.dp else 18.dp,
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            config.name ?: area.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (!isEditMode) {
                        RoomStatusIndicators(
                            summary = roomSummary,
                            compact = true,
                            visibleRoles = ROOM_CARD_TOP_STATUS_ROLES,
                            onIndicatorClick = onActivityClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .widthIn(max = 108.dp),
                        )

                        RoomStatusIndicators(
                            summary = roomSummary,
                            compact = true,
                            visibleRoles = ROOM_CARD_BOTTOM_STATUS_ROLES,
                            onIndicatorClick = onActivityClick,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .widthIn(max = 108.dp),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(end = bottomIndicatorPadding),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                    ) {
                        if (mediaStatus != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (mediaIcon != null) {
                                    Icon(
                                        imageVector = mediaIcon,
                                        contentDescription = null,
                                        tint = secondaryColor,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(Modifier.width(5.dp))
                                }
                                Text(
                                    text = mediaStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        RoomEnvironmentSummary(
                            summary = roomSummary,
                            color = primaryColor,
                            compact = true,
                        )
                    }
                }

                if (isEditMode) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                }
            }
        }
        if (isEditMode) {
            EditSettingsButton(onClick = onSettings, modifier = Modifier.align(Alignment.Center))
            if (canDelete) {
                EditRemoveBadge(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

private fun parseRoomHeaderColor(value: String?): Color? {
    val hex = value?.trim()?.removePrefix("#")?.takeIf { it.length == 6 || it.length == 8 } ?: return null
    return runCatching {
        val argb = when (hex.length) {
            6 -> (0xFF000000uL or hex.toULong(16)).toLong()
            else -> hex.toULong(16).toLong()
        }
        Color(argb)
    }.getOrNull()
}

private val ROOM_CARD_TOP_STATUS_ROLES = setOf(
    RoomStatusRoles.DOORS,
    RoomStatusRoles.WINDOWS,
    RoomStatusRoles.LIGHTS,
    RoomStatusRoles.DEVICES,
)

private val ROOM_CARD_BOTTOM_STATUS_ROLES = setOf(
    RoomStatusRoles.MOTION,
    RoomStatusRoles.PRESENCE,
    RoomStatusRoles.PEOPLE,
    RoomStatusRoles.SMOKE,
    RoomStatusRoles.GAS,
    RoomStatusRoles.FIRE,
)
