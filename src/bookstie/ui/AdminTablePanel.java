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
            /**
             * 관리자 표의 셀 직접 편집을 막음.
             *
             * @param row 행 인덱스
             * @param column 열 인덱스
             * @return 항상 false
             */
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
