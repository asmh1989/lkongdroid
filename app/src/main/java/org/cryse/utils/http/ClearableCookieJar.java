package org.cryse.utils.http;

import org.cryse.utils.http.cookie.CookieStore;

import okhttp3.CookieJar;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public interface ClearableCookieJar extends CookieJar {
    CookieStore getCookieStore();
    void clear();
}
