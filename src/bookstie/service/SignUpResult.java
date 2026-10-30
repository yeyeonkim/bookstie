package bookstie.service;

/**
 * 회원가입의 입력 검사 결과를 나타냄. 빈 입력, 비밀번호 불일치, 중복 ID 순으로 검사한 뒤 저장을 요청함.
 */
public enum SignUpResult {

    /**
     * 입력 검사를 통과하고 회원 저장을 요청한 결과; 파일 저장 성공을 보장하지 않음.
     */
    SUCCESS,
    /**
     * 입력 중 null 또는 trim 후 빈 문자열이 있는 결과.
     */
    EMPTY_FIELD,
    /**
     * 비밀번호와 확인 비밀번호가 다른 결과.
     */
    PASSWORD_MISMATCH,
    /**
     * 동일한 회원 ID가 이미 존재하는 결과.
     */
    DUPLICATE_ID
}