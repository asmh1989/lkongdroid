package org.cryse.lkong.modules.privatemessage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.handmark.pulltorefresh.library.PullToRefreshBase;

import org.cryse.lkong.R;
import org.cryse.lkong.application.LKongApplication;
import org.cryse.lkong.account.UserAccountManager;
import org.cryse.lkong.event.AbstractEvent;
import org.cryse.lkong.logic.RequestPointerType;
import org.cryse.lkong.model.PrivateMessageModel;
import org.cryse.lkong.model.SendNewPrivateMessageResult;
import org.cryse.lkong.ui.adapter.PrivateMessagesAdapter;
import org.cryse.lkong.ui.common.AbstractFragment;
import org.cryse.lkong.ui.navigation.AppNavigation;
import org.cryse.lkong.utils.AnalyticsUtils;
import org.cryse.lkong.utils.DataContract;
import org.cryse.lkong.application.PreferenceConstant;
import org.cryse.utils.preference.Prefs;
import org.cryse.utils.preference.StringPrefs;
import org.cryse.widget.recyclerview.PtrRecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class PrivateChatFragment extends AbstractFragment implements PrivateChatView {
    public static final String LOG_TAG = PrivateChatFragment.class.getSimpleName();
    AppNavigation mNavigation = new AppNavigation();
    @Inject
    PrivateMessagePresenter mPresenter;
    @Inject
    UserAccountManager mUserAccountManager;
    StringPrefs mAvatarDownloadPolicy;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fragment_private_chat_ptrrecyclerview_messages)
    PtrRecyclerView mRecyclerView;
    @BindView(R.id.fragment_private_chat_edittext_message)
    EditText mMessageEditText;
    @BindView(R.id.fragment_private_chat_button_send)
    ImageButton mSendButton;

    PrivateMessagesAdapter mCollectionAdapter;
    private List<PrivateMessageModel> mItemList = new ArrayList<>();

    private long mTargetUserId;
    private String mTargetUserName;
    private boolean isNoMore = false;
    private boolean isLoading = false;
    private boolean isLoadingMore = false;
    private long mCurrentTimeSortKey = -1;
    private long mNextTimeSortKey = -1;
    private MaterialDialog mProgressDialog;

    public static PrivateChatFragment newInstance(Bundle args) {
        PrivateChatFragment fragment = new PrivateChatFragment();
        if(args != null) {
          fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        injectThis();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAvatarDownloadPolicy = Prefs.getStringPrefs(PreferenceConstant.SHARED_PREFERENCE_AVATAR_DOWNLOAD_POLICY,
                PreferenceConstant.SHARED_PREFERENCE_AVATAR_DOWNLOAD_POLICY_VALUE);
        Bundle args = getArguments();
        mTargetUserId = args.getLong(DataContract.BUNDLE_TARGET_USER_ID);
        mTargetUserName = args.getString(DataContract.BUNDLE_TARGET_USER_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_private_chat, container, false);
        ButterKnife.bind(this, contentView);
        setUpRecyclerView();
        getThemedActivity().setSupportActionBar(mToolbar);
        final ActionBar actionBar = getThemedActivity().getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            getThemedActivity().setTitle(mTargetUserName);
        }
        mToolbar.setBackgroundColor(getPrimaryColor());
        mSendButton.setOnClickListener(view -> {
            sendNewPrivateMessage();
        });
        return contentView;
    }

    private void setUpRecyclerView() {
        mCollectionAdapter = new PrivateMessagesAdapter(this, mATEKey, mItemList, Integer.valueOf(mAvatarDownloadPolicy.get()));
        mRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.getRefreshableView().setLayoutManager(linearLayoutManager);
        mRecyclerView.getRefreshableView().setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.getRefreshableView().setAdapter(mCollectionAdapter);
        mRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> recyclerViewPullToRefreshBase) {
                mPresenter.loadPrivateMessages(mUserAccountManager.getAuthObject(), mTargetUserId, mNextTimeSortKey, RequestPointerType.TYPE_NEXT, true);
                recyclerViewPullToRefreshBase.onRefreshComplete();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> recyclerViewPullToRefreshBase) {
                mPresenter.loadPrivateMessages(mUserAccountManager.getAuthObject(), mTargetUserId, mCurrentTimeSortKey, RequestPointerType.TYPE_CURRENT, true);
                recyclerViewPullToRefreshBase.onRefreshComplete();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.loadPrivateMessages(mUserAccountManager.getAuthObject(), mTargetUserId, 0, RequestPointerType.TYPE_NEXT, false);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_private_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.action_user_profile:
                mNavigation.openActivityForUserProfile(getActivity(), null, mTargetUserId);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.bindView(this);
        // loadInitialData();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.unbindView();
        if(mProgressDialog != null && mProgressDialog.isShowing()) {
          mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    protected void injectThis() {
        LKongApplication.get(getActivity()).lKongPresenterComponent().inject(this);
    }

    @Override
    protected void analyticsTrackEnter() {
        AnalyticsUtils.trackFragmentEnter(this, LOG_TAG);
    }

    @Override
    protected void analyticsTrackExit() {
        AnalyticsUtils.trackFragmentExit(this, LOG_TAG);
    }

    @Override
    protected void onEvent(AbstractEvent event) {
        super.onEvent(event);
    }

    @Override
    public void onLoadMessagesComplete(List<PrivateMessageModel> items, int pointerType, boolean isLoadingMore) {
        if(isLoadingMore) {
            if (items.size() == 0) {
              isNoMore = true;
            }
            if(pointerType == RequestPointerType.TYPE_NEXT) {
              mCollectionAdapter.addAll(0, items);
            } else if(pointerType == RequestPointerType.TYPE_CURRENT) {
                mCollectionAdapter.addAll(items);
                mRecyclerView.getRefreshableView().smoothScrollToPosition(mCollectionAdapter.getItemCount() - 1);
            }
        } else {
            isNoMore = false;
            mCollectionAdapter.replaceWith(items);
            int newPostition = mCollectionAdapter.getItemCount() - 1;
            if(newPostition > 0) {
              mRecyclerView.getRefreshableView().smoothScrollToPosition(mCollectionAdapter.getItemCount() - 1);
            }
        }
        if(mCollectionAdapter.getItemCount() > 0) {
            PrivateMessageModel lastItem = mCollectionAdapter.getItem(mCollectionAdapter.getItemCount() - 1);
            mCurrentTimeSortKey = lastItem.getSortKey();
            mCurrentTimeSortKey = mCurrentTimeSortKey < 0 ? -mCurrentTimeSortKey : mCurrentTimeSortKey;
            PrivateMessageModel firstItem = mCollectionAdapter.getItem(0);
            mNextTimeSortKey = firstItem.getSortKey();
            mNextTimeSortKey = mNextTimeSortKey < 0 ? -mNextTimeSortKey : mNextTimeSortKey;
        } else {
            mCurrentTimeSortKey = -1;
            mNextTimeSortKey = -1;
        }
    }

    @Override
    public void onSendNewMessageComplete(SendNewPrivateMessageResult result) {
        if(mProgressDialog != null && mProgressDialog.isShowing()) {
          mProgressDialog.dismiss();
        }
        if(result != null) {
            if(result.isSuccess()) {
                mPresenter.loadPrivateMessages(mUserAccountManager.getAuthObject(), mTargetUserId, mCurrentTimeSortKey, RequestPointerType.TYPE_CURRENT, true);
            } else {
                // Show error here
            }
        }
    }

    @Override
    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    @Override
    public void setLoadingMore(boolean value) {
        isLoadingMore = value;
        if(!value) {
            mRecyclerView.onRefreshComplete();
        }
    }

    @Override
    public void setLoading(Boolean value) {
        isLoading = value;
    }

    @Override
    public Boolean isLoading() {
        return isLoading;
    }

    public void sendNewPrivateMessage() {
        String message = mMessageEditText.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            mPresenter.sendPrivateMessages(mUserAccountManager.getAuthObject(), mTargetUserId, mTargetUserName, message);
            mMessageEditText.getText().clear();
            mProgressDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_title_sending_private_message)
                    .content(R.string.dialog_content_please_wait)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();

        }
    }
}
