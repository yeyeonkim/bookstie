# CP4 검증 기록 (2026-10-30)

## 실행 환경과 명령

프로젝트 SDK ms-25: Microsoft OpenJDK 25.0.2, macOS aarch64. 아래 명령 실행 시 JAVA_HOME은 프로젝트 SDK의 설치 디렉터리로 지정.
개별 PC의 JDK 절대경로는 스크립트에 저장하지 않음.

```sh
python3 scripts/build.py
python3 tests/run-tests.py --gui
python3 tests/run-distribution-tests.py
```

build.py가 실제 실행하는 핵심 명령 (경로와 소스 목록은 스크립트에서 절대경로 인수로 구성):

```sh
javac -encoding UTF-8 --release 25 -d <임시 classes> <src 아래 전체 Java 소스>
jar --create --file <임시 JAR> --main-class bookstie.Main --date 2026-01-01T00:00:00Z -C <classes> .
javadoc -public -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 -Xdoclint:all,-missing -Werror -notimestamp -d docs/api -sourcepath src -subpackages bookstie -doctitle 'Bookstie CP4 API Manual' -windowtitle 'Bookstie CP4 API Manual'
```

## 결과

- 전체 프로젝트 및 테스트 컴파일 통과.
- 기존 회귀 42 checks, 별도 JVM 재실행 4 checks 통과.
- 기존 loans.txt의 임시 복사본 읽기 통과. 원본 파일 수정 없음.
- 실제 Swing 대화상자 조작을 포함한 GUI 자동 검사 54 checks 통과.
- 새 설치 예제를 복사한 ASCII 경로와 `제출 한글 공백 폴더`에서 JAR 실행 통과.
- 배포 폴더 및 별도의 작업 디렉터리에서 `java -Djava.awt.headless=true -jar <JAR 절대경로> --check-installation` 통과.
- JAR을 클래스패스로 사용한 별도 테스트 JVM에서 회원가입·관리자 인증·도서/대출 저장·별점·반납·새 JVM 재조회 통과. 테스트용 비밀번호는 실행 시 생성하며 출력하지 않음.
- 실제 폰트 이름 비교, SVG 별 색상 그리기, JAR 내부 폰트/아이콘/메인 매니페스트 확인 통과.
- 누락 폰트의 SansSerif 굵은 글꼴 대체와 원인 stderr 안내 통과.
- 누락 아이콘 실패, data 부재·상대 설정·출력 위치 판별 불가 시 실패 및 잘못된 data 자동 생성 없음 확인.
- 명시적 데이터 디렉터리 설정과 IntelliJ 형태의 중첩 클래스 출력에서 프로젝트 루트 추론 통과.
- 초기화 스크립트 재실행 시 기존 파일 보존 확인.
- 동일 JDK로 반복 빌드한 JAR SHA-256 동일, 기존 배포 데이터 보존 확인.
- 원본 data 네 파일과 원본 resources는 변경 전 해시/바이트와 동일.
- Git stage의 공개 파일만 별도 한글·공백 임시 폴더로 내보내 다른 작업 디렉터리에서 빌드·회귀 테스트 재실행 통과. 로컬 data나 IDE 설정 없이 재현 확인.
- 공개 예제에는 합성 도서 두 권만 존재. users/admins/loans는 비어 있고 실제 계정 데이터는 Git 제외. 기존 암호의 완전 문자열/필드가 공개 예정 텍스트에 있는지 비공개 점검해 미검출.
- UTF-8 API 문서 생성 통과. missing 그룹만 제외(암시적 생성자·Swing private 직렬화 필드); 나머지 doclint 경고는 오류 처리.

수동으로 디자인을 눈으로 비교하는 검수와 IntelliJ 공유 실행 구성을 IDE에서 클릭해 실행하는 검수는 수행하지 않음.
GUI 자동 검사는 클래스 출력에서 실행했고, `java -jar` 검사는 설치 점검 모드로 수행. JAR의 로그인 화면을 직접 조작하는 검수는 별도.
Windows/Linux 실행은 수행하지 않음. 기존 POSIX 권한 실패 회귀 테스트는 macOS/Linux 대상.
기존 Java 테스트의 상대 data 참조는 임시 작업 폴더의 파일 비교 전용이며, Repository는 명시 JVM 옵션의 동일 임시 디렉터리 사용.

## 생성물

- dist/ssd_ip_cp4_2026f_234019_app.jar
- dist/data/{users,admins,books,loans}.txt
- dist/README.md
- docs/api/index.html
- JAR SHA-256: `93742fac46ed4e585363f0ce02c3dcc924fb675fe9b7a1fc0d9cd94a31a15f65`

## Git 상태와 업로드

작업 시작 시 현재 폴더와 상위에 .git이 없어 기존 이력·브랜치·remote·추적 파일이 없었음.
요청한 로컬 커밋을 위해 main 브랜치로 로컬 저장소를 최초 준비. 원격 저장소 생성 및 push는 수행하지 않음.
공개 파일만 명시적으로 stage하며 data/, .idea/, *.iml, out/, build/, docs/api/, dist/, JAR/ZIP 등은 제외.
.gitignore는 기존 추적 파일을 제거하지 않지만 이번 최초 저장소에는 기존 추적 파일이 없었음.
이후 실제 원격 URL을 준비한 다음 remote가 없는 것을 확인하고 아래 명령 실행:

```sh
git remote -v
git remote add origin <실제-GitHub-저장소-URL>
git push -u origin main
```

로컬 커밋 확인: `git log -1 --oneline`, 작업 상태 확인: `git status --short`.
