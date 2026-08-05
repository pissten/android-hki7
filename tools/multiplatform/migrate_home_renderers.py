#!/usr/bin/env python3
"""Move validated production Home/Room widget renderers from Android into sharedUi."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ROOM_DETAIL = ROOT / "app/src/main/java/com/jimz011apps/hki7/ui/screens/RoomDetailScreen.kt"
ENTITY_CARD = ROOT / "app/src/main/java/com/jimz011apps/hki7/ui/components/EntityCard.kt"
SHARED_SUBTITLE = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/ui/screens/SubtitleWidget.kt"
SHARED_SPACER = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/ui/components/SpacerButtonCard.kt"


def declaration_bounds(text: str, marker: str) -> tuple[int, int] | None:
    start = text.find(marker)
    if start < 0:
        return None
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
                return start, index + 1
    raise RuntimeError(f"Unbalanced function body for {marker}")


def remove_balanced_declaration(text: str, marker: str) -> str:
    bounds = declaration_bounds(text, marker)
    if bounds is None:
        return text
    start, end = bounds
    return text[:start] + text[end:].lstrip("\n")


def migrate_spacer() -> None:
    if not SHARED_SPACER.exists():
        raise RuntimeError("Canonical shared SpacerButtonCard is missing")
    text = ENTITY_CARD.read_text(encoding="utf-8")
    updated = remove_balanced_declaration(text, "@Composable\nfun SpacerButtonCard(")
    if updated != text:
        ENTITY_CARD.write_text(updated, encoding="utf-8")
        print("Removed Android-local SpacerButtonCard; callers now resolve sharedUi implementation")
    else:
        print("Android-local SpacerButtonCard is already removed")


def wire_android_subtitle_wrapper() -> None:
    if not SHARED_SUBTITLE.exists():
        raise RuntimeError("Canonical shared SubtitleWidgetContent is missing")
    text = ROOM_DETAIL.read_text(encoding="utf-8")
    marker = "@Composable\nfun SubtitleWidget("
    bounds = declaration_bounds(text, marker)
    if bounds is None:
        # Idempotent after a previous migration pass.
        if "SubtitleWidgetContent(" in text:
            print("Android SubtitleWidget already delegates to shared content")
            return
        raise RuntimeError("Android SubtitleWidget wrapper is missing")
    start, end = bounds
    wrapper = '''@Composable
fun SubtitleWidget(
    widget: HKISubtitleWidget,
    isEditMode: Boolean,
    onDelete: () -> Unit,
    onSettings: () -> Unit
) {
    if (!isWidgetVisibleNow(widget) && !isEditMode) return
    SubtitleWidgetContent(
        widget = widget,
        isEditMode = isEditMode,
        onDelete = onDelete,
        onSettings = onSettings,
    )
}
'''
    current = text[start:end]
    if "SubtitleWidgetContent(" not in current:
        ROOM_DETAIL.write_text(text[:start] + wrapper + text[end:], encoding="utf-8")
        print("Android SubtitleWidget now delegates its visual tree to sharedUi")
    else:
        print("Android SubtitleWidget already delegates to shared content")


def main() -> None:
    wire_android_subtitle_wrapper()
    migrate_spacer()


if __name__ == "__main__":
    main()
