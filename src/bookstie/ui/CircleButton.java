package bookstie.ui;

import bookstie.util.FontManager;

import javax.swing.*;
import java.awt.*;

/**
 * 검정 원형 배경과 흰 글자를 그리고 원 내부를 클릭 영역으로 사용하는 버튼.
 */
public class CircleButton extends JButton {

    /**
     * 표시 문구와 원형 버튼의 글꼴·색상·커서를 설정함.
     *
     * @param text 버튼에 표시할 문구
     */
    public CircleButton(String text) {
        super(text);

        setFont(FontManager.getFont(14f));
        setForeground(Color.WHITE);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(Color.BLACK);

        g2.fillOval(
                0,
                0,
                getWidth(),
                getHeight()
        );

        g2.setFont(getFont());
        g2.setColor(Color.WHITE);

        FontMetrics fm =
                g2.getFontMetrics();

        int x =
                (getWidth()
                        - fm.stringWidth(getText()))
                        / 2;

        int y =
                (getHeight()
                        + fm.getAscent()
                        - fm.getDescent())
                        / 2;

        g2.drawString(
                getText(),
                x,
                y
        );

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // 기본 버튼 테두리 제거
    }

    /**
     * 컴포넌트 중심에서 너비의 절반을 반지름으로 하는 원 내부인지 판단함.
     *
     * @param x 컴포넌트 기준 x 좌표
     * @param y 컴포넌트 기준 y 좌표
     * @return 원 내부 또는 경계이면 true, 그 밖이면 false
     */
    @Override
    public boolean contains(int x, int y) {

        double radius =
                getWidth() / 2.0;

        double centerX =
                getWidth() / 2.0;

        double centerY =
                getHeight() / 2.0;

        return Math.pow(
                x - centerX,
                2
        ) + Math.pow(
                y - centerY,
                2
        ) <= Math.pow(radius, 2);
    }
}