package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.graphics.Color;
import android.icu.util.MeasureUnit;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.util.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.VideoConferenceActivity;
import cn.closeli.rtc.adapter.InviteJoinAdapter;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.UserDeviceList;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.RoomEventAdapter;
import cn.closeli.rtc.room.RoomEventCallback;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.widget.BasePopupWindow;
import cn.closeli.rtc.widget.UCToast;
import me.jessyan.autosize.internal.CustomAdapt;

//邀请入会弹窗 （带确认取消 选项）
public class InviteJoinInPopupWindow extends BasePopupWindow implements InviteJoinAdapter.OnItemCheckedListener{
    //    private Spinner spinner_city;       //城市
//    private Spinner spinner_part;       //部门
    private TextView tv_checked;
    private RecyclerView rcv_list;
    private Button btn_comfirm;
    private Button btn_cancel;
    private TextView tv_choose_part;
    private TextView tv_meet_psd;
    private TextView tv_meet_psd_content;
    private TextView tv_meet_theme_content;
    private TextView tv_meet_id_content;
    private TextView tv_meet_hour_content;

    private ScrollView scrollView;
    private LinearLayout ll_contair;

    private String channel;
    private ArrayList<UserDeviceModel> listData;     //接口返回的数据
    private List<UserDeviceModel> cacheList;    //确认选中的数据
    private List<UserDeviceModel> errorCacheList;       //错误数据
    private InviteJoinAdapter adapter;
    private Context context;

    public InviteJoinInPopupWindow(Context context, String channel) {
        super(context);
        this.context = context;
        this.channel = channel;

    }

    @Override
    public int thisLayout() {
        return R.layout.popup_invitejoin;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

//        spinner_city = fv(R.id.spinner_city);
//        spinner_part = fv(R.id.spinner_part);
        tv_checked = fv(R.id.tv_checked);
        rcv_list = fv(R.id.rcv_list);
        btn_comfirm = fv(R.id.btn_comfirm);
        btn_cancel = fv(R.id.btn_cancel);
        tv_choose_part = fv(R.id.tv_choose_part);
        tv_meet_psd = fv(R.id.tv_meet_psd);
        tv_meet_psd_content = fv(R.id.tv_meet_psd_content);
        tv_meet_theme_content =fv(R.id.tv_meet_theme_content);
        tv_meet_id_content = fv(R.id.tv_meet_id_content);
        tv_meet_hour_content = fv(R.id.tv_meet_hour_content);

        if (!TextUtils.isEmpty(DeviceSettingManager.getInstance().getFromSp().getPassword()) && DeviceSettingManager.getInstance().getFromSp().isUserPsd()) {
            tv_meet_psd_content.setText(DeviceSettingManager.getInstance().getFromSp().getPassword());
        }  else {
            tv_meet_psd.setVisibility(View.GONE);
            tv_meet_psd_content.setVisibility(View.GONE);
        }

        scrollView = fv(R.id.scroll);
        ll_contair = fv(R.id.ll_contair);
    }

    @Override
    public void doInitData() {

        cacheList = new ArrayList<>();
        errorCacheList = new ArrayList<>();

        RoomClient.get().getOrgList();

        /**
         * rcv 设置数据源
         */
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        listData = new ArrayList<>();
        adapter = new InviteJoinAdapter(mContext, listData);
        rcv_list.setLayoutManager(manager);
        rcv_list.setAdapter(adapter);
        adapter.setListener(this);

        tv_meet_theme_content.setText(RoomManager.get().getSubject());
        tv_meet_id_content.setText(RoomManager.get().getRoomId());
        tv_meet_hour_content.setText(DeviceSettingManager.getInstance().getFromSp().getDuration()+"小时");

        btn_comfirm.setOnClickListener(v -> {
            ArrayList<String> targetIds = new ArrayList<>();
            targetIds.clear();
            if (cacheList.size() > 0) {
                for (UserDeviceModel userDeviceModel : cacheList) {
                    if (TextUtils.isEmpty(userDeviceModel.getUserId())) {           //为空 跳过
                        continue;
                    } else {
                        targetIds.add(userDeviceModel.getUserId());
                    }
                }
                if (targetIds.size() != 0) {
                    RoomClient.get().inviteParticipant(channel, RoomManager.get().getUserId(), targetIds);
                }
            }

            if (cacheList.size() != 0 || errorCacheList.size() != 0) {
                dismiss();
            }

        });

        btn_cancel.setOnClickListener(v -> {
            dismiss();
        });


    }

    public void setData(ArrayList<UserDeviceModel> userDeviceModels) {
        this.listData = userDeviceModels;
        L.d("yuhuizhong");
        //todo 测试代码 20191015
//        List<PartcipantInfoModel> list = new ArrayList<>();
//        for (int i=0;i<10;i++) {
//            PartcipantInfoModel model = new PartcipantInfoModel("1");
//            model.setAccount("乔巴");
//            model.setAudioActive(true);
//            model.setHandStatus(CLRtcSinaling.VALUE_STATUS_UP);
//            model.setRole(ROLE_PUBLISHER_SUBSCRIBER);
//            model.setUserId("1002");
//            model.setVideoActive(true);
//            list.add(model);
//        }
        adapter.setDatas(listData);
    }

    public void setOrgData(OrgList orgList) {
        if (orgList == null || orgList.getOrganizationList() == null || orgList.getOrganizationList().size() == 0) {
            return;
        }
        RoomClient.get().getUserDeviceList(String.valueOf(orgList.getOrganizationList().get(0).getOrgId()));

        tv_choose_part.setOnClickListener(v -> {
            if (scrollView.getVisibility() == View.VISIBLE) {
                return;
            }
            scrollView.setVisibility(View.VISIBLE);
            ll_contair.removeAllViews();
            for (int i = 0; i < orgList.getOrganizationList().size(); i++) {
                String s = orgList.getOrganizationList().get(i).getOrganizationName();
                long orgId = orgList.getOrganizationList().get(i).getOrgId();
                TextView textView = new TextView(mContext);
                textView.setId(i);
                textView.setNextFocusRightId(R.id.btn_comfirm);
                textView.setText(s);
                textView.setTextColor(Color.WHITE);
                textView.setFocusable(true);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                textView.setBackgroundResource(R.drawable.bg_item_selector_white_line);
                textView.setPadding(UIUtils.dip2px(12), UIUtils.dip2px(4), UIUtils.dip2px(7), UIUtils.dip2px(7));
                textView.setOnClickListener(view -> {
                    tv_choose_part.setText(s);
                    RoomClient.get().getUserDeviceList(String.valueOf(orgId));
                    scrollView.setVisibility(View.GONE);
                });

                if (i == orgList.getOrganizationList().size()-1) {
                    textView.setNextFocusDownId(i);
                }

                ll_contair.addView(textView);
            }
        });
    }

    // item checkbox check事件
    @Override
    public void onItemChoose(int position, boolean isChecked) {
        if (isChecked) {
            if (!TextUtils.isEmpty(listData.get(position).getUserId())) {
                cacheList.add(listData.get(position));
            } else {
                errorCacheList.add(listData.get(position));
            }
        } else {
            if (!TextUtils.isEmpty(listData.get(position).getUserId())) {
                cacheList.remove(listData.get(position));
            } else {
                errorCacheList.remove(listData.get(position));
            }
        }
        int chooseNum = cacheList.size()+errorCacheList.size();
        tv_checked.setText("选中" + chooseNum + "人");
    }

    public void showChooseToast() {
        if (errorCacheList.size() == 0) {
            UCToast.show(mContext, "邀请" + cacheList.size() + "人成功！");
        } else {
            UCToast.show(mContext, "邀请" + cacheList.size() + "人成功！邀请"+errorCacheList.size()+"人失败！");
        }
    }
}
