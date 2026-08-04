from __future__ import annotations

from collections import Counter, defaultdict
from pathlib import Path
import re

ROOT = Path("app/src/main/java")
OUT = Path("build/kmp-inventory.md")

HARD_PATTERNS = {
    "android framework": re.compile(r"^import android\.", re.M),
    "AndroidX platform API": re.compile(
        r"^import androidx\.(activity|core|work|datastore|appcompat|lifecycle\.process|webkit)\.", re.M
    ),
    "Android resources": re.compile(r"^import com\.jimz011apps\.hki7\.R$|^import androidx\.compose\.ui\.res\.", re.M),
    "Android View/Context": re.compile(r"LocalContext|LocalView|DialogWindowProvider|WindowCompat|WindowManager"),
    "JVM time/io/util": re.compile(r"^import java\.(time|io|util|net)\.", re.M),
}

SOFT_PATTERNS = {
    "Android view model": re.compile(r"com\.jimz011apps\.hki7\.ui\.MainViewModel"),
    "Android preferences": re.compile(r"PreferencesManager"),
    "Coil image loading": re.compile(r"^import coil3\.", re.M),
    "Android navigation": re.compile(r"^import androidx\.navigation\.", re.M),
    "Google services": re.compile(r"^import com\.google\.", re.M),
    "Android BuildConfig": re.compile(r"BuildConfig"),
}

files = sorted(ROOT.rglob("*.kt"))
rows: list[tuple[str, str, list[str], int]] = []
counts = Counter()
by_status: dict[str, list[tuple[str, list[str], int]]] = defaultdict(list)

for path in files:
    text = path.read_text(encoding="utf-8")
    reasons = [name for name, pattern in HARD_PATTERNS.items() if pattern.search(text)]
    soft = [name for name, pattern in SOFT_PATTERNS.items() if pattern.search(text)]
    if reasons:
        status = "ANDROID_BOUND"
        all_reasons = reasons + soft
    elif soft:
        status = "NEEDS_ADAPTER"
        all_reasons = soft
    else:
        status = "COMMON_READY"
        all_reasons = []
    rel = path.relative_to(ROOT).as_posix()
    lines = text.count("\n") + 1
    rows.append((status, rel, all_reasons, lines))
    counts[status] += 1
    by_status[status].append((rel, all_reasons, lines))

OUT.parent.mkdir(parents=True, exist_ok=True)
with OUT.open("w", encoding="utf-8") as f:
    f.write("# HKI 7 Kotlin Multiplatform source inventory\n\n")
    f.write(f"Scanned **{len(files)}** Kotlin files under `{ROOT}`.\n\n")
    f.write("| Category | Files | Meaning |\n|---|---:|---|\n")
    f.write(f"| COMMON_READY | {counts['COMMON_READY']} | No direct Android/JVM/platform dependency detected |\n")
    f.write(f"| NEEDS_ADAPTER | {counts['NEEDS_ADAPTER']} | Compose code that depends on HKI Android services or portable third-party APIs |\n")
    f.write(f"| ANDROID_BOUND | {counts['ANDROID_BOUND']} | Direct Android/JVM/resource dependency detected |\n\n")

    for status in ("COMMON_READY", "NEEDS_ADAPTER", "ANDROID_BOUND"):
        f.write(f"## {status}\n\n")
        f.write("| File | Lines | Reasons |\n|---|---:|---|\n")
        for rel, reasons, lines in sorted(by_status[status], key=lambda item: (item[2], item[0])):
            reason_text = ", ".join(reasons) if reasons else "—"
            f.write(f"| `{rel}` | {lines} | {reason_text} |\n")
        f.write("\n")

print(OUT.read_text(encoding="utf-8"))
