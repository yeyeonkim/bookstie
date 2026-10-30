package bookstie.model;

/**
 * 도서 ID, 제목, 저자와 현재 대출 가능 상태를 보관함. 상태 변경 메서드는 이 객체만 변경하며, 대출 기록 생성 및 파일 저장은 서비스와 저장소가 담당함.
 */
public class Book {

    private final String bookId;
    private final String title;
    private final String author;
    private BookStatus status;

    // 관리자가 새로운 책을 등록할 때 사용
    /**
     * 대출 가능한 상태의 새 도서를 만듦. 입력값은 검증하거나 변환하지 않음.
     *
     * @param bookId 도서 ID
     * @param title 도서 제목
     * @param author 저자
     */
    public Book(String bookId, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.status = BookStatus.AVAILABLE;
    }

    // books.txt에서 기존 책을 읽어올 때 사용
    /**
     * 저장된 상태를 포함한 도서 정보를 복원함. 입력값은 그대로 보관함.
     *
     * @param bookId 도서 ID
     * @param title 도서 제목
     * @param author 저자
     * @param status 도서 상태
     */
    public Book(String bookId, String title, String author, BookStatus status) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.status = status;
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
     * 제목를 반환함.
     *
     * @return 보관 중인 제목; 생성자에 null을 전달했다면 null
     */
    public String getTitle() {
        return title;
    }

    /**
     * 저자를 반환함.
     *
     * @return 보관 중인 저자; 생성자에 null을 전달했다면 null
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 도서 상태를 반환함.
     *
     * @return 보관 중인 도서 상태; 생성자에 null을 전달했다면 null
     */
    public BookStatus getStatus() {
        return status;
    }

    /**
     * 현재 도서 상태가 대출 가능한지 확인함.
     *
     * @return 상태가 AVAILABLE이면 true, 그 외에는 false
     */
    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE;
    }

    /**
     * 현재 객체의 상태를 LOANED로 설정함. 기존 상태 검증, 대출 기록 생성과 파일 저장은 수행하지 않음.
     */
    public void borrow() {
        status = BookStatus.LOANED;
    } //책상태만 바꾸는 메소드임 실제 대출 LoanService 처리

    /**
     * 현재 객체의 상태를 AVAILABLE로 설정함. 대출 기록의 반납 처리와 파일 저장은 수행하지 않음.
     */
    public void returnBook() {
        status = BookStatus.AVAILABLE;
    }
}