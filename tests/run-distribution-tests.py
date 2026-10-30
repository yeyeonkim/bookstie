#!/usr/bin/env python3
"""Isolated JAR validation in ASCII and Korean/spaced paths, without live-data mutation."""
from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import sys
import tempfile
import zipfile
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from jdk import tool, check_jdk

root = Path(__file__).resolve().parents[1]
check_jdk()
jar = root / "dist/ssd_ip_cp4_2026f_234019_app.jar"
assert jar.exists(), "먼저 python3 scripts/build.py 실행"
def hashes():
    return {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in (root / "data").glob("*.txt")}
before = hashes()
with zipfile.ZipFile(jar) as archive:
    names = set(archive.namelist())
    assert "Main-Class: bookstie.Main" in archive.read("META-INF/MANIFEST.MF").decode()
    assert {"resources/fonts/EF_jejudoldam(TTF).ttf", "resources/icon/redStar.svg", "resources/icon/grayStar.svg"} <= names
    assert not any(n.startswith("data/") for n in names)
    print("PASS JAR manifest, bundled resources, no private data")
with tempfile.TemporaryDirectory(prefix="bookstie-distribution-") as temporary:
    work = Path(temporary)
    classes = work / "tests"
    classes.mkdir()
    subprocess.run([tool("javac"), "-encoding", "UTF-8", "-cp", str(jar), "-d", str(classes),
                    str(root / "tests/DistributionTest.java"), str(root / "tests/FontFallbackTest.java")], check=True)
    elsewhere = work / "unrelated cwd"
    elsewhere.mkdir()
    for name in ("ascii", "제출 한글 공백 폴더"):
        target = work / name
        target.mkdir()
        copied = target / jar.name
        shutil.copyfile(jar, copied)
        shutil.copytree(root / "examples/data", target / "data")
        base = [tool("java"), "-Djava.awt.headless=true"]
        subprocess.run(base + ["-jar", str(copied), "--check-installation"], cwd=target, check=True, timeout=30)
        subprocess.run(base + ["-jar", str(copied), "--check-installation"], cwd=elsewhere, check=True, timeout=30)
        cp = str(classes) + os.pathsep + str(copied)
        for mode,cwd in (("write",target),("read",elsewhere)):
            subprocess.run(base + ["-cp", cp, "DistributionTest", mode, str(target / "data")], cwd=cwd, check=True, timeout=30)
    # Missing/relative/unknown locations must fail without silently creating directories.
    missing = work / "missing data"
    missing.mkdir()
    shutil.copyfile(jar, missing / jar.name)
    def fails(command,cwd=elsewhere):
        result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, timeout=30)
        assert result.returncode != 0 and "bookstie.data.dir" in result.stderr
    fails([tool("java"), "-jar", str(missing / jar.name), "--check-installation"])
    assert not (missing / "data").exists() and not (elsewhere / "data").exists()
    fails([tool("java"), "-Dbookstie.data.dir=relative", "-jar", str(jar), "--check-installation"])
    # Unidentified class output has neither project markers nor an explicit override.
    loose = work / "loose classes"
    with zipfile.ZipFile(jar) as archive: archive.extractall(loose)
    fails([tool("java"), "-cp", str(loose), "bookstie.Main", "--check-installation"])
    override = work / "명시 데이터"
    shutil.copytree(root / "examples/data", override)
    subprocess.run([tool("java"), "-Djava.awt.headless=true", "-Dbookstie.data.dir=" + str(override),
                    "-cp", str(loose), "bookstie.Main", "--check-installation"], cwd=elsewhere, check=True, timeout=30)
    # IDE-style nested output inference, independent of current working directory.
    project = work / "개발 프로젝트"
    (project / "src/bookstie").mkdir(parents=True)
    (project / "src/bookstie/Main.java").touch()
    (project / "resources").mkdir()
    shutil.copytree(root / "examples/data", project / "data")
    ide = project / "out/production/Bookstie"
    shutil.copytree(loose, ide)
    subprocess.run([tool("java"), "-Djava.awt.headless=true", "-cp", str(ide), "bookstie.Main", "--check-installation"],
                   cwd=elsewhere, check=True, timeout=30)
    # Broken packaged resource must surface the error rather than use cwd resources.
    (loose / "resources/icon/redStar.svg").unlink()
    result = subprocess.run([tool("java"), "-Djava.awt.headless=true", "-Dbookstie.data.dir=" + str(override),
                             "-cp", str(loose), "bookstie.Main", "--check-installation"], cwd=elsewhere,
                            capture_output=True, text=True, timeout=30)
    assert result.returncode != 0
    (loose / "resources/fonts/EF_jejudoldam(TTF).ttf").unlink()
    result = subprocess.run([tool("java"), "-Djava.awt.headless=true", "-cp", str(classes) + os.pathsep + str(loose),
                             "FontFallbackTest"], cwd=elsewhere, capture_output=True, text=True, timeout=30)
    assert result.returncode == 0 and "클래스패스 폰트 리소스 누락" in result.stderr
    print(result.stdout.strip())
    # Initialization script is idempotent and never overwrites any pre-existing file.
    previous = {p.name:p.read_bytes() for p in override.iterdir()}
    subprocess.run([sys.executable,str(root / "scripts/init-data.py"),"--target",str(override)], check=True)
    assert previous == {p.name:p.read_bytes() for p in override.iterdir()}
assert hashes() == before
print("PASS location failures, explicit override, IDE inference, missing icon, safe initialization")
print("PASS live data hashes unchanged")
