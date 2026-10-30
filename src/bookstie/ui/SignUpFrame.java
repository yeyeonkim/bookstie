package bookstie.ui;

import bookstie.service.AuthService;
import bookstie.service.SignUpResult;
import bookstie.util.FontManager;

import javax.swing.*;
import java.awt.*;

/**
 * 이름·ID·비밀번호·확인 비밀번호를 입력받아 회원가입을 요청하고 결과를 표시하는 창. 가입 성공 또는 뒤로 이동 시 기존 로그인 창을 다시 표시함.
 */
public class SignUpFrame extends JFrame {

    private final AuthService authService;
    private final LoginFrame loginFrame;

    private PlaceholderTextField nameField;
    private PlaceholderTextField idField;

    private PlaceholderPasswordField passwordField;
    private PlaceholderPasswordField confirmPasswordField;

    private JLabel messageLabel;

    /**
     * 회원가입 입력 창을 만들고 돌아갈 로그인 창을 보관함.
     *
     * @param loginFrame 회원가입 완료 또는 뒤로 이동 시 다시 표시할 로그인 창
     */
    public SignUpFrame(
            LoginFrame loginFrame
    ) {

        this.loginFrame =
                loginFrame;

        authService =
                new AuthService();

        setTitle(
                "Bookstie - Sign Up"
        );

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        setResizable(false);

        BookstieBackgroundPanel panel =
                new BookstieBackgroundPanel(false);

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
         * --------------------------------
         * 위쪽 문구 / 오류 메시지
         * --------------------------------
         */

        messageLabel =
                new JLabel(
                        "HELLO, NICE TO MEET YOU!",
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
                125,
                360,
                35
        );

        panel.add(messageLabel);


        /*
         * --------------------------------
         * 회원가입 박스
         * --------------------------------
         */

        JPanel formPanel =
                new JPanel(null);

        formPanel.setBackground(
                Color.WHITE
        );

        formPanel.setBorder(
                new RoundedBorder(
                        14,
                        3
                )
        );

        formPanel.setBounds(
                250,
                161,
                300,
                255
        );

        panel.add(formPanel);


        /*
         * NAME
         */

        nameField =
                new PlaceholderTextField(
                        "name"
                );

        nameField.setBounds(
                27,
                18,
                240,
                37
        );

        formPanel.add(
                nameField
        );


        /*
         * ID
         */

        idField =
                new PlaceholderTextField(
                        "id"
                );

        idField.setBounds(
                27,
                63,
                240,
                37
        );

        formPanel.add(
                idField
        );


        /*
         * PASSWORD
         */

        passwordField =
                new PlaceholderPasswordField(
                        "password"
                );

        passwordField.setBounds(
                27,
                108,
                240,
                37
        );

        formPanel.add(
                passwordField
        );


        /*
         * CONFIRM PASSWORD
         */

        confirmPasswordField =
                new PlaceholderPasswordField(
                        "confirm password"
                );

        confirmPasswordField.setBounds(
                27,
                153,
                240,
                37
        );

        formPanel.add(
                confirmPasswordField
        );


        /*
         * --------------------------------
         * JOIN 버튼
         * --------------------------------
         */

        JButton joinButton =
                new JButton("join") {

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

                        /*
                         * 둥근 검정 배경
                         */
                        g2.setColor(
                                Color.BLACK
                        );

                        g2.fillRoundRect(
                                0,
                                0,
                                getWidth(),
                                getHeight(),
                                16,
                                16
                        );


                        /*
                         * join 글자
                         */
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
                                )) / 2;

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


        joinButton.setFont(
                FontManager.getFont(14f)
        );

        joinButton.setContentAreaFilled(
                false
        );

        joinButton.setBorderPainted(
                false
        );

        joinButton.setFocusPainted(
                false
        );

        joinButton.setOpaque(
                false
        );

        joinButton.setMargin(
                new Insets(
                        0,
                        0,
                        0,
                        0
                )
        );

        joinButton.setBounds(
                112,
                202,
                76,
                38
        );

        formPanel.add(
                joinButton
        );


        /*
         * --------------------------------
         * BACK TO LOGIN
         * --------------------------------
         */

        JButton backButton =
                new JButton(
                        "<html><u>Or... back to login</u></html>"
                );

        backButton.setFont(
                FontManager.getFont(16f)
        );

        backButton.setForeground(
                Color.BLACK
        );

        backButton.setContentAreaFilled(
                false
        );

        backButton.setBorderPainted(
                false
        );

        backButton.setFocusPainted(
                false
        );

        backButton.setBounds(
                270,
                416,
                260,
                28
        );

        panel.add(
                backButton
        );


        /*
         * --------------------------------
         * 이벤트
         * --------------------------------
         */

        joinButton.addActionListener(
                e -> signUp()
        );

        backButton.addActionListener(
                e -> backToLogin()
        );

        confirmPasswordField
                .addActionListener(
                        e -> signUp()
                );
    }


    /*
     * --------------------------------
     * 회원가입
     * --------------------------------
     */

    private void signUp() {

        String name =
                nameField
                        .getText()
                        .trim();

        String id =
                idField
                        .getText()
                        .trim();

        String password =
                new String(
                        passwordField
                                .getPassword()
                );

        String confirmPassword =
                new String(
                        confirmPasswordField
                                .getPassword()
                );

        SignUpResult result =
                authService.signUp(
                        name,
                        id,
                        password,
                        confirmPassword
                );


        switch (result) {

            /*
             * 빈칸
             */
            case EMPTY_FIELD -> {

                messageLabel.setForeground(
                        new Color(
                                220,
                                35,
                                35
                        )
                );

                messageLabel.setText(
                        "빈칸을 전부 채워주세요."
                );
            }


            /*
             * 비밀번호 불일치
             */
            case PASSWORD_MISMATCH -> {

                messageLabel.setForeground(
                        new Color(
                                220,
                                35,
                                35
                        )
                );

                messageLabel.setText(
                        "비밀번호가 일치하지 않습니다."
                );
            }


            /*
             * ID 중복
             */
            case DUPLICATE_ID -> {

                messageLabel.setForeground(
                        new Color(
                                220,
                                35,
                                35
                        )
                );

                messageLabel.setText(
                        "이미 존재하는 id입니다."
                );
            }


            /*
             * 성공
             */
            case SUCCESS -> {

                JOptionPane.showMessageDialog(
                        this,
                        "회원가입이 완료되었습니다."
                );

                backToLogin();
            }
        }
    }


    /*
     * --------------------------------
     * 로그인 화면으로 복귀
     * --------------------------------
     */

    private void backToLogin() {

        loginFrame.setVisible(
                true
        );

        dispose();
    }
}