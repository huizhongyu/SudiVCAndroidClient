package cn.closeli.rtc.controller.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.MCUVideoActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.VideoConferenceActivity;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.controller.PreviewActivity;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.CLRtcSinaling;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomControl;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.popwindow.MeetSettingPopupWindow;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.constract.Constract.SETTING_CAMERA;
import static cn.closeli.rtc.constract.Constract.SETTING_MICRO;
import static cn.closeli.rtc.constract.Constract.SETTING_NONE;
import static cn.closeli.rtc.constract.Constract.SETTING_SHARE;
import static cn.closeli.rtc.constract.Constract.VALUE_STATUS_ON;
import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;

/**
 * 会控- 设置 - 参会者设置
 */
public class ParticipantSettingFragment extends Fragment implements CustomAdapt {
    private LinearLayout ll_join_camera;
    private TextView tv_camera_state;
    private TextView ll_join_micro;
//    private TextView tv_micro_state;
    private TextView ll_join_share;
//    private TextView tv_share_state;
    private LinearLayout ll_join_record;
    private TextView tv_record_state;
    private int[] allSettings = new int[]{SETTING_CAMERA, SETTING_MICRO, SETTING_SHARE};

    public ParticipantSettingFragment() {
        initRcv();
    }

    private MeetSettingPopupWindow popupWindow;

    private void initRcv() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participantsetting, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof VideoConferenceActivity) {
                RoomClient.get().getParticipants(RoomManager.get().getRoomId());
        } else if (getActivity() instanceof MCUVideoActivity){
            RoomClient.get().getParticipants(RoomManager.get().getRoomId());
            ((MCUVideoActivity) getActivity()).setCurrentType(SETTING_NONE);
        }
    }

    private void initView(View view) {
        ll_join_camera = view.findViewById(R.id.ll_join_camera);
        tv_camera_state = view.findViewById(R.id.tv_camera_state);
        ll_join_micro = view.findViewById(R.id.ll_join_micro);
//        tv_micro_state = view.findViewById(R.id.tv_micro_state);
        ll_join_share = view.findViewById(R.id.ll_join_share);
//        tv_share_state = view.findViewById(R.id.tv_share_state);
        ll_join_record = view.findViewById(R.id.ll_join_record);
        tv_record_state = view.findViewById(R.id.tv_record_state);

        ll_join_camera.setOnClickListener(v -> {
            if (getActivity() instanceof VideoConferenceActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            } else if (getActivity() instanceof MCUVideoActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            }
        });

        ll_join_micro.setOnClickListener(v -> {
            if (getActivity() instanceof VideoConferenceActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            }else if (getActivity() instanceof MCUVideoActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            }

        });

        ll_join_share.setOnClickListener(v -> {
            if (getActivity() instanceof VideoConferenceActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            }else if (getActivity() instanceof MCUVideoActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            }
        });

        ll_join_record.setOnClickListener(v -> {
            if (getActivity() instanceof VideoConferenceActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            }else if (getActivity() instanceof MCUVideoActivity) {
                RoomClient.get().getParticipants(String.valueOf(RoomManager.get().getRoomId()));
            }
        });
        initStatus();
    }

    public void initStatus() {
        List<ParticipantInfoModel> participantInfoModels = RoomManager.get().getParticipants();
        for (int s : allSettings) {
            int OffStatus = 0;
            int onStatus = 0;
            for (ParticipantInfoModel participantInfoModel : participantInfoModels) {
                if (participantInfoModel.getRole().equals(ROLE_PUBLISHER_SUBSCRIBER))
                    continue;
                if (s == SETTING_CAMERA) {
                    if (participantInfoModel.isVideoActive()) {
                        onStatus++;
                    } else {
                        OffStatus++;
                    }
                } else if (s == SETTING_MICRO) {
                    if (participantInfoModel.isAudioActive()) {
                        onStatus++;
                    } else {
                        OffStatus++;
                    }
                } else if (s == SETTING_SHARE) {
                    if (VALUE_STATUS_ON.equals(participantInfoModel.getShareStatus())) {
                        onStatus++;
                    } else {
                        OffStatus++;
                    }
                }

            }
            if (popupWindow != null) {
//                popupWindow.changeOnOffText(onStatus, OffStatus);
            }
        }
    }

    //变更状态文案
    public void changeText(int type, String text) {
        if (type == Constract.SETTING_CAMERA) {
            tv_camera_state.setText(text);
        } else if (type == Constract.SETTING_MICRO) {
            ll_join_micro.setText(text);
        } else if (type == Constract.SETTING_SHARE) {
            ll_join_share.setText(text);
        } else if (type == Constract.SETTING_RECORD) {
            tv_record_state.setText(text);
        }
    }


    /**
     * 是否按照宽度进行等比例适配 (为了保证在高宽比不同的屏幕上也能正常适配, 所以只能在宽度和高度之中选一个作为基准进行适配)
     *
     * @return {@code true} 为按照宽度适配, {@code false} 为按照高度适配
     */
    @Override
    public boolean isBaseOnWidth() {
        return false;
    }

    /**
     * 返回设计图上的设计尺寸, 单位 dp
     * {@link #getSizeInDp} 须配合 {@link #isBaseOnWidth()} 使用, 规则如下:
     * 如果 {@link #isBaseOnWidth()} 返回 {@code true}, {@link #getSizeInDp} 则应该返回设计图的总宽度
     * 如果 {@link #isBaseOnWidth()} 返回 {@code false}, {@link #getSizeInDp} 则应该返回设计图的总高度
     * 如果您不需要自定义设计图上的设计尺寸, 想继续使用在 AndroidManifest 中填写的设计图尺寸, {@link #getSizeInDp} 则返回 {@code 0}
     *
     * @return 设计图上的设计尺寸, 单位 dp
     */
    @Override
    public float getSizeInDp() {
        return 0;
    }

    public MeetSettingPopupWindow getPopupWindow() {
        return popupWindow;
    }

    public void setPopupWindow(MeetSettingPopupWindow popupWindow) {
        this.popupWindow = popupWindow;
    }
}
