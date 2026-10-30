package bookstie.ui;

import bookstie.model.Book;
import bookstie.model.Loan;
import bookstie.model.LoanStatus;
import bookstie.model.User;
import bookstie.service.BookService;
import bookstie.service.LoanService;
import bookstie.util.FontManager;
import bookstie.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 로그인 회원의 현재 대출, 반납 기록, 별점과 완독·대출 수를 표시하는 서재 창. 반납 여부와 관계없이 별점 평가를 저장할 수 있으며 평가가 반납을 수행하지는 않음.
 */
public class MyBookshelfFrame extends JFrame {
    private static final int PAGE_SIZE = 4;
    private static final Color GRAY = new Color(140, 140, 140);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final BookService bookService = new BookService();
    private final LoanService loanService = new LoanService();
    private final JPanel historyPanel = new JPanel(null);
    private final JLabel pageLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton previous = navigation("〈");
    private final JButton next = navigation("〉");
    private List<Loan> returnedLoans = List.of();
    private final JPanel reading = new JPanel();
    private final JLabel completedCount = new JLabel();
    private final JLabel borrowedCount = new JLabel();
    private final User user = SessionManager.getCurrentUser();
    private final MainFrame mainFrame;
    private int page;

    /**
     * 회원 세션을 확인하고 현재 대출 및 반납 기록을 읽어 서재를 구성함.
     *
     * @param mainFrame 평가 저장 또는 창 종료 시 상태를 갱신할 메인 창
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 회원 세션이 없거나 손상된 기록·중복 도서 ID 또는 별 아이콘 로딩 오류가 있는 경우 또는 데이터 디렉터리를 결정할 수 없는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 잘못된 경우
     */
    public MyBookshelfFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (user == null) throw new IllegalStateException("로그인이 필요합니다.");
        setTitle("Bookstie - 내 서재");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        JPanel root = new JPanel(null);
        root.setBackground(Color.WHITE);
        root.setPreferredSize(new Dimension(800, 600));
        setContentPane(root);
        put(root, user.getName() + "님의 서재", 36, Color.BLACK, 16, 46, 476, 48);
        put(root, "reading...", 22, Color.BLACK, 16, 99, 400, 28);

        JPanel readingBox = new JPanel(new BorderLayout());
        readingBox.setBackground(Color.WHITE);
        readingBox.setBorder(new RoundedBorder(18, 3));
        readingBox.setBounds(9, 129, 426, 238);
        reading.setLayout(new BoxLayout(reading, BoxLayout.Y_AXIS));
        reading.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(reading);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        readingBox.add(scroll);
        root.add(readingBox);

        completedCount.setFont(FontManager.getFont(46f));
        completedCount.setForeground(Color.BLACK);
        completedCount.setBounds(400, 397, 110, 56);
        root.add(completedCount);
        put(root, "완독", 16, Color.BLACK, 401, 451, 110, 24);
        borrowedCount.setFont(FontManager.getFont(46f));
        borrowedCount.setForeground(GRAY);
        borrowedCount.setBounds(400, 478, 110, 56);
        root.add(borrowedCount);
        put(root, "대출", 16, GRAY, 401, 532, 110, 24);

        put(root, "반납 완료", 18, GRAY, 510, 17, 260, 26);
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBounds(510, 53, 280, 490);
        root.add(historyPanel);
        previous.setBounds(540, 550, 40, 34);
        next.setBounds(716, 550, 40, 34);
        pageLabel.setFont(FontManager.getFont(17f));
        pageLabel.setForeground(Color.BLACK);
        pageLabel.setBounds(583, 550, 130, 34);
        previous.addActionListener(e -> { page--; refreshHistory(); });
        next.addActionListener(e -> { page++; refreshHistory(); });
        root.add(previous);
        root.add(pageLabel);
        root.add(next);
        JButton back = navigation("메인으로");
        back.setBounds(16, 546, 130, 36);
        back.addActionListener(e -> dispose());
        root.add(back);
        addWindowListener(new WindowAdapter() {
            /**
             * 메인 화면의 상태를 갱신하고 다시 표시함.
             *
             * @param e 창 종료 이벤트
             */
            @Override
            public void windowClosed(WindowEvent e) {
                mainFrame.refreshStatus();
                mainFrame.setVisible(true);
            }
        });
        refreshLoans();
        pack();
        setLocationRelativeTo(mainFrame);
    }

    private JPanel loanRow(Loan loan, boolean returned) {
        Book book = bookService.getBookById(loan.getBookId());
        JPanel row = new JPanel(null);
        row.setBackground(Color.WHITE);
        row.setPreferredSize(new Dimension(390, 114));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 114));
        JPanel spine = new JPanel();
        spine.setBackground(loan.isCompleted() ? Color.BLACK : new Color(224, 224, 224));
        spine.setBounds(0, 0, returned ? 7 : 75, 100);
        row.add(spine);
        int x = returned ? 48 : 84;
        int width = returned ? 222 : 286;
        put(row, book == null ? "삭제된 도서 (" + loan.getBookId() + ")" : book.getTitle(),
                22, loan.isCompleted() ? Color.BLACK : GRAY, x, 19, width, 29);
        put(row, book == null ? "저자 정보 없음" : book.getAuthor(),
                15, GRAY, x, 48, width, 20);
        if (returned) {
            Icon grayStar = new StarSvgIcon("grayStar.svg", 22, 21);
            Icon redStar = new StarSvgIcon("redStar.svg", 22, 21);
            int rating = loan.getRating() == null ? 0 : loan.getRating();
            for (int i = 0; i < 5; i++) {
                JLabel star = new JLabel(i < rating ? redStar : grayStar);
                star.setBounds(x + i * 26, 85, 22, 21);
                star.setToolTipText(rating == 0 ? "아직 평가하지 않은 도서" : rating + "점");
                row.add(star);
            }
        } else {
            boolean overdue = loan.getStatus() == LoanStatus.OVERDUE;
            put(row, "반납 예정: " + date(loan.getDueDate()), 14,
                    overdue ? new Color(220, 35, 35) : GRAY, x, 66, width, 20);
        }
        row.setToolTipText("대출 ID: " + loan.getLoanId() + " / 도서 ID: " + loan.getBookId()
                + " / 대출일: " + date(loan.getLoanDate()));
        if (!returned) put(row, "대출일: " + date(loan.getLoanDate()), 12, GRAY, x, 86, 165, 20);
        JButton rate = navigation("평가하기");
        rate.setFont(FontManager.getFont(12f));
        rate.setBounds(returned ? 190 : 280, 87, 78, 24);
        rate.addActionListener(e -> rate(loan));
        row.add(rate);
        return row;
    }

    private void refreshLoans() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshLoans);
            return;
        }
        loanService.checkOverdue();
        List<Loan> all = loanService.getUserLoans(user.getId());
        List<Loan> current = all.stream().filter(l -> !l.isReturned())
                .sorted(Comparator.comparing(Loan::getDueDate)).toList();
        returnedLoans = all.stream().filter(Loan::isReturned)
                .sorted(Comparator.comparing(Loan::getReturnDate,
                        Comparator.nullsLast(Comparator.reverseOrder()))).toList();
        reading.removeAll();
        if (current.isEmpty()) {
            JLabel empty = new JLabel("현재 대출 중인 책이 없습니다.");
            empty.setFont(FontManager.getFont(18f));
            empty.setForeground(GRAY);
            reading.add(empty);
        }
        for (Loan loan : current) reading.add(loanRow(loan, false));
        borrowedCount.setText(String.valueOf(all.size()));
        completedCount.setText(String.valueOf(all.stream().filter(Loan::isCompleted).count()));
        reading.revalidate();
        reading.repaint();
        refreshHistory();
    }

    private void rate(Loan loan) {
        StarRatingPanel score;
        try {
            score = new StarRatingPanel(loan.getRating());
        } catch (IllegalStateException e) {
            AdminStyle.dialog(this, e.getMessage(), "아이콘 오류", "확인");
            return;
        }
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        Book book = bookService.getBookById(loan.getBookId());
        form.add(new JLabel(book == null ? loan.getBookId() : book.getTitle()));
        form.add(new JLabel("대출일: " + date(loan.getLoanDate()) + " / " + loan.getLoanId()));
        form.add(score);
        while (AdminStyle.dialog(this, form, "평가하기", "저장", "취소") == 0) {
            if (score.getRating() == 0) {
                AdminStyle.dialog(this, "별을 클릭해 평가해주세요.", "평가하기", "확인");
                continue;
            }
            try {
                loanService.rateLoan(user.getId(), loan.getLoanId(), score.getRating());
            } catch (RuntimeException e) {
                AdminStyle.dialog(this, "평가를 저장하지 못했습니다.\n" + e.getMessage(), "저장 오류", "확인");
                return;
            }
            refreshLoans();
            mainFrame.refreshStatus();
            return;
        }
    }

    private void refreshHistory() {
        historyPanel.removeAll();
        int pages = Math.max(1, (returnedLoans.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));
        if (returnedLoans.isEmpty()) {
            put(historyPanel, "반납 완료한 책이 없습니다.",
                    17, GRAY, 0, 20, 275, 30);
        }
        int start = page * PAGE_SIZE;
        for (int i = start; i < Math.min(start + PAGE_SIZE, returnedLoans.size()); i++) {
            JPanel row = loanRow(returnedLoans.get(i), true);
            row.setBounds(0, (i - start) * 125, 280, 114);
            historyPanel.add(row);
        }
        pageLabel.setText((page + 1) + " / " + pages);
        previous.setEnabled(page > 0);
        next.setEnabled(page + 1 < pages);
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private static String date(LocalDate date) {
        return date == null ? "기록 없음" : date.format(DATE);
    }

    private static void put(JPanel panel, String text, float size, Color color,
                            int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getFont(size));
        label.setForeground(color);
        label.setToolTipText(text);
        label.setBounds(x, y, width, height);
        panel.add(label);
    }

    private static JButton navigation(String text) {
        JButton button = new JButton(text);
        button.setFont(FontManager.getFont(18f));
        button.setForeground(Color.BLACK);
        button.setContentAreaFilled(false);
        button.setBorder(new RoundedBorder(16, 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
