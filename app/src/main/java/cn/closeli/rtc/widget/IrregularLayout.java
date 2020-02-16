package cn.closeli.rtc.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.closeli.rtc.App;
import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.info.ParticipantInfoModel;

import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;

public class IrregularLayout extends FrameLayout {

    private ConstraintLayout cl;
    private ImageView iv_speaker, iv_camera, iv_mic, iv_share, iv_chat, iv_see;
    private TextView tv_name, tv_position, tv_role;
    private View view;

    private int seatIndex;
    private boolean isPlay;
    private int[] location = new int[2];
    private boolean isSee = true;
    private ParticipantInfoModel participantInfoModel;

    public IrregularLayout(Context context) {
        this(context, null);
    }

    public IrregularLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        view = LayoutInflater.from(App.getInstance()).inflate(R.layout.layout_particaipant, this);
        iv_speaker = view.findViewById(R.id.iv_speaker);
        iv_camera = view.findViewById(R.id.iv_camera);
        iv_mic = view.findViewById(R.id.iv_mic);
        iv_share = view.findViewById(R.id.iv_share);
        iv_chat = view.findViewById(R.id.iv_chat);
        tv_name = view.findViewById(R.id.tv_name);
        tv_position = view.findViewById(R.id.tv_position);
        tv_role = view.findViewById(R.id.tv_role);
        cl = view.findViewById(R.id.cl_tab);
        iv_see = view.findViewById(R.id.iv_see);
        iv_see.setOnClickListener((v -> {
            isSee = !isSee;

            cl.setVisibility(isSee ? VISIBLE : GONE);
        }));
    }

    public void setParticipant(ParticipantInfoModel model) {
        this.participantInfoModel = model;
        if (model.getRole().equals(ROLE_PUBLISHER_SUBSCRIBER)) {
            tv_role.setText("主持人");
        } else {
            tv_role.setText("与会者");
        }
        tv_name.setText(model.getAppShowName());
        tv_position.setText(model.getAppShowDesc());
        iv_mic.setBackgroundResource(model.isAudioActive() ? R.drawable.maininterface_tab_icon_microphone : R.drawable.maininterface_tab_icon_microphone_unuse_small);
        iv_camera.setBackgroundResource(model.isVideoActive() ? R.drawable.maininterface_tab_icon_camera : R.drawable.maininterface_tab_icon_camera_unuse_small);
        iv_speaker.setBackgroundResource(model.isSpeakerActive() ? R.drawable.maininterface_tab_icon_speaker : R.drawable.maininterface_tab_icon_speaker_unuse_small);
        iv_share.setBackgroundResource(Constract.VALUE_STATUS_ON.equals(model.getShareStatus()) ? R.drawable.maininterface_tab_icon_share : R.drawable.maininterface_tab_icon_share_unuse_small);
    }

    public ParticipantInfoModel getParticipantInfoModel() {
        return participantInfoModel;
    }

}
