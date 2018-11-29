package org.cryse.lkong.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class GsonUtils {
    public static Gson getGson() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }
}
