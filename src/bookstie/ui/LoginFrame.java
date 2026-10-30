package bookstie.ui;

import bookstie.model.User;
import bookstie.service.AuthService;
import bookstie.service.LoanService;
import bookstie.util.FontManager;

import javax.swing.*;
import java.awt.*;

/**
 * 회원 로그인을 처리하고 회원가입 및 관리자 로그인 화면으로의 이동을 제공하는 시작 창.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final LoanService loanService;

    private PlaceholderTextField idField;
    private PlaceholderPasswordField passwordField;

    // HELLO 문구와 로그인 실패 메시지를
    // 같은 위치에서 표시
    private JLabel messageLabel;

    /**
     * 로그인 입력 창, 회원가입 및 관리자 로그인 이동 버튼과 서비스를 준비함.
     *
     * @throws java.io.UncheckedIOException 대출 파일이나 상위 디렉터리를 생성하지 못한 경우
     * @throws IllegalStateException 데이터 디렉터리를 결정할 수 없는 경우
     */
    public LoginFrame() {

        authService = new AuthService();
        loanService = new LoanService();

        setTitle("Bookstie");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setResizable(false);

        BookstieBackgroundPanel panel =
                new BookstieBackgroundPanel(true);

        panel.setPreferredSize(
                new Dimension(
                        800,
                        600
                )
        );

        setContentPane(panel);

        initUI(panel);

        pack();

        setLocationRelativeTo(null);
    }

    private void initUI(
            BookstieBackgroundPanel panel
    ) {

        /*
         * ---------------------------------
         * HELLO / 로그인 실패 메시지
         * ---------------------------------
         */

        messageLabel =
                new JLabel(
                        "HELLO, WELCOME BACK!",
                        SwingConstants.CENTER
                );

        messageLabel.setFont(
                FontManager.getFont(22f)
        );

        messageLabel.setForeground(
                Color.BLACK
        );

        messageLabel.setBounds(
                220,
                191,
                360,
                35
        );

        panel.add(messageLabel);


        /*
         * ---------------------------------
         * 가운데 로그인 박스
         * ---------------------------------
         */

        JPanel loginPanel =
                new JPanel(null);

        loginPanel.setBackground(
                Color.WHITE
        );

        loginPanel.setBorder(
                new RoundedBorder(
                        14,
                        3
                )
        );

        loginPanel.setBounds(
                250,
                225,
                305,
                155
        );

        panel.add(loginPanel);


        /*
         * ---------------------------------
         * ID 입력창
         * ---------------------------------
         */

        idField =
                new PlaceholderTextField(
                        "id"
                );
        idField.setFont(
                FontManager.getFont(17f)
        );

        idField.setBorder(
                new RoundedBorder(
                        14,
                        3
                )
        );

        idField.setBounds(
                25,
                35,
                175,
                37
        );

        idField.putClientProperty(
                "JTextField.placeholderText",
                "id"
        );

        loginPanel.add(idField);


        /*
         * ---------------------------------
         * PASSWORD 입력창
         * ---------------------------------
         */

        passwordField =
                new PlaceholderPasswordField(
                        "password"
                );

        passwordField.setFont(
                FontManager.getFont(17f)
        );

        passwordField.setBorder(
                new RoundedBorder(
                        14,
                        3
                )
        );

        passwordField.setBounds(
                25,
                80,
                175,
                37
        );

        passwordField.putClientProperty(
                "JTextField.placeholderText",
                "password"
        );

        loginPanel.add(passwordField);


        /*
         * ---------------------------------
         * LOGIN 버튼
         * ---------------------------------
         */

        JButton loginButton = new JButton("login") {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 =
                        (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // 둥근 검정 배경
                g2.setColor(Color.BLACK);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        18,
                        18
                );

                // login 글자
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());

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
                // 기본 테두리 제거
            }
        };

        loginButton.setFont(
                FontManager.getFont(15f)
        );

        loginButton.setForeground(Color.WHITE);

// Swing 기본 버튼 배경 제거
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(false);

        loginButton.setMargin(
                new Insets(0, 0, 0, 0)
        );

        loginButton.setBounds(
                210,
                35,
                72,
                82
        );

        loginPanel.add(loginButton);


        /*
         * ---------------------------------
         * CREATE NEW ACCOUNT
         * ---------------------------------
         */

        JButton signUpButton =
                new JButton(
                        "<html><u>Or... Create new account</u></html>"
                );

        signUpButton.setFont(
                FontManager.getFont(16f)
        );

        signUpButton.setForeground(
                Color.BLACK
        );

        signUpButton.setBackground(
                Color.WHITE
        );

        signUpButton.setBorderPainted(false);
        signUpButton.setFocusPainted(false);
        signUpButton.setContentAreaFilled(false);

        signUpButton.setBounds(
                245,
                382,
                320,
                45
        );

        panel.add(signUpButton);


        /*
         * ---------------------------------
         * ADMIN 버튼
         * ---------------------------------
         */

        JButton adminButton =
                new JButton("admin") {

                    @Override
                    protected void paintComponent(
                            Graphics g
                    ) {

                        Graphics2D g2 =
                                (Graphics2D) g.create();

                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        // 검은 원
                        g2.setColor(
                                Color.BLACK
                        );

                        g2.fillOval(
                                0,
                                0,
                                getWidth(),
                                getHeight()
                        );

                        // 글자
                        g2.setColor(
                                Color.WHITE
                        );

                        g2.setFont(
                                getFont()
                        );

                        FontMetrics fm =
                                g2.getFontMetrics();

                        int x =
                                (getWidth()
                                        - fm.stringWidth(
                                        getText()
                                )) / 2 ;

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
                    protected void paintBorder(
                            Graphics g
                    ) {
                        // 기본 버튼 테두리 제거
                    }
                };

        adminButton.setFont(
                FontManager.getFont(14f)
        );

        adminButton.setBorderPainted(
                false
        );

        adminButton.setContentAreaFilled(
                false
        );

        adminButton.setFocusPainted(
                false
        );

        adminButton.setOpaque(false);

        adminButton.setBounds(
                695,
                520,
                70,
                70
        );

        panel.add(adminButton);


        /*
         * ---------------------------------
         * 버튼 이벤트
         * ---------------------------------
         */

        loginButton.addActionListener(
                e -> login()
        );

        signUpButton.addActionListener(
                e -> openSignUp()
        );

        adminButton.addActionListener(
                e -> openAdminLogin()
        );

        // password 입력 후 Enter를 눌러도 로그인
        passwordField.addActionListener(
                e -> login()
        );
    }


    /*
     * ---------------------------------
     * 로그인
     * ---------------------------------
     */

    private void login() {

        String id =
                idField
                        .getText()
                        .trim();

        String password =
                new String(
                        passwordField
                                .getPassword()
                );

        User user =
                authService.login(
                        id,
                        password
                );

        /*
         * 실패
         */
        if (user == null) {

            messageLabel.setText(
                    "Wrong id/password...."
            );

            return;
        }


        /*
         * 성공
         */

        messageLabel.setText(
                "HELLO, NICE TO MEET YOU!"
        );

        loanService.checkOverdue();

        loanService.checkOverdue();

        MainFrame mainFrame =
                new MainFrame();

        mainFrame.setVisible(true);

        dispose();

        /*
         * MainFrame 완성 후:
         *
         * new MainFrame().setVisible(true);
         * dispose();
         */
    }


    /*
     * ---------------------------------
     * 회원가입 화면
     * ---------------------------------
     */

    private void openSignUp() {

        SignUpFrame signUpFrame =
                new SignUpFrame(
                        this
                );

        signUpFrame.setVisible(
                true
        );

        setVisible(
                false
        );
    }


    /*
     * ---------------------------------
     * 관리자 로그인
     * ---------------------------------
     */

    private void openAdminLogin() {

        new AdminLoginFrame().setVisible(true);
        dispose();
    }
}
