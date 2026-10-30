package bookstie.service;

import bookstie.model.Book;
import bookstie.repository.BookRepository;

import java.util.List;

/**
 * 도서 저장소를 이용해 제목 부분 검색, ID 조회, 전체 조회와 대출 가능 상태 확인을 제공함. 도서의 상태만 확인하며 활성 대출 기록을 별도로 검사하지 않음.
 */
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 도서 파일 저장소를 준비함.
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public BookService() {
        this.bookRepository = new BookRepository();
    }

    /**
     * 검색어의 양끝 공백을 제거한 뒤 대소문자를 구분하지 않는 제목 부분 검색을 수행함.
     *
     * @param title 제목 검색어
     * @return 검색 도서 목록; 입력이 null 또는 trim 후 빈 문자열이거나 일치하는 도서가 없으면 빈 목록
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public List<Book> searchByTitle(String title) {

        if (title == null || title.trim().isEmpty()) {
            return List.of();
        }

        return bookRepository.findByTitle(title.trim());
    }

    /**
     * 전달받은 ID와 정확히 일치하는 도서를 조회함. 빈 값 판단 외에 ID를 trim하지 않음.
     *
     * @param bookId 도서 ID
     * @return 조회된 도서; ID가 null 또는 trim 후 빈 문자열이거나 일치하는 도서가 없으면 null
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public Book getBookById(String bookId) {

        if (bookId == null || bookId.trim().isEmpty()) {
            return null;
        }

        return bookRepository.findById(bookId);
    }

    /**
     * 전체 도서 목록을 조회함.
     *
     * @return 도서 목록; 읽은 도서가 없으면 빈 목록
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * 조회한 도서의 상태가 AVAILABLE인지 확인함.
     *
     * @param bookId 도서 ID
     * @return 도서가 존재하고 AVAILABLE이면 true; ID가 null·공백이거나 도서가 없거나 다른 상태이면 false
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public boolean isAvailable(String bookId) {

        Book book = getBookById(bookId);

        return book != null
                && book.isAvailable();
    }
}