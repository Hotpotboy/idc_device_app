package com.zhanghang.idcdevice.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhanghang.idcdevice.R;

/**
 * Created by Administrator on 2016-04-06.
 */
public class PatrolItemProgress extends LinearLayout {
    private static final int CHILDREN_COUNT = 6;
    /**总数*/
    private int mTotalCount = 0;
    /**当前展示值*/
    private int mCurrentIndx = -1;
    /**当前页面*/
    private int mCurrentPage = 0;
    public PatrolItemProgress(Context context) {
        super(context);
    }

    public PatrolItemProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        int size = (int) getResources().getDimension(R.dimen.fourty_six_dp);
        for(int i=0;i<CHILDREN_COUNT;i++){
            TextView textView = new TextView(context);
            textView.setTextSize(getResources().getDimension(R.dimen.thirteen_sp));
            textView.setTextColor(getResources().getColor(android.R.color.white));
            textView.setGravity(Gravity.CENTER);
            addView(textView, new LayoutParams(size, size));

            if(i!=CHILDREN_COUNT-1){
                View view = new View(context);
                LinearLayout.LayoutParams params = new LayoutParams(0,(int)getResources().getDimension(R.dimen.five_dp));
                params.weight = 1;
                params.setMargins((int)getResources().getDimension(R.dimen.ten_dp),0,(int)getResources().getDimension(R.dimen.ten_dp),0);
                params.gravity=Gravity.CENTER_VERTICAL;
                addView(view,params);
            }
        }
        setTotalCount(1 + getChildCount() / 2);
    }

    /**当前总数*/
    public int getTotalCount() {
        return mTotalCount;
    }

    /**
     * 设置总数
     * @param totalCount
     */
    public void setTotalCount(int totalCount) {
        if(totalCount!=mTotalCount) {
            mTotalCount = totalCount;
        }
        updateView();
    }

    /**更新视图*/
    private void updateView(){
        int childrenCount = getChildCount();//子视图个数
        int currentIndex = mCurrentIndx*2;//当前位置
        int add = 0;//增加量
        int leftIndex,rightIndex;
        int addLeftCount = 1,addRightCount = 1;//相对于当前项的偏移量
        while ((currentIndex-add)>=0||(currentIndex+add)<childrenCount){
            leftIndex=currentIndex-add;
            rightIndex=currentIndex+add;
            if(leftIndex!=rightIndex) {//非当前项
                if (leftIndex >= 0 && leftIndex < childrenCount) {
                    View childView = getChildAt(leftIndex);
                    if (childView instanceof TextView) {//已完成的视图
                        childView.setVisibility(VISIBLE);
                        childView.setBackgroundResource(R.drawable.patrol_item_finished);
                        int showNum = (mCurrentPage*CHILDREN_COUNT+(currentIndex/2 + 2)) - addLeftCount;
                        ((TextView) childView).setText(showNum + "");
                        if (leftIndex < childrenCount - 1) {
                            getChildAt(leftIndex + 1).setVisibility(VISIBLE);
                            getChildAt(leftIndex + 1).setBackgroundColor(getResources().getColor(R.color.idc_ff4d4d));
                        }
                        addLeftCount++;
                    }
                }
                if (rightIndex >= 0 && rightIndex < childrenCount) {
                    View childView = getChildAt(rightIndex);
                    if (childView instanceof TextView) {//未完成的视图
                        childView.setBackgroundResource(R.drawable.patrol_item_unfinished);
                        int showNum = (mCurrentPage*CHILDREN_COUNT+(currentIndex/2 + 1)) + addRightCount;
                        if(showNum<=mTotalCount) {//如果未超过当前显示值的总数
                            childView.setVisibility(VISIBLE);
                            ((TextView) childView).setText(showNum + "");
                            if (rightIndex < childrenCount - 1) {
                                getChildAt(rightIndex + 1).setVisibility(VISIBLE);
                                getChildAt(rightIndex + 1).setBackgroundColor(getResources().getColor(R.color.idc_e5e5e5));
                            }
                        }else{
                            childView.setVisibility(GONE);
                            if (rightIndex < childrenCount - 1) {
                                getChildAt(rightIndex + 1).setVisibility(GONE);
                            }
                            if(showNum==(mTotalCount+1)&&rightIndex>=1){
                                getChildAt(rightIndex - 1).setVisibility(GONE);
                            }
                        }
                        addRightCount++;
                    }
                }
            }else{//当前项
                View childView = getChildAt(leftIndex);
                if (childView instanceof TextView) {//已完成的视图
                    childView.setBackgroundResource(R.drawable.patrol_item_finished);
                    int showNum = (mCurrentPage*CHILDREN_COUNT+(currentIndex/2 + 2)) - 1;
                    ((TextView) childView).setText(showNum + "");
                    childView.setVisibility(VISIBLE);
                    if (leftIndex < childrenCount - 1) {
                        getChildAt(leftIndex + 1).setVisibility(VISIBLE);
                        getChildAt(leftIndex + 1).setBackgroundColor(getResources().getColor(R.color.idc_ff4d4d));
                    }
                    addLeftCount++;
                }
            }
            add++;
        }
        requestLayout();
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int currentPage) {
        mCurrentPage = currentPage;
        updateView();
    }

    /**
     * 设置当前最新已完成的索引（不大于它的全部已完成，比它大的全部未完成）
     * @param currentIndx   当前最新已完成的索引，范围为-1到{@link #mTotalCount}
     */
    public void setCurrentIndx(int currentIndx){
        if(currentIndx<-1) currentIndx = -1;
        if(currentIndx>mTotalCount) currentIndx = mTotalCount;
        mCurrentPage = currentIndx/CHILDREN_COUNT;
        mCurrentIndx = currentIndx%CHILDREN_COUNT;
        updateView();
    }

    public int getCurrentIndx(){
        return mCurrentPage*CHILDREN_COUNT+mCurrentIndx;
    }
}
