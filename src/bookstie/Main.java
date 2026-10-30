package bookstie;

import bookstie.ui.LoginFrame;
import bookstie.ui.StarRatingPanel;
import bookstie.util.AppPaths;
import bookstie.repository.*;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.io.IOException;

import javax.swing.*;

/**
 * Swing 이벤트 디스패치 스레드에서 로그인 창을 여는 Bookstie 실행 진입점.
 */
public class Main {

    /**
     * 이벤트 디스패치 스레드에 로그인 창 생성과 표시를 예약함.
     *
     * @param args 명령행 인수; --check-installation이면 GUI 창 없이 데이터 조회 및 폰트·아이콘 로딩 점검
     */
    public static void main(String[] args) {
        boolean check = args.length == 1 && "--check-installation".equals(args[0]);
        try {
            AppPaths.dataDirectory();
            if (check) {
                checkInstallation();
                return;
            }
        } catch (Exception e) {
            System.err.println("Bookstie 시작 실패: " + e.getMessage());
            if (!check && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Bookstie 실행 경로 오류", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
            return;
        }

        SwingUtilities.invokeLater(
                () -> {

                    LoginFrame loginFrame =
                            new LoginFrame();

                    loginFrame.setVisible(true);
                }
        );
    }
    private static void checkInstallation() throws Exception {
        new UserRepository().findAll();
        new AdminRepository().findAll();
        new BookRepository().findAll();
        new LoanRepository().findAll();
        try (InputStream input = Main.class.getResourceAsStream("/resources/fonts/EF_jejudoldam(TTF).ttf")) {
            if (input == null) throw new IOException("클래스패스 폰트 리소스가 없습니다.");
            Font.createFont(Font.TRUETYPE_FONT, input);
        }
        SwingUtilities.invokeAndWait(() -> new StarRatingPanel(null));
        System.out.println("PASS installation: data=" + AppPaths.dataDirectory() + "; UTF-8 data, font, icons loaded");
    }
}