import bookstie.util.AppPaths;
import bookstie.util.FontManager;
import bookstie.repository.*;
import bookstie.service.*;
import bookstie.model.*;
import bookstie.ui.StarRatingPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.UUID;

/** Temporary-distribution persistence and classpath resource test; never uses live data. */
public class DistributionTest {
    private static void check(boolean condition) {
        if (!condition) throw new AssertionError("배포 검증 실패");
    }
    public static void main(String[] args) throws Exception {
        Path expected = Path.of(args[1]).toRealPath();
        check(AppPaths.dataDirectory().equals(expected));
        check(AppPaths.dataFile("users.txt").equals(expected.resolve("users.txt")));
        try { AppPaths.dataFile("../users.txt"); throw new AssertionError(); }
        catch (IllegalArgumentException correct) { }
        BookRepository books = new BookRepository();
        LoanRepository loans = new LoanRepository();
        if (args[0].equals("write")) {
            String secret = UUID.randomUUID().toString();
            AuthService auth = new AuthService();
            check(auth.signUp("배포 테스트", "distribution-test", secret, secret) == SignUpResult.SUCCESS);
            check(auth.login("distribution-test", secret) != null);
            Files.writeString(expected.resolve("admins.txt"), "distribution-admin\t" + secret + "\n");
            check(auth.adminLogin("distribution-admin", secret) != null);
            books.save(new Book("B901", "공백 한글 테스트", "합성 저자"));
            LoanService service = new LoanService();
            Loan loan = service.borrowBook("distribution-test", "B901");
            service.rateLoan("distribution-test", loan.getLoanId(), 5);
            check(loans.findById(loan.getLoanId()).isCompleted());
            check(!loans.findById(loan.getLoanId()).isReturned());
            check(!books.findById("B901").isAvailable());
            check(new AdminService().returnBook(loan.getLoanId()));
            check(loans.findById(loan.getLoanId()).getRating() == 5);
        } else {
            check(new UserRepository().findById("distribution-test") != null);
            check(loans.findByBookId("B901").size() == 1);
            check(loans.findByBookId("B901").get(0).isReturned());
            check(loans.findByBookId("B901").get(0).getRating() == 5);
            check(books.findById("B901").isAvailable());
        }
        try (var input = DistributionTest.class.getResourceAsStream("/resources/fonts/EF_jejudoldam(TTF).ttf")) {
            check(input != null);
            Font actual = FontManager.getFont(18f);
            Font expectedFont = Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(18f);
            check(actual.getFontName().equals(expectedFont.getFontName()));
        }
        SwingUtilities.invokeAndWait(() -> {
            StarRatingPanel panel = new StarRatingPanel(3);
            for (int i = 0; i < 5; i++) {
                Icon icon = ((JToggleButton) panel.getComponent(i)).getIcon();
                BufferedImage image = new BufferedImage(32, 31, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                icon.paintIcon(panel, graphics, 0, 0); graphics.dispose();
                check(image.getRGB(16, 15) == (i < 3 ? 0xFFD22222 : 0xFFE0E0E0));
            }
        });
        System.out.println("PASS distribution " + args[0] + ": path, persistence, policy, font, icons");
    }
}
