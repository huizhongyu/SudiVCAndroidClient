package cn.closeli.rtc.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.R;

public class VideoLayout extends FrameLayout {

    private final int MARGIN = -50;
    private int row;
    private int column;
    private int count;
    private List<Rect> childRect = new ArrayList<>();


    public VideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public VideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.VideoLayout);
            row = typedArray.getInt(R.styleable.VideoLayout_row, -1);
            column = typedArray.getInt(R.styleable.VideoLayout_column, -1);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (count < 0 || column < 0) {
            return;
        }
        row = count / column;
        childRect.clear();
        int pWidth = MeasureSpec.getSize(widthMeasureSpec);
        int pHeight = MeasureSpec.getSize(heightMeasureSpec);
        int itemWidth = (pWidth - MARGIN * (column - 1)) / column;
        int itemHeight = (pHeight - MARGIN * (row - 1)) / row;
        for (int i = 0; i < row * column; i++) {
            int line = i / column;
            int position = i % column;
            int left = position * (MARGIN + itemWidth);
            int top = line * (MARGIN + itemHeight);
            int right = left + itemWidth;
            int bottom = top + itemHeight;
            childRect.add(new Rect(left, top, right, bottom));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            if (i >= childRect.size()) {
                break;
            }
            Rect rect = childRect.get(i);
            View child = getChildAt(i);
            int measuredWidth = child.getMeasuredWidth();
            int measuredHeight = child.getMeasuredHeight();
            int _left = rect.left + (rect.width() - measuredWidth) / 2;
            int _top = rect.top + (rect.height() - measuredHeight) / 2;
            int _right = _left + measuredWidth;
            int _bottom = _top + measuredHeight;
            child.layout(_left, _top, _right, _bottom);
        }
    }
}
