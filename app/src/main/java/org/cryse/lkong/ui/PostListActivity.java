package org.cryse.lkong.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.cryse.lkong.R;
import org.cryse.lkong.application.LKongApplication;
import org.cryse.lkong.model.PostModel;
import org.cryse.lkong.model.ThreadInfoModel;
import org.cryse.lkong.presenter.PostListPresenter;
import org.cryse.lkong.ui.adapter.PostListAdapter;
import org.cryse.lkong.ui.common.AbstractThemeableActivity;
import org.cryse.lkong.utils.DataContract;
import org.cryse.lkong.utils.ToastProxy;
import org.cryse.lkong.utils.UIUtils;
import org.cryse.lkong.view.PostListView;
import org.cryse.utils.ColorUtils;
import org.cryse.widget.recyclerview.SuperRecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PostListActivity extends AbstractThemeableActivity implements PostListView{
    private int mCurrentPage = -1;
    private int mPageCount = 0;
    private ThreadInfoModel mThreadModel;
    @Inject
    PostListPresenter mPresenter;

    @InjectView(R.id.activity_post_list_recyclerview)
    SuperRecyclerView mPostCollectionView;
    @InjectView(R.id.activity_post_list_button_page_indicator)
    Button mPageIndicatorButton;
    @InjectView(R.id.activity_post_list_button_backward)
    ImageButton mPrevPageButton;
    @InjectView(R.id.activity_post_list_button_forward)
    ImageButton mNextPageButton;

    private PostListAdapter mCollectionAdapter;

    List<PostModel> mItemList = new ArrayList<PostModel>();

    private long mThreadId = -1;
    private String mThreadSubject = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        injectThis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        ButterKnife.inject(this);
        /*getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(ColorUtils.getColorFromAttr(this, R.attr.colorPrimaryDark));
        initRecyclerView();
        Intent intent = getIntent();
        if(intent.hasExtra(DataContract.BUNDLE_THREAD_ID)) {
            mThreadId = intent.getLongExtra(DataContract.BUNDLE_THREAD_ID, -1);
        }
        if(mThreadId == -1)
            throw new IllegalStateException("PostListActivity missing extra in intent.");
        setTitle(mThreadSubject);
        setPageControl();
    }
    private void initRecyclerView() {
        UIUtils.InsetsValue insetsValue = UIUtils.getInsets(this, mPostCollectionView.getList(), false);
        mPostCollectionView.getList().setPadding(insetsValue.getLeft(), insetsValue.getTop() + getResources().getDimensionPixelSize(R.dimen.height_activity_post_list_toolbar), insetsValue.getRight(), insetsValue.getBottom());
        mPostCollectionView.getSwipeToRefresh().setProgressViewEndTarget(
                true,
                insetsValue.getTop() + getResources().getDimensionPixelSize(R.dimen.height_activity_post_list_toolbar) + UIUtils.calculateActionBarSize(this));

        mPostCollectionView.setItemAnimator(new DefaultItemAnimator());
        mPostCollectionView.setLayoutManager(new LinearLayoutManager(this));
        mCollectionAdapter = new PostListAdapter(this, mItemList);
        mPostCollectionView.setAdapter(mCollectionAdapter);
    }

    private void setPageControl() {
        mPrevPageButton.setOnClickListener(view -> {
            if(mCurrentPage - 1 >= 1 && mCurrentPage - 1 <= mPageCount)
                getPresenter().loadPostList(mThreadId, mCurrentPage - 1);
        });
        mNextPageButton.setOnClickListener(view -> {
            if(mCurrentPage + 1 >= 1 && mCurrentPage + 1 <= mPageCount)
                getPresenter().loadPostList(mThreadId, mCurrentPage + 1);
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.containsKey(DataContract.BUNDLE_CONTENT_LIST_STORE)) {
            mThreadId = savedInstanceState.getLong(DataContract.BUNDLE_THREAD_ID);
            if(savedInstanceState.containsKey(DataContract.BUNDLE_THREAD_INFO_OBJECT)) {
                mThreadSubject = savedInstanceState.getString(DataContract.BUNDLE_THREAD_SUBJECT);
                mThreadModel = savedInstanceState.getParcelable(DataContract.BUNDLE_THREAD_INFO_OBJECT);
                mCurrentPage = savedInstanceState.getInt(DataContract.BUNDLE_THREAD_CURRENT_PAGE);
                mPageCount = savedInstanceState.getInt(DataContract.BUNDLE_THREAD_PAGE_COUNT);
                ArrayList<PostModel> list = savedInstanceState.getParcelableArrayList(DataContract.BUNDLE_CONTENT_LIST_STORE);
                mCollectionAdapter.addAll(list);
                setTitle(mThreadSubject);
                updatePageIndicator();
            }
        } else {
            mPostCollectionView.getSwipeToRefresh().measure(1,1);
            mPostCollectionView.getSwipeToRefresh().setRefreshing(true);
            getPresenter().loadThreadInfo(mThreadId);
            // getPresenter().loadThreadList(mForumId, mCurrentListType, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DataContract.BUNDLE_THREAD_ID, mThreadId);
        if(mThreadModel != null) {
            outState.putString(DataContract.BUNDLE_THREAD_SUBJECT, mThreadSubject);
            outState.putParcelable(DataContract.BUNDLE_THREAD_INFO_OBJECT, mThreadModel);
            outState.putInt(DataContract.BUNDLE_THREAD_CURRENT_PAGE, mCurrentPage);
            outState.putInt(DataContract.BUNDLE_THREAD_PAGE_COUNT, mPageCount);
            outState.putParcelableArrayList(DataContract.BUNDLE_CONTENT_LIST_STORE, mCollectionAdapter.getItemArrayList());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_theme:
                setNightMode(!isNightMode());
                return true;
            /*case android.R.id.home:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    finishAfterTransition();
                else
                    finish();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void injectThis() {
        LKongApplication.get(this).lKongPresenterComponent().inject(this);
    }

    private void updatePageIndicator() {
        this.mPageIndicatorButton.setText(getString(R.string.format_post_list_page_indicator, mCurrentPage, mPageCount));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().bindView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPresenter().unbindView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().destroy();
    }

    @Override
    public void showPostList(int page, List<PostModel> posts) {
        this.mCurrentPage = page;
        updatePageIndicator();
        mCollectionAdapter.replaceWith(posts);
    }

    @Override
    public void onLoadThreadInfoComplete(ThreadInfoModel threadInfoModel) {
        mThreadModel = threadInfoModel;
        mThreadSubject = threadInfoModel.getSubject();
        setTitle(mThreadSubject);

        // Calculate page here.
        int replyCount = mThreadModel.getReplies();
        mPageCount = replyCount == 0 ? 1 : (int)Math.ceil((double) replyCount / 20d);

        if(mPageCount > 0)
            getPresenter().loadPostList(mThreadId, 1);
    }

    @Override
    public void setLoading(Boolean value) {
        this.mPostCollectionView.getSwipeToRefresh().setRefreshing(value);
    }

    @Override
    public Boolean isLoading() {
        return null;
    }

    @Override
    public void showToast(int text_value, int toastType) {
        ToastProxy.showToast(this, getString(text_value), toastType);
    }

    public PostListPresenter getPresenter() {
        return mPresenter;
    }
}
