package com.jimz011apps.hki7.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jimz011apps.hki7.data.HKIRoomWidget

/**
 * Canonical HKI 7 dashboard grid extracted from the original HomeScreen.
 *
 * Every responsive dashboard column consists of six cells: full widgets span six, half widgets
 * span three, and third-width widgets span two. Android and web inject the same existing widget
 * composables through [content]; this component owns the layout rather than recreating it per host.
 */
@Composable
fun HKIHomeWidgetGrid(
    widgets: List<HKIRoomWidget>,
    dashboardColumnCount: Int,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    content: @Composable (HKIRoomWidget) -> Unit,
) {
    val gridColumns = dashboardColumnCount.coerceAtLeast(1) * CELLS_PER_DASHBOARD_COLUMN

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(
            count = widgets.size,
            key = { index -> widgets[index].id },
            contentType = { index -> widgets[index]::class.simpleName ?: "widget" },
            span = { index -> GridItemSpan(widgetSpanCells(widgets[index])) },
        ) { index ->
            content(widgets[index])
        }
    }
}

fun widgetSpanCells(widget: HKIRoomWidget): Int = when (widget.width) {
    "third" -> 2
    "half" -> 3
    else -> CELLS_PER_DASHBOARD_COLUMN
}

private const val CELLS_PER_DASHBOARD_COLUMN = 6
