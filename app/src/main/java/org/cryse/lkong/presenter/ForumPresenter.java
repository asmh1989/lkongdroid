package org.cryse.lkong.presenter;

import org.cryse.lkong.logic.LKongForumService;
import org.cryse.lkong.model.ThreadModel;
import org.cryse.lkong.utils.SubscriptionUtils;
import org.cryse.lkong.view.ForumView;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ForumPresenter implements BasePresenter<ForumView> {
    public static final String LOG_TAG = ForumPresenter.class.getName();
    LKongForumService mLKongForumService;
    ForumView mView;
    Subscription mLoadThreadListSubscription;
    Subscription mCheckPinnedSubscription;

    @Inject
    public ForumPresenter(LKongForumService forumService) {
        this.mLKongForumService = forumService;
    }

    public void loadThreadList(long fid, int listType, boolean loadingMore) {
        loadThreadList(fid, -1, listType, loadingMore);
    }

    public void loadThreadList(long fid, long start, int listType, boolean loadingMore) {
        SubscriptionUtils.checkAndUnsubscribe(mLoadThreadListSubscription);
        setLoadingStatus(loadingMore, true);
        mLoadThreadListSubscription = mLKongForumService.getForumThread(fid, start, listType)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            mView.showThreadList(result, loadingMore);
                        },
                        error -> {
                            Timber.e(error, "ForumPresenter::loadThreadList() onError().", LOG_TAG);
                            setLoadingStatus(loadingMore, false);
                        },
                        () -> {
                            setLoadingStatus(loadingMore, false);
                        }
                );
    }

    public void unpinForum(long uid, long fid) {
        SubscriptionUtils.checkAndUnsubscribe(mCheckPinnedSubscription);
        mCheckPinnedSubscription = mLKongForumService.unpinForum(uid, fid)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            mView.checkPinnedStatusDone(false);
                        },
                        error -> {
                            Timber.e(error, "ForumPresenter::isForumPinned() onError().", LOG_TAG);
                        },
                        () -> {
                        }
                );
    }

    public void pinForum(long uid, long fid, String forumName, String forumIcon) {
        SubscriptionUtils.checkAndUnsubscribe(mCheckPinnedSubscription);
        mCheckPinnedSubscription = mLKongForumService.pinForum(uid, fid, forumName, forumIcon)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            mView.checkPinnedStatusDone(result);
                        },
                        error -> {
                            Timber.e(error, "ForumPresenter::isForumPinned() onError().", LOG_TAG);
                        },
                        () -> {
                        }
                );
    }

    public void isForumPinned(long uid, long fid) {
        SubscriptionUtils.checkAndUnsubscribe(mCheckPinnedSubscription);
        mCheckPinnedSubscription = mLKongForumService.isForumPinned(uid, fid)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            mView.checkPinnedStatusDone(result);
                        },
                        error -> {
                            Timber.e(error, "ForumPresenter::isForumPinned() onError().", LOG_TAG);
                        },
                        () -> {
                        }
                );
    }

    @Override
    public void bindView(ForumView view) {
        this.mView = view;
    }

    @Override
    public void unbindView() {
        this.mView = new EmptyTheadListView();
    }

    @Override
    public void destroy() {
        SubscriptionUtils.checkAndUnsubscribe(mLoadThreadListSubscription);
        SubscriptionUtils.checkAndUnsubscribe(mCheckPinnedSubscription);
    }

    private void setLoadingStatus(boolean loadingMore, boolean isLoading) {
        if(loadingMore)
            mView.setLoadingMore(isLoading);
        else
            mView.setLoading(isLoading);
    }

    private class EmptyTheadListView implements ForumView {

        @Override
        public void showThreadList(List<ThreadModel> threadList, boolean isLoadMore) {

        }

        @Override
        public void checkPinnedStatusDone(boolean isPinned) {

        }

        @Override
        public boolean isLoadingMore() {
            return false;
        }

        @Override
        public void setLoadingMore(boolean value) {

        }

        @Override
        public void setLoading(Boolean value) {

        }

        @Override
        public Boolean isLoading() {
            return null;
        }

        @Override
        public void showToast(int text_value, int toastType) {

        }
    }
}