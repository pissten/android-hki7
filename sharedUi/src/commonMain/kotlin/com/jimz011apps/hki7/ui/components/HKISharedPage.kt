package com.jimz011apps.hki7.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jimz011apps.hki7.sharedui.LocalHKIAppColors

/**
 * Platform-neutral visual shell extracted from the original Android HKIPage.
 *
 * The geometry and color treatment deliberately mirror the Android page header: a 236 dp hero,
 * three-stage fade into the page background, 24 dp horizontal insets, 40/44 sp title typography,
 * two translucent top pills and a responsive trailing-status column. Android-specific gesture,
 * window and navigation behaviour stays in the Android host.
 */
@Composable
fun HKISharedPage(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    backgroundImageUrl: String? = null,
    headerColor: String? = null,
    leftPill: (@Composable () -> Unit)? = null,
    rightPill: (@Composable () -> Unit)? = null,
    headerTrailingContent: (@Composable (Color) -> Unit)? = null,
    headerBottomContent: (@Composable (Color) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val appColors = LocalHKIAppColors.current
    val parsedHeaderColor = remember(headerColor) { parseSharedHeaderColor(headerColor) }
    val darkAppearance = appColors.background.luminance() < 0.5f
    val customHeaderStart = parsedHeaderColor?.copy(
        alpha = if (darkAppearance) {
            0.45f
        } else if (parsedHeaderColor.luminance() < 0.35f) {
            0.28f
        } else {
            0.18f
        },
    )
    val headerBackdrop = customHeaderStart?.compositeOver(appColors.background)
    val headerTextColor = when {
        backgroundImageUrl != null -> Color.White
        headerBackdrop != null -> if (headerBackdrop.luminance() < 0.5f) Color.White else Color(0xFF1C1B1F)
        else -> appColors.onSurface
    }
    val headerMutedColor = when {
        backgroundImageUrl != null -> Color.White.copy(alpha = 0.80f)
        headerBackdrop != null -> headerTextColor.copy(alpha = 0.75f)
        else -> appColors.onMuted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HKI_PAGE_HEADER_HEIGHT)
                .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)),
        ) {
            if (backgroundImageUrl != null) {
                AsyncImage(
                    model = backgroundImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.42f),
                                    0.5f to Color.Black.copy(alpha = 0.19f),
                                    1.0f to Color.Transparent,
                                ),
                            ),
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                when {
                                    backgroundImageUrl != null -> Color.Black.copy(alpha = 0.30f)
                                    customHeaderStart != null -> customHeaderStart
                                    else -> appColors.headerFallbackStart
                                },
                                when {
                                    backgroundImageUrl != null -> Color.Black.copy(alpha = 0.135f)
                                    customHeaderStart != null -> customHeaderStart.copy(
                                        alpha = customHeaderStart.alpha * 0.45f,
                                    )
                                    else -> appColors.headerFallbackStart.copy(
                                        alpha = appColors.headerFallbackStart.alpha * 0.45f,
                                    )
                                },
                                appColors.background,
                            ),
                        ),
                    )
                    .padding(horizontal = 24.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(contentAlignment = Alignment.CenterStart) {
                            leftPill?.invoke()
                        }
                        Box(contentAlignment = Alignment.CenterEnd) {
                            rightPill?.invoke()
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val trailingWidth = when {
                            maxWidth >= 400.dp -> 180.dp
                            maxWidth >= 280.dp -> 112.dp
                            else -> 53.dp
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = if (headerTrailingContent != null) 12.dp else 0.dp),
                            ) {
                                Text(
                                    text = title,
                                    color = headerTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 40.sp,
                                    lineHeight = 44.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = headerMutedColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (headerBottomContent != null) {
                                    Spacer(Modifier.height(6.dp))
                                    headerBottomContent(headerMutedColor)
                                }
                            }
                            if (headerTrailingContent != null) {
                                Box(
                                    modifier = Modifier
                                        .width(trailingWidth)
                                        .padding(top = 4.dp),
                                    contentAlignment = Alignment.TopEnd,
                                ) {
                                    headerTrailingContent(headerTextColor)
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            content(PaddingValues(0.dp))
        }
    }
}

/** Same translucent 36 dp status-pill family used at the top of Android HKIPage. */
@Composable
fun HKIHeaderStatusPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalHKIAppColors.current
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        appColors.elevated.copy(alpha = 0.96f),
                        appColors.elevated.copy(alpha = 0.76f),
                    ),
                ),
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = appColors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun parseSharedHeaderColor(value: String?): Color? {
    val raw = value?.trim()?.removePrefix("#") ?: return null
    val argb = when (raw.length) {
        6 -> "FF$raw"
        8 -> raw
        else -> return null
    }
    return argb.toULongOrNull(16)?.let(::Color)
}

private val HKI_PAGE_HEADER_HEIGHT = 236.dp
