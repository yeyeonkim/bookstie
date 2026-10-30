package bookstie.model;

/**
 * 회원 ID, 이름과 비밀번호를 보관하는 회원 모델. 생성 시 받은 값을 그대로 보관하며, 회원 정보 조회와 로그인에 사용함.
 */
public class User {

    private final String id;
    private final String name;
    private final String password;

    /**
     * 전달받은 계정 정보를 검증이나 변환 없이 보관함.
     *
     * @param id 계정 ID
     * @param name 회원 이름
     * @param password 비밀번호
     */
    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
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
     * 회원 이름을 반환함.
     *
     * @return 생성자에 전달한 회원 이름; 전달값이 null이면 null
     */
    public String getName() {
        return name;
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