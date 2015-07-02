package org.cryse.lkong.presenter;

import org.cryse.lkong.logic.LKongForumService;
import org.cryse.lkong.utils.LKAuthObject;
import org.cryse.lkong.utils.SubscriptionUtils;
import org.cryse.lkong.utils.ToastErrorConstant;
import org.cryse.lkong.utils.snackbar.SimpleSnackbarType;
import org.cryse.lkong.view.PostListView;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PostListPresenter implements BasePresenter<PostListView> {
    public static final String LOG_TAG = PostListPresenter.class.getName();
    LKongForumService mLKongForumService;
    PostListView mView;
    Subscription mLoadPostListSubscription;
    Subscription mLoadThreadInfoSubscription;
    Subscription mAddOrRemoveFavoriteSubscription;
    Subscription mDataItemLocationSubscription;
    Subscription mRatePostSubscription;

    @Inject
    public PostListPresenter(LKongForumService forumService) {
        this.mLKongForumService = forumService;
        this.mView = null;
    }

    public void getPostLocation(LKAuthObject authObject, long pid, boolean loadThreadInfo) {
        SubscriptionUtils.checkAndUnsubscribe(mDataItemLocationSubscription);
        mView.setLoading(true);
        mDataItemLocationSubscription = mLKongForumService.getPostIdLocation(authObject, pid)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (mView != null) {
                                mView.onGetPostLocationComplete(result, loadThreadInfo);
                            }
                        },
                        error -> {
                            Timber.e(error, "PostListPresenter::loadThreadInfo() onError().", LOG_TAG);
                            if (mView != null) {
                                mView.setLoading(false);
                            }
                        },
                        () -> {
                        }
                );
    }

    public void loadThreadInfo(LKAuthObject authObject, long tid) {
        SubscriptionUtils.checkAndUnsubscribe(mLoadThreadInfoSubscription);
        mView.setLoading(true);
        mLoadThreadInfoSubscription = mLKongForumService.getThreadInfo(authObject, tid)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (mView != null) {
                                mView.onLoadThreadInfoComplete(result);
                            }
                        },
                        error -> {
                            Timber.e(error, "PostListPresenter::loadThreadInfo() onError().", LOG_TAG);
                            if (mView != null) {
                                mView.setLoading(false);
                            }
                        },
                        () -> {
                        }
                );
    }

    public void loadPostList(LKAuthObject authObject, long tid, int page, boolean refreshPosition, int showMode) {
        SubscriptionUtils.checkAndUnsubscribe(mLoadPostListSubscription);
        mView.setLoading(true);
        mLoadPostListSubscription = mLKongForumService.getPostList(authObject, tid, page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (mView != null) {
                                mView.showPostList(page, result, refreshPosition, showMode);
                            }
                        },
                        error -> {
                            Timber.e(error, "PostListPresenter::loadPostList() onError().", LOG_TAG);
                            {
                                mView.setLoading(false);
                            }
                        },
                        () -> {
                            if (mView != null) {
                                mView.setLoading(false);
                            }
                        }
                );
    }

    public void addOrRemoveFavorite(LKAuthObject authObject, long tid, boolean remove) {
        SubscriptionUtils.checkAndUnsubscribe(mAddOrRemoveFavoriteSubscription);
        mAddOrRemoveFavoriteSubscription = mLKongForumService.addOrRemoveFavorite(authObject, tid, remove)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (mView != null) {
                                mView.onAddOrRemoveFavoriteComplete(result);
                            }
                        },
                        error -> {
                            Timber.e(error, "PostListPresenter::addOrRemoveFavorite() onError().", LOG_TAG);
                        },
                        () -> {
                        }
                );
    }

    public void ratePost(LKAuthObject authObject, long postId, int score, String reaseon) {
        SubscriptionUtils.checkAndUnsubscribe(mRatePostSubscription);
        mRatePostSubscription = mLKongForumService.ratePost(authObject, postId, score, reaseon)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (mView != null) {
                                mView.onRatePostComplete(result);
                            }
                        },
                        error -> {
                            if (mView != null) {
                                mView.showSnackbar(
                                        ToastErrorConstant.TOAST_FAILURE_RATE_POST,
                                        SimpleSnackbarType.ERROR,
                                        SimpleSnackbarType.LENGTH_SHORT
                                );
                            }
                            Timber.e(error, "PostListPresenter::ratePost() onError().", LOG_TAG);
                        },
                        () -> {
                        }
                );
    }

    @Override
    public void bindView(PostListView view) {
        this.mView = view;
    }

    @Override
    public void unbindView() {
        this.mView = null;
    }

    @Override
    public void destroy() {
        SubscriptionUtils.checkAndUnsubscribe(mLoadPostListSubscription);
        SubscriptionUtils.checkAndUnsubscribe(mLoadThreadInfoSubscription);
        SubscriptionUtils.checkAndUnsubscribe(mAddOrRemoveFavoriteSubscription);
        SubscriptionUtils.checkAndUnsubscribe(mDataItemLocationSubscription);
        SubscriptionUtils.checkAndUnsubscribe(mRatePostSubscription);
    }
}
