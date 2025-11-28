package model.auth;

import java.util.Objects;
import java.util.Optional;

/** Global authentication context (sufficient for desktop single-user scenarios) */
public final class AuthContext {
    private AuthContext() {}

    private static volatile UserAccount CURRENT;

    /** Call after successful login */
    public static void login(UserAccount account) {
        CURRENT = Objects.requireNonNull(account, "account");
    }

    /** Logout */
    public static void logout() { CURRENT = null; }

    /** Retrieve the current session; consistent with .getProfile() usage */
    public static Optional<UserSession> currentUser() {
        return CURRENT == null ? Optional.empty() : Optional.of(new UserSession(CURRENT));
    }

    /** Development/demo mode: if empty, inject a default user and return the session */
    public static Optional<UserSession> ensureDemoUser() {
        if (CURRENT == null) {
            model.group.UserProfile profile = new model.group.UserProfile("u-demo", "Demo User");
            // Demo password placeholder
            UserAccount acc = new UserAccount("acc-demo", "demo", "demo@example.com",
                    "plain:demo", "nosalt", profile);
            login(acc);
        }
        return Optional.of(new UserSession(CURRENT));
    }

    /** Wrapper to preserve .getProfile() and still access the account entity */
    public static final class UserSession {
        private final UserAccount account;
        public UserSession(UserAccount account) { this.account = account; }
        public UserAccount getAccount() { return account; }
        public model.group.UserProfile getProfile() { return account.getProfile(); }
    }
}

