package bookstie.ui;

import bookstie.service.AuthService;
import bookstie.util.FontManager;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * 관리자 ID와 비밀번호를 입력받아 인증하고 성공 시 관리자 메인 창으로 이동하는 창.
 */
public class AdminLoginFrame extends JFrame {
    /**
     * 관리자 로그인 입력 창과 회원 로그인으로 이동하는 버튼을 구성함.
     */
    public AdminLoginFrame() {
        setTitle("Bookstie - 관리자 로그인");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        JPanel page = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                for (int x = -26; x < 800; x += 128) {
                    g2.fillOval(x, 16, 70, 70);
                    g2.fillOval(x, 448, 70, 70);
                }
                for (int x = 44; x < 800; x += 128) {
                    g2.fillOval(x, -54, 70, 70);
                    g2.fillOval(x, 86, 70, 70);
                    if (x < 680) g2.fillOval(x, 518, 70, 70);
                }
                g2.dispose();
            }
        };
        page.setBackground(Color.BLACK);
        page.setPreferredSize(new Dimension(800, 600));
        setContentPane(page);
        JLabel message = new JLabel("WAIT, ARE YOU SURE?", SwingConstants.CENTER);
        message.setFont(FontManager.getFont(22f));
        message.setForeground(Color.WHITE);
        message.setBounds(220, 191, 360, 35);
        page.add(message);
        JPanel box = new JPanel(null);
        box.setBackground(Color.BLACK);
        box.setBorder(AdminStyle.border(Color.WHITE));
        box.setBounds(252, 227, 300, 150);
        page.add(box);
        JTextField id = new PlaceholderTextField("id");
        JPasswordField password = new PlaceholderPasswordField("password");
        JTextField[] fields = {id, password};
        for (int i = 0; i < fields.length; i++) {
            JTextField field = fields[i];
            field.setOpaque(true);
            field.setBackground(new Color(28, 28, 28));
            field.setForeground(Color.WHITE);
            field.setCaretColor(Color.WHITE);
            field.setFont(FontManager.getFont(20f));
            field.setBorder(AdminStyle.border(Color.WHITE));
            field.setBounds(26, 36 + i * 42, 170, 36);
            box.add(field);
        }
        id.getAccessibleContext().setAccessibleName("관리자 ID");
        password.getAccessibleContext().setAccessibleName("관리자 비밀번호");
        JButton login = AdminStyle.button("login", true, false);
        login.setBorder(BorderFactory.createEmptyBorder());
        login.setBounds(206, 36, 67, 78);
        box.add(login);
        login.addActionListener(e -> {
            char[] secret = password.getPassword();
            boolean success;
            try {
                success = new AuthService().adminLogin(id.getText().trim(),
                        new String(secret)) != null;
            } finally {
                Arrays.fill(secret, '\0');
                password.setText("");
            }
            if (!success) {
                message.setText("Wrong id/password....");
                password.requestFocusInWindow();
                return;
            }
            new AdminMainFrame().setVisible(true);
            dispose();
        });
        getRootPane().setDefaultButton(login);
        JButton user = AdminStyle.button("user", true, true);
        user.setBorder(BorderFactory.createEmptyBorder());
        user.setFont(FontManager.getFont(14f));
        user.setBounds(684, 518, 70, 70);
        user.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        page.add(user);
        JLabel hint = new JLabel("u lost bestie? ↓");
        hint.setFont(FontManager.getFont(17f));
        hint.setForeground(Color.WHITE);
        hint.setBounds(576, 408, 190, 35);
        page.add(hint);
        pack();
        setLocationRelativeTo(null);
    }
}
