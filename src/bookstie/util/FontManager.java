package bookstie.util;

import java.awt.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * 제주돌담 글꼴을 지연 로딩하고 캐시해 화면 구성 요소에 지정 크기의 글꼴을 제공함. 글꼴 형식 또는 입출력 오류 시 기본 글꼴을 반환함.
 */
public class FontManager {

    private static Font bookstieFont;

    private FontManager() {
    }

    /**
     * 글꼴을 처음 요청할 때 클래스패스 리소스에서 읽어 등록한 뒤 지정 크기로 반환함. 형식·입출력 오류를 출력하고 SansSerif 굵은 글꼴로 대체함.
     *
     * @param size 글꼴 크기; 대체 글꼴에서는 정수로 변환
     * @return 지정 크기의 제주돌담 글꼴 또는 로딩 실패 시 SansSerif 굵은 글꼴
     */
    public static Font getFont(float size) {

        if (bookstieFont == null) {

            try (InputStream input = FontManager.class.getResourceAsStream(
                    "/resources/fonts/EF_jejudoldam(TTF).ttf")) {
                if (input == null) throw new IOException("클래스패스 폰트 리소스 누락: /resources/fonts/EF_jejudoldam(TTF).ttf");
                bookstieFont = Font.createFont(Font.TRUETYPE_FONT, input);

                GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .registerFont(bookstieFont);

            } catch (
                    FontFormatException
                    | IOException e
            ) {

                System.err.println(
                        "폰트 로딩 실패: "
                                + e.getMessage()
                );

                return new Font(
                        "SansSerif",
                        Font.BOLD,
                        (int) size
                );
            }
        }

        return bookstieFont
                .deriveFont(size);
    }
}