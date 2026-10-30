package bookstie.ui;

import bookstie.model.Loan;
import bookstie.model.User;
import bookstie.service.AuthService;
import bookstie.service.BookService;
import bookstie.service.LoanService;
import bookstie.util.FontManager;
import bookstie.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 로그인 회원의 대출·연체 수와 올해 대출 책장을 표시하고 도서 검색 및 내 서재로 이동하는 메인 창.
 */
public class MainFrame extends JFrame {

    private final AuthService authService;
    private final LoanService loanService;
    private final BookService bookService;

    private final User currentUser;

    private JLabel loanCountLabel;
    private JLabel overdueCountLabel;

    private BookshelfPanel bookshelfPanel;

    /**
     * 현재 회원의 메인 화면을 구성하고 연체 상태와 반납 하루 전 안내를 갱신함. 회원 세션이 없으면 안내 후 창을 폐기하고 초기화를 중단함.
     *
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우 또는 데이터 디렉터리를 결정할 수 없는 경우
     */
    public MainFrame() {

        this.authService =
                new AuthService();

        this.loanService =
                new LoanService();

        this.bookService =
                new BookService();

        this.currentUser =
                SessionManager
                        .getCurrentUser();

        /*
         * 로그인되지 않은 상태에서
         * MainFrame 접근 방지
         */
        if (currentUser == null) {

            JOptionPane.showMessageDialog(
                    null,
                    "로그인이 필요합니다."
            );

            dispose();

            return;
        }

        setTitle("Bookstie");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setResizable(false);

        JPanel mainPanel =
                new JPanel(null);

        mainPanel.setPreferredSize(
                new Dimension(
                        800,
                        600
                )
        );

        mainPanel.setBackground(
                Color.WHITE
        );

        setContentPane(mainPanel);

        initUI(mainPanel);

        pack();

        setLocationRelativeTo(null);

        /*
         * 연체 상태 최신화
         */
        loanService.checkOverdue();

        refreshStatus();

        /*
         * 반납 하루 전 안내
         */
        checkDueTomorrow();
    }


    private void initUI(
            JPanel panel
    ) {

        /*
         * ==============================
         * 좌측 상단
         * 도서 대출 현황
         * ==============================
         */

        JLabel statusTitle =
                new JLabel(
                        "도서 대출 현황"
                );

        statusTitle.setFont(
                FontManager.getFont(15f)
        );

        statusTitle.setForeground(
                new Color(
                        120,
                        120,
                        120
                )
        );

        statusTitle.setBounds(
                18,
                40,
                200,
                30
        );

        panel.add(
                statusTitle
        );


        /*
         * 밑줄
         */

        JSeparator separator =
                new JSeparator();

        separator.setForeground(
                new Color(
                        130,
                        130,
                        130
                )
        );

        separator.setBounds(
                18,
                72,
                230,
                3
        );

        panel.add(
                separator
        );


        /*
         * 대출 숫자
         */

        loanCountLabel =
                new JLabel(
                        "0",
                        SwingConstants.CENTER
                );

        loanCountLabel.setFont(
                FontManager.getFont(38f)
        );

        loanCountLabel.setBounds(
                35,
                82,
                70,
                50
        );

        panel.add(
                loanCountLabel
        );


        JLabel loanLabel =
                new JLabel(
                        "대출",
                        SwingConstants.CENTER
                );

        loanLabel.setFont(
                FontManager.getFont(17f)
        );

        loanLabel.setBounds(
                35,
                128,
                70,
                30
        );

        panel.add(
                loanLabel
        );


        /*
         * 연체 숫자
         */

        overdueCountLabel =
                new JLabel(
                        "0",
                        SwingConstants.CENTER
                );

        overdueCountLabel.setFont(
                FontManager.getFont(38f)
        );

        overdueCountLabel.setForeground(
                new Color(
                        220,
                        35,
                        35
                )
        );

        overdueCountLabel.setBounds(
                145,
                82,
                70,
                50
        );

        panel.add(
                overdueCountLabel
        );


        JLabel overdueLabel =
                new JLabel(
                        "연체",
                        SwingConstants.CENTER
                );

        overdueLabel.setFont(
                FontManager.getFont(17f)
        );

        overdueLabel.setForeground(
                new Color(
                        220,
                        35,
                        35
                )
        );

        overdueLabel.setBounds(
                145,
                128,
                70,
                30
        );

        panel.add(
                overdueLabel
        );


        /*
         * ==============================
         * 기능 버튼
         * ==============================
         */

        CircleButton searchButton =
                new CircleButton(
                        "도서검색"
                );

        searchButton.setBounds(
                30,
                190,
                76,
                76
        );

        panel.add(
                searchButton
        );


        CircleButton bookshelfButton =
                new CircleButton(
                        "내 서재"
                );

        bookshelfButton.setBounds(
                140,
                190,
                76,
                76
        );

        panel.add(
                bookshelfButton
        );


        /*
         * ==============================
         * 사용자 프로필
         * ==============================
         */

        JPanel profileCircle =
                new JPanel() {

                    @Override
                    protected void paintComponent(
                            Graphics g
                    ) {

                        Graphics2D g2 =
                                (Graphics2D)
                                        g.create();

                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        g2.setColor(
                                Color.BLACK
                        );

                        g2.fillOval(
                                0,
                                0,
                                getWidth(),
                                getHeight()
                        );

                        g2.dispose();
                    }
                };

        profileCircle.setOpaque(false);

        profileCircle.setBounds(
                35,
                410,
                70,
                70
        );

        panel.add(
                profileCircle
        );


        JLabel nameLabel =
                new JLabel(
                        currentUser.getName()
                );

        nameLabel.setFont(
                FontManager.getFont(18f)
        );

        nameLabel.setBounds(
                120,
                415,
                125,
                30
        );

        panel.add(
                nameLabel
        );


        JLabel idLabel =
                new JLabel(
                        "@"
                                + currentUser.getId()
                );

        idLabel.setFont(
                FontManager.getFont(15f)
        );

        idLabel.setBounds(
                120,
                445,
                125,
                30
        );

        panel.add(
                idLabel
        );


        /*
         * ==============================
         * 로그아웃
         * ==============================
         */

        JButton logoutButton =
                new JButton(
                        "<html><u>로그아웃</u></html>"
                );

        logoutButton.setFont(
                FontManager.getFont(15f)
        );

        logoutButton.setForeground(
                new Color(
                        130,
                        130,
                        130
                )
        );

        logoutButton.setContentAreaFilled(
                false
        );

        logoutButton.setBorderPainted(
                false
        );

        logoutButton.setFocusPainted(
                false
        );

        logoutButton.setBounds(
                2,
                530,
                110,
                40
        );

        panel.add(
                logoutButton
        );


        /*
         * ==============================
         * 오른쪽 년도
         * ==============================
         */

        JLabel yearLabel =
                new JLabel(
                        String.valueOf(
                                LocalDate
                                        .now()
                                        .getYear()
                        ),
                        SwingConstants.CENTER
                );

        yearLabel.setFont(
                FontManager.getFont(38f)
        );

        yearLabel.setBounds(
                380,
                20,
                280,
                50
        );

        panel.add(
                yearLabel
        );


        /*
         * ==============================
         * 책장
         * ==============================
         */

        bookshelfPanel =
                new BookshelfPanel(
                        currentUser.getId()
                );

        bookshelfPanel.setBounds(
                265,
                75,
                490,
                445
        );

        panel.add(
                bookshelfPanel
        );


        /*
         * 아래 점 세 개
         */

        JLabel dots =
                new JLabel(
                        "● ○ ○",
                        SwingConstants.CENTER
                );

        dots.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        17
                )
        );

        dots.setBounds(
                450,
                525,
                120,
                30
        );

        panel.add(
                dots
        );


        /*
         * ==============================
         * 이벤트
         * ==============================
         */

        searchButton.addActionListener(
                e -> openBookSearch()
        );

        bookshelfButton.addActionListener(
                e -> openMyBookshelf()
        );

        logoutButton.addActionListener(
                e -> logout()
        );
    }


    /*
     * 대출/연체 숫자 갱신
     */

    /**
     * 연체 상태를 저장한 뒤 현재 회원의 대출·연체 수와 올해 책장을 갱신함. 이벤트 디스패치 스레드 외부에서 호출하면 해당 스레드에 작업을 예약하고 반환함.
     *
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 연체 갱신 대상 대출 기록이 사라진 경우
     */
    public void refreshStatus() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshStatus);
            return;
        }

        loanService.checkOverdue();

        int loanCount =
                loanService
                        .getCurrentLoans(
                                currentUser
                                        .getId()
                        )
                        .size();

        int overdueCount =
                loanService
                        .getOverdueLoans(
                                currentUser
                                        .getId()
                        )
                        .size();

        loanCountLabel.setText(
                String.valueOf(
                        loanCount
                )
        );

        overdueCountLabel.setText(
                String.valueOf(
                        overdueCount
                )
        );

        bookshelfPanel
                .refreshBooks();
    }


    /*
     * 반납 하루 전 안내
     */

    private void checkDueTomorrow() {

        List<Loan> dueLoans =
                loanService
                        .getDueTomorrowLoans(
                                currentUser
                                        .getId()
                        );

        if (dueLoans.isEmpty()) {
            return;
        }

        StringBuilder message =
                new StringBuilder(
                        "반납일이 하루 남았습니다.\n\n"
                );

        for (Loan loan : dueLoans) {

            var book =
                    bookService
                            .getBookById(
                                    loan.getBookId()
                            );

            if (book != null) {

                message
                        .append("• ")
                        .append(
                                book.getTitle()
                        )
                        .append("\n");
            }
        }

        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Bookstie",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /*
     * 도서검색
     */

    private void openBookSearch() {
        BookSearchFrame searchFrame = new BookSearchFrame(this);
        searchFrame.setVisible(true);
        setVisible(false);
    }


    /*
     * 내 서재
     */

    private void openMyBookshelf() {
        MyBookshelfFrame myBookshelfFrame = new MyBookshelfFrame(this);
        myBookshelfFrame.setVisible(true);
        setVisible(false);
    }


    /*
     * 로그아웃
     */

    private void logout() {

        authService.logout();

        LoginFrame loginFrame =
                new LoginFrame();

        loginFrame.setVisible(
                true
        );

        dispose();
    }
}
