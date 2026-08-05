#!/usr/bin/env python3
"""Extend the shared HA session with the canonical area and floor registry models."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SESSION = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/sharedui/ha/Hki7HomeAssistantSession.kt"


def replace_once(text: str, old: str, new: str) -> str:
    if new in text:
        return text
    if old not in text:
        raise ValueError(f"Patch anchor not found:\n{old}")
    return text.replace(old, new, 1)


def main() -> None:
    text = SESSION.read_text(encoding="utf-8")

    text = replace_once(
        text,
        "import com.jimz011apps.hki7.data.HAEntity\n",
        "import com.jimz011apps.hki7.data.HAArea\n"
        "import com.jimz011apps.hki7.data.HAEntity\n"
        "import com.jimz011apps.hki7.data.HAFloor\n",
    )

    text = replace_once(
        text,
        "    private val _entities = MutableStateFlow<Map<String, HAEntity>>(emptyMap())\n"
        "    val entities: StateFlow<Map<String, HAEntity>> = _entities.asStateFlow()\n",
        "    private val _entities = MutableStateFlow<Map<String, HAEntity>>(emptyMap())\n"
        "    val entities: StateFlow<Map<String, HAEntity>> = _entities.asStateFlow()\n\n"
        "    private val _areas = MutableStateFlow<List<HAArea>>(emptyList())\n"
        "    val areas: StateFlow<List<HAArea>> = _areas.asStateFlow()\n\n"
        "    private val _floors = MutableStateFlow<List<HAFloor>>(emptyList())\n"
        "    val floors: StateFlow<List<HAFloor>> = _floors.asStateFlow()\n",
    )

    text = replace_once(
        text,
        "            _entities.value = states.associateBy(HAEntity::entity_id)\n\n"
        "            val subscriptionResponse = sendCommand(\n",
        "            _entities.value = states.associateBy(HAEntity::entity_id)\n\n"
        "            val areasResponse = sendCommand(\"config/area_registry/list\")\n"
        "            requireSuccess(areasResponse, \"config/area_registry/list\")\n"
        "            val areaElements = areasResponse[\"result\"] as? JsonArray ?: JsonArray(emptyList())\n"
        "            _areas.value = json.decodeFromJsonElement(\n"
        "                ListSerializer(HAArea.serializer()),\n"
        "                areaElements,\n"
        "            )\n\n"
        "            val floorsResponse = sendCommand(\"config/floor_registry/list\")\n"
        "            requireSuccess(floorsResponse, \"config/floor_registry/list\")\n"
        "            val floorElements = floorsResponse[\"result\"] as? JsonArray ?: JsonArray(emptyList())\n"
        "            _floors.value = json.decodeFromJsonElement(\n"
        "                ListSerializer(HAFloor.serializer()),\n"
        "                floorElements,\n"
        "            )\n\n"
        "            val subscriptionResponse = sendCommand(\n",
    )

    text = replace_once(
        text,
        "        if (clearEntities) _entities.value = emptyMap()\n",
        "        if (clearEntities) {\n"
        "            _entities.value = emptyMap()\n"
        "            _areas.value = emptyList()\n"
        "            _floors.value = emptyList()\n"
        "        }\n",
    )

    SESSION.write_text(text, encoding="utf-8")
    print("Shared Home Assistant session now exposes canonical HAArea and HAFloor registries")


if __name__ == "__main__":
    main()
