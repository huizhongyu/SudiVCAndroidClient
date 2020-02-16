package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.DeviceSpinnerAdapter;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.widget.BasePopupWindow;

public class DeviceSpinnerPopup extends BasePopupWindow {
    private RecyclerView recycle_view;
    private DeviceSpinnerAdapter adapter;
    private List<String> list;
    private OnSpinnerItemClickListener onSpinnerItemClickListener;
    private int select;
    public DeviceSpinnerPopup(Context context, List<String> list, int select) {
        super(context);
        this.list = list;
        this.select = select;
        initRecycler();
    }

    private void initRecycler() {
        adapter = new DeviceSpinnerAdapter(mContext, list);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        recycle_view.setLayoutManager(manager);
        recycle_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener((string, postion) -> {
            if (onSpinnerItemClickListener != null) {
                onSpinnerItemClickListener.onItemClick(string,postion);
            }
        });

    }

    @Override
    public int thisLayout() {
        return R.layout.popup_device_spinner;
    }

    @Override
    public void doInitView() {
        recycle_view = fv(R.id.recycle_view);
    }

    @Override
    public void doInitData() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public OnSpinnerItemClickListener getOnSpinnerItemClickListener() {
        return onSpinnerItemClickListener;
    }

    public void setOnSpinnerItemClickListener(OnSpinnerItemClickListener onSpinnerItemClickListener) {
        this.onSpinnerItemClickListener = onSpinnerItemClickListener;
    }

    public interface OnSpinnerItemClickListener {
        void onItemClick(String string,int postion);
    }
}
