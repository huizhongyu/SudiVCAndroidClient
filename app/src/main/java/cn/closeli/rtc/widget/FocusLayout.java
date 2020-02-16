package cn.closeli.rtc.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Set;

import cn.closeli.rtc.R;

public class FocusLayout extends RelativeLayout implements ViewTreeObserver.OnGlobalFocusChangeListener {
    private LayoutParams mFocusLayoutParams;
    private View mFocusView;
    private Set<Integer> ingoreIds;          //需要忽略的ids
    private Set<Integer> focusedIds;
    private int heightPixels;
    private int widthPixels;

    public FocusLayout(Context context) {
        super(context);
        init(context);
        ingoreIds = new HashSet<>();
        focusedIds = new HashSet<>();
    }

    public FocusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FocusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        heightPixels = displayMetrics.heightPixels;
        widthPixels = displayMetrics.widthPixels;
        this.mFocusLayoutParams = new RelativeLayout.LayoutParams(0, 0);
        this.mFocusView = new View(context);
        this.mFocusView.setBackgroundResource(R.drawable.bg_item_selector);
        this.addView(this.mFocusView, this.mFocusLayoutParams);
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (newFocus == null) {
            return;
        }
//        if (!focusedIds.contains(newFocus.getId())) {
//            return;
//        }
        Rect viewRect = new Rect();
        newFocus.getGlobalVisibleRect(viewRect);
        correctLocation(viewRect);
        this.mFocusView.setBackgroundResource(R.drawable.bg_item_selector);
        if (newFocus instanceof CompoundButton) {
            this.setFocusLocation(
                    viewRect.left - this.mFocusView.getPaddingLeft(),
                    viewRect.top - this.mFocusView.getPaddingTop(),
                    viewRect.right + this.mFocusView.getPaddingRight(),
                    viewRect.bottom + this.mFocusView.getPaddingBottom());
            return;
        } else if (newFocus instanceof Button || newFocus instanceof EditText) {
            this.mFocusView.setBackgroundResource(R.color.color_translate);
            this.setFocusLocation(
                    viewRect.left + (viewRect.right + this.mFocusView.getPaddingRight() - (viewRect.left - this.mFocusView.getPaddingLeft())) / 2 + 50,
                    viewRect.top + (viewRect.bottom + this.mFocusView.getPaddingBottom() - (viewRect.top - this.mFocusView.getPaddingTop())) / 2 + 50,
                    viewRect.right - ((viewRect.right + this.mFocusView.getPaddingRight() - (viewRect.left - this.mFocusView.getPaddingLeft())) / 2 - 50),
                    viewRect.bottom - (viewRect.bottom + this.mFocusView.getPaddingBottom()) - (viewRect.top - this.mFocusView.getPaddingTop()) / 2 - 50);
            return;
        }

        //需要特殊处理的View
        if (ingoreIds.contains(newFocus.getId())) {
            this.mFocusView.setBackgroundResource(R.color.color_translate);
            this.setFocusLocation(
                    viewRect.left + (viewRect.right + this.mFocusView.getPaddingRight() - (viewRect.left - this.mFocusView.getPaddingLeft())) / 2 + 50,
                    viewRect.top + (viewRect.bottom + this.mFocusView.getPaddingBottom() - (viewRect.top - this.mFocusView.getPaddingTop())) / 2 + 50,
                    viewRect.right - ((viewRect.right + this.mFocusView.getPaddingRight() - (viewRect.left - this.mFocusView.getPaddingLeft())) / 2 - 50),
                    viewRect.bottom - (viewRect.bottom + this.mFocusView.getPaddingBottom()) - (viewRect.top - this.mFocusView.getPaddingTop()) / 2 - 50);
            return;
        }

        this.setFocusLocation(
                viewRect.left - this.mFocusView.getPaddingLeft(),
                viewRect.top - this.mFocusView.getPaddingTop(),
                viewRect.right + this.mFocusView.getPaddingRight(),
                viewRect.bottom + this.mFocusView.getPaddingBottom());

    }

    /**
     * 由于getGlobalVisibleRect获取的位置是相对于全屏的,所以需要减去FocusLayout本身的左与上距离,变成相对于FocusLayout的
     *
     * @param rect
     */
    private void correctLocation(Rect rect) {
        Rect layoutRect = new Rect();
        this.getGlobalVisibleRect(layoutRect);
        rect.left -= layoutRect.left;
        rect.right -= layoutRect.left;
        rect.top -= layoutRect.top;
        rect.bottom -= layoutRect.top;
    }

    /**
     * 设置焦点view的位置,计算焦点框的大小
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    protected void setFocusLocation(int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;

        this.mFocusLayoutParams.width = width;
        this.mFocusLayoutParams.height = height;
        this.mFocusLayoutParams.leftMargin = left;
        this.mFocusLayoutParams.topMargin = top;
        this.mFocusView.layout(left, top, Math.min(right, widthPixels), Math.min(bottom, heightPixels));
    }

    //添加需要忽略的ids
    public void addIngoreIds(int ids) {
        if (ingoreIds != null) {
            if (!ingoreIds.contains(ids)) {
                ingoreIds.add(ids);
            }
        }
    }

    public void removeingoreIds(int ids) {
        if (ingoreIds != null) {
            if (ingoreIds.contains(ids)) {
                ingoreIds.remove(ids);
            }
        }
    }

    public void addIngoreIds(Set<Integer> map) {
        if (ingoreIds != null) {
            ingoreIds = map;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ingoreIds.clear();
    }

    public void clearFocusView() {
        if (mFocusView != null) {
            this.mFocusView.setBackgroundResource(R.color.color_translate);
        }
    }

    public void addFocusedId(int id) {
        focusedIds.add(id);
    }

    public void addFocusedId(Set<Integer> ids) {
        focusedIds.addAll(ids);
    }
}