package org.cryse.lkong.modules.followedforums;

import org.cryse.lkong.logic.LKongForumService;
import org.cryse.lkong.account.LKAuthObject;
import org.cryse.lkong.model.ForumModel;
import org.cryse.lkong.modules.simplecollection.SimpleCollectionPresenter;
import org.cryse.lkong.utils.SubscriptionUtils;
import org.cryse.lkong.modules.simplecollection.SimpleCollectionView;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class FollowedForumsPresenter extends SimpleCollectionPresenter<ForumModel, SimpleCollectionView<ForumModel>> {
    private static final String LOG_TAG = FollowedForumsPresenter.class.getName();

    @Inject
    public FollowedForumsPresenter(LKongForumService forumService) {
        super(forumService);
    }

    @Override
    protected void loadData(LKAuthObject authObject, long start, boolean isLoadingMore, Object... extraArgs) {

    }

    public void loadFollowedForums(LKAuthObject authObject) {
        SubscriptionUtils.checkAndUnsubscribe(mLoadDataSubscription);
        mLoadDataSubscription = mLKongForumService.loadUserFollowedForums(authObject)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            Timber.d("FollowedForumsPresenter::loadData() onNext().", LOG_TAG);
                            if (mView != null) {
                                mView.showSimpleData(result, false);
                            }
                        },
                        error -> {
                            Timber.e(error, "FollowedForumsPresenter::loadData() onError().", LOG_TAG);
                        },
                        () -> {
                            Timber.d("FollowedForumsPresenter::loadData() onComplete().", LOG_TAG);
                        }
                );
    }
}

