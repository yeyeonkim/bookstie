#!/usr/bin/env python3
"""Compile with local JDK and run in temporary directories, never against live data."""
from pathlib import Path
import hashlib
import shutil
import subprocess
import tempfile
import sys
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from jdk import tool, check_jdk
check_jdk()

root = Path(__file__).resolve().parents[1]
data = root / "data"
before = {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in data.glob("*.txt")}
with tempfile.TemporaryDirectory(prefix="bookstie-tests-") as folder:
    work = Path(folder)
    classes = work / "classes"
    classes.mkdir()
    sources = list((root / "src").rglob("*.java")) + list((root / "tests").glob("*.java"))
    subprocess.run([tool("javac"), "-encoding", "UTF-8", "-d", str(classes), *map(str, sources)], check=True)
    shutil.copytree(root / "resources", classes / "resources")
    print("PASS full project compilation", flush=True)
    (work / "data").mkdir()
    command = [tool("java"), "-Dbookstie.data.dir=" + str(work / "data"), "-Djava.awt.headless=true", "-cp", str(classes), "LoanRatingTest"]
    subprocess.run(command, cwd=work, check=True)
    subprocess.run(command + ["restart"], cwd=work, check=True)
    legacy = work / "legacy"
    (legacy / "data").mkdir(parents=True)
    legacy_source = data / "loans.txt" if (data / "loans.txt").exists() else root / "examples/data/loans.txt"
    shutil.copy2(legacy_source, legacy / "data/loans.txt")
    count = sum(bool(line.strip()) for line in legacy_source.read_text(encoding="utf-8").splitlines())
    legacy_command = command.copy()
    legacy_command[1] = "-Dbookstie.data.dir=" + str(legacy / "data")
    subprocess.run(legacy_command + ["legacy", str(count)], cwd=legacy, check=True)
    if "--gui" in sys.argv:
        gui = work / "gui"
        gui.mkdir()
        (gui / "data").mkdir()
        subprocess.run([tool("java"), "-Dbookstie.data.dir=" + str(gui / "data"), "-cp", str(classes), "LoanRatingGuiTest"], cwd=gui, check=True, timeout=45)
after = {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in data.glob("*.txt")}
assert before == after, "Live data changed"
print("PASS live data hashes unchanged")
