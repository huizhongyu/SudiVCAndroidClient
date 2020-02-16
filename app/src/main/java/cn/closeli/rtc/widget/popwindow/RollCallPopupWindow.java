package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.RollCallAdapter;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.ParticipantJoinedModel;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.CLRtcSinaling;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.widget.BasePopupWindow;
import cn.closeli.rtc.widget.UCToast;

import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;

//点名发言 弹窗 （带确认取消按钮）
public class RollCallPopupWindow extends BasePopupWindow implements RollCallAdapter.OnItemClickListener {
    private RecyclerView rcv_list;
    private Button btn_comfirm;
    private Button btn_cancel;

    private RollCallAdapter adapter;
    private ArrayList<ParticipantInfoModel> list;

    private int lastIndex = -1;        //上次点击的index
    private String lastStatus = Constract.ROLLCALL_STATUS_UP;
    private String channel = "";
    private ParticipantInfoModel partcipantInfoModel ;
    private int currentStatus; // 0，允许，1,替换，2结束;
    private boolean isHasSpeak;
    private String currentSpeakUserId;      //当前发言人的id
    public RollCallPopupWindow(Context context, String channel) {

        super(context);
        this.channel = channel;
        RoomClient.get().getParticipants(channel);
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_rollcall;
    }

    @Override
        public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        rcv_list = fv(R.id.rcv_list);
        btn_comfirm = fv(R.id.btn_comfirm);
        btn_cancel = fv(R.id.btn_cancel);

    }

    @Override
    public void doInitData() {
        list = new ArrayList<>();
//        for (int i=0;i<10;i++) {
//                PartcipantInfoModel model = new PartcipantInfoModel("10086");
//                model.setAvatar("http://b.hiphotos.baidu.com/image/pic/item/908fa0ec08fa513db777cf78376d55fbb3fbd9b3.jpg");
//                model.setAccount("张三 ");
//                model.setPost("(Android开发)上海技术部");
//                model.setChecked(false);
//                model.setHandStatus(Constract.ROLLCALL_STATUS_UP);
//                if (i == 0){
//                    model.setHandStatus(Constract.ROLLCALL_STATUS_SPEAKER);
//                }
//                list.add(model);
//        }

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        adapter = new RollCallAdapter(mContext, list);
        rcv_list.setLayoutManager(manager);
        rcv_list.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        btn_comfirm.setOnClickListener((v) -> {
            if(partcipantInfoModel == null){

            }else {
                if(currentStatus == 0) {
                    RoomClient.get().rollCall(channel, RoomManager.get().getUserId(), partcipantInfoModel.getUserId());
                }else  if(currentStatus == 2) {
                    RoomClient.get().endRollCall(channel, RoomManager.get().getUserId(), partcipantInfoModel.getUserId());
                } else if (currentStatus == 1) {        //替换
//                    RoomClient.get().replaceRollCall(channel,RoomManager.get().getUserId(),currentSpeakUserId,partcipantInfoModel.getUserId());
                    UCToast.show(mContext,"请先结束发言");
                }
            }
            dismiss();
        });

        btn_cancel.setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    public void OnItemClick(int position) {
        L.d("do this --->>> isHaveSpeaker "+isHasSpeak);
        String status = Constract.ROLLCALL_STATUS_DOWN;
        if (position == lastIndex) {    //点击了已选择的

        } else {
            status = list.get(position).getHandStatus();
            list.get(position).setChecked(true);
            if (lastIndex != -1) {
                list.get(lastIndex).setChecked(false);
                list.get(lastIndex).setHandStatus(lastStatus);
            }
            adapter.notifyItemChanged(position);
            adapter.notifyItemChanged(lastIndex);
            lastIndex = position;
            lastStatus = status;
            partcipantInfoModel = list.get(position);
            if(status.equals(Constract.ROLLCALL_STATUS_SPEAKER)){
                currentStatus = 2;
                btn_comfirm.setText("结束");
            }else if(status.equals(Constract.ROLLCALL_STATUS_UP) && isHasSpeak){
                currentStatus = 1;
                btn_comfirm.setText("替换");
            }else {
                currentStatus = 0;
                btn_comfirm.setText("允许");
            }
        }

    }

    public void setData(ArrayList<ParticipantInfoModel> participantModels) {
        this.list = participantModels;
        isHaveSpeak(list);
        //todo 测试代码 20191015
//        List<PartcipantInfoModel> list = new ArrayList<>();
//        for (int i=0;i<10;i++) {
//            PartcipantInfoModel model = new PartcipantInfoModel("1");
//            model.setAccount("乔巴");
//            model.setAudioActive(true);
//            model.setHandStatus(Constract.ROLLCALL_STATUS_UP);
//            model.setRole(ROLE_PUBLISHER_SUBSCRIBER);
//            model.setUserId("1002");
//            model.setVideoActive(true);
//            list.add(model);
//        }
        adapter.setDatas(list);
    }

    private void isHaveSpeak(ArrayList<ParticipantInfoModel> participantModels) {
        for (ParticipantInfoModel infoModel : participantModels) {
            if (Constract.ROLLCALL_STATUS_SPEAKER.equals(infoModel.getHandStatus())) {          //发言状态
                currentSpeakUserId = infoModel.getUserId();
                isHasSpeak = true;
                break;
            } else {
                isHasSpeak = false;
            }
        }
    }

    public void initCheckStatus() {
        if (lastIndex != -1) {
            list.get(lastIndex).setChecked(false);
        }
    }

    //根据当前状态判断下一步的状态
//    private String getNextStatus(String statusCode,boolean isChecked) {
//        String status = Constract.ROLLCALL_STATUS_DOWN;
//        if (Constract.ROLLCALL_STATUS_UP.equals(statusCode) && !isChecked) {                //举手状态
//            status = CO;
//        } else if (code == 1) {         //举手选中
//            statusCode = 2;
//        } else if (code == 2) {          //发言中
//            statusCode = 3;
//        }
//        return statusCode;
//    }
}
