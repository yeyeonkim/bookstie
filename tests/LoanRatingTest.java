import bookstie.model.*;
import bookstie.repository.*;
import bookstie.service.*;
import bookstie.ui.BookshelfPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.util.Arrays;

/** Run only in an isolated working directory using tests/run-tests.py. */
public class LoanRatingTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void rejects(Runnable action, String message) {
        boolean failed = false;
        try { action.run(); } catch (RuntimeException expected) { failed = true; }
        check(failed, message);
    }
    private static long completed(LoanService service, String user) {
        return service.getUserLoans(user).stream().filter(Loan::isCompleted).count();
    }
    private static void shelfColor(Color expected) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BookshelfPanel panel = new BookshelfPanel("u1");
            panel.setSize(200, 400);
            BufferedImage image = new BufferedImage(200, 400, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            panel.paint(graphics);
            graphics.dispose();
            check(image.getRGB(27, 25) == expected.getRGB(), "메인 책장 색상 " + expected);
        });
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("legacy")) {
            check(new LoanRepository().findAll().size() == Integer.parseInt(args[1]), "실제 기존 파일 읽기");
            System.out.println("PASS existing loans.txt read (isolated copy)");
            return;
        }
        if (args.length > 0 && args[0].equals("restart")) {
            LoanRepository repo = new LoanRepository();
            check(repo.findById("L003").getRating() == 5, "새 JVM 별점 복원");
            check(repo.findById("L003").isReturned(), "새 JVM 반납 복원");
            check(repo.findById("L001").getRating() == 2, "과거 기록 평가 복원");
            check(new LoanService().getCurrentLoans("u1").size() == 3, "새 JVM 미반납 집계");
            System.out.println("PASS restart: " + checks + " checks");
            return;
        }
        Files.createDirectories(Path.of("data"));
        check(!Files.exists(Path.of("data/loans.txt")), "빈 임시 폴더에서만 실행");
        LocalDate today = LocalDate.now();
        LocalDate old = today.minusYears(1);
        Files.writeString(Path.of("data/loans.txt"),
                "L001\tu1\tB099\t" + old + "\t" + old.plusDays(14) + "\t" + old.plusDays(2) + "\tRETURNED\n"
                + "L002\tu2\tB099\t" + old + "\t" + old.plusDays(14) + "\t" + old.plusDays(2) + "\tRETURNED\t4\n");
        LoanRepository repo = new LoanRepository();
        check(repo.findAll().size() == 2, "7열/8열 혼합 읽기");
        check(!repo.findById("L001").isCompleted(), "반납된 기존 무평가 기록은 미완독");
        check(repo.findById("L002").getRating() == 4, "기존 평가 보존");
        BookRepository books = new BookRepository();
        for (int i = 1; i <= 3; i++) books.save(new Book("B00" + i, "같은 제목", "저자"));
        LoanService service = new LoanService();
        byte[] booksBefore = Files.readAllBytes(Path.of("data/books.txt"));
        rejects(() -> books.save(new Book("B001", "중복 ID", "저자")), "중복 도서 ID 등록 차단");
        check(Arrays.equals(booksBefore, Files.readAllBytes(Path.of("data/books.txt"))), "중복 등록 시 원본 보존");
        Loan first = service.borrowBook("u1", "B001");
        check(books.findById("B002").isAvailable() && books.findById("B003").isAvailable(),
                "동명 도서 한 권 대출 후 나머지는 대출 가능");
        Loan second = service.borrowBook("u1", "B002");
        Loan third = service.borrowBook("u1", "B003");
        check(first != null && second != null && third != null, "3권 대출");
        check(first.getDueDate().equals(today.plusDays(14)), "14일 대출");
        check(!first.isCompleted() && first.getRating() == null, "신규 대출 미평가");
        check(service.getLoansByYear("u1", today.getYear()).size() == 3, "올해 3권 대출");
        check(service.getUserLoans("u1").size() == 4 && completed(service, "u1") == 0, "누적 과거 기록 포함");
        shelfColor(new Color(224, 224, 224));
        byte[] before = Files.readAllBytes(Path.of("data/loans.txt"));
        rejects(() -> service.rateLoan("u1", first.getLoanId(), 0), "0점 거부");
        rejects(() -> service.rateLoan("u1", first.getLoanId(), 6), "6점 거부");
        rejects(() -> service.rateLoan("u2", first.getLoanId(), 5), "다른 사용자 거부");
        check(Arrays.equals(before, Files.readAllBytes(Path.of("data/loans.txt"))), "무효 평가 데이터 불변");
        service.rateLoan("u1", first.getLoanId(), 4);
        check(completed(service, "u1") == 1, "완독 1권");
        check(service.getLoansByYear("u1", today.getYear()).size() == 3, "평가 후 대출 3권 유지");
        check(service.getCurrentLoans("u1").size() == 3, "평가 후 현재 대출 유지");
        check(repo.findById(first.getLoanId()).getReturnDate() == null, "평가 후 반납일 없음");
        check(books.findById("B001").getStatus() == BookStatus.LOANED, "평가 후 도서 대출 상태 유지");
        check(!repo.findById(second.getLoanId()).isCompleted(), "동일 제목 다른 ID 분리");
        shelfColor(Color.BLACK);
        service.rateLoan("u1", first.getLoanId(), 5);
        check(completed(service, "u1") == 1, "재평가 중복 집계 없음");
        check(repo.findById("L002").getRating() == 4, "다른 사용자 기존 평가 유지");
        check(service.returnBook(first.getLoanId()), "평가한 책 반납");
        check(repo.findById(first.getLoanId()).getRating() == 5 && repo.findById(first.getLoanId()).isCompleted(), "반납 후 평가 유지");
        check(books.findById("B001").isAvailable(), "반납 후 AVAILABLE");
        Loan otherUser = service.borrowBook("u2", "B001");
        check(!otherUser.isCompleted(), "동일 책 다른 사용자 미평가");
        service.rateLoan("u2", otherUser.getLoanId(), 1);
        check(repo.findById(first.getLoanId()).getRating() == 5, "다른 사용자 평가 독립");
        service.returnBook(otherUser.getLoanId());
        Loan repeat = service.borrowBook("u1", "B001");
        check(!repeat.isCompleted(), "재대출 별점 복사 없음");
        service.returnBook(second.getLoanId());
        check(!repo.findById(second.getLoanId()).isCompleted(), "무평가 반납은 미완독");
        service.rateLoan("u1", second.getLoanId(), 3);
        check(repo.findById(second.getLoanId()).isReturned() && repo.findById(second.getLoanId()).getRating() == 3, "반납 후 평가");
        service.rateLoan("u1", "L001", 2);
        check(repo.findById("L001").getRating() == 2, "삭제된 도서의 과거 기록 평가");
        books.save(new Book("B004", "연체 도서", "저자", BookStatus.LOANED));
        repo.save(new Loan("L900", "u1", "B004", today.minusDays(20), today.minusDays(6)));
        service.checkOverdue();
        service.rateLoan("u1", "L900", 2);
        check(repo.findById("L900").getStatus() == LoanStatus.OVERDUE, "평가 후 OVERDUE 유지");
        check(service.getOverdueLoans("u1").size() == 1, "연체 집계 유지");
        before = Files.readAllBytes(Path.of("data/loans.txt"));
        var permissions = Files.getPosixFilePermissions(Path.of("data"));
        try {
            Files.setPosixFilePermissions(Path.of("data"), PosixFilePermissions.fromString("r-x------"));
            rejects(() -> service.rateLoan("u1", third.getLoanId(), 5), "쓰기 실패 전파");
            check(!repo.findById(third.getLoanId()).isCompleted(), "저장 실패 후 미완독 유지");
            check(Arrays.equals(before, Files.readAllBytes(Path.of("data/loans.txt"))), "저장 실패 원본 보존");
        } finally {
            Files.setPosixFilePermissions(Path.of("data"), permissions);
        }
        Files.writeString(Path.of("data/loans.txt"), "malformed\n", StandardOpenOption.APPEND);
        byte[] damaged = Files.readAllBytes(Path.of("data/loans.txt"));
        rejects(() -> service.rateLoan("u1", third.getLoanId(), 5), "손상 행이 있으면 저장 중단");
        check(Arrays.equals(damaged, Files.readAllBytes(Path.of("data/loans.txt"))), "손상 행 삭제 방지");
        Files.write(Path.of("data/loans.txt"), before);
        System.out.println("PASS regression: " + checks + " checks (including EDT offscreen shelf rendering)");
    }
}
