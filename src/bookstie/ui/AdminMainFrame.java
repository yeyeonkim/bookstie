package bookstie.ui;

import bookstie.service.AdminService;
import bookstie.service.AuthService;
import bookstie.util.SessionManager;
import javax.swing.*;
import java.awt.*;

/**
 * 관리자 로그인 여부를 확인한 뒤 도서 관리, 대출 현황, 회원 관리 패널을 전환하고 로그아웃을 제공하는 창.
 */
public class AdminMainFrame extends JFrame {
    /**
     * 관리자 세션을 확인하고 관리 패널과 메뉴를 구성함.
     *
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 관리자 세션이 없거나 도서 ID 중복 또는 손상된 대출 기록이 있는 경우 또는 데이터 디렉터리를 결정할 수 없는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 잘못된 경우
     */
    public AdminMainFrame() {
        if (!SessionManager.isAdminLoggedIn()) {
            throw new IllegalStateException("관리자 로그인이 필요합니다.");
        }
        setTitle("Bookstie - 관리자");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        JPanel root = new JPanel(null);
        root.setBackground(Color.BLACK);
        root.setPreferredSize(new Dimension(800, 600));
        setContentPane(root);
        CardLayout cards = new CardLayout();
        JPanel content = new JPanel(cards) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.setBounds(165, 42, 598, 516);
        root.add(content);
        AdminService service = new AdminService();
        AdminTablePanel[] panels = {new AdminBookPanel(service),
                new AdminLoanPanel(service), new AdminUserPanel(service)};
        String[] labels = {"<html><center>도서<br>등록/삭제</center></html>",
                "<html><center>대출<br>현황 조회</center></html>", "회원 관리"};
        for (int i = 0; i < panels.length; i++) {
            String key = String.valueOf(i);
            AdminTablePanel panel = panels[i];
            content.add(panel, key);
            JButton menu = AdminStyle.button(labels[i], true, true);
            menu.setBounds(34, 100 + i * 150, 100, 100);
            menu.addActionListener(e -> {
                panel.refresh();
                cards.show(content, key);
            });
            root.add(menu);
        }
        JButton logout = AdminStyle.button("로그아웃", true, false);
        logout.setBounds(34, 530, 100, 34);
        logout.addActionListener(e -> {
            new AuthService().logout();
            new LoginFrame().setVisible(true);
            dispose();
        });
        root.add(logout);
        cards.show(content, "0");
        pack();
        setLocationRelativeTo(null);
    }
}
