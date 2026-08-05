#!/usr/bin/env python3
"""Move validated production Home/Room widget renderers from Android into sharedUi."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ROOM_DETAIL = ROOT / "app/src/main/java/com/jimz011apps/hki7/ui/screens/RoomDetailScreen.kt"
SHARED_SUBTITLE = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/ui/screens/SubtitleWidget.kt"


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


def main() -> None:
    if not SHARED_SUBTITLE.exists():
        raise RuntimeError("Canonical shared SubtitleWidget is missing")

    text = ROOM_DETAIL.read_text(encoding="utf-8")
    updated = remove_balanced_declaration(text, "@Composable\nfun SubtitleWidget(")
    if updated != text:
        ROOM_DETAIL.write_text(updated, encoding="utf-8")
        print("Removed Android-local SubtitleWidget; callers now resolve sharedUi implementation")
    else:
        print("Android-local SubtitleWidget is already removed")


if __name__ == "__main__":
    main()
