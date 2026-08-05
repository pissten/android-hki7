#!/usr/bin/env python3
"""Deterministic source moves for the HKI 7 Compose Multiplatform port.

The script is intentionally idempotent. It moves canonical Android sources into sharedUi and
applies only mechanical platform-neutral transformations. A dedicated GitHub Actions workflow
builds both Wasm and Android before committing the result.
"""

from __future__ import annotations

from copy import deepcopy
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "app/src/main/java/com/jimz011apps/hki7"
SHARED = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7"
ANDROID_RES = ROOT / "app/src/main/res"
SHARED_RES = ROOT / "sharedUi/src/commonMain/composeResources"


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


def sync_common_string_resources() -> None:
    """Make every original base string/plural available to common Compose UI.

    Android keeps its resource set unchanged. commonMain receives the same named resources so a
    screen can be moved without replacing labels or hard-coding a web-specific copy.
    """
    source = ANDROID_RES / "values/strings.xml"
    destination = SHARED_RES / "values/strings.xml"
    ET.register_namespace("xliff", "urn:oasis:names:tc:xliff:document:1.2")
    source_root = ET.parse(source).getroot()
    destination_tree = ET.parse(destination)
    destination_root = destination_tree.getroot()

    supported_tags = {"string", "plurals"}
    existing = {
        (child.tag.split("}")[-1], child.attrib.get("name"))
        for child in destination_root
        if child.attrib.get("name")
    }
    added = 0
    for child in source_root:
        local_tag = child.tag.split("}")[-1]
        name = child.attrib.get("name")
        key = (local_tag, name)
        if local_tag not in supported_tags or not name or key in existing:
            continue
        destination_root.append(deepcopy(child))
        existing.add(key)
        added += 1

    if added:
        ET.indent(destination_tree, space="    ")
        destination_tree.write(destination, encoding="unicode", xml_declaration=False)
        with destination.open("a", encoding="utf-8") as output:
            output.write("\n")
    print(f"Synced {added} original string/plural resources into commonMain")


def migrate_navigation_labels() -> None:
    shared_path = SHARED / "ui/Navigation.kt"
    text = shared_path.read_text(encoding="utf-8")
    if "fun Screen.localizedTitle()" not in text:
        import_anchor = "import androidx.compose.material.icons.Icons\n"
        common_imports = (
            "import androidx.compose.runtime.Composable\n"
            "import org.jetbrains.compose.resources.stringResource\n"
            "import com.jimz011apps.hki7.resources.*\n"
        )
        text = text.replace(import_anchor, common_imports + import_anchor)
        text = text.rstrip() + """


/** Resource-backed labels for the canonical navigation model, shared by Android and web. */
@Composable
fun Screen.localizedTitle(): String = when (this) {
    Screen.Home -> stringResource(Res.string.nav_home)
    Screen.Rooms -> stringResource(Res.string.nav_rooms)
    Screen.Security -> stringResource(Res.string.nav_security)
    Screen.Energy -> stringResource(Res.string.nav_energy)
    Screen.Climate -> stringResource(Res.string.nav_climate)
    Screen.Battery -> stringResource(Res.string.nav_battery)
    Screen.Settings -> stringResource(Res.string.nav_settings)
    Screen.RoomDetail -> stringResource(Res.string.nav_room_detail)
    is Screen.Custom -> page.name
}
"""
        write_if_changed(shared_path, text)

    android_path = APP / "ui/Navigation.kt"
    if android_path.exists():
        android_path.unlink()


def main() -> None:
    migrate_room_status_state()
    migrate_room_follow_state()
    migrate_room_media_state()
    migrate_android_navigation_items()
    sync_common_string_resources()
    migrate_navigation_labels()
    print("Migrated validated room logic, navigation rendering/labels, and original UI resources to sharedUi")


if __name__ == "__main__":
    main()
