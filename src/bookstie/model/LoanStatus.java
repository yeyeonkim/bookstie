package bookstie.model;

/**
 * 개별 대출 기록의 대출, 연체 및 반납 상태. 별점에 따른 완독 여부와는 독립적임.
 */
public enum LoanStatus {
    /**
     * 정상 대출 상태.
     */
    LOANED, //정상 대출
    /**
     * 연체로 표시된 미반납 상태.
     */
    OVERDUE, //연체중
    /**
     * 관리자 반납 처리가 완료된 상태; 별점 유무와는 무관함.
     */
    RETURNED //반납 완료
}