package org.cryse.lkong.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.cryse.lkong.R;
import org.cryse.lkong.model.PrivateChatModel;
import org.cryse.lkong.utils.ImageLoader;
import org.cryse.lkong.utils.TimeFormatUtils;
import org.cryse.lkong.utils.transformation.CircleTransform;
import org.cryse.lkong.utils.UIUtils;
import org.cryse.widget.recyclerview.RecyclerViewHolder;
import org.cryse.widget.recyclerview.SimpleRecyclerViewAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

public class NoticePrivateChatsCollectionAdapter extends SimpleRecyclerViewAdapter<PrivateChatModel> {
    private String mATEKey;
    private CircleTransform mCircleTransform;
    private final String mTodayPrefix;
    private final int mAvatarSize;
    private int mAvatarLoadPolicy;
    public NoticePrivateChatsCollectionAdapter(Context context, String ateKey, List<PrivateChatModel> items, int avatarLoadPolicy) {
        super(context, items);
        this.mATEKey = ateKey;
        this.mTodayPrefix = mContext.getString(R.string.text_datetime_today);
        this.mAvatarSize = UIUtils.getDefaultAvatarSize(context);
        this.mCircleTransform = new CircleTransform(mContext);
        this.mAvatarLoadPolicy = avatarLoadPolicy;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_private_chat, parent, false);
        return new ViewHolder(v, mATEKey);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder viewHolder = (ViewHolder) holder;
        PrivateChatModel model = getItem(position);

        viewHolder.mChatMessageTextView.setText(model.getMessage());
        viewHolder.mUserNameTextView.setText(model.getTargetUserName());
        viewHolder.mDatelineTextView.setText(TimeFormatUtils.formatDateDividByToday(
                model.getDateline(),
                mTodayPrefix,
                mContext.getResources().getConfiguration().locale));
        ImageLoader.loadAvatar(
                mContext,
                viewHolder.mAvatarImageView,
                model.getTargetUserAvatar(),
                mAvatarSize,
                mCircleTransform,
                mAvatarLoadPolicy
        );
    }

    public static class ViewHolder extends RecyclerViewHolder {
        @BindView(R.id.recyclerview_item_private_chat_imageview_icon)
        public ImageView mAvatarImageView;
        @BindView(R.id.recyclerview_item_private_chat_textview_message)
        public TextView mChatMessageTextView;
        @BindView(R.id.recyclerview_item_private_chat_textview_username)
        public TextView mUserNameTextView;
        @BindView(R.id.recyclerview_item_private_chat_textview_dateline)
        public TextView mDatelineTextView;

        public ViewHolder(View v, String ateKey) {
            super(v);
            ButterKnife.bind(this, v);
            // ATE.apply(itemView, ateKey);
        }
    }
}
