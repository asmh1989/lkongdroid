package org.cryse.utils.http.cookie;

import okhttp3.Cookie;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class CookieUtils {
    public static boolean hasExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }
}
