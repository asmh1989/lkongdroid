package org.cryse.lkong.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.cryse.lkong.R;
import org.cryse.lkong.model.BrowseHistory;
import org.cryse.lkong.ui.listener.OnItemThreadClickListener;
import org.cryse.lkong.utils.TimeFormatUtils;
import org.cryse.widget.recyclerview.RecyclerViewHolder;
import org.cryse.widget.recyclerview.SimpleRecyclerViewAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class BrowseHistoryAdapter extends SimpleRecyclerViewAdapter<BrowseHistory> {
    private String mATEKey;
    private final String mTodayPrefix;

    OnBrowseHistoryItemClickListener mOnBrowseHistoryItemClickListener;

    public BrowseHistoryAdapter(Context context, String ateKey, List<BrowseHistory> mItemList) {
        super(context, mItemList);
        this.mATEKey = ateKey;
        this.mTodayPrefix = mContext.getString(R.string.text_datetime_today);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_browse_history, parent, false);
        return new ViewHolder(v, mATEKey, mOnBrowseHistoryItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        BrowseHistory historyItem = getItem(position);

        viewHolder.mTitleTextView.setText(historyItem.getThreadTitle());
        viewHolder.mSecondaryTextView.setText(historyItem.getForumTitle() + " - " + historyItem.getThreadAuthorName());
        viewHolder.mTimeTextView.setText(TimeFormatUtils.formatDateDividByToday(
                historyItem.getLastReadTimeDate(),
                mTodayPrefix,
                mContext.getResources().getConfiguration().locale));
    }

    public void setOnBrowseHistoryItemClickListener(OnBrowseHistoryItemClickListener listener) {
        this.mOnBrowseHistoryItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerViewHolder {
        // each data item is just a string in this case

        @BindView(R.id.recyclerview_item_browse_history_relative_layout_root)
        RelativeLayout mRootView;
        @BindView(R.id.recyclerview_item_browse_history_textview_title)
        public TextView mTitleTextView;
        @BindView(R.id.recyclerview_item_browse_history_textview_secondary)
        public TextView mSecondaryTextView;
        @BindView(R.id.recyclerview_item_browse_history_textview_time)
        public TextView mTimeTextView;

        OnBrowseHistoryItemClickListener mOnThreadItemClickListener;
        public ViewHolder(View v, String ateKey, OnBrowseHistoryItemClickListener listener) {
            super(v);
            ButterKnife.bind(this, v);
            mOnThreadItemClickListener = listener;
            itemView.setOnClickListener(view -> {
                if(mOnThreadItemClickListener != null) {
                    mOnThreadItemClickListener.onItemThreadClick(view, getAdapterPosition());
                }
            });
            // ATE.apply(itemView, ateKey);
        }
    }

    public interface OnBrowseHistoryItemClickListener extends OnItemThreadClickListener {

    }
}
