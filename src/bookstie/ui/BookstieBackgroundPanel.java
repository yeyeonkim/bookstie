package bookstie.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 로그인·회원가입 화면의 원형 장식 배경을 그리는 절대 배치 패널. 관리자 버튼 위치의 장식을 생략할 수 있음.
 */
public class BookstieBackgroundPanel extends JPanel {

    private final boolean reserveAdminSpace;

    /**
     * 원형 장식 배경과 절대 배치를 설정함.
     *
     * @param reserveAdminSpace true이면 오른쪽 아래 관리자 버튼 위치의 장식 원을 생략
     */
    public BookstieBackgroundPanel(boolean reserveAdminSpace) {
        this.reserveAdminSpace = reserveAdminSpace;

        setLayout(null);
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setColor(Color.BLACK);

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int circleSize = 70;

        // 위 첫 번째 줄
        int[] topRow1 = {
                -35, 95, 225, 355, 485, 615, 745
        };

        for (int x : topRow1) {
            g2.fillOval(
                    x,
                    -35,
                    circleSize,
                    circleSize
            );
        }

        // 위 두 번째 줄
        int[] topRow2 = {
                40, 170, 300, 430, 560, 690
        };

        for (int x : topRow2) {
            g2.fillOval(
                    x,
                    50,
                    circleSize,
                    circleSize
            );
        }

        // 아래 첫 번째 줄
        int[] bottomRow1 = {
                -35, 95, 225, 355, 485, 615, 745
        };

        for (int x : bottomRow1) {
            g2.fillOval(
                    x,
                    450,
                    circleSize,
                    circleSize
            );
        }

        // 아래 두 번째 줄
        int[] bottomRow2;

        if (reserveAdminSpace) {

            // 로그인 화면:
            // 마지막 자리는 admin 버튼이 들어가므로 비움
            bottomRow2 = new int[] {
                    40, 170, 300, 430, 560
            };

        } else {

            // 회원가입 화면:
            // admin 버튼이 없으므로 마지막 원까지 그림
            bottomRow2 = new int[] {
                    40, 170, 300, 430, 560, 690
            };
        }

        for (int x : bottomRow2) {
            g2.fillOval(
                    x,
                    520,
                    circleSize,
                    circleSize
            );
        }

        g2.dispose();
    }
}