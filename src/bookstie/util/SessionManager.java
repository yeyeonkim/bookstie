package bookstie.util;
//현재 로그인한 사용자 기억

import bookstie.model.Admin;
import bookstie.model.User;

/**
 * 현재 로그인한 회원 또는 관리자를 정적 필드로 보관함. 한 종류로 로그인하면 다른 종류의 세션을 지우며 로그아웃하면 둘 다 지움.
 */
public class SessionManager {

    private static User currentUser;
    private static Admin currentAdmin;

    private SessionManager() {
    }

    /**
     * 현재 회원을 설정하고 관리자 세션을 해제함.
     *
     * @param user 로그인할 회원; null이면 회원 세션도 해제
     */
    public static void loginUser(User user) {
        currentUser = user;
        currentAdmin = null;
    }

    /**
     * 현재 관리자를 설정하고 회원 세션을 해제함.
     *
     * @param admin 로그인할 관리자; null이면 관리자 세션도 해제
     */
    public static void loginAdmin(Admin admin) {
        currentAdmin = admin;
        currentUser = null;
    }

    /**
     * 현재 회원 세션을 반환함.
     *
     * @return 현재 회원; 회원 세션이 없으면 null
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * 현재 관리자 세션을 반환함.
     *
     * @return 현재 관리자; 관리자 세션이 없으면 null
     */
    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * 회원 세션 존재 여부를 확인함.
     *
     * @return 현재 회원이 있으면 true, 없으면 false
     */
    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    /**
     * 관리자 세션 존재 여부를 확인함.
     *
     * @return 현재 관리자가 있으면 true, 없으면 false
     */
    public static boolean isAdminLoggedIn() {
        return currentAdmin != null;
    }

    /**
     * 회원과 관리자 세션을 모두 null로 해제함.
     */
    public static void logout() {
        currentUser = null;
        currentAdmin = null;
    }
}
