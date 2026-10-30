import bookstie.util.FontManager;
import java.awt.Font;

/** Missing-font fallback verification in an isolated classpath. */
public class FontFallbackTest {
    public static void main(String[] args) {
        Font font = FontManager.getFont(18f);
        if (!font.getFamily().equals("SansSerif") || !font.isBold() || font.getSize() != 18) {
            throw new AssertionError("기본 글꼴 대체 실패");
        }
        System.out.println("PASS missing-font fallback");
    }
}
