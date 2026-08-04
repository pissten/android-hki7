from pathlib import Path

source = Path("app/src/main/java/com/jimz011apps/hki7/data/PreferencesManager.kt")
target = Path("sharedUi/src/commonMain/kotlin/com/jimz011apps/hki7/data/DashboardModels.kt")

if target.exists() and source.exists():
    text = source.read_text(encoding="utf-8")
    if "data class HKIDashboard(" not in text:
        print("Dashboard models are already shared; nothing to migrate")
        raise SystemExit(0)

text = source.read_text(encoding="utf-8")
start_marker = "@Serializable\ndata class HKIDashboard("
end_marker = "/** Outcome of pruning shared dashboards that were unpublished in the cloud. */"
start = text.find(start_marker)
end = text.find(end_marker)
if start < 0 or end < 0 or end <= start:
    raise SystemExit("Could not locate the dashboard model block in PreferencesManager.kt")

block = text[start:end].rstrip() + "\n"
shared = (
    "package com.jimz011apps.hki7.data\n\n"
    "import kotlinx.serialization.Serializable\n\n"
    + block
)

target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(shared, encoding="utf-8")
source.write_text(text[:start] + text[end:], encoding="utf-8")
print("Moved HKIDashboard and HeaderPillConfig into sharedUi")
