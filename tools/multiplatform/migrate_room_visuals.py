#!/usr/bin/env python3
"""Move original room-card visual components into Compose commonMain without redesigning them."""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "app/src/main/java/com/jimz011apps/hki7"
SHARED = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7"


def move_and_transform(source: Path, destination: Path) -> str:
    if destination.exists():
        text = destination.read_text(encoding="utf-8")
    elif source.exists():
        destination.parent.mkdir(parents=True, exist_ok=True)
        text = source.read_text(encoding="utf-8")
        source.unlink()
    else:
        raise FileNotFoundError(f"Missing source and destination: {source} -> {destination}")
    return text


def write_if_changed(path: Path, text: str) -> None:
    current = path.read_text(encoding="utf-8") if path.exists() else None
    if current != text:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")


def remove_balanced_declaration(text: str, marker: str) -> str:
    start = text.find(marker)
    if start < 0:
        return text
    opening = text.find("{", start)
    if opening < 0:
        raise ValueError(f"No body found for declaration: {marker}")
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
                return text[:start] + text[index + 1 :].lstrip("\n")
    raise ValueError(f"Unbalanced declaration: {marker}")


def migrate_localized_labels() -> None:
    source = APP / "ui/LocalizedLabels.kt"
    destination = SHARED / "ui/LocalizedLabels.kt"
    text = move_and_transform(source, destination)
    for line in (
        "import androidx.compose.ui.res.stringResource\n",
        "import com.jimz011apps.hki7.R\n",
        "import java.text.NumberFormat\n",
        "import java.util.Locale\n",
    ):
        text = text.replace(line, "")
    imports = (
        "import org.jetbrains.compose.resources.stringResource\n"
        "import com.jimz011apps.hki7.resources.Res\n"
        "import com.jimz011apps.hki7.resources.*\n"
    )
    anchor = "import androidx.compose.runtime.Composable\n"
    if "org.jetbrains.compose.resources.stringResource" not in text:
        text = text.replace(anchor, anchor + imports)
    text = text.replace("R.string.", "Res.string.")
    text = text.replace(".lowercase(Locale.ROOT)", ".lowercase()")
    text = re.sub(
        r"\s*val formatter = NumberFormat\.getNumberInstance\(\)\.apply \{\n"
        r"\s*maximumFractionDigits = 1\n"
        r"\s*minimumFractionDigits = 0\n"
        r"\s*\}\n"
        r"\s*return listOfNotNull\(formatter\.format\(value\), unit\?\.takeIf\(String::isNotBlank\)\)\n"
        r"\s*\.joinToString\(\" \"\)",
        "\n        val compactValue = (kotlin.math.round(value * 10.0) / 10.0).toString()\n"
        "            .trimEnd('0')\n"
        "            .trimEnd('.')\n"
        "        return listOfNotNull(compactValue, unit?.takeIf(String::isNotBlank))\n"
        "            .joinToString(\" \")",
        text,
    )
    write_if_changed(destination, text)


def migrate_room_status_summary() -> None:
    source = APP / "ui/components/RoomStatusSummary.kt"
    destination = SHARED / "ui/components/RoomStatusSummary.kt"
    text = move_and_transform(source, destination)

    text = text.replace("import com.jimz011apps.hki7.R\n\n", "")
    text = text.replace("import androidx.compose.ui.res.pluralStringResource\n", "")
    text = text.replace("import androidx.compose.ui.res.stringResource\n", "")
    text = text.replace(
        "import com.jimz011apps.hki7.ui.theme.LocalHKIAppColors\n",
        "import com.jimz011apps.hki7.sharedui.LocalHKIAppColors\n",
    )

    resource_imports = (
        "import org.jetbrains.compose.resources.pluralStringResource\n"
        "import org.jetbrains.compose.resources.stringResource\n"
        "import com.jimz011apps.hki7.resources.Res\n"
        "import com.jimz011apps.hki7.resources.*\n"
    )
    anchor = "import androidx.compose.ui.unit.dp\n"
    if "org.jetbrains.compose.resources.pluralStringResource" not in text:
        text = text.replace(anchor, anchor + resource_imports)

    text = text.replace("R.string.", "Res.string.")
    text = text.replace("R.plurals.", "Res.plurals.")
    text = re.sub(r"(?m)^internal\s+", "", text)
    write_if_changed(destination, text)


def remove_shared_helpers_from_android_entity_card() -> None:
    path = APP / "ui/components/EntityCard.kt"
    text = path.read_text(encoding="utf-8")
    for marker in (
        "@Composable\nfun EditRemoveBadge(",
        "@Composable\nfun EditSettingsButton(",
        "@Composable\nfun mediaPlayerStatus(",
        "fun mediaPlayerStateIcon(",
    ):
        text = remove_balanced_declaration(text, marker)
    write_if_changed(path, text)


def remove_android_room_media_wrapper() -> None:
    # migrate_batch.py creates this temporary Android resource wrapper for earlier batches. The
    # canonical localized implementation now lives in commonMain, so the wrapper must not coexist.
    wrapper = APP / "ui/RoomMediaStatus.kt"
    if wrapper.exists() and (SHARED / "ui/RoomMediaStatus.kt").exists():
        wrapper.unlink()


def main() -> None:
    migrate_localized_labels()
    migrate_room_status_summary()
    remove_shared_helpers_from_android_entity_card()
    remove_android_room_media_wrapper()
    print(
        "Moved original localized HA labels, room status visuals, edit badges, and media helpers "
        "into sharedUi/commonMain"
    )


if __name__ == "__main__":
    main()
