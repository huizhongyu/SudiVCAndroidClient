package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.App;
import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.PartListAdapter;
import cn.closeli.rtc.model.ParticipantModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.CLRtcSinaling;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.widget.BasePopupWindow;

import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;

public class ParticipantPopWindow extends BasePopupWindow implements View.OnClickListener {
    View lineview;
    private RecyclerView recyclerView;
    private TextView tv_theme;
    private TextView tv_num;
    private TextView tv_meet_id;
    private TextView tv_meet_during;
    private TextView tv_meet_location;

    private String channel;
    private PartListAdapter partListAdapter;
    private List<ParticipantInfoModel> participantModels;
    private boolean isHost;

    public ParticipantPopWindow(Context context, String channel,boolean isHost) {
        super(context);
        this.channel = channel;
        this.isHost = isHost;
        initData();


    }


    @Override
    public int thisLayout() {
        return R.layout.pop_participant;
    }

    @Override
    public void doInitView() {

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
//        this.setAnimationStyle(R.style.PopupWindowBottomAnimation);
        recyclerView = view.findViewById(R.id.recycle_view);

        tv_theme = fv(R.id.tv_theme);
        tv_num = fv(R.id.tv_num);
        tv_meet_id = fv(R.id.tv_meet_id);
    }


    @Override
    public void doInitData() {
//        this.setAnimationStyle(R.style.PopupWindowBottomAnimation);
        tv_theme.setText("当前会议:"+RoomManager.get().getSubject());
        tv_num.setText("参会人数:"+RoomManager.get().getRoomCapacity()+"人");
        tv_meet_id.setText("会议ID:"+RoomManager.get().getRoomId());
    }


    private void initData() {
        recyclerView.setLayoutManager(new LinearLayoutManager(App.getInstance()));
        partListAdapter = new PartListAdapter(mContext, new ArrayList<>(), channel);
        recyclerView.setAdapter(partListAdapter);
        RoomClient.get().getParticipants(channel);
    }

    public void setData(List<ParticipantInfoModel> participantModels) {
        this.participantModels = participantModels;
        L.d("do this ---->>>>"+participantModels.size());
        //todo 测试代码 20191015
//        List<ParticipantInfoModel> list = new ArrayList<>();
//        for (int i=0;i<10;i++) {
//            ParticipantInfoModel model = new ParticipantInfoModel("1");
//            model.setAccount("乔巴");
//            model.setAudioActive(true);
//            model.setHandStatus(CLRtcSinaling.VALUE_STATUS_UP);
//            model.setRole(ROLE_PUBLISHER_SUBSCRIBER);
//            model.setUserId("1002");
//            model.setVideoActive(true);
//            list.add(model);
//        }
        partListAdapter.setDatas(participantModels);
    }

    public void setAudioStatus(String targetId, boolean status) {
        for (ParticipantInfoModel participantModel : participantModels) {
            if (RoomManager.get().isAllMute(targetId) || participantModel.getUserId().equals(targetId)) {
                participantModel.setAudioActive(status ? CLRtcSinaling.ACTIVE_TRUE : CLRtcSinaling.ACTIVE_FALSE);
            }
        }
        setData(participantModels);
    }
    public void setPutDown(String targetId) {
        for (ParticipantInfoModel participantModel : participantModels) {
            if (participantModel.getUserId().equals(targetId)) {
                participantModel.setHandStatus("off");
            }
        }
        setData(participantModels);
    }

    public void setRaise(String targetId) {
        for (ParticipantInfoModel participantModel : participantModels) {
            if (participantModel.getUserId().equals(targetId)) {
                participantModel.setHandStatus("on");
            }
        }
        setData(participantModels);
    }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_audience) {
        }
    }
}
