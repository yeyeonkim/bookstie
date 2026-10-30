package bookstie.model;

/**
 * 도서의 대출 가능 여부를 나타냄. 개별 대출 기록의 연체 및 반납 상태는 LoanStatus로 관리함.
 */
public enum BookStatus {
    /**
     * 대출 가능한 도서 상태.
     */
    AVAILABLE,
    /**
     * 대출 중인 도서 상태.
     */
    LOANED
}