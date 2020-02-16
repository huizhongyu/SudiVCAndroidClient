package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.MCUVideoActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.widget.BasePopupWindow;

public class LayoutChoosePopupWindow extends BasePopupWindow implements View.OnClickListener {
    private LayoutChoose layoutChoose;
    private TextView restoreDefault;
    private RecyclerView layouts;
    private View cancel;
    private List<LayoutItem> items = new ArrayList<>();
    private Adapter adapter;

    public LayoutChoosePopupWindow(Context context) {
        super(context);
    }


    @Override
    public int thisLayout() {
        return R.layout.pop_layout_choose;
    }

    @Override
    public void doInitView() {

        layouts = view.findViewById(R.id.layouts);
        adapter = new Adapter(items);
        layouts.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = 10;
                outRect.top = 10;
            }
        });
        layouts.setLayoutManager(new GridLayoutManager(mContext, 4, LinearLayoutManager.HORIZONTAL, true));
        layouts.setAdapter(adapter);
        restoreDefault = view.findViewById(R.id.restore_default);
        cancel = view.findViewById(R.id.cancel);

        restoreDefault.setOnClickListener(this);
        cancel.setOnClickListener(this);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            if (layoutChoose != null) {
                LayoutItem item = (LayoutItem) adapter1.getData().get(position);
                layoutChoose.layout(item.count, false);
            }
            dismiss();
        });
    }


    @Override
    public void doInitData() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        if (items == null) {
            items = new ArrayList<>();
        }
        adapter.setNewData(items);
        items.clear();
        items.add(new LayoutItem(1, "全屏布局", R.drawable.meeting_layout_pic_one));
        items.add(new LayoutItem(2, "1+1布局", R.drawable.meeting_layout_pic_two));
        items.add(new LayoutItem(3, "1+2布局", R.drawable.meeting_layout_pic_three));
        items.add(new LayoutItem(4, "1+3布局", R.drawable.meeting_layout_pic_four));
        items.add(new LayoutItem(5, "1+4布局", R.drawable.meeting_layout_pic_five));
        items.add(new LayoutItem(6, "1+5布局", R.drawable.meeting_layout_pic_six));
        items.add(new LayoutItem(7, "1+6布局", R.drawable.meeting_layout_pic_seven));
        items.add(new LayoutItem(8, "1+7布局", R.drawable.meeting_layout_pic_eight));
        items.add(new LayoutItem(9, "1+8布局", R.drawable.meeting_layout_pic_nine));
        if (mContext instanceof MCUVideoActivity) {
            items.add(new LayoutItem(10, "2+8布局", R.drawable.meeting_layout_pic_ten));
            items.add(new LayoutItem(11, "1+10布局", R.drawable.meeting_layout_pic_eleven));
            items.add(new LayoutItem(12, "1+1+10布局", R.drawable.meeting_layout_pic_twelve));
            items.add(new LayoutItem(13, "1+12布局", R.drawable.meeting_layout_pic_thirteen));
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.restore_default) {
            if (layoutChoose != null && adapter.getData().size() > 0) {
                layoutChoose.layout(adapter.getData().get(0).count, true);
            }
        }
        dismiss();
    }

    class Adapter extends BaseQuickAdapter<LayoutItem, BaseViewHolder> {
        private OnItemClickListener onItemClickListener;

        public Adapter(@Nullable List<LayoutItem> data) {
            super(R.layout.item_video_setting_layout, data);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder helper, LayoutItem item) {
            ImageView layoutImg = helper.itemView.findViewById(R.id.layoutImg);
            TextView layoutDes = helper.itemView.findViewById(R.id.layoutDes);
            layoutImg.setImageResource(item.imgId);
            layoutDes.setText(item.des);
            layoutImg.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(Adapter.this, helper.itemView, helper.getAdapterPosition());
                }
            });
        }

        @Override
        public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }
    }

    class LayoutItem {
        private int count;
        private String des;
        private int imgId;

        public LayoutItem(int count, String des, int imgId) {
            this.count = count;
            this.des = des;
            this.imgId = imgId;
        }
    }

    public interface LayoutChoose {
        void layout(int position, boolean b);
    }

    public LayoutChoose getLayoutChoose() {
        return layoutChoose;
    }

    public void setLayoutChoose(LayoutChoose layoutChoose) {
        this.layoutChoose = layoutChoose;
    }
}

