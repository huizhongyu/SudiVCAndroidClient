package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.RoomControl;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.widget.BasePopupWindow;

public class VideoItemPopWindow extends BasePopupWindow implements View.OnClickListener, View.OnFocusChangeListener {
    private FrameLayout flZoomToScreen, flOpenMic, flOpenShareScreen, flRollCall;
    private TextView tvZoomToScreen;
    private TextView tvOpenMic;
    private TextView tvRollCall;
    private VideoItemPop videoItemPop;
    private ParticipantInfoModel participantInfoModel;
    private boolean isBySFU;
    public VideoItemPopWindow(Context context, @NonNull ParticipantInfoModel participantInfoModel) {
        super(context);
        this.participantInfoModel = participantInfoModel;
        this.isBySFU = isBySFU;
    }


    @Override
    public int thisLayout() {
        return R.layout.pop_video_item;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setAnimationStyle(R.style.PopupWindowDownAnimStyle);
        flZoomToScreen = view.findViewById(R.id.fl_zoom_to_screen);
        flOpenMic = view.findViewById(R.id.fl_open_mic);
        flOpenShareScreen = view.findViewById(R.id.fl_open_share_screen);
        flRollCall = view.findViewById(R.id.fl_roll_call);

        tvZoomToScreen = view.findViewById(R.id.tv_zoom_to_screen);
        tvOpenMic = view.findViewById(R.id.tv_open_mic);
        tvRollCall = view.findViewById(R.id.roll_call);

        flZoomToScreen.setOnFocusChangeListener(this);
        flOpenMic.setOnFocusChangeListener(this);
        flOpenShareScreen.setOnFocusChangeListener(this);
        flRollCall.setOnFocusChangeListener(this);

        flZoomToScreen.setOnClickListener(this);
        flOpenMic.setOnClickListener(this);
        flOpenShareScreen.setOnClickListener(this);
        flRollCall.setOnClickListener(this);

    }

    @Override
    public void doInitData() {

    }

    public void initData() {
        flRollCall.setVisibility(participantInfoModel.isHost() ? View.GONE : View.VISIBLE);
        flOpenMic.setVisibility(participantInfoModel.isHost() ? View.GONE : View.VISIBLE);
        switchMic(false);
        switchZoom(false);
        switchRollCall(false);
    }

    private void switchZoom(boolean focus) {
        tvZoomToScreen.setTextColor(ContextCompat.getColor(mContext, focus ? R.color.white : R.color.black));
        Drawable zoomDrawable = ContextCompat.getDrawable(mContext, focus ? R.drawable.maininterface_tab_icon_enlarge_chose : R.drawable.maininterface_tab_icon_enlarge);
        if (zoomDrawable != null) {
            zoomDrawable.setBounds(0, 0, zoomDrawable.getMinimumWidth(), zoomDrawable.getMinimumHeight());
            tvZoomToScreen.setCompoundDrawables(zoomDrawable, null, null, null);
        }
    }

    private void switchMic(boolean focus) {
        tvOpenMic.setText(mContext.getString(participantInfoModel.isAudioActive() ? R.string.close_micro : R.string.open_mic));
        tvOpenMic.setTextColor(ContextCompat.getColor(mContext, focus ? R.color.white : R.color.black));
        Drawable micDrawable = ContextCompat.getDrawable(mContext, focus ? (participantInfoModel.isAudioActive() ? R.drawable.maininterface_tab_icon_microphone_chose :
                R.drawable.maininterface_tab_icon_microphone_chose_unuse) : (participantInfoModel.isAudioActive() ? R.drawable.maininterface_tab_icon_microphone_new
                : R.drawable.maininterface_tab_icon_microphone_unuse));
        if (micDrawable != null) {
            micDrawable.setBounds(0, 0, micDrawable.getMinimumWidth(), micDrawable.getMinimumHeight());
            tvOpenMic.setCompoundDrawables(micDrawable, null, null, null);
        }
    }

    private void switchRollCall(boolean focus) {
        tvRollCall.setTextColor(ContextCompat.getColor(mContext, focus ? R.color.white : R.color.black));
        tvRollCall.setText(participantInfoModel.isHandSpeaker() ? "取消发言" : "点名发言");
        Drawable zoomDrawable = ContextCompat.getDrawable(mContext, focus ? R.drawable.maininterface_tab_icon_hand_white : R.drawable.roll_call);
        if (zoomDrawable != null) {
            zoomDrawable.setBounds(0, 0, zoomDrawable.getMinimumWidth(), zoomDrawable.getMinimumHeight());
            tvRollCall.setCompoundDrawables(zoomDrawable, null, null, null);
        }
    }

    @Override
    public void onClick(View v) {
        if (videoItemPop == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.fl_zoom_to_screen) {
            videoItemPop.zoom2Screen(participantInfoModel.getConnectionId());
        } else if (id == R.id.fl_open_mic) {
            RoomControl.get().switchAudio(participantInfoModel.isAudioActive(), Collections.singletonList(participantInfoModel.getUserId()));
        } else if (id == R.id.fl_roll_call) {
            if (participantInfoModel.isHandSpeaker()) {
                RoomClient.get().endRollCall(RoomManager.get().getRoomId(), RoomManager.get().getUserId(), participantInfoModel.getUserId());
            } else {
                RoomClient.get().rollCall(RoomManager.get().getRoomId(), RoomManager.get().getUserId(), participantInfoModel.getUserId());
            }
        }
        dismiss();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.fl_open_mic) {
            switchMic(true);
            switchZoom(false);
            switchRollCall(false);
        } else if (v.getId() == R.id.fl_zoom_to_screen) {
            switchMic(false);
            switchZoom(true);
            switchRollCall(false);
        } else if (v.getId() == R.id.fl_roll_call) {
            switchMic(false);
            switchZoom(false);
            switchRollCall(true);
        }
    }

    public interface VideoItemPop {
        /**
         * 放大到全屏
         *
         * @param userId
         */
        void zoom2Screen(String userId);

    }

    public VideoItemPop getVideoItemPop() {
        return videoItemPop;
    }

    public void setVideoItemPop(VideoItemPop videoItemPop) {
        this.videoItemPop = videoItemPop;
    }
}
