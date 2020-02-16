package cn.closeli.rtc.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.vhd.middleware.CameraManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.constract.GlobalValue;
import cn.closeli.rtc.model.rtc.UserDeviceModel;

//终端设置管理类
public class DeviceSettingManager {
    private CameraManager mCameraManager = null;
    private DeviceModel model = new DeviceModel();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private DeviceSettingManager() {
        mCameraManager = CameraManager.getInstance();
    }

    public static final DeviceSettingManager getInstance() {
        return Inner.inner;
    }

    private OnSetSetting onSetSetting;

    //设置相机控制
    public void setCameraControl(String key, String value) {
        if (mCameraManager != null) {
            executorService.execute(() -> mCameraManager.setCameraControl(key, value));
        }
    }

    public void setCameraControlSynchro(String key, String value) {
        if (mCameraManager != null) {
            mCameraManager.setCameraControl(key, value);
        }
    }

    //开启云台控制
    public void startCameraControl() {
        if (mCameraManager != null) {
            mCameraManager.setCameraControl(GlobalValue.KEY_ROLE_CONTROL, GlobalValue.ROLE_DIRECT);
        }
    }

    //关闭云台控制
    public void endCameraControl() {
        if (mCameraManager != null) {
            mCameraManager.setCameraControl(GlobalValue.KEY_ROLE_CONTROL, GlobalValue.ROLE_INDIRECT);
        }
    }

    public int getSaveKeyData(int keyNum) {
        return 1 << keyNum;
    }


    /*****************************************************************/

    public DeviceModel getModel() {
        return model;
    }

    public void setModel(DeviceModel model) {
        this.model = model;
    }

    //保存为sp
    public void saveSp() {
        Gson gson = new Gson();
        String str = gson.toJson(model);
//        SPEditor.instance().setString(SPEditor.instance().getAccount(),str);
        if (onSetSetting != null) {
            onSetSetting.setSetting();
        }
//        SPEditor.instance().setString(SPEditor.instance().getUserId(),str);
        SPEditor.instance().setString(SPEditor.instance().getAccount(), str);
    }

    public DeviceModel getFromSp() {
        return getFromSp(SPEditor.instance().getAccount());
    }

    public DeviceModel getFromSp(String userId) {
        String str = SPEditor.instance().getString(userId);
        if (TextUtils.isEmpty(str)) {
            return new DeviceModel();
        }
        Gson gson = new Gson();
        DeviceModel deviceModel = gson.fromJson(str, DeviceModel.class);
        return deviceModel;
    }

    public static class DeviceModel {
        private String subject = SPEditor.instance().getUserNick() == null ? SPEditor.instance().getAccount() + "的会议" : SPEditor.instance().getUserNick() + "的会议";      //会议主题
        private float duration = 9999;       //会议时长
        private String meetId = SPEditor.instance().getAccount();      //会议Id，空则 系统自动分配
        private boolean isUserPsd;  //是否使用密码
        private String password="";    //密码
        private boolean isAllIdInto = true;   //id入会 全体
        private boolean isAllShare = true;     //共享权限 全体
        private String isSettingMicroOn = "on"; //入会时关闭麦克风 默认关闭
        private String isSettingCameraOn = "on";    //入会时开启摄像头
        private boolean isDeviceAuto = true;        //终端自动入会
        private boolean isOpenMicroSelf = true;     //允许被主持人关闭麦克风的情况下自己打开
        private boolean isOpenShareSelf = true;     //允许被主持人关闭共享权限的情况下自己打开
        private boolean isInvertCamera = false;      //摄像头角度倒置
        private boolean isCameraFocusAuto = true;   //摄像头聚焦 true：自动 false:手动
        private int indexAudioInput = 1;            //音频输入
        private int indexAudioOutput = 2;             //音频输出
        private boolean isZimuOpen = true;          //字幕开关
        private String zimu;
        private int indexWindow = 0;                 //显示器
        private String currentPreset = "0";          //预置位信息 （二进制 1111111111）
        private boolean isScreenMain = false;        //辅流是否是主频显示
        private boolean isSFUMode = false;           //是否是SFU 模式
        private int roomCapacity = isSFUMode? Constract.SFU_CAPACITY:Constract.MCU_CAPACITY;   //会议人数

        private Map<String, UserDeviceModel> modelMap = new HashMap<>();        //存放上次邀请的人

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public int getRoomCapacity() {
            return roomCapacity;
        }

        public void setRoomCapacity(int roomCapacity) {
            this.roomCapacity = roomCapacity;
        }

        public float getDuration() {
            return duration;
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }

        public String getMeetId() {
            return meetId;
        }

        public void setMeetId(String meetId) {
            this.meetId = meetId;
        }

        public boolean isAllIdInto() {
            return isAllIdInto;
        }

        public void setAllIdInto(boolean allIdInto) {

            isAllIdInto = allIdInto;
        }

        public boolean isAllShare() {
            return isAllShare;
        }

        public void setAllShare(boolean allShare) {
            isAllShare = allShare;
        }

        public String getIsSettingMicroOn() {
            return isSettingMicroOn;
        }

        public void setIsSettingMicroOn(String isSettingMicroOn) {
            this.isSettingMicroOn = isSettingMicroOn;
        }

        public String getIsSettingCameraOn() {
            return isSettingCameraOn;
        }

        public void setIsSettingCameraOn(String isSettingCameraOn) {
            this.isSettingCameraOn = isSettingCameraOn;
        }

        public boolean isDeviceAuto() {
            return isDeviceAuto;
        }

        public void setDeviceAuto(boolean deviceAuto) {
            isDeviceAuto = deviceAuto;
        }

        public boolean isOpenMicroSelf() {
            return isOpenMicroSelf;
        }

        public void setOpenMicroSelf(boolean openMicroSelf) {
            isOpenMicroSelf = openMicroSelf;
        }

        public boolean isOpenShareSelf() {
            return isOpenShareSelf;
        }

        public void setOpenShareSelf(boolean openShareSelf) {
            isOpenShareSelf = openShareSelf;
        }

        public boolean isInvertCamera() {
            return isInvertCamera;
        }

        public void setInvertCamera(boolean invertCamera) {
            isInvertCamera = invertCamera;
        }

        public boolean isCameraFocusAuto() {
            return isCameraFocusAuto;
        }

        public void setCameraFocusAuto(boolean cameraFocusAuto) {
            isCameraFocusAuto = cameraFocusAuto;
        }

        public int getIndexAudioInput() {
            return indexAudioInput;
        }

        public void setIndexAudioInput(int indexAudioInput) {
            this.indexAudioInput = indexAudioInput;
        }

        public int getIndexAudioOutput() {
            return indexAudioOutput;
        }

        public void setIndexAudioOutput(int indexAudioOutput) {
            this.indexAudioOutput = indexAudioOutput;
        }

        public boolean isZimuOpen() {
            return isZimuOpen;
        }

        public void setZimuOpen(boolean zimuOpen) {
            isZimuOpen = zimuOpen;
        }

        public String getZimu() {
            return zimu;
        }

        public void setZimu(String zimu) {
            this.zimu = zimu;
        }

        public int getIndexWindow() {
            return indexWindow;
        }

        public void setIndexWindow(int indexWindow) {
            this.indexWindow = indexWindow;
        }

        public boolean isUserPsd() {
            return isUserPsd;
        }

        public void setUserPsd(boolean userPsd) {
            isUserPsd = userPsd;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCurrentPreset() {
            return currentPreset;
        }

        public void setCurrentPreset(String keyValue) {
            this.currentPreset = keyValue;
        }

        public boolean isScreenMain() {
            return isScreenMain;
        }

        public void setScreenMain(boolean screenMain) {
            isScreenMain = screenMain;
        }

        public boolean isSFUMode() {
            return isSFUMode;
        }

        public void setSFUMode(boolean SFUMode) {
            isSFUMode = SFUMode;
        }

        public Map<String, UserDeviceModel> getModelMap() {
            return modelMap;
        }

        public void setModelMap(Map<String, UserDeviceModel> modelMap) {
            this.modelMap = modelMap;
        }
    }

    static class Inner {
        private static final DeviceSettingManager inner = new DeviceSettingManager();
    }

    public interface OnSetSetting {
        void setSetting();
    }

    public OnSetSetting getOnSetSetting() {
        return onSetSetting;
    }

    public void setOnSetSetting(OnSetSetting onSetSetting) {
        this.onSetSetting = onSetSetting;
    }
}
