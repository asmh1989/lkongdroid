package org.cryse.lkong.ui.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cryse.lkong.R;
import org.cryse.lkong.model.NoticeRateModel;
import org.cryse.widget.recyclerview.RecyclerViewHolder;
import org.cryse.widget.recyclerview.SimpleRecyclerViewAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

public class NoticeRateCollectionAdapter extends SimpleRecyclerViewAdapter<NoticeRateModel> {
    private String mATEKey;
    public NoticeRateCollectionAdapter(Context context, String ateKey, List<NoticeRateModel> items) {
        super(context, items);
        this.mATEKey = ateKey;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice_rate, parent, false);
        return new ViewHolder(v, mATEKey);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder viewHolder = (ViewHolder) holder;
        NoticeRateModel noticeRateModel = getItem(position);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        String prefix = mContext.getString(R.string.format_note_rate_log_prefix);
        spannableStringBuilder.append(prefix);
        spannableStringBuilder.append(noticeRateModel.getMessage());

        String middle = mContext.getString(R.string.format_note_rate_log_middle, noticeRateModel.getUserName());
        spannableStringBuilder.append(middle);
        String suffix = mContext.getString(R.string.format_note_rate_log_suffix, noticeRateModel.getExtCredits(), noticeRateModel.getScore());
        spannableStringBuilder.append(suffix);
        spannableStringBuilder.append('\n').append(noticeRateModel.getReason());
        viewHolder.mNoticeMessageTextView.setText(spannableStringBuilder);
    }

    public static class ViewHolder extends RecyclerViewHolder {
        @BindView(R.id.recyclerview_item_notice_rate_cardview_root_container)
        public CardView mRootCardView;
        @BindView(R.id.recyclerview_item_notice_textview_message)
        public TextView mNoticeMessageTextView;

        public ViewHolder(View v, String ateKey) {
            super(v);
            ButterKnife.bind(this, v);
            // ATE.apply(itemView, ateKey);
            //mRootCardView.setCardBackgroundColor(Config.textColorPrimaryInverse(itemView.getContext(), ateKey));
        }
    }
}
