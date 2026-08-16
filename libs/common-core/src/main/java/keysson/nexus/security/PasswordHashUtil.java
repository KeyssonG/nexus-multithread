package keysson.nexus.security;

public class PasswordHashUtil {

    private static final String ARGON2_PREFIX = "{argon2}";

    private PasswordHashUtil() {
    }

    public static boolean needsUpgrade(String encodedPassword) {
        return encodedPassword != null && !encodedPassword.startsWith(ARGON2_PREFIX);
    }
}
