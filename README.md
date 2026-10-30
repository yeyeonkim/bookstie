# Bookstie

Java Swing 기반 도서 대출 및 독서 기록 관리 프로그램. JDK 25 사용, 외부 DB 없이 UTF-8 txt 파일로 저장.

주요 기능: 회원가입·회원/관리자 로그인, 제목 검색·대출, 대출/연체 현황, 내 서재와 별점·완독 표시, 관리자 도서·회원 관리 및 반납 처리.
별점 1~5 등록에 따른 완독과 관리자 반납은 독립적이며 기존 화면·대출 정책 유지.

## 빌드와 실행

JDK 25, Python 3 필요. JAVA_HOME에 사용할 JDK 위치를 지정하거나 JDK 25의 bin을 PATH에 추가.
빌드 도구는 프로젝트 작업 디렉터리와 무관하게 스크립트 파일 위치를 기준으로 실행.

```sh
python3 scripts/build.py
java -jar dist/ssd_ip_cp4_2026f_234019_app.jar
python3 tests/run-tests.py
python3 tests/run-tests.py --gui
python3 tests/run-distribution-tests.py
```

생성물: `dist/ssd_ip_cp4_2026f_234019_app.jar`, `dist/data/`의 네 txt 파일, `dist/README.md`, `docs/api/index.html`.
동일한 JDK·소스·리소스로 빌드하면 고정 JAR 항목 날짜를 사용해 동일 JAR 생성.
빌드는 원본 data를 복사하지 않고 공개 예제만 별도 배포 데이터로 준비하며, 기존 배포 데이터 파일도 덮어쓰지 않음.
공개 배포 시 사용 후 dist/data에 계정이나 기록이 남았으면 포함하지 말고 새 임시 배포 폴더에 examples/data를 복사할 것.

## 데이터 위치와 초기 준비

- JAR 실행: 실제 JAR 위치 바로 옆 `data/` 사용. 작업 디렉터리(user.dir)는 사용하지 않음.
- IntelliJ/클래스 실행: 클래스 출력 위치의 상위에서 `src/bookstie/Main.java`와 `resources/`가 확인되는 프로젝트의 `data/` 사용.
- 우선 설정: `-Dbookstie.data.dir="기존 데이터 디렉터리의 절대경로"`. 상대경로는 거부.
- 판별 불가 또는 데이터 디렉터리 부재 시 원인과 해결 방법을 표시하고 중단. 임의 위치에 빈 data를 만들지 않음.
- 알려진 디렉터리 안에서 없는 txt 파일만 빈 파일로 준비. 기존 파일은 생성 시 덮어쓰지 않음.

공개 저장소의 examples/data는 합성 도서 예제만 포함하며 계정·비밀번호·대출 기록은 비어 있음.
새 체크아웃의 최초 실행 전:

```sh
python3 scripts/init-data.py
# 관리자 계정이 필요한 경우 직접 입력 (기존 파일 보존, 비밀번호 비표시)
python3 scripts/init-data.py --admin
```

이 명령은 프로젝트 data에 없는 파일만 복사. 이미 사용 중인 data는 변경하지 않음(--admin 선택 시 지정 계정만 추가).
다른 경로 초기화는 `--target "/데이터 절대경로"` 사용. 실제 개인정보와 암호는 공개 Git에 포함하지 않음.
현행 저장 형식의 암호는 평문이므로 테스트 전용 암호 사용.

## IntelliJ 설정

Project SDK = JDK 25, src = Sources Root.
resources = Resources Root, 해당 리소스 루트의 Relative output path = `resources`.
결과 클래스패스 배치가 `/resources/fonts/EF_jejudoldam(TTF).ttf`, `/resources/icon/redStar.svg`, `/resources/icon/grayStar.svg`가 되도록 설정.
공유 실행 구성 `.run/Bookstie.run.xml`은 프로젝트 루트를 추가 클래스패스로 사용해 동일한 `/resources/...` 배치를 제공. IntelliJ에서 공유 `Bookstie` 구성을 선택하면 별도의 리소스 루트 설정 없이 실행 가능. 다른 실행 구성을 직접 만들 경우 위 Resources Root 설정을 적용하거나 프로젝트 루트를 추가 클래스패스로 지정. IDE의 자동 저장으로 로컬 iml이 재작성될 수 있으므로 iml 자체는 Git 제외 대상.
메인 클래스는 `bookstie.Main`. Working directory 설정에 의존하지 않음.
외부 임시 출력 폴더를 사용하는 실행 구성에는 `-Dbookstie.data.dir`를 명시.

## 검증 및 문서

테스트는 임시 데이터 디렉터리를 JVM 옵션으로 지정하며 원본 data의 해시 보존을 확인.
기존 회귀 테스트, 새 JVM 재실행, 배포 경로·리소스 검증 포함. GUI 테스트는 데스크톱이 없으면 SKIP 출력.
POSIX 쓰기 권한 회귀 테스트는 macOS/Linux에서 실행 가능.
API 생성은 UTF-8 사용. 암시적 기본 생성자와 Swing 직렬화 private 필드의 missing 경고만 제외하고 나머지 doclint 경고는 오류로 처리.
설계 변경 사항은 [docs/cp4-design-changes.md](docs/cp4-design-changes.md), 실행 안내는 [docs/RUNNING.md](docs/RUNNING.md) 참고.

## GitHub 업로드 준비

src, resources, tests, scripts, examples, .run 공유 실행 구성, README 및 설계 문서를 포함.
로컬 data, IDE 설정, 빌드 결과, API 생성물, JAR·ZIP은 .gitignore로 제외.
이미 추적된 파일은 .gitignore만으로 제외되지 않으므로 `git ls-files`로 확인 후 필요 시 `git rm --cached -- <파일>` 사용(실제 파일 보존).
기존 Git 이력이 있는 작업 사본에서는 재초기화하거나 remote를 바꾸지 말 것.
현재 작업 폴더의 Git/검증 결과는 docs/cp4-verification.md에 기록.
원격 생성과 push는 수행하지 않음. 이후 사용자가 저장소를 준비한 뒤:

```sh
git remote -v
# remote가 없는 경우에만 실제 저장소 URL로 추가
git remote add origin <저장소-URL>
git push -u origin main
```
