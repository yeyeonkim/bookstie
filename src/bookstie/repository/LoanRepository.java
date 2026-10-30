package bookstie.repository;

import bookstie.util.AppPaths;

import bookstie.model.Loan;
import bookstie.model.LoanStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AppPaths로 결정한 외부 데이터 디렉터리의 loans.txt에 대출 기록을 UTF-8 탭 구분 형식으로 저장함.
 * <p>별점 없는 기존 7필드 형식과 별점을 포함한 8필드 형식을 읽으며, 빈 행은 건너뜀. 손상된 행은 누락시키지 않고 예외로 보고함. 변경 시 전체 기록을 임시 파일에 쓴 뒤 원자적으로 교체하며 입출력 오류를 호출자에게 전달함.</p>
 */
public class LoanRepository {
    private final Path FILE_PATH = AppPaths.dataFile("loans.txt");

    /**
     * 확인된 공통 데이터 디렉터리에 대출 파일이 없으면 생성함.
     *
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     * @throws java.io.UncheckedIOException 디렉터리 또는 파일 생성에 실패한 경우
     */
    public LoanRepository() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            if (!Files.exists(FILE_PATH)) Files.createFile(FILE_PATH);
        } catch (IOException e) {
            throw new UncheckedIOException("대출 파일 생성 실패", e);
        }
    }

    /**
     * 7필드 또는 8필드 대출 기록을 UTF-8로 읽음. 7필드 기록이나 빈 문자열·null 표기의 별점은 미평가로 읽고, 빈 문자열·null 표기의 반납일은 null로 읽음.
     *
     * @return 대출 기록 목록; 비어 있거나 빈 행만 있으면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> findAll() {
        List<Loan> loans = new ArrayList<>();
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(FILE_PATH, StandardCharsets.UTF_8)) {
                lineNumber++;
                if (line.isBlank()) continue;
                String[] p = line.split("\t", -1);
                try {
                    if (p.length != 7 && p.length != 8) {
                        throw new IllegalArgumentException("필드 수 오류");
                    }
                    Integer rating = p.length == 7 || p[7].isBlank() || p[7].equals("null")
                            ? null : Integer.valueOf(p[7]);
                    loans.add(new Loan(p[0], p[1], p[2], LocalDate.parse(p[3]),
                            LocalDate.parse(p[4]), p[5].equals("null") || p[5].isBlank()
                            ? null : LocalDate.parse(p[5]), LoanStatus.valueOf(p[6]), rating));
                } catch (RuntimeException e) {
                    // 손상된 기록을 누락한 채 파일 전체를 다시 저장하지 않는다.
                    throw new IllegalStateException("대출 파일 " + lineNumber + "행을 읽을 수 없습니다.", e);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("대출 파일 읽기 실패", e);
        }
        return loans;
    }

    /**
     * 파일의 기록 순서로 조건에 맞는 대출 기록을 조회함.
     *
     * @param loanId 대출 ID
     * @return 첫 일치 대출 기록; 없거나 입력이 null이면 null
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public Loan findById(String loanId) {
        return findAll().stream().filter(l -> l.getLoanId().equals(loanId)).findFirst().orElse(null);
    }

    /**
     * 파일의 기록 순서로 조건에 맞는 대출 기록을 조회함.
     *
     * @param userId 회원 ID
     * @return 해당 회원의 대출 기록 목록; 일치 기록이 없거나 입력이 null이면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> findByUserId(String userId) {
        return new ArrayList<>(findAll().stream().filter(l -> l.getUserId().equals(userId)).toList());
    }

    /**
     * 파일의 기록 순서로 조건에 맞는 대출 기록을 조회함.
     *
     * @param bookId 도서 ID
     * @return 해당 도서의 대출 기록 목록; 일치 기록이 없거나 입력이 null이면 빈 목록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public List<Loan> findByBookId(String bookId) {
        return new ArrayList<>(findAll().stream().filter(l -> l.getBookId().equals(bookId)).toList());
    }

    /**
     * 파일의 기록 순서로 조건에 맞는 대출 기록을 조회함. 반납 상태가 RETURNED가 아닌 기록을 선택함.
     *
     * @param bookId 도서 ID
     * @return 해당 도서의 첫 미반납 기록; 없거나 입력이 null이면 null
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public Loan findActiveByBookId(String bookId) {
        return findByBookId(bookId).stream().filter(l -> !l.isReturned()).findFirst().orElse(null);
    }

    /**
     * 대출 ID 중복을 검사하고 새 기록을 추가한 전체 목록을 저장함.
     *
     * @param loan 추가할 대출 기록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 동일한 대출 ID가 이미 존재하는 경우
     * @throws NullPointerException 대출 기록이 null인 경우
     */
    public void save(Loan loan) {
        List<Loan> loans = findAll();
        if (loans.stream().anyMatch(l -> l.getLoanId().equals(loan.getLoanId()))) {
            throw new IllegalArgumentException("이미 존재하는 대출 ID입니다.");
        }
        loans.add(loan);
        writeAll(loans);
    }

    /**
     * 동일 ID를 가진 첫 대출 기록을 교체하고 전체 목록을 저장함.
     *
     * @param updatedLoan 교체할 대출 기록
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 동일 ID의 대출 기록이 없는 경우
     * @throws NullPointerException 교체할 기록이 null이고 비교할 기존 기록이 있는 경우
     */
    public void update(Loan updatedLoan) {
        List<Loan> loans = findAll();
        for (int i = 0; i < loans.size(); i++) {
            if (loans.get(i).getLoanId().equals(updatedLoan.getLoanId())) {
                loans.set(i, updatedLoan);
                writeAll(loans);
                return;
            }
        }
        throw new IllegalArgumentException("대출 기록이 없습니다.");
    }

    /**
     * 동일 회원 ID인 기록을 모두 제거하고 전체 목록을 저장함. 일치 기록이 없어도 다시 저장함.
     *
     * @param userId 삭제할 회원 ID; null이면 어떤 기록과도 일치하지 않음
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public void deleteByUserId(String userId) {
        List<Loan> loans = findAll();
        loans.removeIf(l -> l.getUserId().equals(userId));
        writeAll(loans);
    }

    // 도서 상태 저장 실패 시 이번 대출만 롤백한다. 과거 기록은 유지한다.
    /**
     * 동일 대출 ID인 기록을 모두 제거하고 전체 목록을 저장함. 일치 기록이 없어도 다시 저장함.
     *
     * @param loanId 삭제할 대출 ID; null이면 어떤 기록과도 일치하지 않음
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public void deleteById(String loanId) {
        List<Loan> loans = findAll();
        loans.removeIf(l -> l.getLoanId().equals(loanId));
        writeAll(loans);
    }

    private void writeAll(List<Loan> loans) {
        Path temporary = null;
        try {
            temporary = Files.createTempFile(FILE_PATH.toAbsolutePath().getParent(), "loans-", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                for (Loan loan : loans) {
                    writer.write(loan.getLoanId() + "\t" + loan.getUserId() + "\t"
                            + loan.getBookId() + "\t" + loan.getLoanDate() + "\t"
                            + loan.getDueDate() + "\t" + loan.getReturnDate() + "\t"
                            + loan.getStatus() + "\t" + loan.getRating());
                    writer.newLine();
                }
            }
            Files.move(temporary, FILE_PATH.toAbsolutePath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("대출 기록 저장 실패. 기존 파일을 유지합니다.", e);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }
}
