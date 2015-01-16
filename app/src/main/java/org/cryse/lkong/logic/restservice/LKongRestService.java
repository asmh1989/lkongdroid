package org.cryse.lkong.logic.restservice;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.cryse.lkong.logic.ThreadListType;
import org.cryse.lkong.logic.restservice.exception.IdentityExpiredException;
import org.cryse.lkong.logic.restservice.exception.NeedIdentityException;
import org.cryse.lkong.logic.restservice.exception.NeedSignInException;
import org.cryse.lkong.logic.restservice.exception.SignInExpiredException;
import org.cryse.lkong.logic.restservice.model.LKForumInfo;
import org.cryse.lkong.logic.restservice.model.LKForumListItem;
import org.cryse.lkong.logic.restservice.model.LKForumNameList;
import org.cryse.lkong.logic.restservice.model.LKForumThreadList;
import org.cryse.lkong.logic.restservice.model.LKPostList;
import org.cryse.lkong.logic.restservice.model.LKThreadInfo;
import org.cryse.lkong.logic.restservice.model.LKUserInfo;
import org.cryse.lkong.model.ForumModel;
import org.cryse.lkong.model.PostModel;
import org.cryse.lkong.model.SignInResult;
import org.cryse.lkong.model.ForumThreadModel;
import org.cryse.lkong.model.ThreadInfoModel;
import org.cryse.lkong.model.UserInfoModel;
import org.cryse.lkong.model.converter.ModelConverter;
import org.cryse.lkong.utils.CookieUtils;
import org.cryse.lkong.utils.LKAuthObject;
import org.cryse.lkong.utils.SerializableHttpCookie;
import org.cryse.utils.MiniIOUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

import timber.log.Timber;

public class LKongRestService {
    public static final String LOG_TAG = LKongRestService.class.getName();
    public static final String LKONG_DOMAIN_URL = "http://lkong.cn";
    public static final String LKONG_INDEX_URL = LKONG_DOMAIN_URL + "/index.php";
    OkHttpClient okHttpClient;
    CookieManager cookieManager;
    Gson gson;
    @Inject
    public LKongRestService(Context context) {
        this.okHttpClient = new OkHttpClient();
        this.cookieManager = new CookieManager(
        );
        this.okHttpClient.setCookieHandler(cookieManager);

        this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    public SignInResult signIn(String email, String password) throws Exception {
        RequestBody formBody = new FormEncodingBuilder()
                .add("action", "login")
                .add("email", email)
                .add("password", password)
                .add("rememberme", "on")
                .build();
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip")
                .url(LKONG_INDEX_URL + "?mod=login")
                .post(formBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String responseBody = getStringFromGzipResponse(response);
        JSONObject jsonObject = new JSONObject(responseBody);
        boolean success = jsonObject.getBoolean("success");
        UserInfoModel me = getUserConfigInfo();
        SignInResult signInResult = new SignInResult();

        signInResult.setSuccess(success);
        signInResult.setMe(me);
        readCookies(signInResult);
        cookieManager.getCookieStore().removeAll();

        return signInResult;
    }

    private UserInfoModel getUserConfigInfo() throws Exception {
        // when call this method, the cookie manager should at least contain auth and dzsbhey cookie
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip")
                .url(LKONG_INDEX_URL + "?mod=ajax&action=userconfig")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String responseString = getStringFromGzipResponse(response);
        LKUserInfo lkUserInfo = gson.fromJson(responseString, LKUserInfo.class);
        UserInfoModel userInfoModel = ModelConverter.toUserInfoModel(lkUserInfo);
        return userInfoModel;
    }

    public UserInfoModel getUserInfo(LKAuthObject authObject) throws Exception {
        checkSignInStatus(authObject, false);
        cookieManager.getCookieStore().add(authObject.getAuthURI(), authObject.getAuthHttpCookie());
        cookieManager.getCookieStore().add(authObject.getDzsbheyURI(), authObject.getDzsbheyHttpCookie());
        cookieManager.getCookieStore().add(authObject.getIdentityURI(), authObject.getIdentityHttpCookie());

        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip")
                .url(LKONG_INDEX_URL + "?mod=ajax&action=userconfig")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String responseString = getStringFromGzipResponse(response);
        Gson customGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        LKUserInfo lkUserInfo = customGson.fromJson(responseString, LKUserInfo.class);
        UserInfoModel userInfoModel = ModelConverter.toUserInfoModel(lkUserInfo);
        cookieManager.getCookieStore().removeAll();
        return userInfoModel;
    }

    public List<ForumModel> getForumList() throws Exception {
        // checkSignInStatus();
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip")
                .url(LKONG_INDEX_URL + "?mod=ajax&action=forumlist")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException("Unexpected code " + response);
        String responseString = getStringFromGzipResponse(response);
        LKForumNameList lkForumNameList = gson.fromJson(responseString, LKForumNameList.class);

        List<ForumModel> forumModels = new ArrayList<ForumModel>(lkForumNameList.getForumlist().size());
        for(LKForumListItem item : lkForumNameList.getForumlist()) {
            Response itemInfoResponse = null;
            ForumModel forumModel = new ForumModel();
            forumModel.setFid(item.getFid());
            forumModel.setName(item.getName());
            forumModel.setIcon(ModelConverter.fidToForumIconUrl(item.getFid()));

            try {
                Request itemInfoRequest = new Request.Builder()
                        .addHeader("Accept-Encoding", "gzip")
                        .url(LKONG_INDEX_URL + "?mod=ajax&action=forumconfig_" + Long.toString(item.getFid()))
                        .build();

                itemInfoResponse = okHttpClient.newCall(itemInfoRequest).execute();
                if (!response.isSuccessful())
                    throw new IOException("Get forum detail info failed, reason: " + response);
                String itemInfoResponseString = getStringFromGzipResponse(itemInfoResponse);
                LKForumInfo forumInfo = gson.fromJson(itemInfoResponseString, LKForumInfo.class);
                forumModel.setDescription(forumInfo.getDescription());
                forumModel.setBlackboard(forumInfo.getBlackboard());
                forumModel.setFansNum(forumInfo.getFansnum());
                forumModel.setStatus(forumInfo.getStatus());
                forumModel.setSortByDateline(forumInfo.getSortbydateline());
                forumModel.setThreads(Integer.parseInt(forumInfo.getThreads()));
                forumModel.setTodayPosts(Integer.parseInt(forumInfo.getTodayposts()));
            } catch (Exception ex) {
                Timber.e(ex, "Get forum detail info exception.", LOG_TAG);
            } finally {
                forumModels.add(forumModel);
            }
        }
        return forumModels;
    }
    
    public List<ForumThreadModel> getForumThreadList(long fid, long start, int listType) throws Exception {
        String url = String.format(LKONG_INDEX_URL + "?mod=data&sars=forum/%d%s", fid, ThreadListType.typeToRequestParam(listType));
        url = url + (start >= 0 ? "&nexttime=" + Long.toString(start) : "");
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip")
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String responseString = getStringFromGzipResponse(response);
        LKForumThreadList lKThreadList = gson.fromJson(responseString, LKForumThreadList.class);
        Timber.d(String.format("LKongRestService::getForumThreadList() lkThreadList.size() = %d ", lKThreadList.getData().size()), LOG_TAG);
        List<ForumThreadModel> threadList = ModelConverter.toForumThreadModel(lKThreadList);
        Timber.d(String.format("LKongRestService::getForumThreadList() threadList.size() = %d ", threadList.size()), LOG_TAG);
        return threadList;
    }

    public ThreadInfoModel getThreadInfo(long tid) throws Exception {
        String url = String.format(LKONG_INDEX_URL + "?mod=ajax&action=threadconfig_%d", tid);
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip")
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String responseString = getStringFromGzipResponse(response);
        LKThreadInfo lkThreadInfo = gson.fromJson(responseString, LKThreadInfo.class);
        ThreadInfoModel threadInfoModel = ModelConverter.toThreadInfoModel(lkThreadInfo);
        return threadInfoModel;
    }

    public List<PostModel> getThreadPostList(long tid, int page) throws Exception {
        String url = String.format(LKONG_INDEX_URL + "?mod=data&sars=thread/%d/%s", tid, page);
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip")
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String responseString = getStringFromGzipResponse(response);
        LKPostList lkPostList = gson.fromJson(responseString, LKPostList.class);
        Timber.d(String.format("LKongRestService::getForumThreadList() lkThreadList.size() = %d ", lkPostList.getData().size()), LOG_TAG);
        List<PostModel> postList = ModelConverter.toPostModelList(lkPostList);
        Timber.d(String.format("LKongRestService::getForumThreadList() threadList.size() = %d ", postList.size()), LOG_TAG);
        return postList;
    }

    private static String decompress(byte[] bytes) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        GZIPInputStream gis = new GZIPInputStream(byteArrayInputStream);
        String resultString = MiniIOUtils.toString(gis);
        gis.close();
        byteArrayInputStream.close();
        return resultString;
    }

    private String getStringFromGzipResponse(Response response) throws Exception {
        return decompress(response.body().bytes());
    }

    private void checkSignInStatus(LKAuthObject authObject, boolean checkIdentity) {
        if(!authObject.isSignedIn()) {
            if(authObject.hasExpired()) {
                throw new SignInExpiredException();
            } else {
                throw new NeedSignInException();
            }
        }
        if(checkIdentity) {
            if(authObject.hasIdentity()) {
                throw new IdentityExpiredException();
            } else {
                throw new NeedIdentityException();
            }
        }
    }

    private String getLKForumIconUrl(long fid) {
        String fidString = String.format("%1$06d", fid);
        String iconUrl = String.format("http://img.lkong.cn/forumavatar/000/%s/%s/%s_avatar_middle.jpg",
                fidString.substring(0, 2),
                fidString.substring(2, 4),
                fidString.substring(4, 6)
        );
        return iconUrl;
    }

    private void readCookies(SignInResult signInResult) {
        URI authURI = null, dzsbheyURI = null, identityURI = null;
        HttpCookie authHttpCookie = null, dzsbheyHttpCookie = null, identityHttpCookie = null;

        List<URI> uris = cookieManager.getCookieStore().getURIs();
        for(URI uri : uris) {
            List<HttpCookie> httpCookies = cookieManager.getCookieStore().get(uri);
            for(HttpCookie cookie : httpCookies) {
                if(cookie.getName().compareToIgnoreCase("auth") == 0) {
                    // auth cookie pair
                    if(cookie.hasExpired())
                        continue;
                    authURI = uri;
                    authHttpCookie = cookie;
                } else if (cookie.getName().compareToIgnoreCase("dzsbhey") == 0) {
                    // dzsbhey cookie pair
                    if(cookie.hasExpired())
                        continue;;
                    dzsbheyURI = uri;
                    dzsbheyHttpCookie = cookie;
                } else if (cookie.getName().compareToIgnoreCase("identity") == 0) {
                    // identity cookie pair
                    if(cookie.hasExpired())
                        continue;;
                    identityURI = uri;
                    identityHttpCookie = cookie;
                }
            }
        }
        if(authURI != null && authHttpCookie != null &&
                dzsbheyURI != null && dzsbheyHttpCookie != null &&
                identityURI != null && identityHttpCookie != null) {
            signInResult.setAuthCookie(CookieUtils.serializeHttpCookie(authURI, authHttpCookie));
            signInResult.setDzsbheyCookie(CookieUtils.serializeHttpCookie(dzsbheyURI, dzsbheyHttpCookie));
            signInResult.setDzsbheyCookie(CookieUtils.serializeHttpCookie(identityURI, identityHttpCookie));
        } else {
            throw new NeedSignInException("Cookie expired.");
        }
    }
}