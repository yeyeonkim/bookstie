package bookstie.ui;

import javax.swing.*;
import java.awt.*;

/** 별 클릭은 임시 선택만 변경함. 실제 저장은 평가 대화상자가 담당함. */
public final class StarRatingPanel extends JPanel {
    private final JToggleButton[] stars = new JToggleButton[5];
    private final Icon gray = new StarSvgIcon("grayStar.svg", 32, 31);
    private final Icon red = new StarSvgIcon("redStar.svg", 32, 31);
    private int rating;

    /**
     * 다섯 개 별 버튼과 초기 선택값을 설정함. 초기값을 별점 범위로 검증하지 않음.
     *
     * @param initialRating 초기 선택값; null이면 미선택 0
     * @throws IllegalStateException 별 SVG 아이콘을 읽거나 해석하지 못한 경우
     */
    public StarRatingPanel(Integer initialRating) {
        super(new FlowLayout(FlowLayout.CENTER, 8, 8));
        setBackground(Color.WHITE);
        for (int i = 0; i < stars.length; i++) {
            int value = i + 1;
            JToggleButton star = new JToggleButton();
            star.setPreferredSize(new Dimension(40, 40));
            star.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            star.setContentAreaFilled(false);
            star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            star.setToolTipText(value + "점");
            star.getAccessibleContext().setAccessibleName(value + "점 선택");
            star.addActionListener(e -> select(value));
            stars[i] = star;
            add(star);
        }
        select(initialRating == null ? 0 : initialRating);
    }

    private void select(int value) {
        rating = value;
        for (int i = 0; i < stars.length; i++) {
            stars[i].setSelected(i < rating);
            stars[i].setIcon(i < rating ? red : gray);
        }
    }

    /**
     * 현재 임시 선택값을 반환함. 파일에 저장하지 않음.
     *
     * @return 현재 선택값; 초기값이 null이고 아직 선택하지 않았으면 0, 별 클릭 후에는 1~5. 그 외 초기값은 검증 없이 유지
     */
    public int getRating() { return rating; }
}
