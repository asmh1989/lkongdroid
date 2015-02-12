package org.cryse.lkong.ui;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cryse.lkong.R;
import org.cryse.lkong.application.UserAccountManager;
import org.cryse.lkong.event.AbstractEvent;
import org.cryse.lkong.event.RxEventBus;
import org.cryse.lkong.model.SimpleCollectionItem;
import org.cryse.lkong.presenter.BasePresenter;
import org.cryse.lkong.ui.common.AbstractFragment;
import org.cryse.lkong.ui.navigation.AndroidNavigation;
import org.cryse.lkong.utils.AnalyticsUtils;
import org.cryse.lkong.utils.DataContract;
import org.cryse.lkong.utils.LKAuthObject;
import org.cryse.lkong.utils.ToastProxy;
import org.cryse.lkong.utils.UIUtils;
import org.cryse.lkong.view.SimpleCollectionView;
import org.cryse.widget.recyclerview.RecyclerViewBaseAdapter;
import org.cryse.widget.recyclerview.SuperRecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class SimpleCollectionFragment<
        ItemType extends SimpleCollectionItem,
        AdapterType extends RecyclerViewBaseAdapter<ItemType>,
        PresenterType extends BasePresenter>
        extends AbstractFragment
        implements SimpleCollectionView<ItemType> {
    private boolean isNoMore = false;
    private boolean isLoading = false;
    private boolean isLoadingMore = false;
    private long mLastItemSortKey = -1;

    @Inject
    RxEventBus mEventBus;

    @Inject
    AndroidNavigation mAndroidNavigation;

    @Inject
    UserAccountManager mUserAccountManager;

    @InjectView(R.id.simple_collection_recyclerview)
    SuperRecyclerView mCollectionView;

    AdapterType mCollectionAdapter;

    List<ItemType> mItemList = new ArrayList<ItemType>();
    boolean mInMainActivity = false;

    @Override
    protected abstract void injectThis();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectThis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(getLayoutId(), null);
        ButterKnife.inject(this, contentView);
        initRecyclerView();
        return contentView;
    }

    private void initRecyclerView() {
        UIUtils.InsetsValue insetsValue = UIUtils.getInsets(getActivity(), mCollectionView, mInMainActivity);
        mCollectionView.setPadding(insetsValue.getLeft(), insetsValue.getTop(), insetsValue.getRight(), insetsValue.getBottom());
        mCollectionView.setItemAnimator(new DefaultItemAnimator());
        mCollectionView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCollectionAdapter = createAdapter(mItemList);
        mCollectionView.setAdapter(mCollectionAdapter);
        mCollectionView.setRefreshListener(() ->
                loadData(mUserAccountManager.getAuthObject(), 0, false));
        mCollectionView.setOnMoreListener((numberOfItems, numberBeforeMore, currentItemPos) -> {
            if (!isNoMore && !isLoadingMore && mLastItemSortKey != -1) {
                loadData(mUserAccountManager.getAuthObject(), mLastItemSortKey, true);
            } else {
                mCollectionView.setLoadingMore(false);
                mCollectionView.hideMoreProgress();
            }
        });
        mCollectionView.setOnItemClickListener(this::onItemClick);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DataContract.BUNDLE_THREAD_LIST_LAST_SORTKEY, mLastItemSortKey);
        outState.putParcelableArrayList(DataContract.BUNDLE_CONTENT_LIST_STORE, mCollectionAdapter.getItemArrayList());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.containsKey(DataContract.BUNDLE_CONTENT_LIST_STORE)) {
            ArrayList<ItemType> list = savedInstanceState.getParcelableArrayList(DataContract.BUNDLE_CONTENT_LIST_STORE);
            mCollectionAdapter.addAll(list);
            mLastItemSortKey = savedInstanceState.getLong(DataContract.BUNDLE_THREAD_LIST_LAST_SORTKEY);
        } else {
            mCollectionView.getSwipeToRefresh().measure(1,1);
            mCollectionView.getSwipeToRefresh().setRefreshing(true);
            loadData(mUserAccountManager.getAuthObject(), mLastItemSortKey, false);
        }

        mEventBus.toObservable().subscribe(this::onEvent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        getPresenter().bindView(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPresenter().unbindView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().destroy();
    }

    @Override
    protected void analyticsTrackEnter() {
        AnalyticsUtils.trackFragmentEnter(this, getLogTag());
    }

    @Override
    protected void analyticsTrackExit() {
        AnalyticsUtils.trackFragmentExit(this, getLogTag());
    }

    @Override
    public void showSimpleData(List<ItemType> items, boolean loadMore) {
        if(loadMore) {
            if (items.size() == 0) isNoMore = true;
            mCollectionAdapter.addAll(items);
        } else {
            isNoMore = false;
            mCollectionAdapter.replaceWith(items);
        }
        if(mCollectionAdapter.getItemCount() > 0) {
            ItemType lastItem = mCollectionAdapter.getItem(mCollectionAdapter.getItemCount() - 1);
            mLastItemSortKey = lastItem.getSortKey();
        } else {
            mLastItemSortKey = -1;
        }
    }

    @Override
    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    @Override
    public void setLoadingMore(boolean value) {
        isLoadingMore = value;
        mCollectionView.setLoadingMore(value);
        if(value)
            mCollectionView.showMoreProgress();
        else
            mCollectionView.hideMoreProgress();
    }

    @Override
    public void setLoading(Boolean value) {
        isLoading = value;
        mCollectionView.getSwipeToRefresh().setRefreshing(value);
    }

    @Override
    public Boolean isLoading() {
        return isLoading;
    }

    @Override
    public void showToast(int text_value, int toastType) {
        ToastProxy.showToast(getActivity(), getString(text_value), toastType);
    }

    protected abstract String getLogTag();

    protected abstract int getLayoutId();

    protected abstract PresenterType getPresenter();

    protected abstract AdapterType createAdapter(List<ItemType> itemList);

    protected abstract void loadData(LKAuthObject authObject, long start, boolean isLoadingMore, Object... extraArgs);

    protected abstract void onItemClick(View view, int position, long id);

    protected abstract void onEvent(AbstractEvent event);
}