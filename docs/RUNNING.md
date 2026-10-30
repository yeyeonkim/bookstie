# Bookstie CP4 실행 안내

JDK 25와 그래픽 데스크톱 환경 필요. Java Swing, 외부 DB 미사용.

```sh
java -jar ssd_ip_cp4_2026f_234019_app.jar
```

다른 작업 디렉터리에서도 JAR의 절대경로로 실행 가능. 공백·한글이 있으면 경로 전체를 따옴표로 묶기.

```sh
java -jar "/배포 경로/ssd_ip_cp4_2026f_234019_app.jar"
java -Djava.awt.headless=true -jar "/배포 경로/ssd_ip_cp4_2026f_234019_app.jar" --check-installation
```

수정 가능한 데이터는 항상 JAR 옆 `data/`에 저장. JAR과 data 폴더를 함께 이동할 것.
데이터 디렉터리가 없으면 오류를 표시하며 임의의 작업 폴더에 새 data를 만들지 않음.
명시적 경로를 사용하려면 기존 디렉터리의 절대경로 지정:

```sh
java -Dbookstie.data.dir="/별도 데이터 경로" -jar ssd_ip_cp4_2026f_234019_app.jar
```

배포 기본값은 개인정보·계정·암호·대출 기록이 없는 테스트용 예제. 도서 두 권만 포함.
회원가입으로 테스트 회원 생성 가능. 관리자 계정은 기본 제공하지 않음.
소스 저장소에서 `python3 scripts/init-data.py --target "/배포 경로/data" --admin`으로 직접 지정.
암호는 화면이나 로그에 출력되지 않으며 기존 파일은 초기 데이터로 덮어쓰지 않음.
현행 프로그램은 암호를 txt에 평문 저장하므로 실제 사용하는 암호 대신 제출 검증 전용 암호 사용.
별점 등록은 완독 처리이며, 관리자 반납과 독립적. 별점 등록으로 대출·반납 상태는 변경되지 않음.
폰트·아이콘은 JAR 내부 리소스이므로 외부 resources 폴더 불필요.
--check-installation은 경로·데이터 조회·폰트 및 아이콘 파싱 점검용이며 화면 디자인이나 대화상자 조작 검증은 별도.
