package com.myimageselectcontainer.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.myimageselectcontainer.click.OnImageDirItemListener;
import com.myimageselectcontainer.R;
import com.myimageselectcontainer.bean.ImageDirBean;

import java.util.List;

public class ImageDirAdapter extends RecyclerView.Adapter<ImageDirAdapter.ImageDirViewHolder> {

    private Context mContext;
    private List<ImageDirBean> mList;
    private OnImageDirItemListener mListener;

    public ImageDirAdapter(Context context, List<ImageDirBean> list, OnImageDirItemListener listener) {
        this.mContext = context;
        this.mList = list;
        this.mListener = listener;
    }

    @Override
    public ImageDirViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageDirViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.image_dir_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(ImageDirViewHolder holder, int position) {
        Glide.with(mContext).load(mList.get(position).getImagePath()).into(holder.imageDir);
        holder.tvImageDirName.setText(mList.get(position).getImageName());
        holder.tvImageCount.setText(mList.get(position).getImageCount() + "张");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ImageDirViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageDir;
        TextView tvImageDirName;
        ImageView imageSelect;
        TextView tvImageCount;

        OnImageDirItemListener mListener;

        public ImageDirViewHolder(View itemView, OnImageDirItemListener listener) {
            super(itemView);
            this.mListener = listener;
            imageDir = (ImageView) itemView.findViewById(R.id.image_dir);
            tvImageDirName = (TextView) itemView.findViewById(R.id.tv_imageDirName);
            tvImageCount = (TextView) itemView.findViewById(R.id.tv_imageCount);
            imageSelect = (ImageView) itemView.findViewById(R.id.image_select);
            if (mListener != null) {
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onImageDirItemListener(v, getAdapterPosition());
            }
        }
    }
}
