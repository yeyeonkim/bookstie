package bookstie.model;

import java.time.LocalDate;

/**
 * 회원과 도서를 연결하는 대출 기록으로 대출일, 반납 예정일, 반납일, 대출 상태와 별점을 보관함.
 * <p>별점이 있으면 완독으로 판단함. 별점 등록과 관리자 반납은 독립적이므로, 완독 여부와 반납 상태는 서로 변경하지 않음. 이 모델의 변경은 파일에 자동 저장되지 않음.</p>
 */
public class Loan {

    private final String loanId;
    private final String userId;
    private final String bookId;

    private final LocalDate loanDate;
    private final LocalDate dueDate;

    private LocalDate returnDate;
    private LoanStatus status;
    private Integer rating; // null: 미평가, 1~5: 평가 및 완독

    // 새로운 대출을 생성할 때 사용
    /**
     * 반납일과 별점이 null이고 상태가 LOANED인 새 대출 기록을 만듦. 입력값의 유효성은 검사하지 않음.
     *
     * @param loanId 대출 ID
     * @param userId 대출 회원 ID
     * @param bookId 대출 도서 ID
     * @param loanDate 대출일
     * @param dueDate 반납 예정일
     */
    public Loan(
            String loanId,
            String userId,
            String bookId,
            LocalDate loanDate,
            LocalDate dueDate
    ) {
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = LoanStatus.LOANED;
    }

    // loans.txt에서 기존 대출 기록을 읽어올 때 사용
    /**
     * 별점이 없는 기존 대출 기록을 복원함. 날짜와 상태의 일관성은 검사하지 않음.
     *
     * @param loanId 대출 ID
     * @param userId 대출 회원 ID
     * @param bookId 대출 도서 ID
     * @param loanDate 대출일
     * @param dueDate 반납 예정일
     * @param returnDate 반납일; 미반납 기록은 null
     * @param status 대출 상태
     */
    public Loan(
            String loanId,
            String userId,
            String bookId,
            LocalDate loanDate,
            LocalDate dueDate,
            LocalDate returnDate,
            LoanStatus status
    ) {
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    /**
     * 별점을 포함한 대출 기록을 복원함. 별점이 있으면 완독으로 판단하며 반납 상태와는 독립적임.
     *
     * @param loanId 대출 ID
     * @param userId 대출 회원 ID
     * @param bookId 대출 도서 ID
     * @param loanDate 대출일
     * @param dueDate 반납 예정일
     * @param returnDate 반납일; 미반납 기록은 null
     * @param status 대출 상태
     * @param rating 별점 1~5; 미평가이면 null
     * @throws IllegalArgumentException 별점이 null이 아니면서 1~5 범위를 벗어난 경우
     */
    public Loan(String loanId, String userId, String bookId, LocalDate loanDate,
                LocalDate dueDate, LocalDate returnDate, LoanStatus status, Integer rating) {
        this(loanId, userId, bookId, loanDate, dueDate, returnDate, status);
        if (rating != null) setRating(rating);
    }

    /**
     * 등록된 별점을 반환함.
     *
     * @return 별점 1~5; 미평가이면 null
     */
    public Integer getRating() { return rating; }

    /**
     * 별점 등록 여부로 완독을 판단함. 반납 상태는 검사하지 않음.
     *
     * @return 별점이 있으면 true, 미평가이면 false
     */
    public boolean isCompleted() { return rating != null; }

    /**
     * 별점을 등록하거나 변경해 완독으로 표시함. 반납일과 대출 상태는 변경하지 않음.
     *
     * @param rating 등록할 별점 1~5
     * @throws IllegalArgumentException 별점이 1~5 범위를 벗어난 경우
     */
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1~5점이어야 합니다.");
        }
        this.rating = rating;
    }

    /**
     * 대출 ID를 반환함.
     *
     * @return 보관 중인 대출 ID; 생성자에 null을 전달했다면 null
     */
    public String getLoanId() {
        return loanId;
    }

    /**
     * 회원 ID를 반환함.
     *
     * @return 보관 중인 회원 ID; 생성자에 null을 전달했다면 null
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 도서 ID를 반환함.
     *
     * @return 보관 중인 도서 ID; 생성자에 null을 전달했다면 null
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * 대출일을 반환함.
     *
     * @return 보관 중인 대출일; 생성자에 null을 전달했다면 null
     */
    public LocalDate getLoanDate() {
        return loanDate;
    }

    /**
     * 반납 예정일을 반환함.
     *
     * @return 보관 중인 반납 예정일; 생성자에 null을 전달했다면 null
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * 반납일을 반환함.
     *
     * @return 보관 중인 반납일; 새 대출이거나 null로 설정된 경우 null
     */
    public LocalDate getReturnDate() {
        return returnDate;
    }

    /**
     * 대출 상태를 반환함.
     *
     * @return 보관 중인 대출 상태; 생성자에 null을 전달했다면 null
     */
    public LoanStatus getStatus() {
        return status;
    }

    /**
     * 대출 상태로 반납 여부를 판단함.
     *
     * @return 상태가 RETURNED이면 true, 그 외에는 false
     */
    public boolean isReturned() {
        return status == LoanStatus.RETURNED;
    }

    /**
     * 미반납 기록의 반납 예정일이 기준일보다 이전인지 판단함. 상태를 변경하지 않으며 예정일 당일은 연체가 아님.
     *
     * @param today 판단 기준일
     * @return 미반납이고 기준일이 예정일 이후이면 true, 반납되었거나 예정일 이전 또는 당일이면 false
     * @throws NullPointerException 미반납 기록에서 기준일 또는 반납 예정일이 null인 경우
     */
    public boolean isOverdue(LocalDate today) {
        return !isReturned() && today.isAfter(dueDate);
    }

    /**
     * 미반납 기록의 상태를 OVERDUE로 바꿈. 반납된 기록은 유지하며 날짜는 검사하지 않음.
     */
    public void markOverdue() {
        if (!isReturned()) {
            status = LoanStatus.OVERDUE;
        }
    }

    /**
     * 반납일을 설정하고 상태를 RETURNED로 바꿈. 별점은 유지하며 기존 상태나 날짜를 검증하지 않음.
     *
     * @param returnDate 설정할 반납일; null도 그대로 저장
     */
    public void returnLoan(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.status = LoanStatus.RETURNED;
    }
} //반납처리시 Loaned->AVAILABLE 은 LoanService 처리