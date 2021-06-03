package com.rpathechicken.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rpathechicken.R;
import com.rpathechicken.model.Default;
import com.rpathechicken.model.Tonase;
import com.rpathechicken.utils.ItemAnimation;

import java.util.ArrayList;
import java.util.List;

public class AdapterListTonase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Tonase> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDeleteListener mOnItemDestroyListener;
    private int animation_type = 0;

    public interface OnItemClickListener {
        void onItemClick(View view, Tonase obj, int position);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(View view, Tonase obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setmOnItemDestroyListener(final OnItemDeleteListener mOnItemDestroyListener) {
        this.mOnItemDestroyListener = mOnItemDestroyListener;
    }

    public AdapterListTonase(Context context, List<Tonase> items, int animation_type) {
        this.items = items;
        ctx = context;
        this.animation_type = animation_type;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView rpa_name;
        public TextView tonase_date;
        public View lyt_parent;
        public ImageView btn_destroy;
        public TextView tonase_price;

        public OriginalViewHolder(View v) {
            super(v);
            tonase_price = (TextView) v.findViewById(R.id.txt_tonase_price);
            rpa_name = (TextView) v.findViewById(R.id.txt_tonase_rpa);
            tonase_date = (TextView) v.findViewById(R.id.txt_tonase_date);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            btn_destroy = (ImageView) v.findViewById(R.id.btn_destroy);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_tonase, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Log.e("onBindViewHolder", "onBindViewHolder : " + position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Tonase p = items.get(position);

            view.rpa_name.setText(p.getRpaName());
            view.tonase_date.setText(p.getTonaseDate());
            view.tonase_price.setText(p.getTonasePrice());
            //Tools.displayImageRound(ctx, view.image, p.image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            view.btn_destroy.setOnClickListener(view1 -> {
                if (mOnItemDestroyListener != null) {
                    mOnItemDestroyListener.onItemDelete(view1, items.get(position), position);
                }
            });

            setAnimation(view.itemView, position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int lastPosition = -1;
    private boolean on_attach = true;

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }

}