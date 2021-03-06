package org.cryse.lkong.modules.timeline;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;

import org.cryse.lkong.R;
import org.cryse.lkong.application.LKongApplication;
import org.cryse.lkong.event.AbstractEvent;
import org.cryse.lkong.event.CurrentAccountChangedEvent;
import org.cryse.lkong.model.TimelineModel;
import org.cryse.lkong.modules.simplecollection.SimpleCollectionFragment;
import org.cryse.lkong.ui.adapter.TimelineAdapter;
import org.cryse.lkong.account.LKAuthObject;
import org.cryse.lkong.ui.navigation.AppNavigation;
import org.cryse.lkong.utils.UIUtils;
import org.cryse.utils.preference.BooleanPrefs;
import org.cryse.lkong.application.PreferenceConstant;
import org.cryse.utils.preference.Prefs;
import org.cryse.utils.preference.StringPrefs;

import java.util.List;

import javax.inject.Inject;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class TimelineFragment extends SimpleCollectionFragment<
        TimelineModel,
        TimelineAdapter,
        TimelinePresenter> {
    private static final String LOG_TAG = TimelineFragment.class.getName();
    AppNavigation mNavigation = new AppNavigation();

    @Inject
    TimelinePresenter mPresenter;

    BooleanPrefs mTimelineOnlyThread;

    StringPrefs mAvatarDownloadPolicy;

    public static TimelineFragment newInstance(Bundle args) {
        TimelineFragment fragment = new TimelineFragment();
        if(args != null) {
          fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        injectThis();
        super.onCreate(savedInstanceState);
        mAvatarDownloadPolicy = Prefs.getStringPrefs(PreferenceConstant.SHARED_PREFERENCE_AVATAR_DOWNLOAD_POLICY,
                PreferenceConstant.SHARED_PREFERENCE_AVATAR_DOWNLOAD_POLICY_VALUE);
        mTimelineOnlyThread = Prefs.getBooleanPrefs(PreferenceConstant.SHARED_PREFERENCE_TIMELINE_ONLY_SHOW_THREAD,
                PreferenceConstant.SHARED_PREFERENCE_TIMELINE_ONLY_SHOW_THREAD_VALUE);
        setHasOptionsMenu(false);
    }

    @Override
    protected void injectThis() {
        LKongApplication.get(getActivity()).lKongPresenterComponent().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_simple_collection;
    }

    @Override
    protected TimelinePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected TimelineAdapter createAdapter(List<TimelineModel> itemList) {
        TimelineAdapter adapter = new TimelineAdapter(
                getActivity(),
                mItemList,
                Integer.valueOf(mAvatarDownloadPolicy.get()),
                mATEKey
        );
        adapter.setOnTimelineModelItemClickListener(new TimelineAdapter.OnTimelineModelItemClickListener() {
            @Override
            public void onProfileAreaClick(View view, int adapterPosition, long uid) {
                if (adapterPosition >= 0 && adapterPosition < mCollectionAdapter.getItemCount()) {
                    TimelineModel model = mCollectionAdapter.getItem(adapterPosition);
                    int[] startingLocation = new int[2];
                    view.getLocationOnScreen(startingLocation);
                    startingLocation[0] += view.getWidth() / 2;
                    mNavigation.openActivityForUserProfile(getActivity(), startingLocation, model.getUserId());
                }
            }

            @Override
            public void onItemTimelineClick(View view, int adapterPosition) {
                int itemIndex = adapterPosition;
                if (adapterPosition >= 0 && itemIndex < mCollectionAdapter.getItemCount()) {
                    TimelineModel model = mCollectionAdapter.getItem(adapterPosition);
                    mNavigation.openActivityForPostListByTimelineModel(getActivity(), model);
                }
            }
        });
        return adapter;
    }

    @Override
    protected void loadData(LKAuthObject authObject, long start, boolean isLoadingMore, Object... extraArgs) {
        getPresenter().loadTimeline(authObject, start, isLoadingMore, mTimelineOnlyThread.get());
    }

    @Override
    protected void onItemClick(View view, int position, long id) {
    }

    @Override
    protected void onEvent(AbstractEvent event) {
        super.onEvent(event);
        if (event instanceof CurrentAccountChangedEvent) {
            loadData(mUserAccountManager.getAuthObject(), 0, false);
        }
    }

    @Override
    protected UIUtils.InsetsValue getRecyclerViewInsets() {
        return null;
    }


    @Override
    protected void onCollectionViewInitComplete() {
        super.onCollectionViewInitComplete();
        mCollectionView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(getThemedActivity() != null && !getThemedActivity().isActivityDestroyed()) {
                      Glide.with(getActivity()).resumeRequests();
                    }
                } else {
                    Glide.with(getActivity()).pauseRequests();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
}
