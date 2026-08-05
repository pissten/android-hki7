#!/usr/bin/env python3
"""Small idempotent source fixes discovered by Android/Wasm cross-compilation."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SHARED = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7"


def add_import(path: Path, anchor: str, import_line: str) -> None:
    text = path.read_text(encoding="utf-8")
    if import_line not in text:
        if anchor not in text:
            raise ValueError(f"Import anchor not found in {path}: {anchor}")
        text = text.replace(anchor, anchor + import_line, 1)
        path.write_text(text, encoding="utf-8")


def main() -> None:
    add_import(
        SHARED / "ui/screens/HKIRoomsSurface.kt",
        "import androidx.compose.foundation.layout.width\n",
        "import androidx.compose.foundation.layout.weight\n",
    )
    add_import(
        SHARED / "ui/components/HKIAreaCard.kt",
        "import com.jimz011apps.hki7.ui.displayedRoomControlEntityIds\n",
        "import com.jimz011apps.hki7.ui.localizedText\n",
    )
    print("Applied common source imports required by Android and Wasm")


if __name__ == "__main__":
    main()
