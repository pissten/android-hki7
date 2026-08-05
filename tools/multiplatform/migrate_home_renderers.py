#!/usr/bin/env python3
"""Move validated production Home/Room widget renderers from Android into sharedUi."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ROOM_DETAIL = ROOT / "app/src/main/java/com/jimz011apps/hki7/ui/screens/RoomDetailScreen.kt"
ENTITY_CARD = ROOT / "app/src/main/java/com/jimz011apps/hki7/ui/components/EntityCard.kt"
SHARED_SUBTITLE = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/ui/screens/SubtitleWidget.kt"
SHARED_SPACER = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/ui/components/SpacerButtonCard.kt"


def remove_balanced_declaration(text: str, marker: str) -> str:
    start = text.find(marker)
    if start < 0:
        return text
    opening = text.find("{", start)
    if opening < 0:
        raise RuntimeError(f"No function body found for {marker}")

    depth = 0
    in_string = False
    escaped = False
    for index in range(opening, len(text)):
        character = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == '"':
                in_string = False
            continue
        if character == '"':
            in_string = True
        elif character == "{":
            depth += 1
        elif character == "}":
            depth -= 1
            if depth == 0:
                return text[:start] + text[index + 1 :].lstrip("\n")
    raise RuntimeError(f"Unbalanced function body for {marker}")


def migrate_function(source: Path, shared: Path, marker: str, label: str) -> None:
    if not shared.exists():
        raise RuntimeError(f"Canonical shared {label} is missing")
    text = source.read_text(encoding="utf-8")
    updated = remove_balanced_declaration(text, marker)
    if updated != text:
        source.write_text(updated, encoding="utf-8")
        print(f"Removed Android-local {label}; callers now resolve sharedUi implementation")
    else:
        print(f"Android-local {label} is already removed")


def main() -> None:
    migrate_function(
        source=ROOM_DETAIL,
        shared=SHARED_SUBTITLE,
        marker="@Composable\nfun SubtitleWidget(",
        label="SubtitleWidget",
    )
    migrate_function(
        source=ENTITY_CARD,
        shared=SHARED_SPACER,
        marker="@Composable\nfun SpacerButtonCard(",
        label="SpacerButtonCard",
    )


if __name__ == "__main__":
    main()
