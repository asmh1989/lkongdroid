package org.cryse.lkong.ui.adapter;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.cryse.lkong.R;
import org.cryse.lkong.model.TimelineModel;
import org.cryse.lkong.model.converter.ModelConverter;
import org.cryse.lkong.utils.UIUtils;
import org.cryse.lkong.utils.htmltextview.HtmlTagHandler;
import org.cryse.lkong.utils.htmltextview.HtmlTextUtils;
import org.cryse.lkong.utils.htmltextview.UrlImageGetter;
import org.cryse.utils.DateFormatUtils;
import org.cryse.widget.recyclerview.RecyclerViewBaseAdapter;
import org.cryse.widget.recyclerview.RecyclerViewHolder;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TimelineAdapter extends RecyclerViewBaseAdapter<TimelineModel> {
    private final String mTodayPrefix;
    public TimelineAdapter(Context context, List<TimelineModel> items) {
        super(context, items);
        mTodayPrefix = getString(R.string.datetime_today);
    }

    @Override
    public RecyclerViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_timeline, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder)holder;
            Object item = getObjectItem(position);
            if(item instanceof TimelineModel) {
                TimelineModel threadModel = (TimelineModel)item;

                UrlImageGetter urlImageGetter = new UrlImageGetter(getContext(), viewHolder.mMessageTextView)
                        .setEmoticonSize(UIUtils.getSpDimensionPixelSize(getContext(), R.dimen.text_size_body1))
                        .setPlaceHolder(R.drawable.image_placeholder)
                        .setError(R.drawable.image_placeholder);
                Spanned spannedText = HtmlTextUtils.htmlToSpanned(threadModel.getMessage(), urlImageGetter, new HtmlTagHandler());
                viewHolder.mMessageTextView.setText(spannedText);
                viewHolder.mMessageTextView.setMovementMethod(LinkMovementMethod.getInstance());

                viewHolder.mAuthorTextView.setText(threadModel.getUserName());
                viewHolder.mDatelineTextView.setText(DateFormatUtils.formatFullDateDividByToday(threadModel.getDateline(), mTodayPrefix));
                Picasso.with(getContext())
                        .load(ModelConverter.uidToAvatarUrl(threadModel.getUserId()))
                        .error(R.drawable.ic_default_avatar)
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(viewHolder.mAuthorAvatarImageView);
            }
        }
    }

    public static class ViewHolder extends RecyclerViewHolder {
        // each data item is just a string in this case
        @InjectView(R.id.recyclerview_item_timeline_textview_author_name)
        TextView mAuthorTextView;
        @InjectView(R.id.recyclerview_item_timeline_textview_dateline)
        TextView mDatelineTextView;
        @InjectView(R.id.recyclerview_item_timeline_textview_ordinal)
        TextView mOrdinalTextView;
        @InjectView(R.id.recyclerview_item_timeline_textview_message)
        TextView mMessageTextView;
        @InjectView(R.id.recyclerview_item_timeline_imageview_author_avatar)
        ImageView mAuthorAvatarImageView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }
}
