package cn.closeli.rtc.widget.dialog;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import cn.closeli.rtc.R;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.widget.BaseDialogFragment;

public class AddressServerDialog extends BaseDialogFragment implements View.OnClickListener {
    private TextView tv_title;
    private TextView tv_cancel;
    private TextView tv_comfirm;
    private String title;
    private String strCancel;
    private String strComfirm;
    private OnDialogClick onDialogClick;
    boolean mDismissed;
    boolean mShownByMe;
    private String str;
    private EditText editText;
    //开发
    public static final String baseUrlOpen = "http://172.25.23.212:5000";
    //测试环境
    public static final String baseUrlTest = "https://172.25.21.255";
    //sss 公网
    public static final String baseUrlRelease = "http://119.23.239.146:5000";

    public static AddressServerDialog newInstance() {
        AddressServerDialog dialog = new AddressServerDialog();
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


        editText = findViewById(R.id.et_address);

        tv_cancel.setOnClickListener(this);
        tv_comfirm.setOnClickListener(this);
    }




    public interface OnDialogClick {
        void onClick(AddressServerDialog orderCancelDialog, int position, String text);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_address_server;
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
            onDialogClick.onClick(this, 0, str);

        } else if (i == R.id.tv_comfirm) {
            if (!TextUtils.isEmpty(editText.getText().toString())) {
                str = editText.getText().toString();
            }

            onDialogClick.onClick(this, 1, str);

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