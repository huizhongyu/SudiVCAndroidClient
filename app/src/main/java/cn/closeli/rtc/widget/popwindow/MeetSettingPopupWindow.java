package cn.closeli.rtc.widget.popwindow;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.PersonSettingAdapter;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.BasePopupWindow;

/**
 * 会议-侧边栏 设置 弹窗
 * @author Administrator
 */
public class MeetSettingPopupWindow extends BasePopupWindow implements
        PersonSettingAdapter.OnItemClickListener, View.OnClickListener {
    private LinearLayout root;
    private LinearLayout constraintJoinSecond;
    private LinearLayout checkContainer;
    private CheckBox cbOpenAll;
    private CheckBox cbCloseAll;
    private TextView tvStatusNum;
    private RecyclerView rcvList;
    private View divide;


    private boolean manageMicro = true;
    private List<ParticipantInfoModel> list = null;
    private PersonSettingAdapter adapter;

    public MeetSettingPopupWindow(Activity activity) {
        super(activity);
        //是否是主持人，主持人显示2个tab ， 非主持人1个
        initRecycler();
        initData();
        RoomClient.get().getParticipants(RoomClient.get().getRoomId());
    }

    private void initData() {
        List<ParticipantInfoModel> models = RoomManager.get().getFilterParticipants(RoomClient.get().getRemoteParticipants());
        //过滤掉主持人
        List<ParticipantInfoModel> participantInfoModels = filterHost(models);
        if (participantInfoModels == null){
            return;
        }
        list.clear();
        list.addAll(participantInfoModels);
        adapter.notifyDataSetChanged();
        changeOnOffText();
    }

    private List<ParticipantInfoModel> filterHost(List<ParticipantInfoModel> models){
        if (models == null) {
            return null;
        }
        List<ParticipantInfoModel> temps = new ArrayList<>();
        String userId = RoomManager.get().getUserId();
        for (int i = models.size() - 1; i >= 0; i--) {
            ParticipantInfoModel model = models.get(i);
            if (!TextUtils.isEmpty(userId) && !userId.equals(model.getUserId())) {
                temps.add(model);
            }
        }
        return temps;
    }

    private void initRecycler() {
        if (list == null) {
            list = new ArrayList<>();
        }
        adapter = new PersonSettingAdapter(mContext, list, manageMicro);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        rcvList.setLayoutManager(manager);
        rcvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(this);
    }


    @Override
    public int thisLayout() {
        return R.layout.popup_meetsetting;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        root = fv(R.id.root);
        constraintJoinSecond = fv(R.id.constraint_join_second);
        cbOpenAll = fv(R.id.cb_open_all);
        cbCloseAll = fv(R.id.cb_close_all);
        tvStatusNum = fv(R.id.tv_status_num);
        rcvList = fv(R.id.rcv_list);
        checkContainer = fv(R.id.check_container);
        divide = fv(R.id.divide);

        fv(R.id.ll_join_micro).setOnClickListener(this);
        fv(R.id.ll_join_share).setOnClickListener(this);
    }

    @Override
    public void doInitData() {
        cbOpenAll.setOnClickListener(this);
        cbCloseAll.setOnClickListener(this);
    }

    public void showNextPopup(List<ParticipantInfoModel> list) {
        List<ParticipantInfoModel> participantInfoModels = filterHost(list);
        if (participantInfoModels == null){
            return;
        }
        RoomManager.get().filterHost(list);
        adapter.changeData(list);
    }

    /**
     * 入会者设置 - 摄像头，麦克风，共享屏幕，录屏
     *
     * @param position
     */
    @Override
    public void onItemClick(int position, boolean isChecked) {
        boolean isCheckSuccess; //操作成功标志
        L.d("do this isChecked --->>>" + isChecked);
        isCheckSuccess = !isChecked;
        ParticipantInfoModel model = list.get(position);
        if (manageMicro) {
            model.setAudioActive(isCheckSuccess);
        } else {
            model.setShareStatus(isCheckSuccess ? "on" : "off");
        }
        changeOnOffText();
        requestSingleData(model.getUserId(), isCheckSuccess);
        adapter.notifyItemChanged(position);
    }

    /**
     * 变更 开启/关闭 文案
     */
    private void changeOnOffText() {
        int onNum = 0;

        for (ParticipantInfoModel model : list) {
            onNum += manageMicro ? (model.isAudioActive() ? 1 : 0) : (model.isShareable() ? 1 : 0);
        }
        String str = (manageMicro ? "麦克风:" : "共享屏幕:  ") + onNum + "人开启/" + (list.size() - onNum) + "人关闭";
        tvStatusNum.setText(str);
        cbOpenAll.setChecked(onNum == list.size());
        cbCloseAll.setChecked(onNum == 0);
        checkContainer.setVisibility(manageMicro ? View.VISIBLE : View.GONE);
    }

    /**
     * 操作数据源，全体开启 or 关闭
     *
     * @Params type : 当前页面类型：camera，micro...
     */
    private void setAllDataStatus(boolean isAllOpen) {
        for (ParticipantInfoModel model : list) {
            if (manageMicro) {
                model.setAudioActive(isAllOpen);
            } else {
                model.setShareStatus(isAllOpen ? "on" : "off");
            }
        }
        requestData(isAllOpen);
        adapter.notifyDataSetChanged();
    }

    /**
     * Called when a view has been clicked.
     * 只有cb 选中的时候 才与后台 交互
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_comfirm) {
            //交互
//            if (lastMineIndex != -1) {
//                RoomClient.get().transferModerator(RoomManager.get().getRoomId(), SPEditor.instance().getUserId(), list.get(lastMineIndex).getUserId());
//            }
            dismiss();
        } else if (v.getId() == R.id.btn_cancel) {
            dismiss();
        } else if (v.getId() == R.id.cb_open_all) {
            cbOpenAll.setChecked(cbOpenAll.isChecked());
            cbCloseAll.setChecked(false);
            if (cbOpenAll.isChecked()) {
                setAllDataStatus(true);
            }
        } else if (v.getId() == R.id.cb_close_all) {
            cbCloseAll.setChecked(cbCloseAll.isChecked());
            cbOpenAll.setChecked(false);
            if (cbCloseAll.isChecked()) {
                setAllDataStatus(false);
            }
        } else if (v.getId() == R.id.ll_join_micro) {
            divide.setVisibility(View.VISIBLE);
            constraintJoinSecond.setVisibility(View.VISIBLE);
            switchManage(true);
        } else if (v.getId() == R.id.ll_join_share) {
            divide.setVisibility(View.VISIBLE);
            constraintJoinSecond.setVisibility(View.VISIBLE);
            switchManage(false);
        }
    }

    private void switchManage(boolean manageMicro) {
        this.manageMicro = manageMicro;
        changeOnOffText();
        adapter.refreshData(manageMicro);
    }

    //根据当前page 与后台交互

    private void requestSingleData(String userId, boolean open) {
        //修改文本显示 和选中状态
        changeOnOffText();
        if (manageMicro) {
            RoomClient.get().setAudioStatus(RoomManager.get().getRoomId(), SPEditor.instance().getUserId(), userId, open);
        } else {
            RoomClient.get().setSharePower(RoomManager.get().getRoomId(), SPEditor.instance().getUserId(), userId);
        }
    }

    private void requestData(boolean isAllOpen) {
        changeOnOffText();
        String hostId = RoomManager.get().getHostId();
        List<String> userIds = new ArrayList<>();
        for (ParticipantInfoModel model : list) {
            if (model.getUserId().equals(hostId)) {
                continue;
            }
            userIds.add(model.getUserId());
        }
        if (manageMicro) {
            RoomClient.get().setAudioStatus(RoomManager.get().getRoomId(), SPEditor.instance().getUserId(), userIds, isAllOpen);
        }
    }
}
