from pathlib import Path

SOURCE_ROOT = Path("app/src/main/java")
TARGET_ROOT = Path("sharedUi/src/commonMain/kotlin")

PATHS = [
    "com/jimz011apps/hki7/ui/theme/Color.kt",
    "com/jimz011apps/hki7/ui/theme/Type.kt",
    "com/jimz011apps/hki7/ui/screens/VacuumEntityResolver.kt",
    "com/jimz011apps/hki7/data/AdaptiveLightingModels.kt",
    "com/jimz011apps/hki7/data/WeatherRoleDiscovery.kt",
    "com/jimz011apps/hki7/ui/utils/MdiIconRegistry.kt",
    "com/jimz011apps/hki7/data/SharedDashboardMerge.kt",
    "com/jimz011apps/hki7/data/AutomationModels.kt",
]

moved = 0
for relative in PATHS:
    source = SOURCE_ROOT / relative
    target = TARGET_ROOT / relative
    if target.exists() and not source.exists():
        print(f"already shared: {relative}")
        continue
    if not source.exists():
        raise SystemExit(f"missing source: {source}")
    if target.exists():
        raise SystemExit(f"target already exists while source remains: {target}")
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(source.read_bytes())
    source.unlink()
    moved += 1
    print(f"moved: {relative}")

print(f"Moved {moved} original source files into sharedUi")
