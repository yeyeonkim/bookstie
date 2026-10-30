package bookstie.repository;

import bookstie.util.AppPaths;

import bookstie.model.Admin;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * AppPaths로 결정한 외부 데이터 디렉터리의 admins.txt에서 UTF-8 탭으로 구분된 ID·비밀번호를 읽는 저장소. 빈 행과 필드 수가 2개가 아닌 행은 건너뛰며, 파일 생성·읽기 입출력 오류는 출력하고 호출자에게 전달하지 않음.
 */
public class AdminRepository {

    private final Path FILE_PATH = AppPaths.dataFile("admins.txt");

    /**
     * 공통 데이터 디렉터리의 admins.txt가 없으면 생성을 시도함. 파일 생성 입출력 오류는 출력함.
     *
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public AdminRepository() {
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        File file = FILE_PATH.toFile();

        try {
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            System.out.println("admins.txt 파일 생성 오류: " + e.getMessage());
        }
    }

    /**
     * 탭으로 구분된 관리자 정보를 읽음. 빈 행과 필드 수가 2개가 아닌 행을 건너뛰고, 입출력 오류는 출력만 함.
     *
     * @return 오류 발생 전까지 읽은 관리자 목록; 유효한 기록이 없거나 첫 기록 전 읽기 오류가 나면 빈 목록
     */
    public List<Admin> findAll() {

        List<Admin> admins = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(FILE_PATH.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\t");

                if (parts.length != 2) {
                    continue;
                }

                String id = parts[0];
                String password = parts[1];

                admins.add(new Admin(id, password));
            }

        } catch (IOException e) {
            System.out.println("관리자 파일 읽기 오류: " + e.getMessage());
        }

        return admins;
    }

    /**
     * ID가 정확히 일치하는 첫 관리자을 조회함. 파일 읽기 입출력 오류는 출력만 함.
     *
     * @param id 관리자 ID
     * @return 첫 일치 관리자; 읽은 목록에 일치하는 ID가 없으면 null. null 입력도 일치하지 않으며 읽기 오류로 찾지 못해도 null
     */
    public Admin findById(String id) {

        for (Admin admin : findAll()) {

            if (admin.getId().equals(id)) {
                return admin;
            }
        }

        return null;
    }
}
