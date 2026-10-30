package bookstie.ui;

import javax.swing.border.Border;
import java.awt.*;

/**
 * 지정한 모서리 곡률과 선 두께로 검정 둥근 테두리를 그리고 내부 여백을 제공함.
 */
public class RoundedBorder implements Border {

    private final int radius;
    private final int thickness;

    /**
     * 모서리 곡률과 선 두께를 보관함.
     *
     * @param radius 둥근 모서리 호의 폭과 높이
     * @param thickness 테두리 선 두께
     */
    public RoundedBorder(int radius, int thickness) {
        this.radius = radius;
        this.thickness = thickness;
    }

    /**
     * 테두리 두께에 상하 4, 좌우 8을 더한 여백을 반환함.
     *
     * @param c 테두리를 적용할 컴포넌트; 계산에는 사용하지 않음
     * @return 계산한 상하좌우 여백
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(
                thickness + 4,
                thickness + 8,
                thickness + 4,
                thickness + 8
        );
    }

    /**
     * 테두리 전체 영역을 불투명하게 채우는지 반환함.
     *
     * @return 항상 false
     */
    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    /**
     * 지정 영역에 검정 둥근 테두리를 그림.
     *
     * @param c 대상 컴포넌트; 그리기에는 사용하지 않음
     * @param g 그리기에 사용할 Graphics2D 호환 그래픽 컨텍스트
     * @param x 테두리 시작 x 좌표
     * @param y 테두리 시작 y 좌표
     * @param width 테두리 영역 너비
     * @param height 테두리 영역 높이
     * @throws IllegalArgumentException 선 두께가 음수인 경우
     */
    @Override
    public void paintBorder(
            Component c,
            Graphics g,
            int x,
            int y,
            int width,
            int height
    ) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(Color.BLACK);
        g2.setStroke(
                new BasicStroke(thickness)
        );

        g2.drawRoundRect(
                x + thickness / 2,
                y + thickness / 2,
                width - thickness,
                height - thickness,
                radius,
                radius
        );

        g2.dispose();
    }
}