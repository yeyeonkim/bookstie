package bookstie.service;

import bookstie.model.Book;
import bookstie.model.Loan;
import bookstie.model.LoanStatus;
import bookstie.repository.BookRepository;
import bookstie.repository.LoanRepository;
import bookstie.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 대출 생성, 관리자 반납, 별점 저장과 대출 기록 조회를 담당함. 대출 기간은 대출일로부터 14일.
 * <p>별점 등록은 완독 여부만 바꾸며 반납을 수행하지 않음. 관리자 반납은 반납일·대출 상태·도서 상태를 변경하고 별점은 유지함. 연체 상태 갱신은 checkOverdue를 호출할 때 수행함.</p>
 */
public class LoanService {

    private static final int LOAN_PERIOD_DAYS = 14;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    /**
     * 대출 파일 저장소와 도서 파일 저장소를 준비함.
     *
     * @throws java.io.UncheckedIOException 대출 파일이나 상위 디렉터리를 생성하지 못한 경우
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public LoanService() {

        this.loanRepository =
                new LoanRepository();

        this.bookRepository =
                new BookRepository();
    }

    /**
     * 오늘을 대출일로, 14일 후를 반납 예정일로 설정해 대출 기록을 저장하고 도서를 LOANED로 갱신함. 회원 존재 여부는 검사하지 않음.
     * <p>도서 상태 갱신의 실행 예외 발생 시 이번 대출 기록 삭제를 시도함. 복구 실패 예외는 원래 예외의 suppressed 목록에 추가해 전달함.</p>
     *
     * @param userId 대출 기록에 저장할 회원 ID
     * @param bookId 대출할 도서 ID
     * @return 생성한 대출 기록; 도서가 없거나 AVAILABLE이 아니거나 미반납 대출 기록이 있으면 null
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태가 잘못되었거나 생성한 대출 ID가 이미 존재하는 경우
     */
    public Loan borrowBook(
            String userId,
            String bookId
    ) {

        Book book =
                bookRepository.findById(bookId);

        if (book == null) {
            return null;
        }

        if (!book.isAvailable()) {
            return null;
        }

        Loan activeLoan =
                loanRepository
                        .findActiveByBookId(bookId);

        if (activeLoan != null) {
            return null;
        }

        LocalDate loanDate =
                LocalDate.now();

        LocalDate dueDate =
                calculateDueDate(loanDate);

        String loanId =
                IdGenerator.generateLoanId(
                        loanRepository
                );

        Loan loan =
                new Loan(
                        loanId,
                        userId,
                        bookId,
                        loanDate,
                        dueDate
                );

        loanRepository.save(loan);

        book.borrow();
        try {
            bookRepository.update(book);
        } catch (RuntimeException e) {
            try { loanRepository.deleteById(loanId); } catch (RuntimeException rollback) {
                e.addSuppressed(rollback);
            }
            throw e;
        }

        return loan;
    }

    /**
     * 관리자 반납을 처리해 반납일을 오늘로, 대출 상태를 RETURNED로, 도서 상태를 AVAILABLE로 저장함. 별점과 완독 여부는 유지함.
     * <p>도서 갱신의 실행 예외 발생 시 원래 대출 기록 복구를 시도하고, 복구 실패 예외는 suppressed 목록에 추가해 전달함.</p>
     *
     * @param loanId 반납할 대출 ID
     * @return 두 상태 저장을 마치면 true; 대출이 없거나 이미 반납되었거나 연결된 도서가 없으면 false
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 도서 상태 값이 잘못되었거나 갱신 대상 대출 기록이 사라진 경우
     */
    public boolean returnBook(String loanId) {

        Loan loan =
                loanRepository.findById(loanId);

        if (loan == null) {
            return false;
        }

        if (loan.isReturned()) {
            return false;
        }

        Book book =
                bookRepository.findById(
                        loan.getBookId()
                );

        if (book == null) {
            return false;
        }

        Loan original = loanRepository.findById(loanId);

        loan.returnLoan(
                LocalDate.now()
        );

        loanRepository.update(loan);

        book.returnBook();
        try {
            bookRepository.update(book);
        } catch (RuntimeException e) {
            try { loanRepository.update(original); } catch (RuntimeException rollback) {
                e.addSuppressed(rollback);
            }
            throw e;
        }

        return true;
    }

    /**
     * 본인의 대출 기록에 별점 1~5를 저장함. 대출 중·연체·반납 여부와 무관하게 등록 또는 변경할 수 있고, 완독으로 표시되지만 반납 처리는 수행하지 않음.
     *
     * @param userId 평가할 회원 ID; 대출 기록의 회원 ID와 비교
     * @param loanId 평가할 대출 ID
     * @param rating 등록할 별점 1~5
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 대출이 없거나 본인의 기록이 아니거나 별점이 1~5가 아니거나 갱신 대상 기록이 사라진 경우
     */
    public void rateLoan(String userId, String loanId, int rating) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null || !loan.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 대출 기록만 평가할 수 있습니다.");
        }
        loan.setRating(rating);
        loanRepository.update(loan);
    }

    /**
     * 회원의 전체 대출 기록을 조회함.
     *
     * @param userId 회원 ID
     * @return 회원의 대출 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getUserLoans(String userId) {
        return loanRepository.findByUserId(userId);
    }

    // 메인 책장의 기존 올해 범위는 대출일을 기준으로 적용한다.
    /**
     * 대출일의 연도가 지정 연도인 회원 대출 기록을 조회함. 반납일이나 완독 여부는 기준에 포함하지 않음.
     *
     * @param userId 회원 ID
     * @param year 대출일 기준 연도
     * @return 지정 연도에 대출한 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getLoansByYear(String userId, int year) {
        return getUserLoans(userId).stream()
                .filter(loan -> loan.getLoanDate().getYear() == year).toList();
    }

    /**
     * 저장된 상태가 LOANED 또는 OVERDUE인 회원 대출 기록을 조회함. 연체 상태를 자동 갱신하지 않음.
     *
     * @param userId 회원 ID
     * @return 현재 대출·연체 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getCurrentLoans(
            String userId
    ) {

        List<Loan> result =
                new ArrayList<>();

        for (Loan loan :
                loanRepository.findByUserId(userId)) {

            if (loan.getStatus()
                    == LoanStatus.LOANED
                    || loan.getStatus()
                    == LoanStatus.OVERDUE) {

                result.add(loan);
            }
        }

        return result;
    }

    /**
     * 저장된 상태가 RETURNED인 회원 대출 기록을 조회함. 완독 여부는 검사하지 않음.
     *
     * @param userId 회원 ID
     * @return 반납 완료 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getReturnedLoans(
            String userId
    ) {

        List<Loan> result =
                new ArrayList<>();

        for (Loan loan :
                loanRepository.findByUserId(userId)) {

            if (loan.getStatus()
                    == LoanStatus.RETURNED) {

                result.add(loan);
            }
        }

        return result;
    }

    /**
     * 저장된 상태가 OVERDUE인 회원 대출 기록을 조회함. 현재 날짜로 상태를 자동 갱신하지 않음.
     *
     * @param userId 회원 ID
     * @return 연체 상태 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getOverdueLoans(
            String userId
    ) {

        List<Loan> result =
                new ArrayList<>();

        for (Loan loan :
                loanRepository.findByUserId(userId)) {

            if (loan.getStatus()
                    == LoanStatus.OVERDUE) {

                result.add(loan);
            }
        }

        return result;
    }

    /**
     * 반납 완료 기록 중 반납일이 null이 아니고 그 연도가 지정 연도인 기록을 조회함.
     *
     * @param userId 회원 ID
     * @param year 반납일 기준 연도
     * @return 지정 연도에 반납한 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getReturnedLoansByYear(
            String userId,
            int year
    ) {

        List<Loan> result =
                new ArrayList<>();

        for (Loan loan :
                getReturnedLoans(userId)) {

            LocalDate returnDate =
                    loan.getReturnDate();

            if (returnDate != null
                    && returnDate.getYear()
                    == year) {

                result.add(loan);
            }
        }

        return result;
    }

    /**
     * 모든 회원의 대출 기록을 조회함.
     *
     * @return 전체 대출 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * 도서에 연결된 첫 미반납 대출 기록을 조회함.
     *
     * @param bookId 도서 ID
     * @return 첫 미반납 대출 기록; 없으면 null
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public Loan getActiveLoanByBook(
            String bookId
    ) {

        return loanRepository
                .findActiveByBookId(bookId);
    }

    /**
     * 오늘이 반납 예정일 이후인 미반납 기록을 OVERDUE로 변경해 저장함. 이미 OVERDUE인 기록과 반납 완료 기록은 변경하지 않음.
     *
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 갱신 대상 대출 기록이 사라진 경우
     */
    public void checkOverdue() {

        LocalDate today =
                LocalDate.now();

        for (Loan loan :
                loanRepository.findAll()) {

            if (loan.isOverdue(today)
                    && loan.getStatus()
                    != LoanStatus.OVERDUE) {

                loan.markOverdue();
                loanRepository.update(loan);
            }
        }
    }

    /**
     * 현재 대출·연체 기록 중 반납 예정일이 내일인 회원 기록을 조회함.
     *
     * @param userId 회원 ID
     * @return 반납 예정일이 내일인 기록 목록; 일치하는 기록이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> getDueTomorrowLoans(
            String userId
    ) {

        List<Loan> result =
                new ArrayList<>();

        LocalDate tomorrow =
                LocalDate.now().plusDays(1);

        for (Loan loan :
                getCurrentLoans(userId)) {

            if (loan.getDueDate()
                    .equals(tomorrow)) {

                result.add(loan);
            }
        }

        return result;
    }

    /**
     * 대출일에서 14일 후의 반납 예정일을 계산함.
     *
     * @param loanDate 대출일
     * @return 대출일에 14일을 더한 날짜
     * @throws NullPointerException 대출일이 null인 경우
     * @throws java.time.DateTimeException 계산 결과가 LocalDate의 지원 범위를 벗어난 경우
     */
    public LocalDate calculateDueDate(
            LocalDate loanDate
    ) {

        return loanDate
                .plusDays(LOAN_PERIOD_DAYS);
    }
}