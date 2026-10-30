package bookstie.ui;

import bookstie.model.Loan;
import bookstie.model.User;
import bookstie.service.AdminService;
import java.util.HashMap;
import java.util.Map;

/**
 * 회원 목록과 현재 미반납 대출 수를 표시하고 선택한 회원의 삭제를 요청하는 관리자 패널.
 */
public class AdminUserPanel extends AdminTablePanel {
    /**
     * 회원 목록과 삭제 버튼을 만들고 현재 대출 수를 읽음.
     *
     * @param service 조회 및 변경을 요청할 관리자 서비스
     * @throws java.io.UncheckedIOException 대출 파일 읽기 또는 필요한 저장에 실패한 경우
     * @throws IllegalStateException 대출 파일에 해석할 수 없는 행이 있는 경우
     */
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
