package com.jimz011apps.hki7.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.data.HAArea
import com.jimz011apps.hki7.data.HAEntity
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors
import com.jimz011apps.hki7.ui.components.HKIHeaderStatusPill
import com.jimz011apps.hki7.ui.components.HKISharedPage

/**
 * Platform-neutral first extraction of HKI 7's room-detail route.
 *
 * Navigation, page chrome and live entity inspection are shared by Android and web. The richer
 * Android entity controls will be moved into this surface incrementally rather than recreated in
 * browser-only code.
 */
@Composable
fun HKIRoomDetailSurface(
    area: HAArea,
    entities: List<HAEntity>,
    baseUrl: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedEntity by remember(entities) { mutableStateOf<HAEntity?>(null) }
    val sortedEntities = remember(entities) {
        entities.sortedWith(
            compareBy<HAEntity> { it.friendlyName?.lowercase() ?: it.entity_id.lowercase() }
                .thenBy { it.entity_id },
        )
    }
    val backgroundImageUrl = remember(area.picture, baseUrl) {
        area.picture?.let { picture ->
            if (picture.startsWith("http://") || picture.startsWith("https://")) {
                picture
            } else {
                "${baseUrl.removeSuffix("/")}/${picture.removePrefix("/")}"
            }
        }
    }

    HKISharedPage(
        title = area.name,
        subtitle = "${entities.size} entities",
        modifier = modifier,
        backgroundImageUrl = backgroundImageUrl,
        leftPill = {
            HKIHeaderStatusPill(
                text = "‹ Rooms",
                modifier = Modifier.clickable(onClick = onBack),
            )
        },
        rightPill = {
            HKIHeaderStatusPill(text = "Home Assistant")
        },
    ) {
        if (sortedEntities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No entities are assigned to this room.",
                    color = LocalHKIAppColors.current.onMuted,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 32.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = sortedEntities,
                    key = HAEntity::entity_id,
                ) { entity ->
                    HKIRoomDetailEntityCard(
                        entity = entity,
                        onClick = { selectedEntity = entity },
                    )
                }
            }
        }
    }

    selectedEntity?.let { entity ->
        AlertDialog(
            onDismissRequest = { selectedEntity = null },
            title = {
                Text(entity.friendlyName ?: entity.entity_id.substringAfter('.'))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = entity.state.replace('_', ' '),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = entity.entity_id,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalHKIAppColors.current.onMuted,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEntity = null }) {
                    Text("Close")
                }
            },
        )
    }
}

@Composable
private fun HKIRoomDetailEntityCard(
    entity: HAEntity,
    onClick: () -> Unit,
) {
    val appColors = LocalHKIAppColors.current
    val unavailable = entity.state.equals("unavailable", ignoreCase = true) ||
        entity.state.equals("unknown", ignoreCase = true)
    val displayName = entity.friendlyName ?: entity.entity_id.substringAfter('.')
    val domain = entity.entity_id.substringBefore('.').replace('_', ' ')

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        appColors.elevated.copy(alpha = 0.98f),
                        appColors.background.copy(alpha = 0.94f),
                    ),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(appColors.background.copy(alpha = 0.56f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = appColors.onMuted,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.onMuted,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.padding(top = 14.dp))
            Text(
                text = entity.state.replace('_', ' '),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (unavailable) MaterialTheme.colorScheme.error else appColors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
