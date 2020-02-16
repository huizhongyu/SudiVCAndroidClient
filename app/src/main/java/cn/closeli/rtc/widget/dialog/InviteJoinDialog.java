package cn.closeli.rtc.widget.dialog;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import cn.closeli.rtc.R;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.widget.BaseDialogFragment;

public class InviteJoinDialog extends BaseDialogFragment implements View.OnClickListener{
    private TextView tv_title;
    private TextView tv_cancel;
    private TextView tv_comfirm;
    private String title;
    private String strCancel;
    private String strComfirm;
    private OnDialogClick onDialogClick;
    boolean mDismissed;
    boolean mShownByMe;

    public static InviteJoinDialog newInstance() {
        InviteJoinDialog dialog = new InviteJoinDialog();
        return dialog;
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

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_comfirm = findViewById(R.id.tv_comfirm);


        tv_title.setText(title);
        tv_cancel.setText(strCancel);
        tv_comfirm.setText(strComfirm);


        tv_cancel.setOnClickListener(this);
        tv_comfirm.setOnClickListener(this);
    }


    public interface OnDialogClick {
        void onClick(InviteJoinDialog orderCancelDialog, int position);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_invite_join;
    }

    @Override
    protected void afterOnViewCreated() {
        initView();
    }

    @Override
    public void onClick(View v) {
        if (onDialogClick == null) {
            return;
        }
        int i = v.getId();
        if (i == R.id.tv_cancel) {
            onDialogClick.onClick(this,0);

        } else if (i == R.id.tv_comfirm) {


            onDialogClick.onClick(this,1);

        }
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStrCancel() {
        return strCancel == null ? "" : strCancel;
    }

    public void setStrCancel(String strCancel) {
        this.strCancel = strCancel;
    }

    public String getStrComfirm() {
        return strComfirm == null ? "" : strComfirm;
    }

    public void setStrComfirm(String strComfirm) {
        this.strComfirm = strComfirm;
    }

    public void setOnDialogClick(OnDialogClick onDialogClick) {
        this.onDialogClick = onDialogClick;
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
