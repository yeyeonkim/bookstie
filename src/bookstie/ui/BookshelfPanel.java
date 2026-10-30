package bookstie.ui;

import bookstie.model.Loan;
import bookstie.service.LoanService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 대출일이 올해인 회원의 대출 기록을 최대 40개의 책등으로 그리는 패널. 별점 등록 여부에 따라 완독 표시를 구분함.
 */
public class BookshelfPanel extends JPanel {

    private final LoanService loanService;
    private List<Loan> loans = List.of();
    private final String userId;

    /**
     * 회원 ID와 대출 서비스를 보관하고 올해 대출 기록 갱신을 요청함.
     *
     * @param userId 책장에 표시할 회원 ID
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우 또는 데이터 디렉터리를 결정할 수 없는 경우
     */
    public BookshelfPanel(String userId) {
        this.userId = userId;
        this.loanService = new LoanService();
        refreshBooks();

        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));

        /*
         * =========================
         * 바깥 책장 테두리
         * =========================
         */
        g2.drawRoundRect(
                2,
                2,
                getWidth() - 5,
                getHeight() - 5,
                18,
                18
        );

        /*
         * =========================
         * 안쪽 책장 테두리
         * =========================
         */
        int innerX = 14;
        int innerY = 14;
        int innerW = getWidth() - 28;
        int innerH = getHeight() - 28;

        g2.drawRoundRect(
                innerX,
                innerY,
                innerW,
                innerH,
                16,
                16
        );

        /*
         * =========================
         * 책장 칸 선
         * =========================
         */
        int shelfCount = 5;
        int shelfHeight = innerH / shelfCount;

        for (int i = 1; i < shelfCount; i++) {
            int y = innerY + i * shelfHeight;

            g2.drawLine(
                    innerX,
                    y,
                    innerX + innerW,
                    y
            );
        }

        /*
         * =========================
         * 올해 대출 기록 (별점 등록 여부로 완독 표시)
         * =========================
         */
        int startX = innerX + 10;
        int startY = innerY + 6;

        int bookIndex = 0;

        for (Loan loan : loans) {
            /*
             * 첫 번째 칸부터 채우기
             * 한 줄에 최대 8권 정도
             */
            int row = bookIndex / 8;
            int col = bookIndex % 8;

            if (row >= shelfCount) {
                break;
            }

            int bookX = startX + col * 16;
            int bookY = startY + row * shelfHeight;

            /*
             * 책등 폭 조금씩 다르게
             */
            int bookWidth;
            if (bookIndex % 4 == 0) {
                bookWidth = 10;
            } else if (bookIndex % 4 == 1) {
                bookWidth = 14;
            } else if (bookIndex % 4 == 2) {
                bookWidth = 8;
            } else {
                bookWidth = 12;
            }

            int bookHeight = shelfHeight - 12;

            /*
             * 완독한 책은 검정색으로 표시
             */
            g2.setColor(loan.isCompleted() ? Color.BLACK : new Color(224, 224, 224));
            g2.fillRect(
                    bookX,
                    bookY,
                    bookWidth,
                    bookHeight
            );

            bookIndex++;
        }

        g2.dispose();
    }

    /**
     * 대출일이 올해인 회원 대출 기록을 다시 읽고 화면 갱신을 요청함. 이벤트 디스패치 스레드 외부에서 호출하면 작업을 예약하고 반환함.
     *
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
    public void refreshBooks() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshBooks);
            return;
        }
        loans = loanService.getLoansByYear(userId, LocalDate.now().getYear());
        repaint();
    }
}