package cn.closeli.rtc.widget;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.closeli.rtc.App;
import cn.closeli.rtc.BaseActivity;
import cn.closeli.rtc.MCUVideoActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.GroupListInfoModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.model.ws.HandsStatusResp;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.MenuManager;
import cn.closeli.rtc.utils.RoomControl;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.widget.popwindow.HostInfoPopupWindow;
import cn.closeli.rtc.widget.popwindow.InvitePersonPopupWindow;
import cn.closeli.rtc.widget.popwindow.JoinMemberPopWindow;
import cn.closeli.rtc.widget.popwindow.LayoutChoosePopupWindow;
import cn.closeli.rtc.widget.popwindow.ParticipantPopWindow;
import cn.closeli.rtc.widget.popwindow.RollCallPopupWindow;
import cn.closeli.rtc.widget.popwindow.TimeDownPopupWindow;

public class VideoController extends FrameLayout implements View.OnClickListener, LifecycleObserver {
    private final String MCU_MODE = "MCU";
    private FrameLayout cl, flRollName;
    private TextView tvRoomTitle;
    private TextView tvRoomId;
    private TextView tvRoomPerson;
    private TextView tvDot;
    private TextView raiseHand;
    private TextView audioEnable;
    private TextView audioSpeaker;
    private TextView videoEnable;
    private TextView tvHost;
    private TextView rollName;
    private TextView invitation;
    private TextView shareHDMI;
    private TextView roomSetting;
    private TextView splitScreen;
    private TextView joinNum;
    private TextView costTime;
    /*本地基本属性初始化*/
    private boolean enabledAudio = true;
    private boolean enabledVideo = true;
    private boolean enabledSpeaker = true;

    private WebRTCManager webRTCManager;
    private int currentHandUpNum = 0;        //当前举手人数
    private int currentSpeakNum = 0;         //当前发言人数
    private RollCallPopupWindow rollCallPopupWindow;
    private InvitePersonPopupWindow invitePersonPopupWindow;
    private ParticipantPopWindow participantPopWindow;
    private TimeDownPopupWindow popupWindow;
    private String channelId;
    private String userId;
    private BaseActivity baseActivity;
    private VideoPlayerControl mVideo;
    private View rightMenu;
    private View bottomMenu;
    private View divideView;
    private MenuManager menuManager;
    private int videoMode = Constract.VIDEO_MODE_MCU;
    private LayoutChoosePopupWindow layoutChoosePopupWindow;

    public VideoController(Context context) {
        this(context, null);
    }

    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(App.getInstance()).inflate(R.layout.layout_room_control, this);
        roomSetting = view.findViewById(R.id.room_setting);
        cl = view.findViewById(R.id.cl_content);
        splitScreen = view.findViewById(R.id.splitScreen);
        tvHost = view.findViewById(R.id.tv_host);
        LinearLayout hangup = view.findViewById(R.id.hangup);
        shareHDMI = view.findViewById(R.id.shareHDMI);
        invitation = view.findViewById(R.id.invitation);
        rollName = view.findViewById(R.id.roll_name);
        flRollName = view.findViewById(R.id.fl_roll_name);
        tvRoomTitle = view.findViewById(R.id.tv_room_title);
        tvRoomId = view.findViewById(R.id.tv_room_id);
        tvRoomPerson = view.findViewById(R.id.tv_room_person);
        tvDot = view.findViewById(R.id.tv_dot);
        raiseHand = view.findViewById(R.id.raise_hand);
        audioEnable = view.findViewById(R.id.audio_enable);
        audioSpeaker = view.findViewById(R.id.audio_speaker);
        videoEnable = view.findViewById(R.id.video_enable);
        cl = view.findViewById(R.id.cl_content);
        LinearLayout rl_info = view.findViewById(R.id.rl_info);
        rightMenu = view.findViewById(R.id.linearLayoutRight);
        bottomMenu = view.findViewById(R.id.linearLayout1);
        joinNum = view.findViewById(R.id.join_num);
        costTime = view.findViewById(R.id.cost_time);
        hangup.setNextFocusLeftId(R.id.tv_room_person);
        divideView = findViewById(R.id.divide_view);
        if (SPEditor.instance().getUserId().equals(RoomManager.get().getHostId())) {
            flRollName.setNextFocusRightId(R.id.tv_room_person);
        } else {
            shareHDMI.setNextFocusRightId(R.id.tv_room_person);
        }

        rl_info.setOnClickListener(this);
        roomSetting.setOnClickListener(this);
        splitScreen.setOnClickListener(this);
        raiseHand.setOnClickListener(this);
        shareHDMI.setOnClickListener(this);
        invitation.setOnClickListener(this);
        rollName.setOnClickListener(this);
        tvRoomPerson.setOnClickListener(this);
        audioSpeaker.setOnClickListener(this);
        videoEnable.setOnClickListener(this);
        audioEnable.setOnClickListener(this);
        hangup.setOnClickListener(this);
        joinNum.setOnClickListener(this);
        menuManager = new MenuManager();
        menuManager.registerMain(this);
        menuManager.registerRightMenu(rightMenu);
        menuManager.registerBottomMenu(bottomMenu);
        menuManager.startSchedule();

        tvRoomPerson.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP){
                    scheduleHide();
                }
                return false;
            }
        });
    }

    public void initBasis() {
        webRTCManager = WebRTCManager.get();
    }

    public void initData() {
        channelId = RoomManager.get().getRoomId();
        userId = RoomManager.get().getUserId();
    }

    public void setRoleViewStatus() {
        if (!RoomManager.get().getHostId().equals(RoomManager.get().getUserId())) {
            roomSetting.setVisibility(View.GONE);
            raiseHand.setVisibility(View.VISIBLE);
        } else {
            roomSetting.setVisibility(View.VISIBLE);
            raiseHand.setVisibility(View.GONE);
        }
        //不是主持人,普通与会者, 不能锁定会议
        if (!RoomManager.get().getHostId().equals(RoomManager.get().getUserId())) {
            flRollName.setVisibility(View.GONE);
            invitation.setVisibility(View.GONE);
            roomSetting.setVisibility(View.GONE);
            splitScreen.setVisibility(View.GONE);
        } else {//主持人不必举手,
//            hangup.setText("结束");
        }
        tvDot.setVisibility(View.GONE);
    }

    public void setHDMIStatus(int status) {
        shareHDMI.setVisibility(status);
    }

    public void setViewData(String channelId) {
        tvRoomId.setText(String.format(Locale.getDefault(), "会议ID:%s", channelId));
        tvRoomTitle.setText(String.format(Locale.getDefault(), "会议名称:%s", RoomManager.get().getSubject()));
        tvRoomPerson.setText(String.format(Locale.getDefault(), "参会者:%d/%d", RoomManager.get().getFilterParticipants(RoomClient.get().getRemoteParticipants()).size(), RoomManager.get().getRoomCapacity()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.room_setting:
                if (mVideo != null) {
                    showMenu();
                    mVideo.roomSettingControl(divideView);
                }
                break;
            case R.id.splitScreen:
                showMenu();
                layoutChange();
                break;
            case R.id.rl_info:
                showMenu();
                hostPop();
                break;
            case R.id.audio_speaker:
                switchSpeaker();
                break;
            case R.id.video_enable:
                switchVideo();
                break;
            case R.id.audio_enable:
                if (RoomManager.get().isHostByClose() && !RoomManager.get().isRoomAllowMic()) {
                    UIUtils.toastMessage("您已被主持人静音");
                    return;
                }
                switchAudio();
                break;
            case R.id.raise_hand:
                if (MCU_MODE.equals(RoomManager.get().getConferenceMode())){
                    showMenu();
                }
                raiseHand();
                break;
            case R.id.shareHDMI:
                if (mVideo != null) {
                    if (MCU_MODE.equals(RoomManager.get().getConferenceMode())){
                        showMenu();
                    }
                    mVideo.shareControl(shareHDMI);
                }
                break;
            case R.id.invitation:
                invitationJoin();
                break;
            case R.id.roll_name:
                showMenu();
                waitingSpeaking();
                break;
            case R.id.tv_room_person:
                showMenu();
                showQueuePop();
                break;
            case R.id.hangup:
                if (mVideo != null) {
                    mVideo.roomClosed();
                }
                break;
            case R.id.join_num:
                showMenu();
                showJoinNum();
                break;
            default:
        }
    }

    private void showJoinNum() {
        List<ParticipantInfoModel> participants = RoomManager.get().getFilterParticipants(RoomClient.get().getRemoteParticipants());
        JoinMemberPopWindow popWindow = new JoinMemberPopWindow(baseActivity, participants);
        popWindow.showAsLeft(divideView);
        menuManager.registerWindows(popupWindow);
        CallUtil.asyncCall(400, () -> {
            reset();
        });
    }

    /**
     * @param enabled
     */
    public void audioStatus(boolean enabled) {
        enabledAudio = enabled;
        if (enabled) {
            audioEnable.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.meet_tab_menu_icon_mai, null), null, null);
        } else {
            audioEnable.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.meet_tab_menu_icon_mai_unuse, null), null, null);
        }
        webRTCManager.enableLocalAudio(enabled);
    }

    public void speakerStatus(boolean enabled) {
        enabledSpeaker = enabled;
        if (enabled) {
            audioSpeaker.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.meet_tab_menu_icon_voice, null), null, null);
        } else {
            audioSpeaker.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.meet_tab_menu_icon_voice_unuse, null), null, null);

        }
        webRTCManager.enableLocalSpeaker(enabled);
    }

    public void videoStatus(boolean enabled) {
        enabledVideo = enabled;
        if (enabled) {
            videoEnable.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.meet_tab_menu_icon_camera, null), null, null);
        } else {
            videoEnable.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.meet_tab_menu_icon_camera_unuse, null), null, null);
        }
        webRTCManager.enableLocalVideo(enabled);
    }

    public void shareStatus(boolean enabled) {
//        enabledVideo = enabled;

    }

    /********************************信令发起相关************************************/
    /**
     * 扬声器 本地扬声器开关
     */
    public void switchSpeaker() {
        if (MCU_MODE.equals(RoomManager.get().getConferenceMode())){
            showMenu();
        }
        RoomControl.get().switchSpeaker(enabledSpeaker, userId);
    }

    /**
     * 视频开关 本地视频 开关
     */
    public void switchVideo() {
        if (MCU_MODE.equals(RoomManager.get().getConferenceMode())){
            showMenu();
        }
        List<String> list = new ArrayList<>();
        list.add(userId);
        RoomControl.get().switchVideo(enabledVideo, list);
    }

    public void shareHDMI() {
        if (mVideo != null) {
            mVideo.shareControl(shareHDMI);
        }
    }

    /**
     * 音频开关
     */
    public void switchAudio() {
        if (MCU_MODE.equals(RoomManager.get().getConferenceMode())){
            showMenu();
        }
        //音量调节
        List<String> list = new ArrayList<>();
        list.add(userId);
        RoomControl.get().switchAudio(enabledAudio, list);
    }

    public void waitingSpeaking() {
        L.d("do this websocket---->>>currentHandUpNum : " + currentHandUpNum + ", currentSpeak: " + currentSpeakNum + " , " + RoomClient.get().isShowRollCall());
        if (currentHandUpNum <= 0 && currentSpeakNum <= 0 && !RoomClient.get().isShowRollCall()) {
            UCToast.show(baseActivity, "当前无申请发言人员");
            return;
        }
        rollCallPopupWindow = new RollCallPopupWindow(baseActivity, channelId);
        rollCallPopupWindow.showAsLeft(divideView);
        rollCallPopupWindow.setOnDismissListener(() -> {
            rollCallPopupWindow.initCheckStatus();
            menuManager.scheduleHide();
        });
        CallUtil.asyncCall(400, () -> {
            reset();
        });
    }

    /**
     * 举手 放手
     */
    public void raiseHand() {
        if ("申请发言".contentEquals(raiseHand.getText())) {//即将 举手
            raiseHand(RoomClient.get().getRoomId(), SPEditor.instance().getUserId());
            raiseHand.setText("等待中");
            raiseHand.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.maininterface_menu_icon_speak_wainting, null), null, null);
        } else if (raiseHand.getText().equals("等待中")) {//即将 放手
            raiseHand.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.maininterface_menu_icon_speak, null), null, null);
            putDownHand(RoomClient.get().getRoomId(),
                    SPEditor.instance().getUserId(),
                    SPEditor.instance().getUserId());
            raiseHand.setText("申请发言");
        }
    }


    public void invitationJoin() {
        invitePersonPopupWindow = new InvitePersonPopupWindow(baseActivity, channelId, Constract.INVITE_FROMTYPE_MEET);
        invitePersonPopupWindow.showAsDropUp(invitation);
        menuManager.reset();
        menuManager.registerWindows(invitePersonPopupWindow);
    }

    /**
     * 举手
     *
     * @param roomId
     * @param sourceId
     */
    public void raiseHand(String roomId, String sourceId) {
        RoomClient.get().raiseHand(roomId, sourceId);
    }

    /**
     * 手放下
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     */
    public void putDownHand(String roomId, String sourceId, String targetId) {
        RoomClient.get().putDownHand(roomId, sourceId, targetId);
    }

    /**
     * 显示本人信息
     */
    public void hostPop() {
        HostInfoPopupWindow layoutChoosePopupWindow = new HostInfoPopupWindow(baseActivity);
        ParticipantInfoModel infoModel = new ParticipantInfoModel();
        infoModel.setAccount(SPEditor.instance().getAccount());
        infoModel.setDeviceName(SPEditor.instance().getDeviceName());
        layoutChoosePopupWindow.setParticipant(infoModel);
//        layoutChoosePopupWindow.showAsLeft(rl_info);
        layoutChoosePopupWindow.getContentView().measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int measuredWidth = layoutChoosePopupWindow.getContentView().getMeasuredWidth();
//        layoutChoosePopupWindow.showAtLocation(this, Gravity.NO_GRAVITY, (int) (rightMenu.getX() - measuredWidth), 0);
        layoutChoosePopupWindow.showAsLeft(divideView);
        menuManager.registerWindows(layoutChoosePopupWindow);
        CallUtil.asyncCall(400, () -> {
            reset();
        });
    }

    public void layoutChange() {
        if (layoutChoosePopupWindow == null) {
            layoutChoosePopupWindow = new LayoutChoosePopupWindow(baseActivity);
            layoutChoosePopupWindow.setLayoutChoose((position, b) -> {
                if (mVideo != null) {
                    mVideo.splitScreen4Control(position, b);
                }
            });
        }
        menuManager.registerWindows(layoutChoosePopupWindow);
        layoutChoosePopupWindow.showAsLeft(divideView);
        CallUtil.asyncCall(400, () -> {
            reset();
        });
    }

    /********************************信令回调处理************************************/
    /**
     * 刷新邀请者列表
     *
     * @param orgList
     */
    public void notifyOrgTreeList(CompanyModel orgList) {
        if (invitePersonPopupWindow != null && invitePersonPopupWindow.isShowing()) {
            invitePersonPopupWindow.notifyOrgTreeList(orgList);
        }
    }

    public void notifyDeviceData(List<UserDeviceModel> listOfDevice) {
        if (invitePersonPopupWindow != null && invitePersonPopupWindow.isShowing()) {
            invitePersonPopupWindow.notifyDeviceData(listOfDevice);
        }
    }

    public void notifyGroupDeviceData(List<UserDeviceModel> listOfDevice) {
        if (invitePersonPopupWindow != null && invitePersonPopupWindow.isShowing() ) {
            invitePersonPopupWindow.notifyGroupDeviceData(listOfDevice);
        }
    }

    public void setSubGroupItem(GroupListInfoModel groupListInfoModel) {
        if (invitePersonPopupWindow != null && groupListInfoModel != null && groupListInfoModel.getGroupList()!= null ) {
            invitePersonPopupWindow.setSubGroupItem(groupListInfoModel.getGroupList());
        }
    }

    public void inviteJoin(int inviteNum) {
        if (invitePersonPopupWindow != null) {
            invitePersonPopupWindow.showChooseToast();
        } else {
            UCToast.show(getContext(), "邀请" + inviteNum + "人成功");
        }
    }

    public void rollCallByMe() {
        raiseHand.setText("发言中");
        raiseHand.setCompoundDrawablesWithIntrinsicBounds(null,
                getResources().getDrawable(R.drawable.maininterface_menu_icon_speak_wainting, null), null, null);
    }

    public void endCallByMe() {
        raiseHand.setCompoundDrawablesWithIntrinsicBounds(null,
                getResources().getDrawable(R.drawable.maininterface_menu_icon_speak, null), null, null);
        raiseHand.setText("申请发言");
    }

    public void showDotNumber(String raiseHandNum) {
        int num = Integer.valueOf(raiseHandNum);
        raiseHandNum = num < 0 ? "0" : raiseHandNum;
        if (("0").equals(raiseHandNum)) {
            tvDot.setVisibility(View.GONE);
        } else {
            tvDot.setVisibility(View.VISIBLE);
            tvDot.setText(raiseHandNum);
        }
    }

    public void bodyRollCallByHost(HandsStatusResp handsStatusResp) {
        String showName = !TextUtils.isEmpty(handsStatusResp.getUsername()) ? handsStatusResp.getUsername() : handsStatusResp.getAppShowName();
        UCToast.showCustomToast(baseActivity, showName + "申请发言", Gravity.BOTTOM, 200, 120);
        onRaiseHand(handsStatusResp);
    }

    /**
     * 发起 举手 后  回调(仅主持人端显示)
     */

    public void onRaiseHand(HandsStatusResp handsStatus) {
        L.d("onRaiseHandV2: " + handsStatus.toString());
        currentHandUpNum = Integer.valueOf(handsStatus.getRaiseHandNum());
        showDotNumber(handsStatus.getRaiseHandNum());

    }

    /**
     * 手放下 回调
     */

    public void onPutDownHand(HandsStatusResp videoStatus) {
        currentHandUpNum = Integer.valueOf(videoStatus.getRaiseHandNum());
        RoomClient.get().setShowRollCall(false);
        if (!TextUtils.isEmpty(videoStatus.getRaiseHandNum())) {
            showDotNumber(videoStatus.getRaiseHandNum());
        }
        if (!userId.equals(videoStatus.getTargetId())) {
            return;
        }
        raiseHand.setCompoundDrawablesWithIntrinsicBounds(null,
                getResources().getDrawable(R.drawable.maininterface_menu_icon_speak, null), null, null);
        raiseHand.setText("申请发言");
        String targetId = videoStatus.getTargetId();
        String sourceId = videoStatus.getSourceId();
        L.d("onSetAudioStatusV2: " + videoStatus.toString());

        if (sourceId.equals(targetId)) {
            return;
        }

        L.d("do this --------onPutDownHand>>>" + sourceId + " , " + targetId);
    }

    /**
     * 刷新发言者列表
     */
    public void refreshRollCallPop() {
        if (rollCallPopupWindow != null && rollCallPopupWindow.isShowing()) {
            rollCallPopupWindow.setData(RoomManager.get().particalToHands());
        }
    }

    public void refreshParticipant(List<ParticipantInfoModel> participantInfoModels) {
        if (participantPopWindow != null && participantPopWindow.isShowing()) {
            participantPopWindow.setData(participantInfoModels);
        }
    }

    public void updateJoinPersons() {
        //更新文字
        int size = RoomManager.get().getFilterParticipants(RoomClient.get().getRemoteParticipants()).size();

//        for (ParticipantInfoModel infoModel : RoomManager.get().getParticipants()) {
//            L.d("do this ---updateJoinPersons >>>" + infoModel.getConnectionId() + " , name:" + infoModel.getAccount() + ", " + infoModel.getLeft() + " - ");
//        }
        tvRoomPerson.setText(String.format(Locale.getDefault(), "参会者:%d/%d", size, RoomManager.get().getRoomCapacity()));
    }

    public void showTimeDownPopup(int time) {
        if (baseActivity.isFinishing()) {
            return;
        }
        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            popupWindow = null;
        }
        popupWindow = new TimeDownPopupWindow(baseActivity);
        popupWindow.setBackground(time == 10 ? R.color.color_D48806 : R.color.color_a80006);
        popupWindow.setTimeTitle(time == 10 ? getResources().getString(R.string.str_time_10m) : getResources().getString(R.string.str_time_1m));
        if (tvRoomId.getVisibility() == View.VISIBLE) {
            popupWindow.showAtRight(tvRoomId);
        } else {
            popupWindow.showAtLocation(cl, Gravity.BOTTOM, 0, 0);
        }
        popupWindow.startTimeDown(time);

        if (time == 10) {
            CallUtil.asyncCall(10000, () -> {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
            });
        }
    }

    public void roomDelay() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void showQueuePop() {
        participantPopWindow = new ParticipantPopWindow(baseActivity, channelId, true);
        participantPopWindow.showAsLeft(divideView);
        menuManager.registerWindows(participantPopWindow);
        CallUtil.asyncCall(400, () -> {
            reset();
        });
    }


    /**
     * 底部菜单获取焦点
     */
    public void requestBottomFocus() {
        bottomMenu.requestFocus();
    }

    /**
     * 右侧菜单获取焦点
     */
    public void requestRightFocus() {
        rightMenu.requestFocus();
    }

    public void scheduleHide() {
        menuManager.scheduleHide();
    }

    public void reset() {
        menuManager.reset();
    }

    public void showMenu() {
        menuManager.showMenu();
    }

    public interface VideoPlayerControl {
        void roomSettingControl(View view);

        void splitScreen0Control();

        void splitScreen4Control(int count, boolean b);

        void roomClosed();

        void shareControl(View view);
    }

    public BaseActivity getBaseActivity() {
        return baseActivity;
    }

    public void setBaseActivity(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public int getCurrentHandUpNum() {
        return currentHandUpNum;
    }

    public void setCurrentHandUpNum(int currentHandUpNum) {
        this.currentHandUpNum = currentHandUpNum;
    }

    public int getCurrentSpeakNum() {
        return currentSpeakNum;
    }

    public void setCurrentSpeakNum(int currentSpeakNum) {
        this.currentSpeakNum = currentSpeakNum;
    }

    public boolean isEnabledSpeaker() {
        return enabledSpeaker;
    }

    public void setEnabledSpeaker(boolean enabledSpeaker) {
        this.enabledSpeaker = enabledSpeaker;
    }


    public void setVideoPlayControl(VideoPlayerControl mVideo) {
        this.mVideo = mVideo;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    //设置会议时长
    public void setMeetDuring(long second) {
        if (second < 60) {
            costTime.setText(String.format(Locale.getDefault(), "00:00:%02d", (int) second % 60));
        } else if (second < 3600) {
            costTime.setText(String.format(Locale.getDefault(), "00:%02d:%02d", (int) second / 60 % 60, (int) second % 60));
        } else {
            costTime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", (int) second / 60 / 60, (int) second / 60 % 60, (int) second % 60));
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        if (layoutChoosePopupWindow != null && layoutChoosePopupWindow.isShowing()) {
            layoutChoosePopupWindow.dismiss();
        }
        if (rollCallPopupWindow != null && rollCallPopupWindow.isShowing()) {
            rollCallPopupWindow.dismiss();
        }
        if (invitePersonPopupWindow != null && invitePersonPopupWindow.isShowing()) {
            invitePersonPopupWindow.dismiss();
        }
        if (participantPopWindow != null && participantPopWindow.isShowing()) {
            participantPopWindow.dismiss();
        }
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public boolean isEnabledAudio() {
        return enabledAudio;
    }

    public void setEnabledAudio(boolean enabledAudio) {
        this.enabledAudio = enabledAudio;
    }

    public boolean isEnabledVideo() {
        return enabledVideo;
    }

    public void setEnabledVideo(boolean enabledVideo) {
        this.enabledVideo = enabledVideo;
    }
}
