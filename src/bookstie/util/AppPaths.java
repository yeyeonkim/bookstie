package bookstie.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * 실행 작업 디렉터리와 무관하게 모든 저장소의 외부 데이터 경로를 결정하는 공통 경로 처리 클래스.
 * <p>명시적 bookstie.data.dir 절대경로, JAR 옆 data, 확인된 프로젝트의 data 순으로 결정함.
 * 선택한 디렉터리가 없으면 자동 생성하지 않고 원인과 설정 방법을 안내함.</p>
 */
public final class AppPaths {
    private static Path directory;
    private static final Set<String> DATA_FILES = Set.of("users.txt", "admins.txt", "books.txt", "loans.txt");

    private AppPaths() {}

    /**
     * 최초 호출 시 외부 데이터 디렉터리를 결정하고 이후 동일한 경로를 반환함.
     * JAR과 클래스 출력 위치는 CodeSource URL을 URI로 변환해 공백·한글 경로를 보존함.
     *
     * @return 실제 위치로 정규화된 기존 데이터 디렉터리
     * @throws IllegalStateException 경로를 판별할 수 없거나 설정이 상대경로이거나 디렉터리에 접근할 수 없는 경우
     */
    public static synchronized Path dataDirectory() {
        if (directory != null) return directory;
        String configured = System.getProperty("bookstie.data.dir");
        try {
            Path candidate;
            if (configured != null) {
                candidate = Path.of(configured);
                if (!candidate.isAbsolute()) throw new IllegalStateException("bookstie.data.dir은 절대경로여야 합니다.");
            } else {
                var source = AppPaths.class.getProtectionDomain().getCodeSource();
                if (source == null) throw new IllegalStateException("클래스 또는 JAR의 위치 정보가 없습니다.");
                Path location = Path.of(source.getLocation().toURI());
                if (Files.isRegularFile(location) && location.getFileName().toString().endsWith(".jar")) {
                    candidate = location.getParent().resolve("data");
                } else if (Files.isDirectory(location)) {
                    Path project = location;
                    while (project != null && !(Files.isRegularFile(project.resolve("src/bookstie/Main.java"))
                            && Files.isDirectory(project.resolve("resources")))) {
                        project = project.getParent();
                    }
                    if (project == null) throw new IllegalStateException("클래스 출력 위치에서 Bookstie 프로젝트를 찾지 못했습니다.");
                    candidate = project.resolve("data");
                } else {
                    throw new IllegalStateException("지원하지 않는 실행 위치입니다.");
                }
            }
            if (!Files.isDirectory(candidate)) throw new IllegalStateException("데이터 디렉터리가 없거나 디렉터리가 아닙니다: " + candidate);
            directory = candidate.toRealPath();
            return directory;
        } catch (Exception e) {
            throw new IllegalStateException("데이터 경로 결정 실패: " + e.getMessage()
                    + " JAR 옆 또는 프로젝트에 data 디렉터리를 준비하거나, "
                    + "-Dbookstie.data.dir=<기존 데이터 디렉터리의 절대경로>를 지정하세요. "
                    + "초기 예제 준비: python3 scripts/init-data.py --target <데이터 절대경로>", e);
        }
    }

    /**
     * 공통 데이터 디렉터리 아래의 지정 데이터 파일 경로를 반환함. 파일 생성이나 덮어쓰기는 수행하지 않음.
     *
     * @param filename users.txt, admins.txt, books.txt, loans.txt 중 사용할 파일명
     * @return 공통 데이터 디렉터리에 속하는 절대 파일 경로
     * @throws IllegalArgumentException 허용된 데이터 파일명이 아니거나 null인 경우
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public static Path dataFile(String filename) {
        if (filename == null || !DATA_FILES.contains(filename)) throw new IllegalArgumentException("허용되지 않은 데이터 파일명");
        return dataDirectory().resolve(filename);
    }
}
