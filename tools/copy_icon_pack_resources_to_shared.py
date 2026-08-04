from pathlib import Path
import shutil

source_root = Path("app/src/main")
target_root = Path("sharedUi/src/commonMain/composeResources")

fonts = [
    "mdi_icons.ttf",
    "simple_icons.ttf",
    "tabler_icons.ttf",
    "phosphor_icons.ttf",
]
files = [
    "mdi_codepoints.txt",
    "mdi_keywords.txt",
    "simple_codepoints.txt",
    "simple_keywords.txt",
    "tabler_codepoints.txt",
    "tabler_keywords.txt",
    "phosphor_codepoints.txt",
    "phosphor_keywords.txt",
]

for name in fonts:
    source = source_root / "res" / "font" / name
    target = target_root / "font" / name
    if not source.exists():
        raise SystemExit(f"Missing bundled icon font: {source}")
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)
    print(f"copied {source} -> {target}")

for name in files:
    source = source_root / "assets" / name
    target = target_root / "files" / "icons" / name
    if not source.exists():
        raise SystemExit(f"Missing icon lookup table: {source}")
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)
    print(f"copied {source} -> {target}")
