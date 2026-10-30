package bookstie.repository;

import bookstie.util.AppPaths;

import bookstie.model.Book;
import bookstie.model.BookStatus;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * AppPaths로 결정한 외부 데이터 디렉터리의 books.txt에서 ID·제목·저자·상태를 UTF-8 탭으로 구분해 관리함.
 * <p>빈 행과 필드 수가 4개가 아닌 행은 건너뛰며, 중복 ID와 잘못된 상태 값은 예외로 전달함. 읽기 및 상태 갱신의 입출력 오류는 예외로 전달하지만, 파일 생성·추가·삭제 중의 입출력 오류는 출력만 함. 갱신은 UTF-8 임시 파일을 원자적으로 교체함.</p>
 */
public class BookRepository {

    private final Path FILE_PATH = AppPaths.dataFile("books.txt");

    /**
     * 공통 데이터 디렉터리의 books.txt가 없으면 생성을 시도함. 파일 생성 입출력 오류는 출력함.
     *
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public BookRepository() {
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        File file = FILE_PATH.toFile();

        try {
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            System.out.println("books.txt 파일 생성 오류: " + e.getMessage());
        }
    }

    /**
     * 도서 파일의 유효한 4필드 행을 읽고 중복 ID와 상태 값을 확인함.
     *
     * @return 도서 목록; 유효한 4필드 행이 없으면 빈 목록
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public List<Book> findAll() {

        List<Book> books = new ArrayList<>();
        Set<String> ids = new HashSet<>();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(FILE_PATH.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\t");

                if (parts.length != 4) {
                    continue;
                }

                String bookId = parts[0];
                if (!ids.add(bookId)) {
                    throw new IllegalStateException("중복된 도서 ID입니다: " + bookId);
                }
                String title = parts[1];
                String author = parts[2];
                BookStatus status = BookStatus.valueOf(parts[3]);

                books.add(
                        new Book(
                                bookId,
                                title,
                                author,
                                status
                        )
                );
            }

        } catch (IOException e) {
            throw new UncheckedIOException("도서 파일 읽기 오류", e);
        }

        return books;
    }

    /**
     * 도서 ID가 정확히 일치하는 도서를 조회함.
     *
     * @param bookId 조회할 도서 ID
     * @return 일치하는 도서; 없거나 입력이 null이면 null
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public Book findById(String bookId) {

        for (Book book : findAll()) {

            if (book.getBookId().equals(bookId)) {
                return book;
            }
        }

        return null;
    }

    /**
     * 제목과 검색어를 기본 로케일의 소문자로 변환해 부분 일치하는 도서를 조회함.
     *
     * @param title 제목 검색어; 빈 문자열은 모든 제목과 일치
     * @return 검색 결과 목록; 일치하는 도서가 없으면 빈 목록
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     * @throws NullPointerException 검색어가 null이고 비교할 도서가 있는 경우
     */
    public List<Book> findByTitle(String title) {

        List<Book> result = new ArrayList<>();

        for (Book book : findAll()) {

            if (book.getTitle()
                    .toLowerCase()
                    .contains(title.toLowerCase())) {

                result.add(book);
            }
        }

        return result;
    }

    /**
     * ID 중복을 확인한 뒤 도서를 파일 끝에 추가함. 추가 쓰기의 입출력 오류는 출력만 함.
     *
     * @param book 추가할 도서
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 ID가 이미 존재하거나 파일의 상태 값이 잘못된 경우
     * @throws NullPointerException 도서가 null인 경우
     */
    public void save(Book book) {
        if (findById(book.getBookId()) != null) {
            throw new IllegalArgumentException("이미 존재하는 도서 ID입니다: " + book.getBookId());
        }

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_PATH.toFile(), java.nio.charset.StandardCharsets.UTF_8, true))) {

            writeBook(writer, book);
            writer.newLine();

        } catch (IOException e) {
            System.out.println("도서 저장 오류: " + e.getMessage());
        }
    }

    /**
     * ID가 일치하는 도서를 교체한 전체 목록을 UTF-8 임시 파일에 쓴 뒤 원자적으로 교체함. 일치 ID가 없어도 기존 목록을 다시 저장하며 새 도서를 추가하지 않음.
     *
     * @param updatedBook 교체할 도서
     * @throws java.io.UncheckedIOException 도서 파일 읽기, 임시 파일 쓰기 또는 원자적 교체에 실패한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     * @throws NullPointerException 교체할 도서가 null이고 비교할 기존 도서가 있는 경우
     */
    public void update(Book updatedBook) {
        List<Book> books = findAll();
        Path temporary = null;
        try {
            Path target = FILE_PATH;
            temporary = Files.createTempFile(target.getParent(), "books-", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                for (Book book : books) {
                    writeBook(writer, book.getBookId().equals(updatedBook.getBookId()) ? updatedBook : book);
                    writer.newLine();
                }
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("도서 상태 저장 실패", e);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }

    /**
     * 동일 ID인 도서를 제외한 조회 결과로 파일을 다시 씀. 삭제 쓰기의 입출력 오류는 출력만 하며 과거 대출 기록은 변경하지 않음.
     *
     * @param bookId 삭제할 도서 ID; null이면 어떤 도서 ID와도 일치하지 않음
     * @throws java.io.UncheckedIOException 도서 파일을 읽지 못한 경우
     * @throws IllegalStateException 도서 파일에 중복 ID가 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 BookStatus 상수와 일치하지 않는 경우
     */
    public void deleteById(String bookId) {

        List<Book> books = findAll();

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(FILE_PATH.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {

            for (Book book : books) {

                if (!book.getBookId().equals(bookId)) {
                    writeBook(writer, book);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("도서 삭제 오류: " + e.getMessage());
        }
    }

    private void writeBook(
            BufferedWriter writer,
            Book book
    ) throws IOException {

        writer.write(
                book.getBookId() + "\t"
                        + book.getTitle() + "\t"
                        + book.getAuthor() + "\t"
                        + book.getStatus()
        );
    }
}
