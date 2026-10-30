package bookstie.ui;

import bookstie.util.FontManager;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

final class AdminStyle {
    private AdminStyle() {}

    static JButton button(String text, boolean white, boolean circle) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (white ? Color.WHITE : Color.BLACK) : Color.GRAY);
                if (circle) g2.fillOval(0, 0, getWidth(), getHeight());
                else g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                if (hasFocus()) {
                    g2.setColor(white ? Color.BLACK : Color.WHITE);
                    g2.drawRoundRect(5, 5, getWidth() - 11, getHeight() - 11, 18, 18);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FontManager.getFont(13f));
        button.setForeground(white ? Color.BLACK : Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    static AbstractBorder border(Color color) {
        return new AbstractBorder() {
            /**
             * 관리자 입력 테두리의 고정 여백을 반환함.
             *
             * @param c 대상 컴포넌트
             * @return 상하 7, 좌우 10인 여백
             */
            @Override public Insets getBorderInsets(Component c) {
                return new Insets(7, 10, 7, 10);
            }
            /**
             * 지정한 색상으로 둥근 입력 테두리를 그림.
             *
             * @param c 대상 컴포넌트
             * @param g Graphics2D 호환 그래픽 컨텍스트
             * @param x 시작 x 좌표
             * @param y 시작 y 좌표
             * @param w 영역 너비
             * @param h 영역 높이
             */
            @Override public void paintBorder(Component c, Graphics g,
                                               int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 16, 16);
                g2.dispose();
            }
        };
    }

    static int dialog(Component owner, Object message, String title, String... options) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options, options[options.length - 1]);
        style(pane);
        JDialog dialog = pane.createDialog(owner, title);
        dialog.setVisible(true);
        Object value = pane.getValue();
        dialog.dispose();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) return i;
        }
        return -1;
    }

    private static void style(Component component) {
        component.setFont(FontManager.getFont(16f));
        component.setBackground(Color.WHITE);
        component.setForeground(Color.BLACK);
        if (component instanceof JButton button) {
            button.setBorder(new RoundedBorder(16, 2));
            button.setContentAreaFilled(false);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) style(child);
        }
    }
}
