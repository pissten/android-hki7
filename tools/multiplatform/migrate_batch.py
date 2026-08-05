#!/usr/bin/env python3
"""Deterministic source moves for the HKI 7 Compose Multiplatform port.

The script is intentionally idempotent. It moves canonical Android sources into sharedUi and
applies only mechanical platform-neutral transformations. A dedicated GitHub Actions workflow
builds both Wasm and Android before committing the result.
"""

from __future__ import annotations

from pathlib import Path
import re
import shutil

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


def migrate_room_status_state() -> None:
    source = APP / "ui/RoomStatusState.kt"
    destination = SHARED / "ui/RoomStatusState.kt"
    text = move_text(source, destination)
    text = text.replace("import java.util.Locale\n", "")
    text = text.replace(".lowercase(Locale.ROOT)", ".lowercase()")
    text = make_public(text)
    write_if_changed(destination, text)


def main() -> None:
    migrate_room_status_state()
    print("Migrated canonical room-status state logic to sharedUi/commonMain")


if __name__ == "__main__":
    main()
