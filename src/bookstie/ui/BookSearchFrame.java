package bookstie.ui;

import bookstie.model.Book;
import bookstie.model.Loan;
import bookstie.model.User;
import bookstie.service.BookService;
import bookstie.service.LoanService;
import bookstie.util.FontManager;
import bookstie.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 로그인 회원이 도서 제목을 검색하고 대출을 요청하는 창. 창이 닫히면 연결된 메인 창의 상태를 갱신해 다시 표시함.
 */
public class BookSearchFrame extends JFrame {
    private final BookService bookService = new BookService();
    private final LoanService loanService = new LoanService();
    private final User user = SessionManager.getCurrentUser();
    private final JTextField searchField = new JTextField();
    private final JPanel results = new JPanel();
    private final MainFrame mainFrame;
    private String searchedTitle = "";

    /**
     * 로그인 회원의 검색 창을 구성하고 닫을 때 메인 창을 갱신하도록 연결함.
     *
     * @param mainFrame 검색 창을 닫을 때 갱신하고 다시 표시할 메인 창
     * @throws java.io.UncheckedIOException 대출 파일이나 상위 디렉터리를 생성하지 못한 경우
     * @throws IllegalStateException 회원 세션이 없는 경우 또는 데이터 디렉터리를 결정할 수 없는 경우
     */
    public BookSearchFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        setTitle("Bookstie - 도서 검색");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        JPanel page = new JPanel(null);
        page.setBackground(Color.WHITE);
        page.setPreferredSize(new Dimension(800, 600));
        setContentPane(page);

        JLabel heading = label("도서 검색", 21);
        heading.setBounds(160, 36, 200, 28);
        page.add(heading);
        JButton back = pill("메인으로");
        back.setBounds(152, 548, 120, 36);
        back.addActionListener(e -> dispose());
        page.add(back);

        JPanel box = new JPanel(null);
        box.setBackground(Color.WHITE);
        box.setBorder(new RoundedBorder(18, 3));
        box.setBounds(152, 64, 496, 472);
        page.add(box);

        searchField.setFont(FontManager.getFont(21f));
        searchField.setForeground(Color.BLACK);
        searchField.setBackground(Color.WHITE);
        searchField.setBorder(new RoundedBorder(18, 3));
        searchField.setBounds(28, 18, 400, 36);
        searchField.getAccessibleContext().setAccessibleName("검색할 도서 제목");
        box.add(searchField);
        JButton search = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawOval(7, 6, 20, 20);
                g2.drawLine(25, 24, 32, 31);
                if (hasFocus()) g2.drawRoundRect(1, 1, 36, 36, 8, 8);
                g2.dispose();
            }
        };
        search.setContentAreaFilled(false);
        search.setBorderPainted(false);
        search.setToolTipText("제목 검색");
        search.getAccessibleContext().setAccessibleName("검색");
        search.setBounds(432, 18, 40, 40);
        box.add(search);

        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(results);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setBounds(48, 72, 420, 376);
        box.add(scroll);
        search.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
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
        pack();
        setLocationRelativeTo(mainFrame);
    }

    private void search() {
        searchedTitle = searchField.getText().trim();
        refreshResults();
    }

    private void refreshResults() {
        results.removeAll();
        List<Book> books = bookService.searchByTitle(searchedTitle);
        if (!searchedTitle.isEmpty() && books.isEmpty()) {
            results.add(label("검색 결과가 없습니다.", 16));
        }
        for (Book book : books) {
            JPanel row = new JPanel(null);
            row.setBackground(Color.WHITE);
            row.setPreferredSize(new Dimension(396, 80));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            JLabel title = label(book.getTitle(), 14);
            title.setToolTipText(book.getTitle());
            title.setBounds(0, 4, 305, 21);
            row.add(title);
            JLabel author = label(book.getAuthor(), 14);
            author.setToolTipText(book.getAuthor());
            author.setBounds(0, 25, 305, 20);
            row.add(author);
            JLabel id = label("청구기호: " + book.getBookId(), 14);
            id.setBounds(0, 45, 305, 20);
            row.add(id);
            JButton borrow = pill(book.isAvailable() ? "대출하기" : "대출불가");
            borrow.setEnabled(book.isAvailable());
            borrow.setBounds(312, 22, 84, 34);
            borrow.addActionListener(e -> borrow(book));
            row.add(borrow);
            JPanel line = new JPanel();
            line.setBackground(new Color(155, 155, 155));
            line.setBounds(0, 69, 396, 3);
            row.add(line);
            results.add(row);
        }
        results.revalidate();
        results.repaint();
    }

    private void borrow(Book book) {
        if (!showPopup("대출하시겠습니까?", true)) return;
        Loan loan;
        try {
            loan = loanService.borrowBook(user.getId(), book.getBookId());
        } catch (RuntimeException e) {
            AdminStyle.dialog(this, "대출을 저장하지 못했습니다.\n" + e.getMessage(), "저장 오류", "확인");
            return;
        }
        refreshResults();
        mainFrame.refreshStatus();
        if (loan == null) {
            showPopup("현재 대출할 수 없는 도서입니다.", false);
            return;
        }
        showPopup("<html><center>대출되었습니다.<br>반납예정일: "
                + loan.getDueDate().format(DateTimeFormatter.ofPattern("M월 d일"))
                + "</center></html>", false);
    }

    private boolean showPopup(String message, boolean confirmation) {
        boolean[] accepted = {false};
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(18, 3));
        panel.setPreferredSize(new Dimension(360, 150));
        JLabel text = label(message, 22);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        text.setBounds(10, 16, 340, 66);
        panel.add(text);
        JButton yes = pill(confirmation ? "YES" : "확인");
        yes.setBounds(confirmation ? 80 : 138, 96, 84, 34);
        yes.addActionListener(e -> { accepted[0] = true; dialog.dispose(); });
        panel.add(yes);
        if (confirmation) {
            JButton no = pill("NO");
            no.setBounds(196, 96, 84, 34);
            no.addActionListener(e -> dialog.dispose());
            panel.add(no);
        }
        panel.registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setContentPane(panel);
        dialog.getRootPane().setDefaultButton(yes);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return accepted[0];
    }

    private static JLabel label(String text, float size) {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getFont(size));
        label.setForeground(Color.BLACK);
        return label;
    }

    private static JButton pill(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? Color.BLACK : new Color(217, 217, 217));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 34, 34);
                g2.setFont(getFont());
                g2.setColor(isEnabled() ? Color.WHITE : Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                if (hasFocus()) g2.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 28, 28);
                g2.dispose();
            }
        };
        button.setFont(FontManager.getFont(19f));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
