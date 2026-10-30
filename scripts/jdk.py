"""JDK 25 selection shared by build and tests; no machine-specific paths."""
import os
from pathlib import Path
import re
import shutil
import subprocess

def tool(name):
    home = os.environ.get("JAVA_HOME")
    executable = str(Path(home) / "bin" / (name + (".exe" if os.name == "nt" else ""))) if home else shutil.which(name)
    if not executable or not Path(executable).is_file():
        raise SystemExit("JDK 25가 필요합니다. JAVA_HOME 또는 PATH를 설정하세요.")
    return executable

def check_jdk():
    for name in ("java", "javac"):
        result = subprocess.run([tool(name), "-version"], capture_output=True, text=True, check=True)
        if not re.search(r'(?:javac |version ")25(?:[."]|$)', result.stdout + result.stderr):
            raise SystemExit("JDK 25를 선택하세요: " + name)
