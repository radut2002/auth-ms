package auth.ms.response_utils;

public class RefreshTokenCookie {

    public static final String NAME = "r_token";
    private static final String PATH = "/";
    private static final String TEMPLATE = "%s=%s;Path=%s;Max-Age=%d;Domain=.localhost;HttpOnly;SameSite=Strict";

    public final String value;

    public RefreshTokenCookie(String refreshToken, long maxAge) {
        value = String.format(TEMPLATE, NAME, refreshToken, PATH, maxAge);
    }

    @Override
    public String toString() {
        return value;
    }
}
