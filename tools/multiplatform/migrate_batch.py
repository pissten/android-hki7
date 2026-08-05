#!/usr/bin/env python3
"""Deterministic source moves for the HKI 7 Compose Multiplatform port.

The script is intentionally idempotent. It moves canonical Android sources into sharedUi and
applies only mechanical platform-neutral transformations. A dedicated GitHub Actions workflow
builds both Wasm and Android before committing the result.
"""

from __future__ import annotations

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "app/src/main/java/com/jimz011apps/hki7"
SHARED = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7"


def move_text(source: Path, destination: Path) -> str:
    if destination.exists():
        return destination.read_text(encoding="utf-8")
    if not source.exists():
        raise FileNotFoundError(f"Neither source nor destination exists: {source} -> {destination}")
    destination.parent.mkdir(parents=True, exist_ok=True)
    text = source.read_text(encoding="utf-8")
    destination.write_text(text, encoding="utf-8")
    source.unlink()
    return text


def write_if_changed(path: Path, text: str) -> None:
    current = path.read_text(encoding="utf-8") if path.exists() else None
    if current != text:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")


def make_public(text: str) -> str:
    # Android callers live in another Gradle module after the move, so module-internal declarations
    # that form part of the existing UI contract must become public.
    return re.sub(r"(?m)^internal\s+", "", text)


def replace_balanced_lambda(text: str, marker: str, replacement: str) -> str:
    start = text.find(marker)
    if start < 0:
        return text
    opening = text.find("{", start)
    if opening < 0:
        raise ValueError(f"No opening brace after marker: {marker}")
    depth = 0
    in_string = False
    escaped = False
    for index in range(opening, len(text)):
        char = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == '"':
            in_string = True
        elif char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return text[:start] + replacement + text[index + 1 :]
    raise ValueError(f"Unbalanced lambda after marker: {marker}")


def migrate_room_status_state() -> None:
    source = APP / "ui/RoomStatusState.kt"
    destination = SHARED / "ui/RoomStatusState.kt"
    text = move_text(source, destination)
    text = text.replace("import java.util.Locale\n", "")
    text = text.replace(".lowercase(Locale.ROOT)", ".lowercase()")
    text = text.replace(
        'val compactValue = String.format(Locale.US, "%.2f", value)\n'
        "        .trimEnd('0')\n"
        "        .trimEnd('.')",
        "val compactValue = (kotlin.math.round(value * 100.0) / 100.0).toString()\n"
        "        .trimEnd('0')\n"
        "        .trimEnd('.')",
    )
    text = make_public(text)
    write_if_changed(destination, text)


def migrate_room_follow_state() -> None:
    source = APP / "ui/RoomFollowState.kt"
    destination = SHARED / "ui/RoomFollowState.kt"
    text = move_text(source, destination)
    text = text.replace("import java.util.Locale\n", "")
    text = text.replace(".lowercase(Locale.ROOT)", ".lowercase()")
    text = make_public(text)
    write_if_changed(destination, text)


def migrate_room_media_state() -> None:
    source = APP / "ui/RoomMediaStatus.kt"
    destination = SHARED / "ui/RoomMediaStatus.kt"
    text = move_text(source, destination)
    for unwanted_import in (
        "import androidx.compose.runtime.Composable\n",
        "import androidx.compose.ui.res.pluralStringResource\n",
        "import androidx.compose.ui.res.stringResource\n",
        "import com.jimz011apps.hki7.R\n",
        "import com.jimz011apps.hki7.ui.components.mediaPlayerStatus\n",
    ):
        text = text.replace(unwanted_import, "")
    text = re.sub(
        r"/\*\* Localizes the presentation-neutral room media result at the Compose boundary\. \*/\n"
        r"@Composable\ninternal fun RoomMediaSummary\.localizedText\(\): String\? = when \{.*?\n\}\n\n",
        "",
        text,
        flags=re.DOTALL,
    )
    text = make_public(text)
    write_if_changed(destination, text)

    # The resource-backed presentation boundary remains Android-specific while the aggregation and
    # state logic now have a single canonical implementation in commonMain.
    android_wrapper = """package com.jimz011apps.hki7.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.jimz011apps.hki7.R
import com.jimz011apps.hki7.ui.components.mediaPlayerStatus

@Composable
internal fun RoomMediaSummary.localizedText(): String? = when {
    text == null -> null
    activeCount == 0 -> stringResource(R.string.core_no_media_playing)
    activeCount > 1 -> pluralStringResource(
        R.plurals.core_media_players_playing,
        activeCount,
        activeCount
    )
    else -> mediaPlayerStatus(representative)
}
"""
    write_if_changed(source, android_wrapper)


def migrate_android_navigation_items() -> None:
    path = APP / "MainActivity.kt"
    text = path.read_text(encoding="utf-8")
    import_line = "import com.jimz011apps.hki7.ui.components.HKITopLevelNavigationItems\n"
    anchor = "import com.jimz011apps.hki7.ui.components.HKIBottomBar\n"
    if import_line not in text:
        text = text.replace(anchor, anchor + import_line)

    replacement = """                HKITopLevelNavigationItems(
                    screens = screens,
                    isSelected = { screen ->
                        when (screen) {
                            is Screen.Custom ->
                                currentDestination?.route == Screen.CUSTOM_PAGE_ROUTE &&
                                    navBackStackEntry?.arguments?.getString(\"pageId\") == screen.page.id
                            Screen.Rooms ->
                                currentDestination?.route == Screen.RoomDetail.route ||
                                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            Screen.Battery ->
                                currentDestination?.route == Screen.Battery.WIDGET_ROUTE ||
                                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            else -> currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        }
                    },
                    onSelect = navigateToTopLevel,
                    labelFor = { screen -> screen.localizedTitle() },
                    scrollable = navBarScrollable,
                )"""
    text = replace_balanced_lambda(
        text,
        "                screens.forEach { screen ->",
        replacement,
    )
    write_if_changed(path, text)


def main() -> None:
    migrate_room_status_state()
    migrate_room_follow_state()
    migrate_room_media_state()
    migrate_android_navigation_items()
    print("Migrated validated room logic and canonical top-level navigation rendering to sharedUi")


if __name__ == "__main__":
    main()
