from pathlib import Path

SOURCE = Path("app/src/main/java/com/jimz011apps/hki7/data/HomeAssistantModels.kt")
TARGET = Path("sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/data/HomeAssistantModels.kt")

if not SOURCE.exists():
    raise SystemExit(f"Source model file not found: {SOURCE}")
if TARGET.exists():
    raise SystemExit(f"Target model file already exists: {TARGET}")

text = SOURCE.read_text(encoding="utf-8")
needle = "java.util.UUID.randomUUID()"
occurrences = text.count(needle)
if occurrences != 2:
    raise SystemExit(f"Expected exactly 2 JVM UUID usages, found {occurrences}")

text = text.replace(needle, "hki7RandomUuid()")
TARGET.parent.mkdir(parents=True, exist_ok=True)
TARGET.write_text(text, encoding="utf-8")
SOURCE.unlink()

print(f"Moved {SOURCE} -> {TARGET}")
print("Replaced 2 JVM UUID calls with the shared platform UUID abstraction")
