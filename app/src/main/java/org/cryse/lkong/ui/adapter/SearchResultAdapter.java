package org.cryse.lkong.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.cryse.lkong.R;
import org.cryse.lkong.model.AbstractSearchResult;
import org.cryse.lkong.model.SearchDataSet;
import org.cryse.lkong.model.SearchGroupItem;
import org.cryse.lkong.model.SearchPostItem;
import org.cryse.lkong.model.SearchUserItem;
import org.cryse.lkong.utils.CircleTransform;
import org.cryse.widget.recyclerview.RecyclerViewBaseAdapter;
import org.cryse.widget.recyclerview.RecyclerViewHolder;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SearchResultAdapter extends RecyclerViewBaseAdapter<AbstractSearchResult> {
    private int mResultType = 0;
    private Picasso mPicasso;
    private CircleTransform mCircleTransform;
    public void setDataSet(SearchDataSet searchDataSet) {
        this.clear();
        if(searchDataSet != null) {
            this.mResultType = searchDataSet.getDataType();
            this.addAll(searchDataSet.getSearchResultItems());
        }
    }

    public SearchResultAdapter(Context context, Picasso picasso) {
        super(context, new ArrayList<AbstractSearchResult>());
        this.mPicasso = picasso;
        this.mCircleTransform = new CircleTransform();
    }

    @Override
    public RecyclerViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_TYPE_ITEM_START + SearchDataSet.TYPE_POST:
                view = inflater.inflate(R.layout.recyclerview_item_search_post, parent, false);
                return new SearchPostViewHolder(view);
            case ITEM_TYPE_ITEM_START + SearchDataSet.TYPE_USER:
                view = inflater.inflate(R.layout.recyclerview_item_search_user, parent, false);
                return new SearchUserViewHolder(view);
            case ITEM_TYPE_ITEM_START + SearchDataSet.TYPE_GROUP:
                view = inflater.inflate(R.layout.recyclerview_item_search_group, parent, false);
                return new SearchGroupViewHolder(view);
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
        viewHolder.secondaryTextView.setText(item.getUserName());
    }

    private void bindUserResult(SearchUserViewHolder viewHolder, int position, SearchUserItem item) {
        viewHolder.nameTextView.setText(item.getUserName());
        viewHolder.signTextView.setText(item.getSignHtml());
        mPicasso.load(item.getAvatarUrl())
                .placeholder(R.drawable.ic_placeholder_avatar)
                .error(R.drawable.ic_placeholder_avatar)
                .fit()
                .centerCrop()
                .transform(mCircleTransform)
                .into(viewHolder.avatarImageView);
    }

    private void bindGroupResult(SearchGroupViewHolder viewHolder, int position, SearchGroupItem item) {
        viewHolder.nameTextView.setText(item.getGroupName());
        viewHolder.descriptionTextView.setText(item.getGroupDescription());
        mPicasso.load(item.getIconUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .fit()
                .centerCrop()
                .into(viewHolder.iconImageView);
    }

    @Override
    public int onGetItemViewItemType(int position) {
        return ITEM_TYPE_ITEM_START + mResultType;
    }

    protected static class SearchPostViewHolder extends RecyclerViewHolder {
        @InjectView(R.id.recyclerview_item_search_post_title)
        TextView titleTextView;
        @InjectView(R.id.recyclerview_item_search_post_secondary)
        TextView secondaryTextView;
        public SearchPostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    protected static class SearchUserViewHolder extends RecyclerViewHolder {
        @InjectView(R.id.recyclerview_item_search_user_icon)
        ImageView avatarImageView;
        @InjectView(R.id.recyclerview_item_search_user_name)
        TextView nameTextView;
        @InjectView(R.id.recyclerview_item_search_user_sign)
        TextView signTextView;
        public SearchUserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    protected static class SearchGroupViewHolder extends RecyclerViewHolder {
        @InjectView(R.id.recyclerview_item_search_group_icon)
        ImageView iconImageView;
        @InjectView(R.id.recyclerview_item_search_group_name)
        TextView nameTextView;
        @InjectView(R.id.recyclerview_item_search_group_description)
        TextView descriptionTextView;
        public SearchGroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
