from pathlib import Path

SOURCE_ROOT = Path("app/src/main/res")
TARGET_ROOT = Path("sharedUi/src/commonMain/composeResources")

sources = sorted(SOURCE_ROOT.glob("values*/strings.xml"))
if not sources:
    raise SystemExit("No Android strings.xml files found")

copied = 0
for source in sources:
    qualifier_dir = source.parent.name
    target = TARGET_ROOT / qualifier_dir / "strings.xml"
    target.parent.mkdir(parents=True, exist_ok=True)
    content = source.read_bytes()
    if target.exists() and target.read_bytes() == content:
        print(f"unchanged: {target}")
        continue
    target.write_bytes(content)
    copied += 1
    print(f"copied: {source} -> {target}")

print(f"Found {len(sources)} locale resource files; updated {copied}")
