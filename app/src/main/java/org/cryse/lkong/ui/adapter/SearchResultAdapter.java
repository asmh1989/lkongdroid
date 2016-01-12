package org.cryse.lkong.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.bumptech.glide.Glide;

import org.cryse.lkong.R;
import org.cryse.lkong.model.AbstractSearchResult;
import org.cryse.lkong.model.SearchDataSet;
import org.cryse.lkong.model.SearchGroupItem;
import org.cryse.lkong.model.SearchPostItem;
import org.cryse.lkong.model.SearchUserItem;
import org.cryse.lkong.utils.ImageLoader;
import org.cryse.lkong.utils.TimeFormatUtils;
import org.cryse.lkong.utils.UIUtils;
import org.cryse.lkong.utils.transformation.CircleTransform;
import org.cryse.widget.recyclerview.RecyclerViewBaseAdapter;
import org.cryse.widget.recyclerview.RecyclerViewHolder;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Bind;

public class SearchResultAdapter extends RecyclerViewBaseAdapter<AbstractSearchResult> {
    private String mATEKey;
    private int mResultType = 0;
    private CircleTransform mCircleTransform;
    private final String mTodayPrefix;
    private final int mAvatarSize;
    private int mAvatarLoadPolicy;
    public void setDataSet(SearchDataSet searchDataSet) {
        this.clear();
        if(searchDataSet != null) {
            this.mResultType = searchDataSet.getDataType();
            this.addAll(searchDataSet.getSearchResultItems());
        }
    }

    public void appendDataSet(SearchDataSet searchDataSet) {
        if (mResultType == searchDataSet.getDataType()) {
            this.addAll(searchDataSet.getSearchResultItems());
        }
    }

    public SearchResultAdapter(Context context, int avatarLoadPolicy) {
        super(context, new ArrayList<AbstractSearchResult>());
        this.mCircleTransform = new CircleTransform(context);
        this.mTodayPrefix = getString(R.string.datetime_today);
        this.mAvatarSize = UIUtils.getDefaultAvatarSize(context);
        this.mAvatarLoadPolicy = avatarLoadPolicy;
    }

    @Override
    public RecyclerViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_TYPE_ITEM_START + SearchDataSet.TYPE_POST:
                view = inflater.inflate(R.layout.recyclerview_item_search_post, parent, false);
                return new SearchPostViewHolder(view, mATEKey);
            case ITEM_TYPE_ITEM_START + SearchDataSet.TYPE_USER:
                view = inflater.inflate(R.layout.recyclerview_item_search_user, parent, false);
                return new SearchUserViewHolder(view, mATEKey);
            case ITEM_TYPE_ITEM_START + SearchDataSet.TYPE_GROUP:
                view = inflater.inflate(R.layout.recyclerview_item_search_group, parent, false);
                return new SearchGroupViewHolder(view, mATEKey);
            default:
                throw new IllegalArgumentException("Unknown viewType.");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Object item = getObjectItem(position);
        if(item instanceof AbstractSearchResult) {
            switch (mResultType) {
                case SearchDataSet.TYPE_POST:
                    bindPostResult((SearchPostViewHolder) holder, position, (SearchPostItem) item);
                    break;
                case SearchDataSet.TYPE_USER:
                    bindUserResult((SearchUserViewHolder) holder, position, (SearchUserItem) item);
                    break;
                case SearchDataSet.TYPE_GROUP:
                    bindGroupResult((SearchGroupViewHolder) holder, position, (SearchGroupItem) item);
                    break;
            }
        }
    }

    private void bindPostResult(SearchPostViewHolder viewHolder, int position, SearchPostItem item) {
        viewHolder.titleTextView.setText(item.getSubject());
        viewHolder.secondaryTextView.setText(TimeFormatUtils.formatDateDividByToday(
                item.getDateline(),
                mTodayPrefix,
                getContext().getResources().getConfiguration().locale
        ));
        viewHolder.replyCountTextView.setText(Integer.toString(item.getReplyCount()));
    }

    private void bindUserResult(SearchUserViewHolder viewHolder, int position, SearchUserItem item) {
        viewHolder.nameTextView.setText(item.getUserName());
        viewHolder.signTextView.setText(item.getSignHtml());
        ImageLoader.loadAvatar(
                getContext(),
                viewHolder.avatarImageView,
                item.getAvatarUrl(),
                mAvatarSize,
                mCircleTransform,
                mAvatarLoadPolicy
        );
    }

    private void bindGroupResult(SearchGroupViewHolder viewHolder, int position, SearchGroupItem item) {
        viewHolder.nameTextView.setText(item.getGroupName());
        viewHolder.descriptionTextView.setText(item.getGroupDescription());
        Glide.with(getContext()).load(item.getIconUrl())
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.placeholder_error)
                .centerCrop()
                .into(viewHolder.iconImageView);
    }

    @Override
    public int onGetItemViewItemType(int position) {
        return ITEM_TYPE_ITEM_START + mResultType;
    }

    public int getResultType() {
        return mResultType;
    }

    protected static class SearchPostViewHolder extends RecyclerViewHolder {
        @Bind(R.id.recyclerview_item_search_post_title)
        TextView titleTextView;
        @Bind(R.id.recyclerview_item_search_post_secondary)
        TextView secondaryTextView;
        @Bind(R.id.recyclerview_item_search_post_reply_count)
        TextView replyCountTextView;
        public SearchPostViewHolder(View itemView, String ateKey) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            ATE.apply(itemView, ateKey);
        }
    }

    protected static class SearchUserViewHolder extends RecyclerViewHolder {
        @Bind(R.id.recyclerview_item_search_user_icon)
        ImageView avatarImageView;
        @Bind(R.id.recyclerview_item_search_user_name)
        TextView nameTextView;
        @Bind(R.id.recyclerview_item_search_user_sign)
        TextView signTextView;
        public SearchUserViewHolder(View itemView, String ateKey) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            ATE.apply(itemView, ateKey);
        }
    }

    protected static class SearchGroupViewHolder extends RecyclerViewHolder {
        @Bind(R.id.recyclerview_item_search_group_icon)
        ImageView iconImageView;
        @Bind(R.id.recyclerview_item_search_group_name)
        TextView nameTextView;
        @Bind(R.id.recyclerview_item_search_group_description)
        TextView descriptionTextView;
        public SearchGroupViewHolder(View itemView, String ateKey) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            ATE.apply(itemView, ateKey);
        }
    }
}
