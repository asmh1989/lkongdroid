package org.cryse.lkong.presenter;

import org.cryse.lkong.logic.LKongForumService;
import org.cryse.lkong.logic.restservice.LKongRestService;
import org.cryse.lkong.logic.restservice.model.UserInfo;
import org.cryse.lkong.utils.SubscriptionUtils;
import org.cryse.lkong.utils.ToastErrorConstant;
import org.cryse.lkong.utils.ToastSupport;
import org.cryse.lkong.view.UserInfoView;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class UserInfoPresenter implements BasePresenter<UserInfoView> {
    public static final String LOG_TAG = UserInfoPresenter.class.getName();

    LKongForumService lKongForumService;

    Subscription mGetUserInfoSubscription;

    UserInfoView mView;

    @Inject
    public UserInfoPresenter(LKongForumService lKongForumService) {
        this.lKongForumService = lKongForumService;
        this.mView = new EmptyUserInfoView();
    }

    public void getUserInfo() {
        SubscriptionUtils.checkAndUnsubscribe(mGetUserInfoSubscription);
        mGetUserInfoSubscription = lKongForumService.getUserConfigInfo()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            mView.showUserInfo(result);
                        },
                        error -> {
                            mView.showUserInfo(null);
                            mView.showToast(ToastErrorConstant.TOAST_FAILURE_USER_INFO, ToastSupport.TOAST_ALERT);
                            Timber.d(error, "getUserInfo() failed",  LOG_TAG);
                        },
                        () -> {
                            // getUserInfo finished.
                        });
    }

    public boolean isSignedIn() {
        if(lKongForumService.isSignedIn() == LKongRestService.STATUS_SIGNEDIN)
            return true;
        return false;
    }

    @Override
    public void bindView(UserInfoView view) {
        this.mView = view;
    }

    @Override
    public void unbindView() {
        this.mView = new EmptyUserInfoView();
    }

    @Override
    public void destroy() {
        SubscriptionUtils.checkAndUnsubscribe(mGetUserInfoSubscription);
    }

    private class EmptyUserInfoView implements UserInfoView {

        @Override
        public void showUserInfo(UserInfo userConfigInfo) {

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
