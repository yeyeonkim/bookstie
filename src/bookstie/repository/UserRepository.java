package bookstie.repository;

import bookstie.util.AppPaths;

import bookstie.model.User;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * AppPaths로 결정한 외부 데이터 디렉터리의 users.txt에 UTF-8 탭으로 구분된 ID·이름·비밀번호를 저장하고 조회함.
 * <p>빈 행과 필드 수가 3개가 아닌 행은 건너뜀. 중복 검사는 저장소에서 수행하지 않으며, 파일 입출력 오류는 출력하고 호출자에게 전달하지 않음.</p>
 */
public class UserRepository {

    private final Path FILE_PATH = AppPaths.dataFile("users.txt");

    /**
     * 공통 데이터 디렉터리의 users.txt가 없으면 생성을 시도함. 파일 생성 입출력 오류는 출력함.
     *
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public UserRepository() {
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
            System.out.println("users.txt 파일 생성 오류: " + e.getMessage());
        }
    }

    /**
     * 탭으로 구분된 회원 정보를 읽음. 빈 행과 필드 수가 3개가 아닌 행을 건너뛰고, 입출력 오류는 출력만 함.
     *
     * @return 오류 발생 전까지 읽은 회원 목록; 유효한 기록이 없거나 첫 기록 전 읽기 오류가 나면 빈 목록
     */
    public List<User> findAll() { //모든 회원 읽기

        List<User> users = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(FILE_PATH.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\t");

                if (parts.length != 3) {
                    continue;
                }

                String id = parts[0];
                String name = parts[1];
                String password = parts[2];

                users.add(new User(id, name, password));
            }

        } catch (IOException e) {
            System.out.println("사용자 파일 읽기 오류: " + e.getMessage());
        }

        return users;
    }

    /**
     * ID가 정확히 일치하는 첫 회원을 조회함. 파일 읽기 입출력 오류는 출력만 함.
     *
     * @param id 회원 ID
     * @return 첫 일치 회원; 읽은 목록에 일치하는 ID가 없으면 null. null 입력도 일치하지 않으며 읽기 오류로 찾지 못해도 null
     */
    public User findById(String id) { //특정 id 회원 찾기

        for (User user : findAll()) {

            if (user.getId().equals(id)) {
                return user;
            }
        }

        return null;
    }

    /**
     * 회원 ID 존재 여부를 조회함.
     *
     * @param id 확인할 회원 ID
     * @return 읽은 목록에서 동일 ID를 찾으면 true; null 입력, 일치 회원 없음 또는 읽기 오류로 찾지 못한 경우 false
     */
    public boolean existsById(String id) { //id 중복 검사
        return findById(id) != null;
    }

    /**
     * 회원 정보를 탭 구분 행으로 파일 끝에 추가함. 중복 ID를 검사하지 않으며 입출력 오류는 출력만 함.
     *
     * @param user 추가할 회원
     * @throws NullPointerException 회원이 null이고 쓰기 작업에서 회원 정보를 참조하는 경우
     */
    public void save(User user) { //새 회원 저장

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_PATH.toFile(), java.nio.charset.StandardCharsets.UTF_8, true))) {

            writer.write(
                    user.getId() + "\t"
                            + user.getName() + "\t"
                            + user.getPassword()
            );

            writer.newLine();

        } catch (IOException e) {
            System.out.println("사용자 저장 오류: " + e.getMessage());
        }
    }

    /**
     * 동일 ID인 회원을 제외한 조회 결과로 파일을 다시 씀. 회원이 없으면 조회 결과를 그대로 쓰며 읽기·쓰기 입출력 오류는 출력만 함.
     *
     * @param id 삭제할 회원 ID
     */
    public void deleteById(String id) { //회원 삭제

        List<User> users = findAll();

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_PATH.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {

            for (User user : users) {

                if (!user.getId().equals(id)) {

                    writer.write(
                            user.getId() + "\t"
                                    + user.getName() + "\t"
                                    + user.getPassword()
                    );

                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("사용자 삭제 오류: " + e.getMessage());
        }
    }
}