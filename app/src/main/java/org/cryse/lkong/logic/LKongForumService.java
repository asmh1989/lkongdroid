package org.cryse.lkong.logic;

import android.text.format.DateUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.cryse.lkong.data.LKongDatabase;
import org.cryse.lkong.data.model.PinnedForumEntity;
import org.cryse.lkong.data.model.UserAccountEntity;
import org.cryse.lkong.event.FavoritesChangedEvent;
import org.cryse.lkong.event.RxEventBus;
import org.cryse.lkong.logic.restservice.LKongRestService;
import org.cryse.lkong.model.DataItemLocationModel;
import org.cryse.lkong.model.ForumModel;
import org.cryse.lkong.model.NewPostResult;
import org.cryse.lkong.model.NewThreadResult;
import org.cryse.lkong.model.NoticeModel;
import org.cryse.lkong.model.NoticeRateModel;
import org.cryse.lkong.model.PostModel;
import org.cryse.lkong.model.PunchResult;
import org.cryse.lkong.model.SearchDataSet;
import org.cryse.lkong.model.SignInResult;
import org.cryse.lkong.model.ThreadModel;
import org.cryse.lkong.model.ThreadInfoModel;
import org.cryse.lkong.model.TimelineModel;
import org.cryse.lkong.model.UserInfoModel;
import org.cryse.lkong.utils.ContentProcessor;
import org.cryse.lkong.utils.LKAuthObject;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import timber.log.Timber;

public class LKongForumService {
    public static final String LOG_TAG = LKongForumService.class.getName();
    LKongRestService mLKongRestService;
    LKongDatabase mLKongDatabase;
    RxEventBus mEventBus;

    @Inject
    @Singleton
    public LKongForumService(LKongRestService lKongRestService, LKongDatabase lKongDatabase, RxEventBus rxEventBus) {
        this.mLKongRestService = lKongRestService;
        this.mLKongDatabase = lKongDatabase;
        this.mEventBus = rxEventBus;
        try {
            this.mLKongDatabase.initialize();
        } catch (Exception ex) {
            Timber.e(ex, "LKongForumService::LKongForumService() initialize database failed.", LOG_TAG);
            throw new IllegalStateException("Database initialize failed, app may work unproperly.");
        }
    }

    public Observable<SignInResult> signIn(String email, String password) {
        return Observable.create(subscriber -> {
            try {
                SignInResult signInResult = mLKongRestService.signIn(email, password);

                if(signInResult != null && signInResult.isSuccess()) {
                    UserAccountEntity userAccountEntity = new UserAccountEntity(
                            signInResult.getMe().getUid(),
                            email,
                            signInResult.getMe().getUserName(),
                            signInResult.getMe().getUserIcon(),
                            signInResult.getAuthCookie(),
                            signInResult.getDzsbheyCookie(),
                            signInResult.getIdentityCookie()
                    );
                    if (mLKongDatabase != null && mLKongDatabase.isOpen()) {
                        if (mLKongDatabase.isUserAccountExist(userAccountEntity.getUserId())) {
                            mLKongDatabase.updateUserAccount(userAccountEntity);
                        } else {
                            mLKongDatabase.addUserAccount(userAccountEntity);
                        }
                    }
                }
                subscriber.onNext(signInResult);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
                Timber.i("SIGNIN_FAILED_WITH_EXCEPTION", LOG_TAG, e.getMessage());
            }
        });
    }

    public Observable<UserAccountEntity> getUserAccount(long uid) {
        return Observable.create(subscriber -> {
            try {

                subscriber.onNext(mLKongDatabase.getUserAccount(uid));
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<UserInfoModel> getUserInfo(LKAuthObject authObject, long uid, boolean isSelf) {
        return Observable.create(subscriber -> {
            try {
                UserInfoModel userInfoModel = mLKongRestService.getUserInfo(authObject, uid);
                subscriber.onNext(userInfoModel);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    /*public Observable<UserAccountEntity> updateUserAccount(long uid, LKAuthObject authObject) {
        return Observable.create(subscriber -> {
            try {
                UserInfoModel userInfoModel = mLKongRestService.getUserInfo(authObject);
                UserAccountEntity userAccountEntity = mLKongDatabase.getUserAccount(uid);
                userAccountEntity.setUserName(userInfoModel.getUserName());
                userAccountEntity.setUserAvatar(userInfoModel.getUserIcon());
                mLKongDatabase.updateUserAccount(userAccountEntity);
                subscriber.onNext(userAccountEntity);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }*/

    public Observable<List<UserAccountEntity>> getAllUserAccounts() {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(mLKongDatabase.getAllUserAccounts());
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<List<ForumModel>> getForumList(boolean updateFromWeb) {
        return Observable.create(subscriber -> {
            try {
                if (mLKongDatabase.isCachedForumList()) {
                    subscriber.onNext(mLKongDatabase.getCachedForumList());
                }
                if(updateFromWeb || !mLKongDatabase.isCachedForumList()) {
                    List<ForumModel> forumModelList = mLKongRestService.getForumList();
                    if (forumModelList != null)
                        mLKongDatabase.cacheForumList(forumModelList);
                    subscriber.onNext(forumModelList);
                }
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<List<ThreadModel>> getForumThread(long fid, long start, int listType) {
        return Observable.create(subscriber -> {
            try {
                List<ThreadModel> forumModelList = mLKongRestService.getForumThreadList(fid, start, listType);
                subscriber.onNext(forumModelList);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<ThreadInfoModel> getThreadInfo(LKAuthObject authObject, long tid) {
        return Observable.create(subscriber -> {
            try {
                ThreadInfoModel threadModel = mLKongRestService.getThreadInfo(authObject, tid);
                subscriber.onNext(threadModel);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<PostModel>> getPostList(LKAuthObject authObject, long tid, int page) {
        return Observable.create(subscriber -> {
           try {
               List<PostModel> postList = mLKongRestService.getThreadPostList(authObject, tid, page);
               subscriber.onNext(postList);
               subscriber.onCompleted();
           } catch (Exception ex) {
               subscriber.onError(ex);
           }
        });
    }


    public Observable<NewPostResult> newPostReply(LKAuthObject authObject, long tid, Long pid, String content) {
        return Observable.create(subscriber -> {
            try {
                String unescapedContent = StringEscapeUtils.unescapeHtml4(content);
                ContentProcessor contentProcessor = new ContentProcessor(unescapedContent);
                contentProcessor.setUploadImageCallback(path -> {
                    String uploadUrl = "";
                    try {
                        Timber.d("setUploadImageCallback start", LOG_TAG);
                        uploadUrl = mLKongRestService.uploadImageToLKong(authObject, path);
                        Timber.d(String.format("uploadImageToLKong result %s", uploadUrl), LOG_TAG);
                    } catch(Exception ex) {
                        Timber.e(ex, "uploadImageToLKong failed", LOG_TAG);
                    } finally {
                        return uploadUrl;
                    }
                });
                contentProcessor.run();
                String replaceResult = contentProcessor.getResultContent();

                Timber.d(replaceResult, LOG_TAG);
                NewPostResult result = mLKongRestService.newPostReply(authObject, tid, pid, replaceResult);
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<NewThreadResult> newPostThread(LKAuthObject authObject, String title, long fid, String content, boolean follow) {
        return Observable.create(subscriber -> {
            try {
                String unescapedContent = StringEscapeUtils.unescapeHtml4(content);
                ContentProcessor contentProcessor = new ContentProcessor(unescapedContent);
                contentProcessor.setUploadImageCallback(path -> {
                    String uploadUrl = "";
                    try {
                        Timber.d("setUploadImageCallback start", LOG_TAG);
                        uploadUrl = mLKongRestService.uploadImageToLKong(authObject, path);
                        Timber.d(String.format("uploadImageToLKong result %s", uploadUrl), LOG_TAG);
                    } catch(Exception ex) {
                        Timber.e(ex, "uploadImageToLKong failed", LOG_TAG);
                    } finally {
                        return uploadUrl;
                    }
                });
                contentProcessor.run();
                String replaceResult = contentProcessor.getResultContent();

                Timber.d(replaceResult, LOG_TAG);
                NewThreadResult result = mLKongRestService.newPostThread(authObject, title, fid, replaceResult, follow);
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<ThreadModel>> getFavorite(LKAuthObject authObject, long start) {
        return Observable.create(subscriber -> {
            try {
                List<ThreadModel> forumModelList = mLKongRestService.getFavorites(authObject, start);
                subscriber.onNext(forumModelList);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<Boolean> addOrRemoveFavorite(LKAuthObject authObject, long tid, boolean remove) {
        return Observable.create(subscriber -> {
            try {
                Boolean result = mLKongRestService.addOrRemoveFavorite(authObject, tid, remove);
                mEventBus.sendEvent(new FavoritesChangedEvent());
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<TimelineModel>> getTimeline(LKAuthObject authObject, long start, int listType) {
        return Observable.create(subscriber -> {
            try {
                List<TimelineModel> result = mLKongRestService.getTimeline(authObject, start, listType);
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<NoticeModel>> getNotice(LKAuthObject authObject, long start) {
        return Observable.create(subscriber -> {
            try {
                List<NoticeModel> result = mLKongRestService.getNotice(authObject, start);
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<NoticeRateModel>> getNoticeRateLog(LKAuthObject authObject, long start) {
        return Observable.create(subscriber -> {
            try {
                List<NoticeRateModel> result = mLKongRestService.getNoticeRateLog(authObject, start);
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<DataItemLocationModel> getPostIdLocation(LKAuthObject authObject, long postId) {
        return Observable.create(subscriber -> {
            try {
                DataItemLocationModel locationModel = mLKongRestService.getDataItemLocation(authObject, String.format("post_%d", postId));
                subscriber.onNext(locationModel);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<PostModel.PostRate> ratePost(LKAuthObject authObject, long postId, int score, String reaseon) {
        return Observable.create(subscriber -> {
            try {
                PostModel.PostRate postRate = mLKongRestService.ratePost(authObject, postId, score, reaseon);
                subscriber.onNext(postRate);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<SearchDataSet> search(LKAuthObject authObject, long start, String queryString) {
        return Observable.create(subscriber -> {
            try {
                SearchDataSet dataSet = mLKongRestService.searchLKong(authObject, start, queryString);
                subscriber.onNext(dataSet);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<TimelineModel>> getUserAll(LKAuthObject authObject, long start, long uid) {
        return Observable.create(subscriber -> {
            try {
                List<TimelineModel> dataSet = mLKongRestService.getUserAll(authObject, start, uid);
                subscriber.onNext(dataSet);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<ThreadModel>> getUserThreads(LKAuthObject authObject, long start, long uid, boolean isDigest) {
        return Observable.create(subscriber -> {
            try {
                List<ThreadModel> dataSet = mLKongRestService.getUserThreads(authObject, start, uid, isDigest);
                subscriber.onNext(dataSet);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<PunchResult> punch(LKAuthObject authObject) {
        return Observable.create(subscriber -> {
            try {
                    PunchResult result = null;
                    result = mLKongDatabase.getCachePunchResult(authObject.getUserId());
                    if(result != null && result.getPunchTime() != null && DateUtils.isToday(result.getPunchTime().getTime())) {
                        subscriber.onNext(result);
                        return;
                    } else {
                        result = mLKongRestService.punch(authObject);
                        if(result != null)
                            mLKongDatabase.cachePunchResult(result);
                    }
                    subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<PunchResult> punch(List<LKAuthObject> authObjectList) {
        return Observable.create(subscriber -> {
            try {
                for(LKAuthObject authObject: authObjectList) {
                    PunchResult result = null;
                    result = mLKongDatabase.getCachePunchResult(authObject.getUserId());
                    if(result != null && result.getPunchTime() != null && DateUtils.isToday(result.getPunchTime().getTime())) {
                        subscriber.onNext(result);
                        return;
                    } else {
                        result = mLKongRestService.punch(authObject);
                        if(result != null)
                            mLKongDatabase.cachePunchResult(result);
                    }
                    subscriber.onNext(result);
                }
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<Boolean> pinForum(long uid, long fid, String forumName, String forumIcon) {
        return Observable.create(subscriber -> {
            try {
                mLKongDatabase.pinForum(new PinnedForumEntity(
                        fid,
                        uid,
                        forumName,
                        forumIcon,
                        new Date().getTime()
                ));
                subscriber.onNext(mLKongDatabase.isForumPinned(uid, fid));
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<Void> unpinForum(long uid, long fid) {
        return Observable.create(subscriber -> {
            try {
                mLKongDatabase.removePinnedForum(uid, fid);
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<Boolean> isForumPinned(long uid, long fid) {
        return Observable.create(subscriber -> {
            try {
                boolean isForumPinned = mLKongDatabase.isForumPinned(uid, fid);
                subscriber.onNext(isForumPinned);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }

    public Observable<List<PinnedForumEntity>> loadUserPinnedForums(long uid) {
        return Observable.create(subscriber -> {
            try {
                List<PinnedForumEntity> result = mLKongDatabase.loadAllForUser(uid);
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception ex) {
                subscriber.onError(ex);
            }
        });
    }
}
