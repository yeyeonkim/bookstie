import bookstie.model.*;
import bookstie.repository.*;
import bookstie.service.*;
import bookstie.ui.*;
import bookstie.util.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/** Swing integration test with real modal dialogs; uses an isolated working directory. */
public class LoanRatingGuiTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }
    private static <T extends Component> T find(Container root, Class<T> type, String text) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c) && (text == null || c instanceof JButton b && text.equals(b.getText()))) {
                return type.cast(c);
            }
            if (c instanceof Container nested) {
                T result = find(nested, type, text);
                if (result != null) return result;
            }
        }
        return null;
    }
    private static void evaluate(MyBookshelfFrame shelf, Integer rating, String action) throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        Timer timer = new Timer(150, e -> {
            ((Timer) e.getSource()).stop();
            try {
                for (Window window : Window.getWindows()) {
                    if (window instanceof JDialog dialog && dialog.isShowing() && dialog.getTitle().equals("평가하기")) {
                        StarRatingPanel stars = find(dialog, StarRatingPanel.class, null);
                        check(stars != null && stars.getComponentCount() == 5, "별 5개 표시");
                        if (rating != null) ((JToggleButton) stars.getComponent(rating - 1)).doClick();
                        check(stars.getRating() == (rating == null ? 0 : rating), "별 클릭 점수 반영");
                        for (int i = 0; i < 5; i++) {
                            Icon icon = ((JToggleButton) stars.getComponent(i)).getIcon();
                            BufferedImage image = new BufferedImage(32, 31, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D graphics = image.createGraphics();
                            icon.paintIcon(stars, graphics, 0, 0);
                            graphics.dispose();
                            Color expected = i < stars.getRating() ? new Color(0xD22222) : new Color(0xE0E0E0);
                            check(image.getRGB(16, 15) == expected.getRGB(), "SVG 별 색상");
                        }
                        find(dialog, JButton.class, action).doClick();
                        return;
                    }
                }
                throw new AssertionError("평가 대화상자가 열리지 않음");
            } catch (Throwable failure) {
                error.set(failure);
                for (Window window : Window.getWindows()) if (window instanceof JDialog) window.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        find(shelf, JButton.class, "평가하기").doClick();
        timer.stop();
        if (error.get() != null) throw new AssertionError(error.get());
    }
    private static String label(Object object, String name) throws Exception {
        return ((JLabel) field(object, name)).getText();
    }
    private static Color spine(MyBookshelfFrame shelf) throws Exception {
        JPanel reading = (JPanel) field(shelf, "reading");
        JPanel row = (JPanel) reading.getComponent(0);
        return row.getComponent(0).getBackground();
    }
    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("SKIP GUI: no graphical display available");
            return;
        }
        AtomicReference<Throwable> error = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                check(!Files.exists(Path.of("data/loans.txt")), "GUI 전용 임시 폴더");
                BookRepository books = new BookRepository();
                for (int i = 1; i <= 3; i++) books.save(new Book("B00" + i, "테스트 도서 " + i, "저자"));
                LoanService service = new LoanService();
                Loan first = service.borrowBook("gui", "B001");
                service.borrowBook("gui", "B002");
                service.borrowBook("gui", "B003");
                SessionManager.loginUser(new User("gui", "검증", "test"));
                MainFrame main = new MainFrame();
                main.setVisible(true);
                MyBookshelfFrame shelf = new MyBookshelfFrame(main);
                shelf.setVisible(true);
                check(label(shelf, "borrowedCount").equals("3"), "GUI 누적 대출 3");
                check(label(shelf, "completedCount").equals("0"), "GUI 완독 0");
                check(spine(shelf).equals(new Color(224, 224, 224)), "내 서재 회색");
                JPanel reading = (JPanel) field(shelf, "reading");
                for (Component row : reading.getComponents()) {
                    for (Component component : ((JPanel) row).getComponents()) {
                        if (component instanceof JLabel text) {
                            check(!text.getText().contains("LOANED") && !text.getText().contains("미완독"), "상단 상태 문구 제거");
                        }
                    }
                }
                byte[] original = Files.readAllBytes(Path.of("data/loans.txt"));
                evaluate(shelf, 4, "취소");
                check(Arrays.equals(original, Files.readAllBytes(Path.of("data/loans.txt"))), "선택 후 취소 불변");
                evaluate(shelf, null, "취소");
                check(Arrays.equals(original, Files.readAllBytes(Path.of("data/loans.txt"))), "선택 없이 취소 불변");
                evaluate(shelf, 4, "저장");
                check(label(shelf, "completedCount").equals("1"), "저장 즉시 완독 1");
                check(label(shelf, "borrowedCount").equals("3"), "저장 후 대출 3 유지");
                check(label(main, "loanCountLabel").equals("3"), "메인 현재 대출 유지");
                check(spine(shelf).equals(Color.BLACK), "내 서재 검은색");
                evaluate(shelf, 5, "저장");
                check(label(shelf, "completedCount").equals("1"), "GUI 재평가 완독 1 유지");
                check(new LoanRepository().findById(first.getLoanId()).getRating() == 5, "GUI 점수 변경 저장");
                shelf.dispose();
                MyBookshelfFrame reopened = new MyBookshelfFrame(main);
                check(label(reopened, "completedCount").equals("1") && spine(reopened).equals(Color.BLACK), "서재 재열기 복원");
                reopened.dispose();
                main.dispose();
                SessionManager.logout();
                SessionManager.loginUser(new User("gui", "검증", "test"));
                MainFrame relogged = new MainFrame();
                MyBookshelfFrame restored = new MyBookshelfFrame(relogged);
                check(label(restored, "completedCount").equals("1"), "세션 재로그인 복원");
            } catch (Throwable failure) {
                error.set(failure);
            } finally {
                // Prevent windowClosed callbacks reopening main frames during test cleanup.
                for (Window window : Window.getWindows()) {
                    for (var listener : window.getWindowListeners()) window.removeWindowListener(listener);
                    window.dispose();
                }
            }
        });
        if (error.get() != null) throw new AssertionError(error.get());
        System.out.println("PASS Swing GUI: " + checks + " checks");
    }
}
