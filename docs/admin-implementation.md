# Bookstie 관리자 화면 전체 코드

관리자 로그인과 도서 등록·삭제, 전체 대출 조회·반납 처리, 회원 조회·삭제를 구현합니다. 회원 등록은 제공하지 않습니다. 관리자 로그인 정보는 기존 `data/admins.txt`의 `ID\t비밀번호` 형식을 사용합니다.

기존 파일 변경: `LoginFrame`의 admin 버튼 연결, `AdminService`의 대출 도서 삭제 차단·회원 삭제 후 대출 기록 유지·도서 입력 검증, `IdGenerator`의 과거 대출에 남은 도서 ID 재사용 방지.

## AdminLoginFrame.java

```java
package bookstie.ui;

import bookstie.service.AuthService;
import bookstie.util.FontManager;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class AdminLoginFrame extends JFrame {
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
                    g2.fillOval(x, 589, 70, 70);
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

```

## AdminMainFrame.java

```java
package bookstie.ui;

import bookstie.service.AdminService;
import bookstie.service.AuthService;
import bookstie.util.SessionManager;
import javax.swing.*;
import java.awt.*;

public class AdminMainFrame extends JFrame {
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

```

## AdminBookPanel.java

```java
package bookstie.ui;

import bookstie.model.Book;
import bookstie.service.AdminService;
import javax.swing.*;
import java.awt.*;

public class AdminBookPanel extends AdminTablePanel {
    public AdminBookPanel(AdminService service) {
        super(service, "도서 등록/삭제", "도서 ID", "제목", "저자", "상태");
        int[] widths = {70, 200, 130, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        action("도서 등록", this::addBook);
        action("선택 삭제", this::deleteBook);
        action("새로고침", this::refresh);
        refresh();
    }

    private void addBook() {
        JTextField title = new JTextField(22);
        JTextField author = new JTextField(22);
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel("제목"));
        form.add(title);
        form.add(new JLabel("저자"));
        form.add(author);
        while (AdminStyle.dialog(this, form, "도서 등록", "등록", "취소") == 0) {
            Book book = service.addBook(title.getText(), author.getText());
            if (book == null) {
                notice("제목과 저자를 입력해주세요. 탭과 줄바꿈은 사용할 수 없습니다.");
                continue;
            }
            refresh();
            notice("도서가 등록되었습니다.\n도서 ID: " + book.getBookId());
            return;
        }
    }

    private void deleteBook() {
        String id = selectedId();
        if (id == null || !confirm("도서 " + id + "를 삭제하시겠습니까?")) return;
        boolean deleted = service.deleteBook(id);
        refresh();
        notice(deleted ? "삭제되었습니다." : "대출 중이거나 존재하지 않는 도서는 삭제할 수 없습니다.");
    }

    @Override void refresh() {
        model.setRowCount(0);
        for (Book book : service.getAllBooks()) {
            model.addRow(new Object[]{book.getBookId(), book.getTitle(),
                    book.getAuthor(), book.getStatus()});
        }
    }
}

```

## AdminLoanPanel.java

```java
package bookstie.ui;

import bookstie.model.Book;
import bookstie.model.Loan;
import bookstie.service.AdminService;
import bookstie.service.LoanService;
import java.util.HashMap;
import java.util.Map;

public class AdminLoanPanel extends AdminTablePanel {
    public AdminLoanPanel(AdminService service) {
        super(service, "대출 현황 조회", "대출 ID", "회원 ID", "도서 ID", "제목",
                "대출일", "반납 예정일", "반납일", "상태");
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(i == 3 ? 200 : 105);
        }
        action("반납 처리", this::returnBook);
        action("새로고침", this::refresh);
        refresh();
    }

    private void returnBook() {
        String id = selectedId();
        if (id == null) return;
        if (!confirm("대출 " + id + "의 실제 책을 받으셨습니까?\n반납 처리하시겠습니까?")) return;
        boolean returned = service.returnBook(id);
        refresh();
        notice(returned ? "반납 처리되었습니다." : "이미 반납되었거나 처리할 수 없는 대출입니다.");
    }

    @Override void refresh() {
        new LoanService().checkOverdue();
        Map<String, String> titles = new HashMap<>();
        for (Book book : service.getAllBooks()) titles.put(book.getBookId(), book.getTitle());
        model.setRowCount(0);
        for (Loan loan : service.getAllLoans()) {
            model.addRow(new Object[]{loan.getLoanId(), loan.getUserId(), loan.getBookId(),
                    titles.getOrDefault(loan.getBookId(), "삭제된 도서"),
                    loan.getLoanDate(), loan.getDueDate(), loan.getReturnDate(), loan.getStatus()});
        }
    }
}

```

## AdminUserPanel.java

```java
package bookstie.ui;

import bookstie.model.Loan;
import bookstie.model.User;
import bookstie.service.AdminService;
import java.util.HashMap;
import java.util.Map;

public class AdminUserPanel extends AdminTablePanel {
    public AdminUserPanel(AdminService service) {
        super(service, "회원 관리", "회원 ID", "이름", "현재 대출 수");
        action("회원 삭제", this::deleteUser);
        action("새로고침", this::refresh);
        refresh();
    }

    private void deleteUser() {
        String id = selectedId();
        if (id == null || !confirm("회원 " + id + "를 삭제하시겠습니까?")) return;
        boolean deleted = service.deleteUser(id);
        refresh();
        notice(deleted ? "삭제되었습니다." : "대출 중이거나 존재하지 않는 회원은 삭제할 수 없습니다.");
    }

    @Override void refresh() {
        Map<String, Integer> counts = new HashMap<>();
        for (Loan loan : service.getAllLoans()) {
            if (!loan.isReturned()) counts.merge(loan.getUserId(), 1, Integer::sum);
        }
        model.setRowCount(0);
        for (User user : service.getAllUsers()) {
            model.addRow(new Object[]{user.getId(), user.getName(), counts.getOrDefault(user.getId(), 0)});
        }
    }
}

```

## AdminTablePanel.java

```java
package bookstie.ui;

import bookstie.service.AdminService;
import bookstie.util.FontManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

abstract class AdminTablePanel extends JPanel {
    protected final AdminService service;
    protected final DefaultTableModel model;
    protected final JTable table;
    protected final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JLabel count = new JLabel();

    AdminTablePanel(AdminService service, String title, String... columns) {
        this.service = service;
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(24, 18, 20, 18));
        JLabel heading = new JLabel("<" + title + ">", SwingConstants.CENTER);
        heading.setFont(FontManager.getFont(30f));
        heading.setForeground(Color.BLACK);
        add(heading, BorderLayout.NORTH);
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(FontManager.getFont(14f));
        table.setRowHeight(38);
        table.setForeground(Color.BLACK);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(Color.BLACK);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(FontManager.getFont(15f));
        table.getTableHeader().setBackground(Color.BLACK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        DefaultTableCellRenderer header = new DefaultTableCellRenderer();
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setFont(FontManager.getFont(15f));
        header.setBackground(Color.BLACK);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        table.getTableHeader().setDefaultRenderer(header);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        add(scroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        count.setFont(FontManager.getFont(14f));
        count.setForeground(Color.GRAY);
        footer.add(count, BorderLayout.WEST);
        actions.setBackground(Color.WHITE);
        footer.add(actions, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
        model.addTableModelListener(e -> count.setText("총 " + model.getRowCount() + "건"));
        count.setText("총 0건");
    }

    protected void action(String text, Runnable handler) {
        JButton button = AdminStyle.button(text, false, false);
        button.setPreferredSize(new Dimension(108, 36));
        button.addActionListener(e -> handler.run());
        actions.add(button);
    }

    protected String selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            notice("먼저 목록에서 항목을 선택해주세요.");
            return null;
        }
        return model.getValueAt(table.convertRowIndexToModel(row), 0).toString();
    }

    protected void notice(String text) {
        AdminStyle.dialog(this, text, "Bookstie", "확인");
    }

    protected boolean confirm(String text) {
        return AdminStyle.dialog(this, text, "Bookstie", "YES", "NO") == 0;
    }

    abstract void refresh();
}

```

## AdminStyle.java

```java
package bookstie.ui;

import bookstie.util.FontManager;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

final class AdminStyle {
    private AdminStyle() {}

    static JButton button(String text, boolean white, boolean circle) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (white ? Color.WHITE : Color.BLACK) : Color.GRAY);
                if (circle) g2.fillOval(0, 0, getWidth(), getHeight());
                else g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                if (hasFocus()) {
                    g2.setColor(white ? Color.BLACK : Color.WHITE);
                    g2.drawRoundRect(5, 5, getWidth() - 11, getHeight() - 11, 18, 18);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FontManager.getFont(16f));
        button.setForeground(white ? Color.BLACK : Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    static AbstractBorder border(Color color) {
        return new AbstractBorder() {
            @Override public Insets getBorderInsets(Component c) {
                return new Insets(7, 10, 7, 10);
            }
            @Override public void paintBorder(Component c, Graphics g,
                                               int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 16, 16);
                g2.dispose();
            }
        };
    }

    static int dialog(Component owner, Object message, String title, String... options) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options, options[options.length - 1]);
        style(pane);
        JDialog dialog = pane.createDialog(owner, title);
        dialog.setVisible(true);
        Object value = pane.getValue();
        dialog.dispose();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) return i;
        }
        return -1;
    }

    private static void style(Component component) {
        component.setFont(FontManager.getFont(16f));
        component.setBackground(Color.WHITE);
        component.setForeground(Color.BLACK);
        if (component instanceof JButton button) {
            button.setBorder(new RoundedBorder(16, 2));
            button.setContentAreaFilled(false);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) style(child);
        }
    }
}

```
