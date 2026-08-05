#!/usr/bin/env python3
"""Wire Android RoomsScreen to the canonical room tile now living in sharedUi/commonMain."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ROOMS = ROOT / "app/src/main/java/com/jimz011apps/hki7/ui/screens/RoomsScreen.kt"


def replace_function(text: str, marker: str, replacement: str) -> str:
    start = text.find(marker)
    if start < 0:
        # Idempotent after the wrapper has already been installed.
        if "HKIAreaCard(" in text:
            return text
        raise ValueError(f"Could not find function marker: {marker}")
    opening = text.find("{", start)
    if opening < 0:
        raise ValueError(f"Could not find function body: {marker}")

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
    raise ValueError(f"Unbalanced function body: {marker}")


def main() -> None:
    text = ROOMS.read_text(encoding="utf-8")
    import_line = "import com.jimz011apps.hki7.ui.components.HKIAreaCard\n"
    anchor = "import com.jimz011apps.hki7.ui.components.HKIPage\n"
    if import_line not in text:
        text = text.replace(anchor, anchor + import_line)

    wrapper = '''@Composable
fun AreaCard(
    area: HAArea,
    config: HKIAreaConfig,
    widgets: List<HKIRoomWidget>,
    viewModel: MainViewModel,
    baseUrl: String,
    isEditMode: Boolean,
    canDelete: Boolean,
    isDragging: Boolean,
    isSquare: Boolean = false,
    compactTiles: Boolean = true,
    cornerRadius: Int = LocalItemCornerRadius.current,
    onDelete: () -> Unit,
    onSettings: () -> Unit,
    onClick: () -> Unit,
    onActivityClick: ((String, List<String>) -> Unit)? = null
) {
    val mediaPlayerIds = remember(config) { config.roomMediaPlayerIds() }
    val displayedControlIds = remember(widgets) { displayedRoomControlEntityIds(widgets) }
    val dependencyIds = remember(config, mediaPlayerIds, displayedControlIds) {
        (config.roomEntityIds() + mediaPlayerIds + displayedControlIds).distinct()
    }
    val dependencyFlow = remember(viewModel, dependencyIds) { viewModel.entitiesFor(dependencyIds) }
    val roomEntities by dependencyFlow.collectAsState()
    val peopleByArea by viewModel.peopleByAreaId.collectAsState()

    HKIAreaCard(
        area = area,
        config = config,
        widgets = widgets,
        roomEntities = roomEntities,
        peopleHere = peopleByArea[area.area_id] ?: 0,
        baseUrl = baseUrl,
        isEditMode = isEditMode,
        canDelete = canDelete,
        isDragging = isDragging,
        isSquare = isSquare,
        compactTiles = compactTiles,
        cornerRadius = cornerRadius,
        onDelete = onDelete,
        onSettings = onSettings,
        onClick = onClick,
        onActivityClick = onActivityClick,
    )
}
'''
    text = replace_function(text, "@Composable\nfun AreaCard(", wrapper)
    ROOMS.write_text(text, encoding="utf-8")
    print("Android RoomsScreen now delegates its original room tile to sharedUi HKIAreaCard")


if __name__ == "__main__":
    main()
