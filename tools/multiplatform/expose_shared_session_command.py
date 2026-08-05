#!/usr/bin/env python3
"""Expose the authenticated shared HA command channel to commonMain repositories.

The WebSocket/session lifecycle stays encapsulated in Hki7HomeAssistantSession. Only the existing
command method changes from private to internal so other files in the same shared module can add
protocol-specific repositories without duplicating a second socket implementation.
"""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SESSION = ROOT / "sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/sharedui/ha/Hki7HomeAssistantSession.kt"


def main() -> None:
    text = SESSION.read_text(encoding="utf-8")
    old = "    private suspend fun sendCommand(\n"
    new = "    internal suspend fun sendCommand(\n"
    if new in text:
        print("Shared command channel is already module-visible")
        return
    if old not in text:
        raise RuntimeError("Could not find Hki7HomeAssistantSession.sendCommand visibility anchor")
    SESSION.write_text(text.replace(old, new, 1), encoding="utf-8")
    print("Made the existing authenticated command channel visible inside sharedUi")


if __name__ == "__main__":
    main()
