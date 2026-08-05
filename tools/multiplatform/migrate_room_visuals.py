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


def main() -> None:
    migrate_room_status_summary()
    print("Moved original room status indicators and environment summary into sharedUi/commonMain")


if __name__ == "__main__":
    main()
