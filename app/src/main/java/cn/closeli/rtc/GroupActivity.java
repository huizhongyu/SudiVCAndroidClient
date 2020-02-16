package cn.closeli.rtc;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Outline;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocketState;
import com.tencent.bugly.crashreport.CrashReport;
import com.vhd.base.util.LogUtil;
import com.vhd.base.util.SystemProp;
import com.vhd.camera.CameraEncoderv2;

import org.greenrobot.eventbus.EventBus;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.closeli.rtc.command.LoginStateListener;
import cn.closeli.rtc.constract.BundleKey;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.controller.DeviceSettingActivity;
import cn.closeli.rtc.controller.PreviewHdmiActivity;
import cn.closeli.rtc.controller.PreviewActivity;
import cn.closeli.rtc.controller.SettingsActivity;
import cn.closeli.rtc.model.ReJoinModel;
import cn.closeli.rtc.model.UpdateModel;
import cn.closeli.rtc.model.http.AccountModel;
import cn.closeli.rtc.model.http.AddrResp;
import cn.closeli.rtc.model.http.SudiHttpCallback;
import cn.closeli.rtc.model.http.TokenResp;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.GroupInfoModel;
import cn.closeli.rtc.model.info.GroupListInfoModel;
import cn.closeli.rtc.model.info.GroupModel;
import cn.closeli.rtc.model.info.PartDeviceModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.model.ws.WsError;
import cn.closeli.rtc.net.ServerManager;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.RoomEventAdapter;
import cn.closeli.rtc.room.SudiRole;
import cn.closeli.rtc.room.WssMethodNames;
import cn.closeli.rtc.sdk.IViewCallback;
import cn.closeli.rtc.sdk.ProxyVideoSink;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.AccountUtil;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.DateUtil;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.ExecutorFactory;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.utils.ext.WorkThreadFactory;
import cn.closeli.rtc.utils.net.NetType;
import cn.closeli.rtc.utils.net.NetworkUtils;
import cn.closeli.rtc.utils.signature.SignatureUtil;
import cn.closeli.rtc.widget.FocusLayout;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;
import cn.closeli.rtc.widget.popwindow.DevicePopupWindow;
import cn.closeli.rtc.widget.popwindow.InvitePersonPopupWindow;
import cn.closeli.rtc.widget.popwindow.JoinInPopupDialog;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.constract.Constract.JOIN_ACTIVE;
import static cn.closeli.rtc.utils.Constants.STREAM_MAJOR;


public class GroupActivity extends BaseActivity implements CustomAdapt, View.OnFocusChangeListener, LoginStateListener {

    //N个组 每个组里有n个人
    public static String HDMI_DISCONNECT = "com.vhd.state.hdmiin.disconnect";
    public static String HDMI_CONNECT = "com.vhd.state.hdmiin.connect";
    public static String HDMI_UNKNOWN = "com.vhd.state.hdmiin.unknown";
    public static int Intent_AC = 0;
    @BindView(R.id.sfv_local)
    SurfaceViewRenderer surfaceLocal;
    @BindView(R.id.sfv_support)
    SurfaceViewRenderer surfaceSupport;
    private MediaStream mediaStream = new MediaStream(0);
    @BindView(R.id.tv_clock)
    TextView tv_clock;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.account)
    TextView accountText;
    @BindView(R.id.fl_local)
    FrameLayout fl_local;

    @BindView(R.id.iv_create_room)
    ImageView iv_create_room;
    @BindView(R.id.iv_join_room)
    ImageView iv_join_room;
    @BindView(R.id.iv_setting)
    ImageView iv_setting;
    @BindView(R.id.iv_contract)
    ImageView iv_contract;

    @BindView(R.id.iv_out)
    ImageView iv_out;

    @BindView(R.id.constraint_no_share)
    ConstraintLayout constraint_no_share;

    @BindView(R.id.cl_main)
    View cl_main;

    @BindView(R.id.tv_system_name)
    TextView tv_system_name;
    @BindView(R.id.tv_connect_status)
    TextView tv_connect_status;
    @BindView(R.id.iv_icon_status)
    ImageView iv_icon_status;

    @BindView(R.id.tv_version_name)
    TextView tv_version_name;
    //发布 观看 仅发布 仅观看
    ProxyVideoSink localSink;
    private long mExitTime;
    private SimpleDateFormat format;
    private JoinInPopupDialog joinInPopupWindow;
    private DevicePopupWindow devicePopupWindow;
    private FocusLayout mFocusLayout;
    InviteJoinDialog inviteJoinDialog;
    InviteJoinDialog dialog;
    private InvitePersonPopupWindow invitePersonPopupWindow;
    private boolean isFirst = true;
    private ThreadPoolExecutor executorService;
    private IntentFilter intentFilter;
    private NetworkChangReceiver networkChangeReceiver;
    private MyHandler h = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
//        ActivityUtils.getInstance().addActivity(this);
        RoomClient.get().setLoginStateListener(this);
        AccountUtil.INSTANCE.login();
        ButterKnife.bind(this);
        RoomClient.get().addRoomEventCallback(GroupActivity.class, roomEventAdapter);
        executorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new WorkThreadFactory());
        initWebRTC();
        initsetSurface();
        //动态注册 系统时钟的广播
        initLocalTime();
        initHDMI();
        RoomManager.get().setNativeAudio(Constract.VALUE_STATUS_ON.equals(DeviceSettingManager.getInstance().getFromSp().getIsSettingMicroOn()));
        RoomManager.get().setNativeVideo(Constract.VALUE_STATUS_ON.equals(DeviceSettingManager.getInstance().getFromSp().getIsSettingCameraOn()));
        initFocusListener();
        setConfigs();
        tv_version_name.setText(SystemUtil.getVersionName(getApplicationContext()));
        registerNetworkCallback();
    }

    /**
     * 和登录有关的初始化内容
     */
    void initText() {
        tv_system_name.setText(SPEditor.instance().getDeviceName());
        accountText.setText(String.format("ID: %s", SPEditor.instance().getAccount()));
    }

    private void initHDMI() {
        Log.i("receiverHdmiIn", "receiverHdmiIn" + SystemUtil.isHdmiinConnected());
        if (SystemUtil.isHdmiinConnected()) {
            constraint_no_share.setVisibility(View.GONE);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HDMI_DISCONNECT);
        intentFilter.addAction(HDMI_CONNECT);
        intentFilter.addAction(HDMI_UNKNOWN);
        registerReceiver(receiverHdmiIn, intentFilter);
    }

    private void initFocusListener() {
        iv_create_room.setOnFocusChangeListener(this);
        iv_join_room.setOnFocusChangeListener(this);
        iv_contract.setOnFocusChangeListener(this);
        iv_setting.setOnFocusChangeListener(this);
    }


    private void initLocalTime() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, intentFilter);
        Date date = new Date();
        format = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        String formatTime = format.format(date);
        String ymd = formatTime.substring(0, formatTime.indexOf(" "));
        String hour = formatTime.substring(formatTime.indexOf(" ") + 1);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String amOrPm = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

        tv_clock.setText(hour);
        tv_date.setText(String.format("%s %s", ymd, DateUtil.getWeekStr(calendar.get(Calendar.DAY_OF_WEEK))));
    }


    private void initsetSurface() {
        surfaceLocal = WebRTCManager.get().setSurface(surfaceLocal);
//        surfaceLocal.setZOrderMediaOverlay(true);
        surfaceLocal.setMirror(true);

        surfaceSupport = WebRTCManager.get().setSurface(surfaceSupport);
    }

    private void initLocalPreview() {
        ParticipantInfoModel participant = new ParticipantInfoModel();
        participant.setLocalStreamType(0);

        WebRTCManager.get().startPreView(participant);
    }

    private void inithdmiPreview() {
        int[] idArray = new int[2];
        if (!CameraEncoderv2.hasValidCamera(idArray)) {
            Toast.makeText(GroupActivity.this, "no vhd camera", Toast.LENGTH_LONG).show();
            LogUtil.LOG_WARN("shareScreenHDMI", this.getClass().toString(),
                    "not find vhd camera");
            return;
        } else {
            LogUtil.LOG_WARN("shareScreenHDMI", this.getClass().toString(),
                    "find vhd camera vid:pid(" +
                            String.format("%04x", idArray[0]) + ":" +
                            String.format("%04x", idArray[1]) + ")");
        }

        ParticipantInfoModel participant = new ParticipantInfoModel();
        participant.setLocalStreamType(2);
        WebRTCManager.get().shareHdmi(surfaceSupport);
        WebRTCManager.get().startPreView(participant);
    }

    private void initViewCallBack() {
        WebRTCManager.get().addViewCallback(GroupActivity.class, new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream) {
                if (RoomClient.get().getCurrentActivity() instanceof GroupActivity) {
                    mediaStream = stream;
                    localSink = new ProxyVideoSink();
                    localSink.setTarget(surfaceLocal);
                    mediaStream.videoTracks.get(0).addSink(localSink);
                }

            }

            @Override
            public void onSetLocalScreenStream(MediaStream stream) {

            }

            @Override
            public void onSetLocalHDMIStream(MediaStream stream) {

            }

            @Override
            public void onAddRemoteStream(MediaStream stream, ParticipantInfoModel participant) {
                L.d("onRemoteStreamAdded >>>" + "group ," + participant.getConnectionId() + " , streamType : " + participant.getAppShowName());

            }

            @Override
            public void onCloseRemoteStream(MediaStream stream, ParticipantInfoModel participant) {

            }

            @Override
            public void onMixStream(ParticipantInfoModel participant, boolean isHasShare) {

            }

            @Override
            public void publishParticipantInfo(ParticipantInfoModel participant) {

            }
        });
    }

    private void initWebRTC() {
        WebRTCManager.get().initEgl(EglBase.create());
        WebRTCManager.get().initExecutor();
        WebRTCManager.get().create();
        WebRTCManager.get().initClPeer();
    }


    //时钟接收广播
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //系统每分钟发出
            if (Intent.ACTION_TIME_TICK.equals(action)) {
                if (format == null) {
                    format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                }

                executorService.execute(() -> {
                    String formatTime = format.format(new Date());
                    Message msg = Message.obtain();
                    msg.what = 100;
                    Bundle b = new Bundle();
                    b.putString(Constract.BUNDLE_KEY_TIME, formatTime);
                    msg.setData(b);
                    h.sendMessage(msg);
                });

            }
        }
    };
    //时钟接收广播
    private final BroadcastReceiver receiverHdmiIn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("receiverHdmiIn", action);
            //系统每分钟发出
            if (HDMI_DISCONNECT.equals(action)) {
                constraint_no_share.setVisibility(View.VISIBLE);
            } else if (HDMI_CONNECT.equals(action)) {
                constraint_no_share.setVisibility(View.GONE);
            } else if (HDMI_UNKNOWN.equals(action)) {

            }
        }
    };
    private RoomEventAdapter roomEventAdapter = new RoomEventAdapter() {
        @Override
        public void onRoomCreated(String roomId, String sessionId) {
            L.d("onRoomCreated callback in GroupActivity");
            String roomPwd;
            if (DeviceSettingManager.getInstance().getFromSp().isUserPsd()) {
                roomPwd = DeviceSettingManager.getInstance().getFromSp().getPassword();
            } else {
                roomPwd = "";
            }
            ServerManager.get().userInfoToPad();
            doJoinRoom(roomId, roomPwd, SudiRole.moderator, JOIN_ACTIVE);
        }


        @Override
        public void onWebSocketFailMessage(WsError wsErrorr, String method) {
            runOnUiThread(() -> {
                if (RoomClient.get().getCurrentActivity() instanceof GroupActivity) {
                    if (WssMethodNames.createRoom.equals(method)) {
                        new AlertDialog.Builder(GroupActivity.this)
                                .setTitle("发起会议")
                                .setMessage("创建失败!".concat("\n").concat(wsErrorr.getMessage()))
                                .setPositiveButton("确定", null)
                                .show();
                    } else if (WssMethodNames.joinRoom.equals(method)) {
                        UIUtils.toastMessage(wsErrorr.getMessage());
                    } else if (WssMethodNames.accessOut.equals(method)) {
                        UIUtils.toastMessage(wsErrorr.getMessage());
                    }

                }
            });
        }

        @Override
        public void onJoinRoomSuccess(ParticipantInfoModel participant) {
            EventBus.getDefault().removeAllStickyEvents();
            L.d("onJoinRoomSuccess callback in GroupActivity");
            runOnUiThread(() -> {
                if (joinInPopupWindow != null) {
                    joinInPopupWindow.dismiss();
                }
            });

            if (participant != null && !participant.getStreamType().equals(STREAM_MAJOR)) {
                return;
            }
            //role userId
            //到视频会议页
            Intent intent;
            if ("SFU".equals(RoomManager.get().getConferenceMode())) {
                intent = new Intent(GroupActivity.this, VideoConferenceActivity.class);
            } else {
                intent = new Intent(GroupActivity.this, MCUVideoActivity.class);
            }
            intent.putExtra("channelRole", participant.getRole());
            SPEditor.instance().getUserId();

            startActivity(intent);
        }

        @Override
        public void onAccessOut() {
            L.d("onAccessOut GroupAct callback");
            RoomClient.get().close();
            runOnUiThread(() -> {
//                finish();
            });
        }

        @Override
        public void getNotFinishedRoom(ReJoinModel model) {
            if (model != null) {
                runOnUiThread(() -> {
                    showLeftMeet(model);
                });
            }
        }

        @Override
        public void updateApp(UpdateModel model) {
            if (UpgradeAppUtil.isUpgrade(model.getVersion(), "forceUpgrade")) {
                UpgradeAppUtil.upgrade(GroupActivity.this, model.getDownloadUrl(), new UpgradeAppUtil.UpgradCallback() {
                    @Override
                    public void onError(String msg) {

                    }

                    @Override
                    public void onSuccess() {

                    }
                });
            }
        }

        @Override
        public void getSubDevOrUser(PartDeviceModel orgList) {
            runOnUiThread(() -> {
                if (invitePersonPopupWindow != null &&  RoomClient.get().getCurrentActivity() instanceof GroupActivity) {
                    invitePersonPopupWindow.notifyDeviceData(orgList.getDeviceList());
                }
            });
        }

        @Override
        public void getDepartmentTree(CompanyModel companyModel) {
            runOnUiThread(() -> {
                if (invitePersonPopupWindow != null && RoomClient.get().getCurrentActivity() instanceof GroupActivity) {
                    invitePersonPopupWindow.notifyOrgTreeList(companyModel);
                }
            });
        }

        @Override
        public void getGroupList(GroupListInfoModel groupListInfoModel) {
            runOnUiThread(() -> {
                if (invitePersonPopupWindow != null && groupListInfoModel != null && groupListInfoModel.getGroupList() != null && RoomClient.get().getCurrentActivity() instanceof GroupActivity) {
                    invitePersonPopupWindow.setSubGroupItem(groupListInfoModel.getGroupList());
                }
            });
        }

        @Override
        public void getGroupInfo(GroupInfoModel groupInfoModel) {
            runOnUiThread(() -> {
                if (invitePersonPopupWindow != null && groupInfoModel != null && RoomClient.get().getCurrentActivity() instanceof GroupActivity) {
                    invitePersonPopupWindow.notifyGroupDeviceData(groupInfoModel.getGroupInfo());
                }
            });
        }
    };


    /**
     * 发起会议
     *
     * @param view
     */
    @OnClick({R.id.iv_create_room})
    public void create(View view) {
        if (!RoomClient.get().isAccessInSuccess()) {
            UCToast.show(getApplicationContext(), getString(R.string.str_click_noaccessin));
            return;
        }
        if (ViewUtils.isFastClick()) {
            return;
        }

        if (!RoomClient.get().isWebSocketOpen()) {
            UCToast.show(GroupActivity.this, "当前网络条件较差，请稍后再试");
            return;
        }


        invitePersonPopupWindow = new InvitePersonPopupWindow(this, null, Constract.INVITE_FROMTYPE_CREATE);
        invitePersonPopupWindow.showAtLocation(cl_main.getRootView(), Gravity.RIGHT, 0, 0);
        invitePersonPopupWindow.setOutsideTouchable(false);
    }

    /**
     * 加入会议
     *
     * @param view
     */
    @OnClick({R.id.iv_join_room})
    public void join(View view) {
        if (!RoomClient.get().isAccessInSuccess()) {
            UCToast.show(getApplicationContext(), getString(R.string.str_click_noaccessin));
            return;
        }

        if (ViewUtils.isFastClick()) {
            return;
        }

        if (!RoomClient.get().isWebSocketOpen()) {
            UCToast.show(GroupActivity.this, "当前网络条件较差，请稍后再试");
            return;
        }

        if (joinInPopupWindow != null && joinInPopupWindow.isHidden()) {
            joinInPopupWindow.dismiss();
            joinInPopupWindow = null;
        }
        joinInPopupWindow = new JoinInPopupDialog();
        joinInPopupWindow.show(getSupportFragmentManager(),
                "CloseDialog");
    }

    /**
     * 加入房间
     *
     * @param roomId
     * @see GroupActivity#create(View)
     * @see GroupActivity#join(View)
     */
    private void doJoinRoom(String roomId, String roomPwd, SudiRole sudiRole, String joinType) {
//        RoomClient.get().getRoomLayout(roomId);
        RoomClient.get().joinRoom(roomId, roomPwd, sudiRole, 0, STREAM_MAJOR, joinType);

    }

    /**
     * 终端设置
     */

    @OnClick(R.id.iv_setting)
    public void devSetting(View view) {
//        if (!RoomClient.get().isAccessInSuccess()) {
//            UCToast.show(getApplicationContext(),getString(R.string.str_click_noaccessin));
//            return;
//        }
        startActivityForResult(new Intent(GroupActivity.this, SettingsActivity.class), 2);
    }

    @OnClick(R.id.iv_contract)
    public void clickContact(View view) {
        if (!RoomClient.get().isAccessInSuccess()) {
            UCToast.show(getApplicationContext(), getString(R.string.str_click_noaccessin));
            return;
        }
        if (ViewUtils.isFastClick()) {
            return;
        }

        if (!RoomClient.get().isWebSocketOpen()) {
            UCToast.show(GroupActivity.this, "当前网络条件较差，请稍后再试");
            return;
        }


        invitePersonPopupWindow = new InvitePersonPopupWindow(this, null, Constract.INVITE_FROMTYPE_CONTRACT);
        invitePersonPopupWindow.showAtLocation(cl_main.getRootView(), Gravity.RIGHT, 0, 0);
        invitePersonPopupWindow.setOutsideTouchable(false);
    }

    /**
     * 退出登录
     *
     * @param view
     */
    @OnClick(R.id.iv_out)
    public void logout(View view) {
        devicePopupWindow = new DevicePopupWindow(this, false);
//        devicePopupWindow.setOnPopupItemListener(() -> {
//            if (RoomClient.get().isWebSocketOpen()) {
//            httpLoginOut();
//            } else {
//                UCToast.show(GroupActivity.this, "当前网络异常,请稍后再试");
//            }
//            devicePopupWindow.dismiss();
//        });
        devicePopupWindow.showAsDropUp(iv_out);

    }

    @OnClick(R.id.iv_holder_local)
    public void clickScaleLocal() {
//        Intent intent = new Intent(GroupActivity.this, PreviewActivity.class);
//        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(GroupActivity.this, surfaceLocal, getString(R.string.local_preview)).toBundle();
//        startActivity(intent, bundle);
        if (!ViewUtils.isFastClick()) {
            Intent intent = new Intent(GroupActivity.this, PreviewActivity.class);
            intent.putExtra(BundleKey.IS_PREVIEW_LOCAL, true);
            startActivity(intent);
        }
    }

    @OnClick(R.id.iv_holder_support)
    public void clickScaleSupport() {
        if (!ViewUtils.isFastClick()) {
            Intent intent = new Intent(GroupActivity.this, PreviewHdmiActivity.class);
            intent.putExtra(BundleKey.IS_PREVIEW_LOCAL, false);
            startActivity(intent);
        }
    }

    private void httpLoginOut() {
        SudiHttpClient.get().logOut(new SudiHttpCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject response) {
                L.d("logOut onSuccess %1$s", response.toString());
                RoomClient.get().accessOut();
            }

            @Override
            public void onFailed(Throwable e) {
                L.d("logOut onFailed %1$s", e.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(receiverHdmiIn);
        unregisterReceiver(networkChangeReceiver);
        surfaceSupport.release();
        surfaceLocal.release();
        ExecutorFactory.getDefaultWorkExecutor().execute(() -> WebRTCManager.get().doRelease());
        RoomClient.get().setWebSocketStateListener(null);
        RoomClient.get().setForceLogin(false);
        RoomClient.get().invalidAccessedFlag();
        RoomClient.get().accessOut();
        h.removeMessages(100);
        h = null;
        executorService.shutdown();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime < 1000) {
            super.onBackPressed();
        } else {
            android.widget.Toast.makeText(this, "再按一次退出", android.widget.Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebRTCManager.get().stopLocalPreview();
        //HDMI 关闭，日志太恶心了
        WebRTCManager.get().stopHDMIPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoomClient.get().setCurrentActivity(this);
        RoomClient.get().setWebSocketStateListener(this);
//        if (isFirst) {
//            RoomClient.get().getNotFinishedRoom();
//            isFirst = false;
//        }
        initViewCallBack();
        Log.i("GroupActivity......", "GroupActivity   closeRoomNotify");
        initLocalPreview();
        inithdmiPreview();
        registeNoFocusViewId(R.id.iv_create_room, R.id.iv_join_room, R.id.iv_setting, R.id.iv_out, R.id.iv_account);
        initText();
    }

    private void bindListener() {
        //获取根元素
        View mContainerView = this.getWindow().getDecorView();//.findViewById(android.R.id.content);
        //得到整个view树的viewTreeObserver
        ViewTreeObserver viewTreeObserver = mContainerView.getViewTreeObserver();
        //给观察者设置焦点变化监听
        viewTreeObserver.addOnGlobalFocusChangeListener(mFocusLayout);
    }

    //按钮背景随之改变
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.iv_join_room) {
            focusAnimator(iv_join_room, hasFocus);
        } else if (v.getId() == R.id.iv_create_room) {
            focusAnimator(iv_create_room, hasFocus);
        } else if (v.getId() == R.id.iv_setting) {
            focusAnimator(iv_setting, hasFocus);
        } else if (v.getId() == R.id.iv_contract) {
            focusAnimator(iv_contract, hasFocus);
        }
    }

    public void focusAnimator(View view, boolean hasFocus) {
        if (hasFocus) {
            view.animate()
                    .scaleX(view.getScaleX() + 0.1f)
                    .scaleY(view.getScaleY() + 0.1f)
                    .setDuration(300)
                    .start();
        } else {
            view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(300)
                    .start();
        }
    }

    @Override
    public void onStateChange(WebSocketState socketState) {
        runOnUiThread(() -> {
            if (socketState == WebSocketState.OPEN) {
                RoomClient.get().setForceLogin(false);
                isConnected(true);
            }
        });
    }

    @Override
    protected void onSockedConnectFail() {
        super.onSockedConnectFail();
        runOnUiThread(() -> {
            isConnected(false);
        });
    }

    private void isConnected(boolean isConnected) {
        if (isConnected) {
            tv_connect_status.setText(getString(R.string.str_group_connected));
            tv_connect_status.setTextColor(getResources().getColor(R.color.color_00ffff));
            if (NetType.ETHERNET == NetworkUtils.getNetType()) {
                iv_icon_status.setImageResource(R.drawable.icon_group_line_connected);
            } else {
                iv_icon_status.setImageResource(R.drawable.icon_group_connected);
            }
        } else {
            tv_connect_status.setText(getString(R.string.str_group_un_connect));
            tv_connect_status.setTextColor(getResources().getColor(R.color.color_787B84));
            if (NetType.WIFI == NetworkUtils.getNetType()) {
                iv_icon_status.setImageResource(R.drawable.icon_group_disconnected);
            } else {
                iv_icon_status.setImageResource(R.drawable.icon_group_line_disconnected);
            }
        }
    }

    @Override
    protected void onAccessInSuccess() {
        runOnUiThread(() -> {
            if (RoomClient.get().isAccessInSuccess()) {                 //网络良好，请求本人正在参加的会
                CallUtil.asyncCall(1000, () -> {
                    RoomClient.get().getNotFinishedRoom();
                });
            }
        });
    }

    //展示重新加入会议弹窗
    private void showLeftMeet(ReJoinModel model) {
        if (model == null || TextUtils.isEmpty(model.getRoomId())) {
            return;
        }
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = InviteJoinDialog.newInstance();
        dialog.setCancelable(false);
        long min = model.getRemainTime() / 60;
        if (WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER.equals(model.getRole())) {                       //主持人
            if (min < 0) {
                dialog.setTitle("您有未完成的会议，是否进入会议？");
            } else {
                dialog.setTitle("您有未完成的会议，会议时间还剩" + min + "分钟，是否进入会议？");
            }
        } else {
            if (min < 0) {
                dialog.setTitle("您有未结束的会议，是否进入会议？");
            } else {
                dialog.setTitle("您有未结束的会议，会议时间还剩" + min + "分钟，是否进入会议？");
            }
        }
        dialog.setStrComfirm("进入会议");
        dialog.setStrCancel("终止会议");
        dialog.setOnDialogClick((d, index) -> {
            if (index == 1) {                           //重新加入会议
                String roomPwd = "";
                roomPwd = model.getPassword();
                ServerManager.get().userInfoToPad();
                doJoinRoom(model.getRoomId(), roomPwd, WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER.equals(model.getRole()) ? SudiRole.moderator : SudiRole.publisher, JOIN_ACTIVE);
            } else {
                if (WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER.equals(model.getRole())) {
                    //关闭会议
                    RoomClient.get().closeRoom(model.getRoomId());
                } else {
                    RoomClient.get().leaveRoom(SPEditor.instance().getUserId(), model.getRoomId(), STREAM_MAJOR);
                }
            }
            dialog.dismiss();
        });

        dialog.show(getSupportFragmentManager(), "net error");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == DeviceSettingActivity.SETTING_CODE) {
            if (requestCode == 2) {
                RoomManager.get().setNativeAudio(Constract.VALUE_STATUS_ON.equals(DeviceSettingManager.getInstance().getFromSp().getIsSettingMicroOn()));
                RoomManager.get().setNativeVideo(Constract.VALUE_STATUS_ON.equals(DeviceSettingManager.getInstance().getFromSp().getIsSettingCameraOn()));
            }
        }
    }

    public class MyHandler extends Handler {
        private WeakReference<GroupActivity> weakReference;

        public MyHandler(GroupActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (weakReference.get() == null) {
                return;
            }
            if (msg.what == 100) {
                String time = (String) msg.getData().get(Constract.BUNDLE_KEY_TIME);
                String ymd = time.substring(0, time.indexOf(" "));
                String hour = time.substring(time.indexOf(" ") + 1);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                tv_clock.setText(hour);
                tv_date.setText(String.format("%s %s", ymd, DateUtil.getWeekStr(calendar.get(Calendar.DAY_OF_WEEK))));
            }
        }
    }


    private void setConfigs() {
        if (DeviceSettingManager.getInstance().getFromSp().getIndexAudioInput() == 0) {
            SystemProp.setProperty("vhd.audio.micin.force", "3");
        } else if (DeviceSettingManager.getInstance().getFromSp().getIndexAudioInput() == 1) {
            SystemProp.setProperty("vhd.audio.micin.force", "2");
        } else {
            if (SystemUtil.getMtkVersion().equals("v0.0.3")) {
                SystemProp.setProperty("vhd.audio.micin.force", "1");
            } else {
                SystemProp.setProperty("vhd.audio.micin.force", "4");
            }
        }

        if (DeviceSettingManager.getInstance().getFromSp().getIndexAudioOutput() == 0) {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "0");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
        } else if (DeviceSettingManager.getInstance().getFromSp().getIndexAudioOutput() == 1) {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "0");
            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
        } else if (DeviceSettingManager.getInstance().getFromSp().getIndexAudioOutput() == 2) {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
            SystemProp.setProperty("vhd.lineout.audio.mute", "0");
            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
        } else {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
            SystemProp.setProperty("vhd.usbout.audio.mute", "0");
        }
    }


    @Override
    public void onLoginSuccess() {
        initText();
        RoomClient.get().getNotFinishedRoom();
    }

    @Override
    public void onLoginFailed() {

    }

    private void registerNetworkCallback() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    class NetworkChangReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isConnected(NetworkUtils.isNetworkAvailable());
        }
    }
}

