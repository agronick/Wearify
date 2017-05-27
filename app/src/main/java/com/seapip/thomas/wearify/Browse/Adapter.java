package com.seapip.thomas.wearify.Browse;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seapip.thomas.wearify.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private final int ITEM = 0;
    private final int HEADER = 1;
    private final int LETTER_GROUP_HEADER = 2;
    private final int ACTION_BUTTON = 3;
    private final int ACTION_BUTTON_SMALL = 4;
    private final int LOADING = 5;

    private android.content.Context mContext;
    private List<Item> mList;
    private RecyclerView mRecyclerView;

    public Adapter(android.content.Context context, List<Item> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case HEADER:
                view = layoutInflater.inflate(R.layout.browse_header, viewGroup, false);
                viewHolder = new HeaderViewHolder(view);
                break;
            case LETTER_GROUP_HEADER:
                view = layoutInflater.inflate(R.layout.browse_letter_group_header, viewGroup, false);
                viewHolder = new LetterGroupHeaderViewHolder(view);
                break;
            case ACTION_BUTTON:
                view = layoutInflater.inflate(R.layout.browse_action_button, viewGroup, false);
                viewHolder = new ActionButtonViewHolder(view);
                break;
            case ACTION_BUTTON_SMALL:
                view = layoutInflater.inflate(R.layout.browse_action_button_small, viewGroup, false);
                viewHolder = new ActionButtonSmallViewHolder(view);
                break;
            case LOADING:
                view = layoutInflater.inflate(R.layout.browse_loading, viewGroup, false);
                viewHolder = new LoadingViewHolder(view);
                break;
            default:
                view = layoutInflater.inflate(R.layout.browse_item, viewGroup, false);
                viewHolder = new ItemViewHolder(view);
                break;
        }
        view.setOnClickListener(this);

        return viewHolder;
    }

    @Override
    public void onClick(View v) {
        int position = mRecyclerView.getChildLayoutPosition(v);
        Item item = mList.get(position);
        if (item.onClick != null) {
            item.onClick.run(mContext);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case HEADER:
                configureViewHolder((HeaderViewHolder) viewHolder, position);
                break;
            case LETTER_GROUP_HEADER:
                configureViewHolder((LetterGroupHeaderViewHolder) viewHolder, position);
                break;
            case ACTION_BUTTON:
            case ACTION_BUTTON_SMALL:
                configureViewHolder((ActionButtonViewHolder) viewHolder, position);
                break;
            case LOADING:
                configureViewHolder((LoadingViewHolder) viewHolder, position);
                break;
            default:
                configureViewHolder((ItemViewHolder) viewHolder, position);
                break;
        }
    }

    private void configureViewHolder(HeaderViewHolder viewHolder, int position) {
        Header header = (Header) mList.get(position);
        viewHolder.title.setText(header.title);
        if (header.subTitle != null) {
            viewHolder.title.setGravity(Gravity.TOP);
            viewHolder.subTitle.setText(header.subTitle);
            viewHolder.subTitle.setVisibility(View.VISIBLE);
        } else {
            viewHolder.title.setGravity(Gravity.CENTER);
            viewHolder.subTitle.setVisibility(View.GONE);
        }
    }

    private void configureViewHolder(LetterGroupHeaderViewHolder viewHolder, int position) {
        LetterGroupHeader header = (LetterGroupHeader) mList.get(position);
        viewHolder.letter.setText(header.letter);
    }

    private void configureViewHolder(ActionButtonViewHolder viewHolder, int position) {
        ActionButton actionButton = (ActionButton) mList.get(position);
        Drawable icon = actionButton.icon.mutate();
        LayerDrawable layerDrawable = (LayerDrawable) mContext.getDrawable(R.drawable.nested_icon).mutate();
        GradientDrawable background = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.nested_background);
        background.setTint(actionButton.backgroundColor);
        icon.setTint(actionButton.iconColor);
        layerDrawable.setDrawableByLayerId(R.id.nested_icon, icon);
        viewHolder.image.setImageDrawable(layerDrawable);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.image.getLayoutParams();

        if (actionButton.text == null) {
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            viewHolder.image.setLayoutParams(layoutParams);
            viewHolder.text.setVisibility(View.GONE);
        } else {
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            viewHolder.image.setLayoutParams(layoutParams);
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.text.setText(actionButton.text);
        }
    }

    private void configureViewHolder(LoadingViewHolder viewHolder, int position) {
        Loading loading = (Loading) mList.get(position);
        viewHolder.progressBar.getIndeterminateDrawable().setColorFilter(loading.color, PorterDuff.Mode.SRC_ATOP);
    }

    private void configureViewHolder(final ItemViewHolder viewHolder, int position) {
        Item item = mList.get(position);
        viewHolder.title.setText(item.title);
        if (item.subTitle != null) {
            viewHolder.title.setGravity(Gravity.TOP);
            viewHolder.subTitle.setText(item.subTitle);
            viewHolder.subTitle.setVisibility(View.VISIBLE);
        } else {
            viewHolder.title.setGravity(Gravity.CENTER_VERTICAL);
            viewHolder.subTitle.setVisibility(View.GONE);
        }
        if (item.imageUrl != null) {
            Picasso.with(mContext).load(item.imageUrl).fit().into(viewHolder.image, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) viewHolder.image.getDrawable()).getBitmap();
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), bitmap);
                    drawable.setCircular(true);
                    drawable.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
                    viewHolder.image.setImageDrawable(drawable);
                }

                @Override
                public void onError() {
                }
            });
            viewHolder.image.setVisibility(View.VISIBLE);
        } else if (item.image != null) {
            Drawable image = item.image.mutate();
            LayerDrawable layerDrawable = (LayerDrawable) mContext.getDrawable(R.drawable.nested_icon).mutate();
            image.setTint(Color.WHITE);
            layerDrawable.setDrawableByLayerId(R.id.nested_icon, image);
            viewHolder.image.setImageDrawable(layerDrawable);
            viewHolder.image.setVisibility(View.VISIBLE);
        } else {
            viewHolder.image.setVisibility(View.GONE);
        }
        if (item.number > 0) {
            viewHolder.number.setText(String.valueOf(item.number));
            viewHolder.number.setVisibility(View.VISIBLE);
        } else {
            viewHolder.number.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position) instanceof Header) {
            return HEADER;
        } else if (mList.get(position) instanceof LetterGroupHeader) {
            return LETTER_GROUP_HEADER;
        } else if (mList.get(position) instanceof ActionButtonSmall) {
            return ACTION_BUTTON_SMALL;
        } else if (mList.get(position) instanceof ActionButton) {
            return ACTION_BUTTON;
        } else if (mList.get(position) instanceof Loading) {
            return LOADING;
        }
        return ITEM;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ItemViewHolder extends WearableRecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subTitle;
        private final ImageView image;
        private final TextView number;

        public ItemViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            subTitle = (TextView) view.findViewById(R.id.sub_title);
            image = (ImageView) view.findViewById(R.id.image);
            number = (TextView) view.findViewById(R.id.number);
        }
    }

    public static class HeaderViewHolder extends WearableRecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subTitle;

        public HeaderViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            subTitle = (TextView) view.findViewById(R.id.sub_title);
        }
    }

    public static class ActionButtonViewHolder extends WearableRecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView text;

        public ActionButtonViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.button_icon);
            text = (TextView) view.findViewById(R.id.button_text);
        }
    }

    public static class ActionButtonSmallViewHolder extends ActionButtonViewHolder {

        public ActionButtonSmallViewHolder(View view) {
            super(view);
        }
    }

    public static class LoadingViewHolder extends WearableRecyclerView.ViewHolder {

        private final ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        }
    }

    public static class LetterGroupHeaderViewHolder extends WearableRecyclerView.ViewHolder {

        private final TextView letter;

        public LetterGroupHeaderViewHolder(View view) {
            super(view);
            letter = (TextView) view.findViewById(R.id.letter);
        }
    }
}