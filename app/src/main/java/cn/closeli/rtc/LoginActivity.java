
package cn.closeli.rtc;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.closeli.rtc.command.LoginStateListener;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.constract.WsErrorCode;
import cn.closeli.rtc.model.http.AccountModel;
import cn.closeli.rtc.model.http.AddrResp;
import cn.closeli.rtc.model.http.LoginResp;
import cn.closeli.rtc.model.http.SudiException;
import cn.closeli.rtc.model.http.SudiHttpCallback;
import cn.closeli.rtc.model.http.TokenResp;
import cn.closeli.rtc.model.ws.WsError;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.RoomEventAdapter;
import cn.closeli.rtc.room.WssMethodNames;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ext.WorkThreadFactory;
import cn.closeli.rtc.utils.net.NetType;
import cn.closeli.rtc.utils.net.NetworkUtils;
import cn.closeli.rtc.utils.signature.SignatureUtil;
import cn.closeli.rtc.widget.FocusLayout;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;
import cn.closeli.rtc.widget.popwindow.AddressServerPopup;
import cn.closeli.rtc.widget.popwindow.DevicePopupWindow;
import cn.closeli.rtc.widget.popwindow.NetSettingsPopupWindow;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, LoginStateListener {
    private String logTag = this.getClass().getCanonicalName();
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private static final int MY_PERMISSIONS_REQUEST = 103;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageView eye;
    private TextView checkedTextView;
    private FocusLayout mFocusLayout;
    private LinearLayout ll_remeber;
    private ImageView iv_status;
    private TextView tv_network;
    private TextView tv_server_config;
    private ImageView img_close;
    private TextView tv_net_status;
    private ImageView iv_net_status;
    private ConstraintLayout cl_main;
    private TextView tv_time;
    private TextView tv_error;
    private InviteJoinDialog inviteJoinDialog;
    private long mExitTime;
    private boolean isEyeSelected = false;
    private boolean isClickRemeber;
    private ProgressDialog pd;
    private IntentFilter intentFilter;
    private NetworkChangReceiver networkChangeReceiver;
    private SimpleDateFormat format;
    private ThreadPoolExecutor executorService;
    private MyHandler h = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avoidLauncherAgain();
        setContentView(R.layout.activity_login);
//        StatusBarUtil.setColor(LoginActivity.this, getResources().getColor(R.color.color_translate));
//        RoomClient.get().addRoomEventCallback(LoginActivity.class, adapter);
//        showStatusBar();
        cl_main = findViewById(R.id.cl_main);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        checkedTextView = findViewById(R.id.remember_pwd);
        ll_remeber = findViewById(R.id.ll_remeber);
        iv_status = findViewById(R.id.iv_status);
        tv_network = findViewById(R.id.tv_network);
        tv_server_config = findViewById(R.id.tv_server_config);
        img_close = findViewById(R.id.img_close);
        tv_net_status = findViewById(R.id.tv_net_status);
        iv_net_status = findViewById(R.id.iv_net_status);
        tv_time = findViewById(R.id.tv_time);
        tv_error = findViewById(R.id.tv_error);
        eye = findViewById(R.id.iv_pwd_eye);
        eye.setSelected(isEyeSelected);
        RoomClient.get().addRoomEventCallback(LoginActivity.class, roomEventAdapter);
        usernameEditText.setText(SPEditor.instance().getAccount());
        passwordEditText.setText(SPEditor.instance().getPWD());
        isClickRemeber = SPEditor.instance().getBoolean(SPEditor.PASSWORD_STATUS, true);
        if (isClickRemeber) {
            iv_status.setImageResource(R.drawable.icon_cb_square_checked);
        } else {
            iv_status.setImageResource(R.drawable.icon_cb_square_unchecked);
        }
        ll_remeber.setOnClickListener(this);
        tv_network.setOnClickListener(this);
        img_close.setOnClickListener(this);
        askForPermissions();
        checkLocalMac();
//        ServerManager.get().initStartServer();
        passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEditText.setKeyListener(DigitsKeyListener.getInstance(getString(R.string.digits)));
        if (!TextUtils.isEmpty(SPEditor.instance().getString(SPEditor.BASE_URL))) {
            SudiHttpClient.baseUrl = SPEditor.instance().getString(SPEditor.BASE_URL);
        }
        DeviceSettingManager.getInstance().setCameraControl("memory_recall", "0");
        RoomClient.get().setLoginStateListener(this);
        registerNetworkCallback();

        executorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new WorkThreadFactory());
        initLocalTime();
    }

    private void registerNetworkCallback() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void avoidLauncherAgain() {
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot()) { // 判断当前activity是不是所在任务栈的根
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }
    }

    private void checkLocalMac() {
        if (!SPEditor.instance().getString(SPEditor.MAC_ADDR, "").isEmpty()) {
            Log.i(logTag, "checkLocalMac: exist: " + SPEditor.instance().getString(SPEditor.MAC_ADDR));
            return;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean wifiEnable = wifiManager.isWifiEnabled();
                if (!wifiEnable) {
                    wifiManager.setWifiEnabled(true);
                }
                for (int i = 0; i < 50; i++) {
                    Log.i(logTag, "checkLocalMac: getMacAddress count = " + i);
                    String mac = SystemUtil.getMacAddress();
                    if (!mac.isEmpty() && !mac.equals("02:00:00:00:00:00")) {
                        SPEditor.instance().setString(SPEditor.MAC_ADDR, mac);
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!wifiEnable) {
                    wifiManager.setWifiEnabled(false);
                }
                Log.i(logTag, "checkLocalMac: new: " + SPEditor.instance().getString(SPEditor.MAC_ADDR));
            }
        }, 0L);
    }

    private void showStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RoomClient.get().setForceLogin(false);
        RoomClient.get().invalidAccessedFlag();
        unregisterReceiver(networkChangeReceiver);
        unregisterReceiver(receiver);
        h.removeMessages(100);
        h = null;
        executorService.shutdown();
//        ServerManager.get().stop();

    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime < 1000) {
            finish();
        } else {
            android.widget.Toast.makeText(this, "再按一次退出", android.widget.Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    public void logIn(View view) {
        login();
    }

    private void login() {
        if (SystemUtil.isC9Z() && !SystemUtil.isHisiNormal()) {
            Log.e(logTag, "login: hisi init incomplete, please wait a moment.");
            android.widget.Toast.makeText(this, "设备初始化中，请稍后...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordEditText.getText().toString().length() < 6) {
            UIUtils.toastMessage("密码最少为6位");
            return;
        }

        pd = ProgressDialog.show(this, "", "正在登录中，请稍后……");
        pd.setCancelable(true);
        RoomClient.get().invalidAccessedFlag();
        String user = usernameEditText.getText().toString();
        String pwd = passwordEditText.getText().toString();
        SudiHttpClient.get().getToken(user, pwd, new SudiHttpCallback<TokenResp>() {
            @Override
            public void onSuccess(TokenResp response) {
                L.d("getToken onSuccess %1$s", response.toString());
                SPEditor.instance().saveTokenResp(response);

                SudiHttpClient.get().httpLogin(user, pwd, new SudiHttpCallback<LoginResp>() {
                    @Override
                    public void onSuccess(LoginResp response) {
                        L.d("httpLogin onSuccess %1$s", response.toString());


                        SPEditor.instance().saveUser(response);
                        if (isClickRemeber) {
                            SPEditor.instance().setString(SPEditor.PASSWORD, pwd);
                        } else {
                            SPEditor.instance().setString(SPEditor.PASSWORD, "");
                        }
                        SPEditor.instance().setBoolean(SPEditor.PASSWORD_STATUS, isClickRemeber);
//                         go GroupAct
                        SudiHttpClient.get().getAddr(new SudiHttpCallback<AddrResp>() {
                            @Override
                            public void onSuccess(AddrResp response) {
                                L.d("getAddr onSuccess %1$s", response.toString());
                                SPEditor.instance().saveAddrs(response);
                                RoomClient.get().prepare(LoginActivity.this, response.getSignalAddrList().get(0));

                            }

                            @Override
                            public void onFailed(Throwable e) {
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                    pd = null;
                                }
                                L.d("getAddr onFailed %1$s", e.getMessage());
                                UIUtils.toastMessage(e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                            pd = null;
                        }
                        L.d("httpLogin onFailed %1$s", e.getMessage());
                        UIUtils.toastMessage(e.getMessage());
                    }
                });
            }

            @Override
            public void onFailed(Throwable e) {
                if (e instanceof SudiException) {
                    if (Constract.ERROR_12001 == (((SudiException) e).getCode())) {
                        tv_error.setText("账号或密码错误");
                    } else {
                        tv_error.setText("服务器异常");
                    }
                } else {
                    tv_error.setText("服务器异常");
                }
                tv_error.setVisibility(View.VISIBLE);
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                    pd = null;
                }
                L.d("getToken onFailed %1$s", e.getMessage());
//                new AlertDialog.Builder(LoginActivity.this)
//                        .setTitle("登录")
//                        .setMessage(e.getMessage())
//                        .setPositiveButton("确定", null)
//                        .show();
            }
        });
    }

    public void clickEye(View view) {
        isEyeSelected = !isEyeSelected;
        eye.setSelected(isEyeSelected);
        if (isEyeSelected) {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType
//                    .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
//            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    public void forgetPwd(View view) {

        UIUtils.toastMessage("请到其他端上完成密码重置");
    }


    public void getAddressList(View view) {
        AddressServerPopup addressServerPopup = new AddressServerPopup(this);
        addressServerPopup.setLayoutChoose(new AddressServerPopup.ServerPopup() {
            @Override
            public void layout(int position, String address) {
                if (position == 0) {
                } else if (position == 1) {
                    if (!TextUtils.isEmpty(address)) {
                        SudiHttpClient.baseUrl = address;
                        SPEditor.instance().setString(SPEditor.BASE_URL, address);
                    }
                }
            }
        });
        addressServerPopup.showAtLocation(cl_main, Gravity.RIGHT, 0, 0);

    }

    private void askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    private boolean arePermissionGranted() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoomClient.get().setCurrentActivity(this);
        mFocusLayout = new FocusLayout(this);
        mFocusLayout.addIngoreIds(R.id.tv_network);
        bindListener();//绑定焦点变化事件
        addContentView(mFocusLayout,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));//添加焦点层

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
            pd = null;
        }
    }

    private RoomEventAdapter roomEventAdapter = new RoomEventAdapter() {

        @Override
        public void onWebSocketFailMessage(WsError wsErrorr, String method) {
            runOnUiThread(() -> {
                if (RoomClient.get().getCurrentActivity() instanceof LoginActivity) {
                    if (WssMethodNames.accessIn.equals(method)) {
                        if (wsErrorr.getCode() != WsErrorCode.ERROR_ALREADY_ONLINE) {
                            UIUtils.toastMessage(wsErrorr.getMessage());
                        } else {
                            RoomClient.get().setAccessInSuccess(false);
                            showForceDialog();
                        }
//                        UIUtils.toastMessage(wsErrorr.getMessage());
                    }

                }
            });
        }

//        @Override
//        public void resultOfLoginApplyNotify(SingleLoginModel model) {
//            runOnUiThread(() -> {
//                if (model.isLoginAllowable()) {     //允许登录
//                    L.d("websocket ------>> 允许登录");
//                    CallUtil.asyncCall(1000, () -> {
//                        login();
//                    });
//                } else {                            //不允许登录
//                    UIUtils.toastMessage("无法登录");
//                    RoomClient.get().invalidAccessedFlag();
//                    RoomClient.get().close();
//                }
//            });
//
//        }
    };

    private void bindListener() {
        //获取根元素
        View mContainerView = this.getWindow().getDecorView();//.findViewById(android.R.id.content);
        //得到整个view树的viewTreeObserver
        ViewTreeObserver viewTreeObserver = mContainerView.getViewTreeObserver();
        //给观察者设置焦点变化监听
        viewTreeObserver.addOnGlobalFocusChangeListener(mFocusLayout);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_remeber) {
            if (isClickRemeber) {
                iv_status.setImageResource(R.drawable.icon_cb_square_unchecked);
                isClickRemeber = false;
            } else {
                iv_status.setImageResource(R.drawable.icon_cb_square_checked);
                isClickRemeber = true;
            }
        } else if (v.getId() == R.id.tv_network) {
            NetSettingsPopupWindow window = new NetSettingsPopupWindow(this);
            window.showAsLeft(tv_network.getRootView());
        } else if (v.getId() == R.id.img_close) {
            DevicePopupWindow popupWindow = new DevicePopupWindow(this, false);
            popupWindow.showAsDropUp(img_close);
        }
    }

    //登录方展示强制登录的弹窗
    private void showForceDialog() {
        if (isFinishing()) {
            return;
        }
        if (inviteJoinDialog != null) {
            inviteJoinDialog.dismiss();
            inviteJoinDialog = null;
        }

        inviteJoinDialog = new InviteJoinDialog();
        inviteJoinDialog.setTitle("当前账号正在线，是否强制登录?");
        inviteJoinDialog.setStrComfirm("确定");
        inviteJoinDialog.setStrCancel("取消");
        inviteJoinDialog.setOnDialogClick((dialog, index) -> {
            if (index == 1) {
                RoomClient.get().setForceLogin(true);
                login();
            } else {
                RoomClient.get().accessOut();
            }
            dialog.dismiss();

        });

        inviteJoinDialog.show(getSupportFragmentManager(), "forcelogin");
    }

    @Override
    public void onLoginSuccess() {
        runOnUiThread(() -> {
            tv_error.setVisibility(View.GONE);
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
                pd = null;
            }
        });

    }

    @Override
    public void onLoginFailed() {
        runOnUiThread(() -> {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
                pd = null;
            }
        });
    }

    private void initLocalTime() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, intentFilter);
        Date date = new Date();
        format = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        String formatTime = format.format(date);
        String ymd = formatTime.substring(0, formatTime.indexOf(" "));
        String hour = formatTime.substring(formatTime.indexOf(" ") + 1);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String amOrPm = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

        tv_time.setText(hour);
    }

    //时钟接收广播
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //系统每分钟发出
            if (Intent.ACTION_TIME_TICK.equals(action)) {
                if (format == null) {
                    format = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
                }

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {

                        String formatTime = format.format(new Date());
                        Message msg = Message.obtain();
                        msg.what = 100;
                        Bundle b = new Bundle();
                        b.putString(Constract.BUNDLE_KEY_TIME, formatTime);
                        msg.setData(b);
                        h.sendMessage(msg);
                    }
                });

            }
        }
    };


    class NetworkChangReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.isNetworkAvailable()) {
                tv_net_status.setText("网络已链接");
                iv_net_status.setVisibility(View.VISIBLE);
                if (NetType.ETHERNET == NetworkUtils.getNetType()) {
                    iv_net_status.setImageDrawable(getDrawable(R.drawable.icon_net_ethernet));
                } else {
                    iv_net_status.setImageDrawable(getDrawable(R.drawable.icon_net_wifi));
                }
            } else {
                tv_net_status.setText("网络异常");
                iv_net_status.setVisibility(View.VISIBLE);
                iv_net_status.setImageDrawable(getDrawable(R.drawable.icon_net_error));
            }
        }

    }


    public class MyHandler extends Handler {
        private WeakReference<LoginActivity> weakReference;

        public MyHandler(LoginActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(@NonNull Message msg) {
            L.d("do this  --->>>" + msg.what);
            if (msg.what == 100) {
                String time = (String) msg.getData().get(Constract.BUNDLE_KEY_TIME);
                L.d("do this ---->>>>>>" + time);
                String ymd = time.substring(0, time.indexOf(" "));
                String hour = time.substring(time.indexOf(" ") + 1);

                tv_time.setText(hour);
            }
        }
    }
}

