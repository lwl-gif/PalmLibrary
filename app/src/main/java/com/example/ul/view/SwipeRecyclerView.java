package com.example.ul.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ul.adapter.BookViewHolder;

/**
 * @Author: Wallace
 * @Description: 自定义有侧滑删除功能的列表
 * @Date: 2021/4/24 22:01
 * @Modified: By yyyy-MM-dd
 */
public class SwipeRecyclerView extends RecyclerView {

    private static final String TAG = "SwipeRecyclerView";
    private int maxLength, mTouchSlop;
    private int xDown, yDown, xMove, yMove;
    /**
     * 当前选中的item索引（这个很重要）
     */
    private int position;
    private Scroller mScroller;
    /**左半部分*/
    private LinearLayout itemLeftLayout;
    /**隐藏部分(右半部分)*/
    private LinearLayout itemRightLayout;
    /**隐藏部分长度*/
    private int mHiddenWidth;
    /**记录连续移动的长度*/
    private int mMoveWidth = 0;
    /**是否是第一次touch*/
    private boolean isFirst = true;
    /** 删除的监听事件*/
    private OnRightClickListener onRightClickListener;

    public SwipeRecyclerView(Context context) {
        this(context, null);
        init(context);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        //滑动到最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //滑动的最大距离
        maxLength = ((int) (180 * context.getResources().getDisplayMetrics().density + 0.5f));
        //初始化Scroller
        mScroller = new Scroller(context, new LinearInterpolator(context, null));
    }

    private void init(Context context){
        //如果Context没有实现callback,ListClickedCallback接口，则抛出异常
        if (!(context instanceof OnRightClickListener)) {
            throw new IllegalStateException("SwipeRecyclerView所在的Context必须实现OnRightClickListener接口");
        }
        //把该Context当初onRightClickListener对象
        onRightClickListener = (OnRightClickListener)context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int x = (int)e.getX();
        int y = (int)e.getY();
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                //记录当前按下的坐标
                xDown = x;
                yDown = y;
                //计算选中哪个Item
                int firstPosition = ((LinearLayoutManager)getLayoutManager()).findFirstVisibleItemPosition();
                Rect itemRect = new Rect();
                final int count = getChildCount();
                for (int i=0; i<count; i++){
                    final View child = getChildAt(i);
                    if (child.getVisibility() == View.VISIBLE){
                        child.getHitRect(itemRect);
                        if (itemRect.contains(x, y)){
                            position = firstPosition + i;
                            break;
                        }
                    }
                }
                //第一次时，不用重置上一次的Item
                if (isFirst){
                    isFirst = false;
                }else {
                    //屏幕再次接收到点击时，恢复上一次Item的状态
                    if (itemRightLayout != null && mMoveWidth > 0) {
                        //将Item右移，恢复原位
                        scrollRight(itemRightLayout, (-mMoveWidth));
                        //清空变量
                        mHiddenWidth = 0;
                        mMoveWidth = 0;
                    }
                }
                //取到当前选中的Item，赋给itemLeftLayout，以便对其进行左移
                View item = getChildAt(position - firstPosition);
                if (item != null) {
                    //获取当前选中的Item
                    BookViewHolder bookViewHolder = (BookViewHolder) getChildViewHolder(item);
                    itemLeftLayout = bookViewHolder.bookRootLeft;
                    //找到具体元素（这与实际业务相关了~~）
                    itemRightLayout = bookViewHolder.bookRootRight;
                    itemRightLayout.setOnClickListener(v -> {
                        if (onRightClickListener != null){
                            //删除
                            onRightClickListener.onRightClick(position);
                            Log.e(TAG, "onTouchEvent: 删除了");
                        }
                    });
                    //这里将删除按钮的宽度设为可以移动的距离
                    mHiddenWidth = itemRightLayout.getWidth();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = x;
                yMove = y;
                //为负时：手指向左滑动；为正时：手指向右滑动。这与Android的屏幕坐标定义有关
                int dx = xMove - xDown;
                int dy = yMove - yDown;
                //左滑
                if (dx < 0 && Math.abs(dx) > mTouchSlop && Math.abs(dy) < mTouchSlop){
                    int newScrollX = Math.abs(dx);
                    //超过了，不能再移动了
                    if (mMoveWidth >= mHiddenWidth){
                        newScrollX = 0;
                    }
                    //这次要超了，
                    else if (mMoveWidth + newScrollX > mHiddenWidth){
                        newScrollX = mHiddenWidth - mMoveWidth;
                    }
                    //左滑，每次滑动手指移动的距离
                    scrollLeft(itemLeftLayout, newScrollX);
                    //对移动的距离叠加
                    mMoveWidth = mMoveWidth + newScrollX;
                }
                //右滑
                else if (dx > 0){
                    //执行右滑，这里没有做跟随，瞬间恢复
                    scrollRight(itemLeftLayout, -mMoveWidth);
                    mMoveWidth = 0;
                }
                break;
            //手抬起时
            case MotionEvent.ACTION_UP:
                int scrollX = itemLeftLayout.getScrollX();
                if (mHiddenWidth > mMoveWidth) {
                    int toX = (mHiddenWidth - mMoveWidth);
                    //超过一半长度时松开，则自动滑到左侧
                    if (scrollX > mHiddenWidth / 2) {
                        scrollLeft(itemLeftLayout, toX);
                        mMoveWidth = mHiddenWidth;
                    }
                    //不到一半时松开，则恢复原状
                    else {
                        scrollRight(itemLeftLayout, -mMoveWidth);
                        mMoveWidth = 0;
                    }
                }
                itemRightLayout = itemLeftLayout;
                break;
            default:
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            Log.e(TAG, "computeScroll getCurrX ->" + mScroller.getCurrX());
            itemLeftLayout.scrollBy(mScroller.getCurrX(), 0);
            invalidate();
        }
    }
    /**
     * 向左滑动
     */
    private void scrollLeft(View item, int scorllX){
        Log.e(TAG, " scroll left -> " + scorllX);
        item.scrollBy(scorllX, 0);
    }
    /**
     * 向右滑动
     */
    private void scrollRight(View item, int scorllX){
        Log.e(TAG, " scroll right -> " + scorllX);
        item.scrollBy(scorllX, 0);
    }

    public interface OnRightClickListener{
        void onRightClick(int position);
    }
}
