package bookstie.model;

/**
 * 관리자 ID와 비밀번호를 보관하는 인증용 모델. 생성 시 받은 값을 검증하거나 변환하지 않으며, 관리자 저장소와 인증 서비스에서 사용함.
 */
public class Admin {

    private final String id;
    private final String password;

    /**
     * 전달받은 계정 정보를 검증이나 변환 없이 보관함.
     *
     * @param id 관리자 ID
     * @param password 비밀번호
     */
    public Admin(String id, String password) {
        this.id = id;
        this.password = password;
    }

    /**
     * 계정 ID를 반환함.
     *
     * @return 생성자에 전달한 계정 ID; 전달값이 null이면 null
     */
    public String getId() {
        return id;
    }

    /**
     * 비밀번호를 반환함.
     *
     * @return 생성자에 전달한 비밀번호; 전달값이 null이면 null
     */
    public String getPassword() {
        return password;
    }
}
