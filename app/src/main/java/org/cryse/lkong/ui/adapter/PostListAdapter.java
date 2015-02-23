package org.cryse.lkong.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.cryse.lkong.R;
import org.cryse.lkong.model.PostModel;
import org.cryse.lkong.model.converter.ModelConverter;
import org.cryse.lkong.utils.DebugUtils;
import org.cryse.lkong.utils.SimpleImageGetter;
import org.cryse.lkong.utils.UIUtils;
import org.cryse.lkong.utils.htmltextview.HtmlTagHandler;
import org.cryse.lkong.utils.htmltextview.HtmlTextUtils;
import org.cryse.lkong.utils.htmltextview.UrlImageGetter;
import org.cryse.lkong.widget.PostItemView;
import org.cryse.utils.ColorUtils;
import org.cryse.utils.DateFormatUtils;
import org.cryse.widget.recyclerview.RecyclerViewBaseAdapter;
import org.cryse.widget.recyclerview.RecyclerViewHolder;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PostListAdapter extends RecyclerViewBaseAdapter<PostModel> {
    private static final String LOG_TAG = PostListAdapter.class.getName();
    public static final String POST_PICASSO_TAG = "picasso_post_list_adapter";
    private final String mTodayPrefix;
    private OnItemButtonClickListener mOnItemButtonClickListener;
    private long mThreadAuthorId;
    private int mMaxImageWidth;
    private int mImageDownloadPolicy;

    public PostListAdapter(Context context, List<PostModel> mItemList, int imageDownloadPolicy) {
        super(context, mItemList);
        mTodayPrefix = getString(R.string.datetime_today);
        mMaxImageWidth = UIUtils.dp2px(context, 128f);
        mImageDownloadPolicy = imageDownloadPolicy;
    }

    public void setImageDownloadPolicy(int imageDownloadPolicy) {
        mImageDownloadPolicy = imageDownloadPolicy;
    }

    public void setOnItemButtonClickListener(OnItemButtonClickListener onItemButtonClickListener) {
        this.mOnItemButtonClickListener = onItemButtonClickListener;
    }

    @Override
    public RecyclerViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_post, parent, false);
        return new ViewHolder(v, mOnItemButtonClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder)holder;
            Object item = getObjectItem(position);
            if(item instanceof PostModel) {
                PostModel postModel = (PostModel)item;

                if(postModel.getSpannedMessage() == null) {
                    SimpleImageGetter imageGetter = new SimpleImageGetter(getContext(), mImageDownloadPolicy)
                            .setEmoticonSize(UIUtils.getSpDimensionPixelSize(getContext(), R.dimen.text_size_body1))
                            .setPlaceHolder(R.drawable.image_placeholder)
                            .setMaxImageSize(mMaxImageWidth, mMaxImageWidth)
                            .setError(R.drawable.image_placeholder);
                    Spanned spannedText = HtmlTextUtils.htmlToSpanned(postModel.getMessage(), imageGetter, new HtmlTagHandler());
                    postModel.setSpannedMessage(spannedText);
                }

                SpannableStringBuilder autherNameSpannable = new SpannableStringBuilder();
                autherNameSpannable.append(postModel.getAuthorName());
                if(postModel.getAuthorId() == mThreadAuthorId) {
                    String threadAuthorIndicator = getString(R.string.indicator_thread_author);
                    autherNameSpannable.append(threadAuthorIndicator);
                    autherNameSpannable.setSpan(new ForegroundColorSpan(ColorUtils.getColorFromAttr(getContext(), R.attr.colorAccent)),
                            postModel.getAuthorName().length(),
                            postModel.getAuthorName().length() + threadAuthorIndicator.length(),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                viewHolder.mPostItemView.setAuthorInfo(autherNameSpannable, DateFormatUtils.formatFullDateDividByToday(postModel.getDateline(), mTodayPrefix));
                viewHolder.mPostItemView.setMessageText(postModel.getSpannedMessage());
                viewHolder.mPostItemView.setOrdinal(getString(R.string.format_post_ordinal, postModel.getOrdinal()));
                /*viewHolder.mMessageTextView.setMovementMethod(LinkMovementMethod.getInstance());

                *//*try {
                    DebugUtils.saveToSDCard("lkdata", Long.toString(postModel.getPid()), postModel.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }*//*

                SpannableStringBuilder autherNameSpannable = new SpannableStringBuilder();
                autherNameSpannable.append(postModel.getAuthorName());
                autherNameSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, postModel.getAuthorName().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                if(postModel.getAuthorId() == mThreadAuthorId) {
                    String threadAuthorIndicator = getString(R.string.indicator_thread_author);
                    autherNameSpannable.append(threadAuthorIndicator);
                    autherNameSpannable.setSpan(new ForegroundColorSpan(ColorUtils.getColorFromAttr(getContext(), R.attr.colorAccent)),
                            postModel.getAuthorName().length(),
                            postModel.getAuthorName().length() + threadAuthorIndicator.length(),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                viewHolder.mAuthorTextView.setText(autherNameSpannable);

                viewHolder.mDatelineTextView.setText(DateFormatUtils.formatFullDateDividByToday(postModel.getDateline(), mTodayPrefix));
                viewHolder.mOrdinalTextView.setText(getString(R.string.format_post_ordinal, postModel.getOrdinal()));
                if(postModel.getRateScore() != 0) {
                    viewHolder.mRateButton.setText(String.format("+ %d", postModel.getRateScore()));
                } else {
                    viewHolder.mRateButton.setText(R.string.button_rate);
                }*/

                Picasso.with(getContext())
                        .load(ModelConverter.uidToAvatarUrl(postModel.getAuthorId()))
                        .tag(POST_PICASSO_TAG)
                        .error(R.drawable.ic_default_avatar)
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(viewHolder.mPostItemView);
            }
        }
    }

    public void setThreadAuthorId(long threadAuthorId) {
        if(threadAuthorId != mThreadAuthorId) {
            this.mThreadAuthorId = threadAuthorId;
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerViewHolder {
        // each data item is just a string in this case
        @InjectView(R.id.recyclerview_item_post_view_item)
        PostItemView mPostItemView;
        @InjectView(R.id.recyclerview_item_post_button_rate)
        Button mRateButton;
        @InjectView(R.id.recyclerview_item_post_button_replay)
        Button mReplyButton;

        OnItemButtonClickListener mOnItemReplyClickListener;
        public ViewHolder(View v, OnItemButtonClickListener onItemReplyClickListener) {
            super(v);
            ButterKnife.inject(this, v);
            mOnItemReplyClickListener = onItemReplyClickListener;
            mReplyButton.setOnClickListener(view -> {
                if(mOnItemReplyClickListener != null) {
                    mOnItemReplyClickListener.onReplyClick(view, getPosition());
                }
            });
            mRateButton.setOnClickListener(view -> {
                if(mOnItemReplyClickListener != null) {
                    mOnItemReplyClickListener.onRateClick(view, getPosition());
                }
            });
        }
    }

    public interface OnItemButtonClickListener {
        public void onRateClick(View view, int position);
        public void onReplyClick(View view, int position);
    }
}
