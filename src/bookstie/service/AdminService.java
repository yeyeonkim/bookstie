package bookstie.service;

import bookstie.model.Book;
import bookstie.model.Loan;
import bookstie.model.LoanStatus;
import bookstie.model.User;
import bookstie.repository.BookRepository;
import bookstie.repository.LoanRepository;
import bookstie.repository.UserRepository;
import bookstie.util.IdGenerator;

import java.util.List;

/**
 * 관리자 화면에서 사용하는 도서 등록·삭제, 회원 조회·삭제 및 대출 조회·반납을 제공함.
 * <p>반납은 LoanService에 위임하며 별점에 따른 완독 처리와 독립적임. 이 서비스 자체는 관리자 세션을 검사하지 않음. 회원 삭제 시 과거 대출 기록은 유지함.</p>
 */
public class AdminService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final LoanService loanService;

    /**
     * 도서·대출·회원 저장소와 반납을 처리할 대출 서비스를 준비함.
     *
     * @throws java.io.UncheckedIOException 대출 파일이나 상위 디렉터리를 생성하지 못한 경우
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public AdminService() {
        this.bookRepository = new BookRepository();
        this.loanRepository = new LoanRepository();
        this.userRepository = new UserRepository();
        this.loanService = new LoanService();
    }

    /**
     * 제목과 저자의 양끝 공백을 제거하고 자동 생성한 ID로 대출 가능한 도서를 저장함. 추가 저장의 입출력 오류는 저장소에서 출력만 함.
     *
     * @param title 도서 제목
     * @param author 저자
     * @return 생성한 도서; 제목 또는 저자가 null·trim 후 빈 문자열이거나 탭·캐리지 리턴·줄바꿈을 포함하면 null. 도서 반환이 추가 저장 성공을 보장하지는 않음
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 잘못되었거나 생성한 도서 ID가 이미 존재하는 경우
     */
    public Book addBook(
            String title,
            String author
    ) {

        if (isBlank(title) || isBlank(author)
                || title.matches("(?s).*[\\t\\r\\n].*")
                || author.matches("(?s).*[\\t\\r\\n].*")) {
            return null;
        }

        String bookId =
                IdGenerator.generateBookId(
                        bookRepository
                );

        Book book =
                new Book(
                        bookId,
                        title.trim(),
                        author.trim()
                );

        bookRepository.save(book);

        return book;
    }

    /**
     * 대출 가능한 도서이고 미반납 대출 기록이 없으면 도서 삭제를 요청함. 과거 대출 기록은 유지하며 삭제의 입출력 오류는 저장소가 출력만 함.
     *
     * @param bookId 삭제할 도서 ID
     * @return 삭제를 요청하면 true; 도서가 없거나 AVAILABLE이 아니거나 미반납 기록이 있으면 false. true가 파일 저장 성공을 보장하지는 않음
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 잘못된 경우
     */
    public boolean deleteBook(String bookId) {

        Book book =
                bookRepository.findById(bookId);

        if (book == null || !book.isAvailable()) {
            return false;
        }

        Loan activeLoan =
                loanRepository
                        .findActiveByBookId(bookId);

        if (activeLoan != null) {
            return false;
        }

        bookRepository.deleteById(bookId);

        return true;
    }

    /**
     * 전체 도서 목록을 조회함.
     *
     * @return 전체 도서 목록; 없으면 빈 목록
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * 전체 대출 기록을 조회함.
     *
     * @return 전체 대출 기록 목록; 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * LoanService에 관리자 반납을 위임함. 반납일·대출 상태·도서 상태를 갱신하며 별점과 완독 여부는 유지함.
     *
     * @param loanId 반납할 대출 ID
     * @return 반납 저장을 마치면 true; 기록이 없거나 이미 반납되었거나 도서가 없으면 false
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 도서 상태 값이 잘못되었거나 갱신 대상 대출이 사라진 경우
     */
    public boolean returnBook(String loanId) {
        return loanService.returnBook(loanId);
    }

    /**
     * 전체 회원을 조회함. 저장소 읽기 오류는 출력만 함.
     *
     * @return 읽은 회원 목록; 기록이 없거나 첫 기록을 읽기 전에 입출력 오류가 나면 빈 목록
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 회원이 존재하고 모든 대출 기록이 RETURNED이면 회원 삭제를 요청함. 과거 대출 기록은 삭제하지 않으며 회원 삭제 입출력 오류는 출력만 함.
     *
     * @param userId 삭제할 회원 ID
     * @return 삭제를 요청하면 true; 회원이 없거나 미반납 기록이 있으면 false. 회원 읽기 오류로 찾지 못해도 false이며 true가 파일 저장 성공을 보장하지는 않음
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public boolean deleteUser(String userId) {

        User user =
                userRepository.findById(userId);

        if (user == null) {
            return false;
        }

        for (Loan loan :
                loanRepository.findByUserId(userId)) {

            if (loan.getStatus()
                    != LoanStatus.RETURNED) {

                return false;
            }
        }

        userRepository.deleteById(userId);

        return true;
    }

    private boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
