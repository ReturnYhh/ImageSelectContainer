package com.myimageselectcontainer.adapter;


import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.myimageselectcontainer.click.OnChangeListener;
import com.myimageselectcontainer.click.OnItemClickListener;
import com.myimageselectcontainer.R;
import com.myimageselectcontainer.bean.ImageBean;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //相机布局
    private static final int CAMERA_TYPE = 0;
    //普通布局
    private static final int LAYOUT_TYPE = 1;


    private List<ImageBean> mList;
    private Context mContext;
    private float mScreenWidth;
    //可选择的图片数，动态控制选择图片
    private int mMaxImageCount;
    private OnItemClickListener mOnItemClickListener;
    private OnChangeListener mOnChangeListener;
    private List<ImageBean> mSelectImages;


    public MyAdapter(Context context, int maxImageCount, List<ImageBean> list, OnChangeListener onChangeListener,
                     OnItemClickListener onItemClickListener) {
        //获取屏幕宽度
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;


        this.mContext = context;
        this.mMaxImageCount = maxImageCount;
        this.mOnItemClickListener = onItemClickListener;
        this.mOnChangeListener = onChangeListener;
        this.mList = list;
        Log.e("list",mList.toString()+"");
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return CAMERA_TYPE;
        }
        return LAYOUT_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == CAMERA_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.camera_item, parent, false);
            return new CameraViewHolder(view, mOnItemClickListener);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.image_select_item, parent, false);
            return new MyViewHolder(view, mOnItemClickListener, mOnChangeListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (getItemViewType(position)==LAYOUT_TYPE) {
            MyViewHolder holder = (MyViewHolder) viewHolder;
            holder.cbSelect.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(mList.get(position).getPath()).into(holder.imageView);
            if (mList.get(position).isSelect()) {
                holder.cbSelect.setChecked(true);
                holder.canSelect();
            } else {
                if (mSelectImages == null) {
                    holder.canSelect();
                } else if (mSelectImages.size() == mMaxImageCount) {
                    holder.cannotSelect();
                } else {
                    holder.canSelect();
                }
                holder.cbSelect.setChecked(false);
            }

            if (mMaxImageCount == 1) {
                holder.cbSelect.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //当选择的图片大于可选择图片数的时候，就不能继续选择
    public void notifyData(List<ImageBean> list) {
        mSelectImages = list;
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        private ImageView imageView;
        private CheckBox cbSelect;
        private OnItemClickListener mOnItemClickListener;
        private OnChangeListener mOnChangeListener;

        public MyViewHolder(View itemView, OnItemClickListener onItemClickListener,
                            OnChangeListener onChangeListener) {
            super(itemView);
            this.mOnItemClickListener = onItemClickListener;
            this.mOnChangeListener = onChangeListener;
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            cbSelect = (CheckBox) itemView.findViewById(R.id.ch_select);
            //适配imageView，正方形，宽和高都是屏幕宽度的1/3
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            params.width = (int) mScreenWidth / 3 - params.rightMargin - params.leftMargin;
            params.height = (int) mScreenWidth / 3 - params.topMargin - params.bottomMargin;
            imageView.setLayoutParams(params);
            if (onItemClickListener != null) {
                itemView.setOnClickListener(this);
            }
            if (onChangeListener != null) {
                cbSelect.setOnCheckedChangeListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClickListener(v, getAdapterPosition());
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mOnChangeListener != null) {
                mOnChangeListener.OnChangeListener(getAdapterPosition(), isChecked);
            }
        }

        public void cannotSelect() {
            imageView.setAlpha(0.3f);
            cbSelect.setClickable(false);
        }

        public void canSelect() {
            imageView.setAlpha(1.0f);
            cbSelect.setClickable(true);
        }
    }

    class CameraViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private ImageView imageView;
        private OnItemClickListener mOnItemClickListener;

        public CameraViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.mOnItemClickListener = onItemClickListener;
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
//            适配imageView，正方形，宽和高都是屏幕宽度的1/3
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            params.width = (int) mScreenWidth / 3 - params.rightMargin - params.leftMargin;
            params.height = (int) mScreenWidth / 3 - params.topMargin - params.bottomMargin;
            imageView.setLayoutParams(params);
            if (onItemClickListener != null) {
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClickListener(v, getAdapterPosition());
            }
        }
    }
}
