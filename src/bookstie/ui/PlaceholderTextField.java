package bookstie.ui;

import bookstie.util.FontManager;

import javax.swing.*;
import java.awt.*;

/**
 * 입력값이 비어 있고 포커스가 없을 때 안내 문구를 그리는 텍스트 입력 필드.
 */
public class PlaceholderTextField extends JTextField {

    private final String placeholder;

    /**
     * 안내 문구와 글꼴·둥근 테두리를 설정함.
     *
     * @param placeholder 비어 있고 포커스가 없을 때 표시할 문구
     */
    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;

        setFont(
                FontManager.getFont(16f)
        );

        setBorder(
                new RoundedBorder(
                        14,
                        3
                )
        );

        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (getText().isEmpty()
                && !isFocusOwner()) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g2.setFont(
                    FontManager.getFont(16f)
            );

            g2.setColor(
                    new Color(
                            165,
                            165,
                            165
                    )
            );

            FontMetrics fm =
                    g2.getFontMetrics();

            int y =
                    (getHeight()
                            + fm.getAscent()
                            - fm.getDescent())
                            / 2;

            g2.drawString(
                    placeholder,
                    12,
                    y
            );

            g2.dispose();
        }
    }
}