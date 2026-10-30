#!/usr/bin/env python3
"""Build JDK 25 executable JAR, safe distribution data, and UTF-8 API docs."""
from pathlib import Path
import shutil
import subprocess
import tempfile
from jdk import tool, check_jdk

root = Path(__file__).resolve().parents[1]
check_jdk()
build = root / "build"
dist = root / "dist"
build.mkdir(exist_ok=True)
dist.mkdir(exist_ok=True)
with tempfile.TemporaryDirectory(prefix="compile-", dir=build) as temporary:
    stage = Path(temporary)
    classes = stage / "classes"
    classes.mkdir()
    sources = sorted((root / "src").rglob("*.java"))
    subprocess.run([tool("javac"), "-encoding", "UTF-8", "--release", "25", "-d", str(classes), *map(str, sources)], check=True)
    for resource in sorted((root / "resources").rglob("*")):
        if resource.is_file() and resource.name != ".DS_Store":
            target = classes / resource.relative_to(root)
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(resource, target)
    jar = stage / "ssd_ip_cp4_2026f_234019_app.jar"
    subprocess.run([tool("jar"), "--create", "--file", str(jar), "--main-class", "bookstie.Main",
                    "--date", "2026-01-01T00:00:00Z", "-C", str(classes), "."], check=True)
    jar.replace(dist / jar.name)
# Never copy private live data; initialize examples only when destination files do not exist.
subprocess.run([__import__("sys").executable, str(root / "scripts/init-data.py"), "--target", str(dist / "data")], check=True)
shutil.copyfile(root / "docs/RUNNING.md", dist / "README.md")
command = [tool("javadoc"), "-public", "-encoding", "UTF-8", "-docencoding", "UTF-8", "-charset", "UTF-8",
           "-Xdoclint:all,-missing", "-Werror", "-notimestamp", "-d", str(root / "docs/api"),
           "-sourcepath", str(root / "src"), "-subpackages", "bookstie",
           "-doctitle", "Bookstie CP4 API Manual", "-windowtitle", "Bookstie CP4 API Manual"]
subprocess.run(command, check=True)
print("JAR:", dist / "ssd_ip_cp4_2026f_234019_app.jar")
print("API:", root / "docs/api/index.html")
