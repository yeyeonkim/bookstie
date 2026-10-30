package bookstie.ui;

import bookstie.model.Book;
import bookstie.service.AdminService;
import javax.swing.*;
import java.awt.*;

/**
 * 관리자용 도서 목록을 표시하고 도서 등록·선택 삭제·새로고침을 서비스에 연결하는 패널.
 */
public class AdminBookPanel extends AdminTablePanel {
    /**
     * 도서 목록과 등록·삭제 버튼을 만들고 목록을 읽음.
     *
     * @param service 조회 및 변경을 요청할 관리자 서비스
     * @throws java.io.UncheckedIOException 도서·대출 파일 읽기 또는 예외를 전달하는 저장 작업에 실패한 경우
     * @throws IllegalStateException 도서 ID가 중복되거나 대출 파일에 해석할 수 없는 행이 있는 경우
     * @throws IllegalArgumentException 도서 파일의 상태 값이 잘못된 경우
     */
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
