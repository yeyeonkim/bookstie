#!/usr/bin/env python3
"""Initialize only missing data files; optional interactive administrator creation."""
import argparse
import getpass
from pathlib import Path
import shutil

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--target", type=Path, default=root / "data")
parser.add_argument("--admin", action="store_true", help="관리자 계정을 대화식으로 추가 (암호 출력 없음)")
args = parser.parse_args()
target = args.target.expanduser().resolve()
target.mkdir(parents=True, exist_ok=True)
for source in sorted((root / "examples/data").glob("*.txt")):
    try:
        with (target / source.name).open("xb") as output:
            output.write(source.read_bytes())
        print("준비:", source.name)
    except FileExistsError:
        print("기존 파일 보존:", source.name)
if args.admin:
    account = input("추가할 관리자 ID: ").strip()
    password = getpass.getpass("관리자 비밀번호 (표시 안 됨): ")
    confirm = getpass.getpass("비밀번호 확인: ")
    if not account or not password or password != confirm or any(c in account + password for c in "\t\r\n"):
        raise SystemExit("빈 값·탭·줄바꿈 또는 비밀번호 불일치로 취소했습니다.")
    path = target / "admins.txt"
    text = path.read_text(encoding="utf-8")
    if any(line.split("\t")[0] == account for line in text.splitlines()):
        raise SystemExit("동일 관리자 ID가 있어 취소했습니다.")
    with path.open("a", encoding="utf-8", newline="\n") as output:
        if text and not text.endswith("\n"): output.write("\n")
        output.write(account + "\t" + password + "\n")
    print("관리자 추가 완료 (자격 증명 비공개)")
print("데이터 위치:", target)
