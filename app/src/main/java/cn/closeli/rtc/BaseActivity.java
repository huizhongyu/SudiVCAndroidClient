package cn.closeli.rtc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocketState;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import cn.closeli.rtc.command.WebSocketStateListener;
import cn.closeli.rtc.constract.WsErrorCode;
import cn.closeli.rtc.model.InviteModel;
import cn.closeli.rtc.model.ws.WsError;
import cn.closeli.rtc.net.ServerManager;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.RoomEventAdapter;
import cn.closeli.rtc.room.SudiRole;
import cn.closeli.rtc.utils.AccountUtil;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.widget.FocusLayout;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.constract.Constract.JOIN_ACTIVE;
import static cn.closeli.rtc.constract.Constract.JOIN_INVITED;
import static cn.closeli.rtc.utils.Constants.STREAM_MAJOR;

//基类
public abstract class BaseActivity extends AppCompatActivity implements WebSocketStateListener, CustomAdapt {
    private boolean isExit;
    InviteJoinDialog inviteJoinDialog;
    private FocusLayout mFocusLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        initFocusView();
        super.onCreate(savedInstanceState);

        RoomClient.get().setWebSocketStateListener(this);
        RoomClient.get().addRoomEventCallback(BaseActivity.class, adapter);
        initFocusLayout();
    }

    /**
     * 初始化统一设置选择器
     */
    private void initFocusView() {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new LayoutInflater.Factory2() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                View view = getDelegate().createView(parent, name, context, attrs);
                return view;
            }

            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                return onCreateView(null, name, context, attrs);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void initFocusLayout(){
        if (mFocusLayout == null) {
            mFocusLayout = new FocusLayout(this);
            addContentView(mFocusLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //获取根元素
            View mContainerView = this.getWindow().getDecorView();//.findViewById(android.R.id.content);
            //得到整个view树的viewTreeObserver
            ViewTreeObserver viewTreeObserver = mContainerView.getViewTreeObserver();
            //给观察者设置焦点变化监听
            viewTreeObserver.addOnGlobalFocusChangeListener(mFocusLayout);
        }
    }

    protected void registeNoFocusViewId(int... id) {
        if (mFocusLayout == null) {
            return;
        }
        for (int i : id) {
            mFocusLayout.addIngoreIds(i);
        }
    }

    public void registerFocusedId(int id) {
        if (mFocusLayout == null || id < 0) {
            return;
        }
        mFocusLayout.addFocusedId(id);
    }

    protected void registerFocusedIds(Set<Integer> ids){
        mFocusLayout.addFocusedId(ids);
    }

    protected boolean useEventbus() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        RoomClient.get().removeRoomEventCallback(getClass());
        super.onDestroy();
    }

    public FocusLayout getmFocusLayout() {
        return mFocusLayout;
    }

    //websocket
    public abstract void onStateChange(WebSocketState socketState);

    protected void onSockedConnectFail() {
    }

    protected void onAccessInSuccess() {
    }

    @Override
    public void onStateChangeListener(WebSocketState socketState) {

        onStateChange(socketState);
    }

    @Override
    public void onSocketConnectedFail() {
        onSockedConnectFail();
    }

    @Override
    public void onAccessInSuccessCallback() {
        onAccessInSuccess();
    }

    private void doJoinRoom(String roomId, String roomPwd, SudiRole sudiRole, String joinType) {
//        RoomClient.get().getRoomLayout(roomId);
        RoomClient.get().joinRoom(roomId, roomPwd, sudiRole, 0, STREAM_MAJOR, joinType);

    }

    private RoomEventAdapter adapter = new RoomEventAdapter() {

        @Override
        public void onWebSocketFailMessage(WsError wsErrorr, String method) {
            runOnUiThread(() -> {
                if (wsErrorr.getCode() == WsErrorCode.ERROR_TOKEN_INVALID) {                //token 失效
                    UIUtils.toastMessage(wsErrorr.getMessage());
                    RoomClient.get().setForceLogin(false);
                    RoomClient.get().invalidAccessedFlag();
                    ActivityUtils.getInstance().finishAllActivity();
                    //重新登陆
                    RoomClient.get().accessOut();
                } else if (wsErrorr.getCode() == WsErrorCode.ERROR_ALREADY_ONLINE) {
                    RoomClient.get().setAccessInSuccess(false);
                }
            });

        }

        //通知远程已登录的用户强制下线
        @Override
        public void remoteLoginNotify() {
            runOnUiThread(() -> {
                L.d("do this ---->>>base remoteLoginNotify"+RoomClient.get().getCurrentActivity());
                UCToast.show(BaseActivity.this, "您的账号被强制下线");
                if(RoomClient.get().getCurrentActivity() instanceof MCUVideoActivity) {
                    ((MCUVideoActivity)RoomClient.get().getCurrentActivity()).onRemoteLoginNotify();
                } else if (RoomClient.get().getCurrentActivity() instanceof VideoConferenceActivity) {
                    ((VideoConferenceActivity)RoomClient.get().getCurrentActivity()).onRemoteLoginNotify();
                } else {
                        RoomClient.get().accessOut();
                }
//                SudiHttpClient.get().logOut(new SudiHttpCallback<JsonObject>() {
//                    @Override
//                    public void onSuccess(JsonObject response) {
//                        L.d("logOut onSuccess %1$s", response.toString());
//                        RoomClient.get().accessOut();
//                    }
//
//                    @Override
//                    public void onFailed(Throwable e) {
//                        L.d("logOut onFailed %1$s", e.getMessage());
//                    }
//                });

            });
        }

        @Override
        public void onAccessOut() {
            runOnUiThread(() -> {
                RoomClient.get().setForceLogin(false);
                RoomClient.get().invalidAccessedFlag();
                RoomClient.get().close();
                ActivityUtils.getInstance().finishAllActivity();
            });
        }

        @Override
        public void onInviteJoinSuccess(InviteModel model) {
            L.d("do this ---onInviteJoinSuccess >>>>" +RoomClient.get().getCurrentActivity()+"  - "+model.targetId+" , "+SPEditor.instance().getUserId());
            if (model.targetId.equals(SPEditor.instance().getUserId()) && !(RoomClient.get().getCurrentActivity() instanceof MCUVideoActivity)) {
                //web 端创建会议
                if (SPEditor.instance().getUserId().equals(model.sourceId)) {
                    //
                    RoomManager.get().setHostId(model.sourceId);
                    doJoinRoom(model.roomId, "", SudiRole.moderator, JOIN_INVITED);
                    return;
                }
                if (DeviceSettingManager.getInstance().getFromSp().isDeviceAuto()) {
                    ServerManager.get().userInfoToPad();
                    RoomManager.get().setHostId(model.sourceId);
                    doJoinRoom(model.roomId, "", SudiRole.publisher, JOIN_INVITED);
                } else {
                    if (inviteJoinDialog != null) {
                        inviteJoinDialog.dismiss();
                    }
                    L.d("do this ---onInviteJoinSuccess >>>> show");
                    inviteJoinDialog = InviteJoinDialog.newInstance();
                    inviteJoinDialog.setTitle(getString(R.string.invite_join));
                    inviteJoinDialog.setTitle(TextUtils.isEmpty(model.getUsername()) ? model.getDeviceName() + "邀请你加入会议室" : model.getUsername() + "邀请你加入会议室");
                    inviteJoinDialog.setStrComfirm("接受");
                    inviteJoinDialog.setStrCancel("拒绝");
                    inviteJoinDialog.setOnDialogClick(new InviteJoinDialog.OnDialogClick() {
                        @Override
                        public void onClick(InviteJoinDialog orderCancelDialog, int position) {
                            if (position == 1) {
                                ServerManager.get().userInfoToPad();
                                RoomManager.get().setHostId(model.sourceId);
                                doJoinRoom(model.roomId, "", SudiRole.publisher, JOIN_INVITED);
                            } else {
                                RoomClient.get().refuseInvite(model.roomId, SPEditor.instance().getUserId());
                            }
                            orderCancelDialog.dismiss();
                        }
                    });
                    inviteJoinDialog.show(RoomClient.get().getCurrentActivity().getSupportFragmentManager(),
                            "CloseDialog");
                }
            }
        }

        //
//        @Override
//        public void onAccessOut() {
//            RoomClient.get().close();
//            runOnUiThread(() -> {
//                RoomClient.get().invalidAccessedFlag();
//                ActivityUtils.getInstance().finishAllActivity();
//            });
//        }
    };
//
//    private void httpLoginOut() {
//        SudiHttpClient.get().logOut(new SudiHttpCallback<JsonObject>() {
//            @Override
//            public void onSuccess(JsonObject response) {
//                L.d("logOut onSuccess %1$s", response.toString());
//                RoomClient.get().accessOut();
//            }
//
//            @Override
//            public void onFailed(Throwable e) {
//                L.d("logOut onFailed %1$s", e.getMessage());
//            }
//        });
//    }


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
}
