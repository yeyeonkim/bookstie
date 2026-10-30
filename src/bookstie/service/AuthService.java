package bookstie.service;

import bookstie.model.Admin;
import bookstie.model.User;
import bookstie.repository.AdminRepository;
import bookstie.repository.UserRepository;
import bookstie.util.SessionManager;

/**
 * 회원가입과 회원·관리자 로그인을 처리하고 로그인 성공 시 세션을 설정함.
 * <p>빈 입력은 null 또는 trim 후 빈 문자열로 판단하며, 인증 비교와 회원 저장에는 전달받은 원래 값을 사용함. 사용자·관리자 저장소가 입출력 오류를 출력만 하므로 회원가입 성공 결과가 파일 저장 성공을 보장하지는 않음.</p>
 */
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    /**
     * 회원 및 관리자 파일 저장소를 준비함.
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public AuthService() {

        this.userRepository =
                new UserRepository();

        this.adminRepository =
                new AdminRepository();
    }

    /**
     * 빈 입력, 비밀번호 일치, 회원 ID 중복을 순서대로 검사한 뒤 회원 저장을 요청함. 입력값을 trim해 저장하지 않음.
     *
     * @param name 회원 이름
     * @param id 회원 ID
     * @param password 비밀번호
     * @param confirmPassword 확인 비밀번호
     * @return 빈 입력이면 EMPTY_FIELD, 비밀번호가 다르면 PASSWORD_MISMATCH, 기존 회원 ID이면 DUPLICATE_ID, 그 외에는 저장 요청 후 SUCCESS. 저장소가 출력만 하는 입출력 오류는 결과에 반영되지 않음
     */
    public SignUpResult signUp(
            String name,
            String id,
            String password,
            String confirmPassword
    ) {

        if (isBlank(name)
                || isBlank(id)
                || isBlank(password)
                || isBlank(confirmPassword)) {

            return SignUpResult.EMPTY_FIELD;
        }

        if (!password.equals(confirmPassword)) {

            return SignUpResult.PASSWORD_MISMATCH;
        }

        if (userRepository.existsById(id)) {

            return SignUpResult.DUPLICATE_ID;
        }

        User user =
                new User(
                        id,
                        name,
                        password
                );

        userRepository.save(user);

        return SignUpResult.SUCCESS;
    }
    /**
     * 회원 ID와 비밀번호를 비교하고 성공 시 해당 세션을 설정하며 다른 종류의 세션을 해제함. 실패 시 기존 세션은 변경하지 않음.
     *
     * @param id 회원 ID
     * @param password 비밀번호
     * @return 인증된 회원; ID·비밀번호가 null 또는 trim 후 빈 문자열이거나, 계정이 없거나, 비밀번호가 다르면 null. 저장소 읽기 오류로 계정을 찾지 못한 경우도 null
     */
    public User login(
            String id,
            String password
    ) {

        if (isBlank(id) || isBlank(password)) {
            return null;
        }

        User user =
                userRepository.findById(id);

        if (user == null) {
            return null;
        }

        if (!user.getPassword()
                .equals(password)) {

            return null;
        }

        SessionManager.loginUser(user);

        return user;
    }

    /**
     * 관리자 ID와 비밀번호를 비교하고 성공 시 해당 세션을 설정하며 다른 종류의 세션을 해제함. 실패 시 기존 세션은 변경하지 않음.
     *
     * @param id 관리자 ID
     * @param password 비밀번호
     * @return 인증된 관리자; ID·비밀번호가 null 또는 trim 후 빈 문자열이거나, 계정이 없거나, 비밀번호가 다르면 null. 저장소 읽기 오류로 계정을 찾지 못한 경우도 null
     */
    public Admin adminLogin(
            String id,
            String password
    ) {

        if (isBlank(id) || isBlank(password)) {
            return null;
        }

        Admin admin =
                adminRepository.findById(id);

        if (admin == null) {
            return null;
        }

        if (!admin.getPassword()
                .equals(password)) {

            return null;
        }

        SessionManager.loginAdmin(admin);

        return admin;
    }

    /**
     * 회원 저장소에 동일한 ID가 있는지 확인함. 관리자 ID는 검사하지 않음.
     *
     * @param id 확인할 회원 ID
     * @return 동일 ID가 있으면 true; 입력이 null 또는 trim 후 빈 문자열이거나 조회된 회원이 없으면 false. 저장소 읽기 오류로 찾지 못한 경우도 false
     */
    public boolean isDuplicateId(String id) {

        if (isBlank(id)) {
            return false;
        }

        return userRepository.existsById(id);
    }

    /**
     * 현재 회원 세션과 관리자 세션을 모두 해제함.
     */
    public void logout() {
        SessionManager.logout();
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}
