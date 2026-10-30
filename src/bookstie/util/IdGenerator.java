package bookstie.util;
//book, bookid 자동 생성
import bookstie.model.Book;
import bookstie.model.Loan;
import bookstie.repository.BookRepository;
import bookstie.repository.LoanRepository;

/**
 * 저장된 ID의 숫자 부분에서 최댓값을 구해 다음 도서·대출 ID를 만듦. 숫자로 해석할 수 없는 접미사는 건너뛰며 도서 ID 생성에는 과거 대출에 남은 도서 ID도 포함함.
 */
public class IdGenerator {

    private IdGenerator() {
    }

    /**
     * 도서 목록과 과거 대출 기록의 B 접두사 ID에서 숫자 최댓값 다음 ID를 만듦. 숫자 접미사를 해석할 수 없는 ID는 건너뜀.
     *
     * @param bookRepository 전체 도서를 조회할 저장소
     * @return B 접두사와 최소 세 자리 숫자로 구성한 다음 도서 ID
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있거나 데이터 디렉터리를 결정할 수 없는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 잘못된 경우
     * @throws NullPointerException 도서 저장소가 null인 경우
     */
    public static String generateBookId(BookRepository bookRepository) {

        int maxNumber = 0;

        for (Book book : bookRepository.findAll()) {

            String id = book.getBookId();

            if (id != null && id.startsWith("B")) {

                try {
                    int number =
                            Integer.parseInt(id.substring(1));

                    if (number > maxNumber) {
                        maxNumber = number;
                    }

                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 삭제된 도서의 대출 기록에 남아 있는 ID도 재사용하지 않는다.
        for (Loan loan : new LoanRepository().findAll()) {
            String id = loan.getBookId();
            if (id != null && id.startsWith("B")) {
                try {
                    maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return String.format(
                "B%03d",
                maxNumber + 1
        );
    }

    /**
     * 대출 목록의 L 접두사 ID에서 숫자 최댓값 다음 ID를 만듦. 숫자 접미사를 해석할 수 없는 ID는 건너뜀.
     *
     * @param loanRepository 대출 기록을 조회할 저장소
     * @return L 접두사와 최소 세 자리 숫자로 구성한 다음 대출 ID
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws NullPointerException 대출 저장소가 null인 경우
     */
    public static String generateLoanId(LoanRepository loanRepository) {

        int maxNumber = 0;

        for (Loan loan : loanRepository.findAll()) {

            String id = loan.getLoanId();

            if (id != null && id.startsWith("L")) {

                try {
                    int number =
                            Integer.parseInt(id.substring(1));

                    if (number > maxNumber) {
                        maxNumber = number;
                    }

                } catch (NumberFormatException ignored) {
                }
            }
        }

        return String.format(
                "L%03d",
                maxNumber + 1
        );
    }
}
