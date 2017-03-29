package com.myimageselectcontainer.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class NineImageView extends ViewGroup {


    /**
     * 存储所有的View，按行记录
     */
    private List<List<View>> mAllViews = new ArrayList<>();
    /**
     * 记录每一行的最大高度
     */
    private List<Integer> mLineHeight = new ArrayList<>();

    public NineImageView(Context context) {
        super(context);
    }

    public NineImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //总宽度和高度
        int totalWidth = 0;
        int totalHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 得到child的lp
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            // 当前子空间实际占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;
            // 当前子空间实际占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;
            if (getChildCount() <= 3) {
                totalWidth += childWidth;
                totalHeight = childHeight;
            } else if (getChildCount() <= 6) {
                if (getChildCount() == 4) {
                    totalWidth = childWidth * 2;
                    totalHeight = childHeight * 2;
                } else {
                    totalWidth = childWidth * 3;
                    totalHeight = childHeight * 2;
                }
            } else if (getChildCount() <= 9) {
                totalWidth = childWidth * 3;
                totalHeight = childHeight * 3;
            }
        }
        setMeasuredDimension(totalWidth + getPaddingLeft() + getPaddingRight(),
                totalHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //onLayout会被调用多次，为了预防重叠
        mAllViews.clear();
        mLineHeight.clear();

        //获取总宽度
        int width = getWidth();

        //单行宽度和当行高度
        int lineWidth = 0;
        int lineHeight = 0;
        // 存储每一行所有的childView
        List<View> childViews = new ArrayList<>();
        int childCount = getChildCount();
        // 遍历所有的子view
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                // 记录这一行所有的View以及最大高度
                mLineHeight.add(lineHeight);
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllViews.add(childViews);
                lineWidth = 0;// 重置行宽
                childViews = new ArrayList<>();
            }

            //  如果不需要换行，则累加
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin
                    + lp.bottomMargin);
            childViews.add(child);
        }
        // 记录最后一行
        mLineHeight.add(lineHeight);
        mAllViews.add(childViews);

        int left = getPaddingLeft();
        int top = getPaddingTop();
        // 得到总行数
        int lineNum = mAllViews.size();
        for (int i = 0; i < lineNum; i++) {
            // 每一行的所有的views
            childViews = mAllViews.get(i);
            // 当前行的最大高度
            lineHeight = mLineHeight.get(i);

            // 遍历当前行所有的子View
            for (int j = 0; j < childViews.size(); j++) {
                View child = childViews.get(j);
                if (child.getVisibility() != View.GONE) {
                    MarginLayoutParams lp = (MarginLayoutParams) child
                            .getLayoutParams();

                    //计算childView的left,top,right,bottom
                    int childLeft = left + lp.leftMargin;
                    int childTop = top + lp.topMargin;
                    int childRight = childLeft + child.getMeasuredWidth();
                    int childBottom = childTop + child.getMeasuredHeight();

                    child.layout(childLeft, childTop, childRight, childBottom);

                    left += child.getMeasuredWidth() + lp.rightMargin
                            + lp.leftMargin;
                }
            }
            //换行后，重新从第一个开始，高度累加
            left = getPaddingTop();
            top += lineHeight;
        }

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}