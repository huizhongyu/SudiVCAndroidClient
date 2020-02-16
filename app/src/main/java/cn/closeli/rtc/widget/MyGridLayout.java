package cn.closeli.rtc.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import cn.closeli.rtc.R;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class MyGridLayout extends ViewGroup {
    private final String TAG = "MyGridLayout";

    int margin = 2;// 每个格子的水平和垂直间隔
    int column = 2;
    private int mMaxChildWidth = 0;
    private int mMaxChildHeight = 0;
    int count = 0;

    GridAdatper adapter;

    public MyGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.MyGridLayout);
            column = a.getInteger(R.styleable.MyGridLayout_numColumns, 2);
            margin = a.getInteger(R.styleable.MyGridLayout_itemMargin, 20);
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

    public MyGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyGridLayout(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        mMaxChildWidth = 0;
        mMaxChildHeight = 0;

        int modeW = 0, modeH = 0;
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED)
            modeW = MeasureSpec.UNSPECIFIED;
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED)
            modeH = MeasureSpec.UNSPECIFIED;
//        Log.i("MyGridLayout", "widthMeasureSpec" + widthMeasureSpec + ",heightMeasureSpec" + heightMeasureSpec);
//        Log.i("MyGridLayout", "modeW" + modeW + ",modeH" + modeH);
        final int childWidthMeasureSpec = makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), modeW);
        final int childHeightMeasureSpec = makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), modeH);
//        Log.i("MyGridLayout", "childWidthMeasureSpec" + childWidthMeasureSpec + ",childHeightMeasureSpec" + childHeightMeasureSpec);
//        count = getChildCount();
        if (count == 0)
            count = getChildCount();
        if (count == 0) {
            super.onMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
            return;
        }
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child == null) {
                break;
            }

            if (child.getVisibility() == GONE) {
                continue;
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            Log.i("MyGridLayout", "MyGridLayout" + (child instanceof FrameLayout));
            Log.i("MyGridLayout", "mMaxChildWidthBefore" + child.getMeasuredWidth() + ",mMaxChildHeight" + child.getMeasuredHeight());
            mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
            mMaxChildHeight = Math.max(mMaxChildHeight,
                    child.getMeasuredHeight());
//            mMaxChildWidth = child.getMeasuredHeight();
//            mMaxChildHeight = child.getMeasuredHeight();
            Log.i("MyGridLayout", "mMaxChildWidth" + mMaxChildWidth + ",mMaxChildHeight" + mMaxChildHeight);
        }
        setMeasuredDimension(resolveSize(mMaxChildWidth, widthMeasureSpec),
                resolveSize(mMaxChildHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        int height = b - t;// 布局区域高度
        int width = r - l;// 布局区域宽度
        int rows = count % column == 0 ? count / column : count / column + 1;// 行数
        if (count == 0)
            return;
        int gridW = (width - margin * (column - 1)) / column;// 格子宽度
        int gridH = (height - margin * rows) / rows;// 格子高度

        int left = 0;
        int top = margin;

        for (int i = 0; i < rows; i++) {// 遍历行
            for (int j = 0; j < column; j++) {// 遍历每一行的元素
                View child = this.getChildAt(i * column + j);
                if (child == null)
                    return;
                left = j * gridW + j * margin;
                // 如果当前布局宽度和测量宽度不一样，就直接用当前布局的宽度重新测量
                System.out
                        .println("gridW -" + gridW + ",gridH -" + gridH);
//                if (gridW != child.getMeasuredWidth()
//                        || gridH != child.getMeasuredHeight()) {
//                    child.measure(makeMeasureSpec(gridW, EXACTLY),
//                            makeMeasureSpec(gridH, EXACTLY));
//                }
                child.layout(left, top, left + gridW, top + gridH);
                System.out
                        .println("--top--" + top + ",bottom=" + (top + gridH));

            }
            top += gridH + margin;
        }
    }

    public interface GridAdatper {
        View getView(int index);

        int getCount();
    }

    /**
     * 设置适配器
     */
    public void setGridAdapter(GridAdatper adapter) {
        this.adapter = adapter;
        // 动态添加视图
        int size = adapter.getCount();
        for (int i = 0; i < size; i++) {
            addView(adapter.getView(i));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int index);
    }

    public void setOnItemClickListener(final OnItemClickListener click) {
        if (this.adapter == null)
            return;
        for (int i = 0; i < adapter.getCount(); i++) {
            final int index = i;
            View view = getChildAt(i);
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    click.onItemClick(v, index);
                }
            });
        }
    }

}