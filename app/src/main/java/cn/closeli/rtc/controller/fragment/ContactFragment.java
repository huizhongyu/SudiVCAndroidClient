package cn.closeli.rtc.controller.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.InvitePersonAdapter;
import cn.closeli.rtc.controller.SettingsActivity;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.PartDeviceModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.UserDeviceList;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.UIUtils;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 终端设置 - 联系人
 */
public class ContactFragment extends Fragment implements CustomAdapt {
    private RecyclerView rcv_setting_org_list;
    private LinearLayout ll_title_contanir;
    private ImageView iv_empty;
    private TextView tv_empty_tip;
    private LinearLayout ll_contanir;
    private TextView tv_rcv_top_title;
    private CheckBox cb_rcv_top_state;
    private RelativeLayout rl_rcv_top;
    private List<UserDeviceModel> list;
    private InvitePersonAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        rcv_setting_org_list = view.findViewById(R.id.rcv_setting_org_list);
        ll_title_contanir = view.findViewById(R.id.ll_title_contanir);
        rcv_setting_org_list = view.findViewById(R.id.rcv_setting_org_list);
        ll_contanir = view.findViewById(R.id.ll_contanir);
        iv_empty = view.findViewById(R.id.iv_empty);
        tv_empty_tip = view.findViewById(R.id.tv_empty_tip);
        tv_rcv_top_title = view.findViewById(R.id.tv_rcv_top_title);
        cb_rcv_top_state = view.findViewById(R.id.cb_rcv_top_state);
        rl_rcv_top = view.findViewById(R.id.rl_rcv_top);

        ((SettingsActivity) getActivity()).childFragmentViewRequestFocus(this);
        initData();
        initView();
        initFocusMove();
        return view;
    }

    private void initFocusMove() {
        ll_title_contanir.setNextFocusDownId(((SettingsActivity)getActivity()).getIdByPosition(2));
        ll_title_contanir.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(2));
    }

    private void initView() {
        ll_title_contanir.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((SettingsActivity) getActivity()).setContactItemChecked();
                }
            }
        });

        rcv_setting_org_list.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((SettingsActivity) getActivity()).setContactItemChecked();
                }
            }
        });
    }

    private void initData() {
        list = new ArrayList<>();
        adapter = new InvitePersonAdapter(getContext(),list,false);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rcv_setting_org_list.setLayoutManager(manager);
        rcv_setting_org_list.setAdapter(adapter);

//        ll_title_contanir.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
                RoomClient.get().getDepartmentTree();
//            }
//        });
    }

    public void notifyOrgList(CompanyModel orgList) {
        if (iv_empty.getVisibility() == View.VISIBLE) {
            iv_empty.setVisibility(View.GONE);
        }

        if (tv_empty_tip.getVisibility() == View.VISIBLE) {
            tv_empty_tip.setVisibility(View.GONE);
        }

        ll_contanir.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2px(40));
        View view = View.inflate(getContext(),R.layout.item_contact_company,null);
        ConstraintLayout rootCompany = view.findViewById(R.id.constaint_company_root);
        ImageView iv_icon_company = view.findViewById(R.id.iv_icon_company);
        TextView tv_title_company = view.findViewById(R.id.tv_title_company);
        tv_title_company.setText(orgList.getOrganizationName());
        rootCompany.setBackgroundColor(getResources().getColor(R.color.color_3c4779));
        view.setLayoutParams(params);
        ll_contanir.addView(view);
        rootCompany.setOnClickListener(v-> {
            RoomClient.get().getSubDevOrUser(String.valueOf(orgList.getOrgId()));
        });

        if (orgList.getOrganizationList() != null && orgList.getOrganizationList().size() > 0) {
            setSubItem(orgList.getOrganizationList());
        }
        rl_rcv_top.setVisibility(View.VISIBLE);

        if(orgList.getOrganizationList() != null & orgList.getOrganizationList().size() >0) {
            RoomClient.get().getSubDevOrUser(String.valueOf(orgList.getOrgId()));
        }
    }

    //添加子部门
    public void setSubItem(List<OrgList> orgList) {
        for(OrgList org : orgList) {
            View view = View.inflate(getContext(),R.layout.item_contact_part,null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2px(40));
            ConstraintLayout constraint_part_root = view.findViewById(R.id.constraint_part_root);
            constraint_part_root.setOnClickListener(v-> {
                RoomClient.get().getSubDevOrUser(String.valueOf(org.getOrgId()));
            });
            TextView tv_title_part = view.findViewById(R.id.tv_title_part);
            tv_title_part.setText(org.getOrganizationName());
            view.setLayoutParams(params);
            ll_contanir.addView(view);
            view.setNextFocusLeftId(R.id.ll_title_contanir);
            if (org.getOrganizationList() != null && org.getOrganizationList().size()>0) {
                setSubItem(org.getOrganizationList());
            }
        }
    }

    //更新RecyclerView 数据
    public void notifyRcvData(List<UserDeviceModel> listOfDevice) {
        list.clear();
        list = listOfDevice;
        adapter.setData(list);
    }



    public void findFirstFocus() {
        ll_title_contanir.requestFocus();
    }

    public void showError() {
        if (ll_contanir.getVisibility() == View.VISIBLE) {
            ll_contanir.setVisibility(View.GONE);
        }
        if (rcv_setting_org_list.getVisibility() == View.VISIBLE) {
            rcv_setting_org_list.setVisibility(View.GONE);
        }

        if (rl_rcv_top.getVisibility() == View.VISIBLE) {
            rl_rcv_top.setVisibility(View.GONE);
        }

        iv_empty.setVisibility(View.VISIBLE);
        tv_empty_tip.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity)getActivity()).getmFocusLayout().addIngoreIds(R.id.rcv_setting_org_list);
        ((SettingsActivity)getActivity()).getmFocusLayout().addIngoreIds(R.id.constaint_item_device);
    }

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
