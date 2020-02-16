package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.closeli.rtc.App;
import cn.closeli.rtc.GroupActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.AccountListAdapter;
import cn.closeli.rtc.adapter.PartListAdapter;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.SudiRole;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.widget.BasePopupWindow;

import static cn.closeli.rtc.utils.Constants.STREAM_MAJOR;

public class AccountListPopup extends BasePopupWindow {

    private ArrayList<String> list = new ArrayList<>();
    private Set<String> set = new HashSet<>();
    private ImageView iv_account;
    private AccountListAdapter accountListAdapter;
    private OnRecycViewClick onRecycViewClick;
    public AccountListPopup(Context context, ArrayList<String> strings) {

        super(context);
        initData(strings);
    }

    private void initData(ArrayList<String> strings) {
        recyclerView.setLayoutManager(new LinearLayoutManager(App.getInstance()));
        accountListAdapter = new AccountListAdapter(mContext, new ArrayList<>());
        recyclerView.setAdapter(accountListAdapter);
        accountListAdapter.setDatas(strings);
        accountListAdapter.setOnItemClickListener(str -> {
            onRecycViewClick.OnRecycViewClick(str);
        });
    }

    public OnRecycViewClick getOnRecycViewClick() {
        return onRecycViewClick;
    }

    public void setOnRecycViewClick(OnRecycViewClick onRecycViewClick) {
        this.onRecycViewClick = onRecycViewClick;
    }

    private RecyclerView recyclerView;

    @Override
    public int thisLayout() {
        return R.layout.popup_account_list;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        iv_account = fv(R.id.iv_account);
        recyclerView = fv(R.id.recycle_view);
    }

    @Override
    public void doInitData() {


    }
    public interface OnRecycViewClick{
        void OnRecycViewClick(String str);
    }
}

