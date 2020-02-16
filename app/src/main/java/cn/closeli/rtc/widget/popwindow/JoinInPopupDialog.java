package cn.closeli.rtc.widget.popwindow;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.closeli.rtc.GroupActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.SudiRole;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.widget.BaseDialogFragment;
import cn.closeli.rtc.widget.BasePopupWindow;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;

import static cn.closeli.rtc.utils.Constants.STREAM_MAJOR;

/**
 * 加入会议 弹窗
 */
public class JoinInPopupDialog extends BaseDialogFragment {
    private EditText et_id;
    private EditText et_pwd;
    private CheckBox cb_close_audio;
    private CheckBox cb_open_audio;
    private CheckBox cb_open_video;
    private CheckBox cb_close_video;
    private TextView tv_join;
    private boolean switch_auto = true;
    private boolean switch_camera = true;
    private ArrayList<String> list = new ArrayList<>();
    private Set<String> set;
    private ImageView iv_account;
    private AccountListPopup accountListPopup;
    private View cl_main;
    boolean mDismissed;
    boolean mShownByMe;

    public static JoinInPopupDialog newInstance() {
        JoinInPopupDialog dialog = new JoinInPopupDialog();
        return dialog;
    }

    private void initView() {
        et_id = findViewById(R.id.et_id);
        et_pwd = findViewById(R.id.et_pwd);

        cb_open_audio = findViewById(R.id.cb_open_audio);
        cb_close_audio = findViewById(R.id.cb_close_audio);
        cb_open_video = findViewById(R.id.cb_open_video);
        cb_close_video = findViewById(R.id.cb_close_video);


        tv_join = findViewById(R.id.tv_join);
        iv_account = findViewById(R.id.iv_account);
        doInitData();
    }

    @Override
    protected void beforeOnViewCreated() {
        super.beforeOnViewCreated();
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());
    }

    public void checkAudio(boolean check_audio) {
        if (check_audio) {
            switch_auto = true;
            cb_open_audio.setChecked(true);
            cb_close_audio.setChecked(false);
        } else {
            switch_auto = false;
            cb_open_audio.setChecked(false);
            cb_close_audio.setChecked(true);
        }
    }

    public void checkVideo(boolean check_video) {
        if (check_video) {
            switch_camera = true;
            cb_open_video.setChecked(true);
            cb_close_video.setChecked(false);
        } else {
            switch_camera = false;
            cb_open_video.setChecked(false);
            cb_close_video.setChecked(true);
        }
    }

    public void doInitData() {

        DeviceSettingManager.DeviceModel model = DeviceSettingManager.getInstance().getFromSp();

        if ("on".equals(model.getIsSettingMicroOn())) {
            checkAudio(true);
        } else {
            checkAudio(false);
        }

        if ("on".equals(model.getIsSettingCameraOn())) {
            checkVideo(true);
        } else {
            checkVideo(false);
        }

        tv_join.setOnClickListener(v -> {
            if (TextUtils.isEmpty(et_id.getText().toString())) {
                UCToast.show(getContext(), getContext().getString(R.string.str_toast_input_empty));
                return;
            }
            if (ViewUtils.isFastClick()) {
                return;
            }
            joinRoom();
        });
        cb_open_audio.setOnClickListener(v -> {
            checkAudio(true);

        });
        cb_close_audio.setOnClickListener(v -> {
            checkAudio(false);

        });
        cb_open_video.setOnClickListener(v -> {
            checkVideo(true);
        });
        cb_close_video.setOnClickListener(v -> {
            checkVideo(false);
        });
        iv_account.setOnClickListener(v -> {
            accountListPopup = new AccountListPopup(et_id.getContext(), list);
            accountListPopup.setOnRecycViewClick(str -> {
                et_id.setText(str);
                accountListPopup.dismiss();
            });
            accountListPopup.showAsDropDown(et_id, 0, 8);
        });
        if (!TextUtils.isEmpty(SPEditor.instance().getString(SPEditor.JOIN_ACCOUNT))) {
            et_id.setText(SPEditor.instance().getString(SPEditor.JOIN_ACCOUNT));
        }

        set = SPEditor.instance().getSet(SPEditor.JOIN_ROOM);
        if (set == null) {
            set = new HashSet<>();
        } else {
            list.clear();
            int index = 0;
            for (String str : set) {
                if (index++ > 5) {
                    break;
                }
                list.add(str);
            }
        }
        iv_account.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void joinRoom() {
        String roomId = et_id.getText().toString();
        String roomPwd = et_pwd.getText().toString();
        int chnIdLen = roomId.length();
        RoomManager.get().setNativeAudio(switch_auto);
        RoomManager.get().setNativeVideo(switch_camera);
        set.add(roomId);
        SPEditor.instance().setStringSet(SPEditor.JOIN_ROOM, set);
        SPEditor.instance().setString(SPEditor.JOIN_ACCOUNT, roomId);

        doJoinRoom(roomId, roomPwd, SudiRole.publisher, Constract.JOIN_ACTIVE);
    }

    /**
     * 加入房间
     *
     * @param roomId
     * @see GroupActivity#create(View)
     * @see GroupActivity#join(View)
     */
    private void doJoinRoom(String roomId, String roomPwd, SudiRole sudiRole, String joinType) {
        RoomClient.get().joinRoom(roomId, roomPwd, sudiRole, 0, STREAM_MAJOR, joinType);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.popup_joinin;
    }

    @Override
    protected void afterOnViewCreated() {
        initView();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        mDismissed = false;
        mShownByMe = true;
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

}
