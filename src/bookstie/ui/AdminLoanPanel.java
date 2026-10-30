package bookstie.ui;

import bookstie.model.Book;
import bookstie.model.Loan;
import bookstie.service.AdminService;
import bookstie.service.LoanService;
import java.util.HashMap;
import java.util.Map;

/**
 * 관리자용 대출 목록과 연체 상태를 표시하고 실물 수령 확인 후 반납 처리를 요청하는 패널. 별점에 따른 완독 처리와 반납은 독립적임.
 */
public class AdminLoanPanel extends AdminTablePanel {
    /**
     * 대출 목록과 반납 버튼을 만들고 연체 상태 및 목록을 갱신함.
     *
     * @param service 조회 및 변경을 요청할 관리자 서비스
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 잘못된 경우
     */
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
        boolean returned;
        try {
            returned = service.returnBook(id);
        } catch (RuntimeException e) {
            notice("반납을 저장하지 못했습니다.\n" + e.getMessage());
            return;
        }
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
