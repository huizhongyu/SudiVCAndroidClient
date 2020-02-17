package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.InvitePersonAdapter;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.GroupListInfoModel;
import cn.closeli.rtc.model.info.GroupModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.widget.BasePopupWindow;
import cn.closeli.rtc.widget.UCToast;

import static cn.closeli.rtc.constract.Constract.BUNDLE_FOCUS;
import static cn.closeli.rtc.constract.Constract.MSG_FOCUS;
import static cn.closeli.rtc.constract.Constract.MSG_FOCUS_GROUP;

/**
 * 新邀请入会
 */
public class InvitePersonPopupWindow extends BasePopupWindow implements InvitePersonAdapter.OnInviteItemClickListener, View.OnClickListener, View.OnFocusChangeListener {
    private final int DELAY_TIME = 300;
    private LinearLayout ll_contanir;
    private LinearLayout ll_cache_contair;      //缓存的数据
    private ScrollView scroll_cache;
    private TextView tv_ignore;
    private RecyclerView rcv_has_choose;
//    private CheckBox cb_second_head;
    private TextView tv_start_meet;
    private TextView tv_third_head;
    private TextView tv_title_item_company;
    private TextView tv_title_item_group;
    private TextView tv_has_choose;

    private List<UserDeviceModel> list = new ArrayList<>();         //展示的list
    private InvitePersonAdapter adapter;        //展示设备的adapter
    private boolean isCheck;
    private int fromType;
    private Map<String, UserDeviceModel> modelMap = new HashMap<>();
    private Map<String, String> serialMap = new HashMap<>();
    private String channel;
    private boolean isModelFromGroup = true;           //是否是 群组的数据
    private boolean isClickAllChoose = false;           //是否点击全选
    private long currentClickOrgId = 0;
    private long currentClickGroupId = 0;
    private Map<Long,View> mapOfTempLinear = new HashMap<>();
    private Map<Long,Boolean> currentChooseAllState;  //当前item全选状态
    private Map<String,View> currentExpandItem;         //当前展开的设备item
    private Map<Long,View> currentExpandCompanyItem;      //当前展开的企业item
    private Map<String, UserDeviceModel> tempModelMap;       //当前item下的设备集合 临时

    private Map<Long,View> mapOfTempGroup;          //临时group
    private Map<Long,Boolean> currentChooseAllStateGroup;   //当前group 选中
    private Map<String,View> currentExpandItemGroup;

    public InvitePersonPopupWindow(Context context, String channel, int fromType) {
        super(context);
        this.fromType = fromType;
        this.channel = channel;
        if (fromType != Constract.INVITE_FROMTYPE_MEET) {
            modelMap = DeviceSettingManager.getInstance().getFromSp().getModelMap();

            for (UserDeviceModel um : modelMap.values()) {
                serialMap.put(um.getSerialNumber(), um.getUserId());
                list.add(um);
            }
        }
        initView();
        initData();
//        if (fromType != Constract.INVITE_FROMTYPE_MEET) {
//            addLastData();
//         }

//        mapOfTempLinear = new HashMap<>();
        currentChooseAllState =new HashMap<>();
        currentExpandItem = new HashMap<>();
        currentExpandCompanyItem = new HashMap<>();
        tempModelMap = new HashMap<>();

        mapOfTempGroup = new HashMap<>();
        currentChooseAllStateGroup = new HashMap<>();
        currentExpandItemGroup = new HashMap<>();
//        RoomClient.get().getDepartmentTree();
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_inviteperson;
    }

    @Override
    public void doInitView() {

    }

    @Override
    public void doInitData() {

    }

    public void initView() {
        ll_contanir = fv(R.id.ll_contanir);
        tv_start_meet = fv(R.id.tv_start_meet);

        tv_title_item_company = fv(R.id.tv_title_item_company);
        tv_title_item_group = fv(R.id.tv_title_item_group);

        tv_ignore = fv(R.id.tv_ignore);
        tv_has_choose = fv(R.id.tv_has_choose);
        rcv_has_choose = fv(R.id.rcv_has_choose);
        tv_start_meet.setOnFocusChangeListener(this);
        tv_ignore.setOnFocusChangeListener(this);
//
        if (fromType == Constract.INVITE_FROMTYPE_MEET) {
            tv_ignore.setVisibility(View.GONE);
            tv_start_meet.setText(mContext.getString(R.string.str_invite));
        } else if (fromType == Constract.INVITE_FROMTYPE_CREATE){
            tv_ignore.setVisibility(View.VISIBLE);
            tv_start_meet.setText(mContext.getString(R.string.str_start_meet));
        } else if (fromType == Constract.INVITE_FROMTYPE_CONTRACT){
            tv_ignore.setVisibility(View.GONE);
            tv_start_meet.setText(mContext.getString(R.string.str_start_a_meet));
        }


        if (ll_contanir.getChildCount() > 0) {
            tv_title_item_company.setNextFocusRightId(ll_contanir.getChildAt(0).getId());
            tv_title_item_group.setNextFocusRightId(ll_contanir.getChildAt(0).getId());
        }
        tv_ignore.setNextFocusLeftId(R.id.tv_ignore);
        tv_ignore.setNextFocusRightId(R.id.tv_ignore);
        tv_start_meet.setNextFocusRightId(R.id.tv_start_meet);

    }


    public void initData() {
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        tv_ignore.setOnClickListener(this);
        tv_start_meet.setOnClickListener(this);
        tv_title_item_company.setOnFocusChangeListener(this);
        tv_title_item_group.setOnFocusChangeListener(this);
//        list = new ArrayList<>();
        adapter = new InvitePersonAdapter(mContext, list, true);
        GridLayoutManager manager = new GridLayoutManager(mContext,4);
        rcv_has_choose.setLayoutManager(manager);
        rcv_has_choose.setAdapter(adapter);

        tv_title_item_company.requestFocus();
        tv_has_choose.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
    }

    //expand 组织树
    public void notifyOrgTreeList(CompanyModel orgList) {
        ll_contanir.removeAllViews();
        View view = View.inflate(mContext, R.layout.item_contact_tree_conpany, null);
        ConstraintLayout rootCompany = view.findViewById(R.id.constaint_company_root);
        LinearLayout ll_company_contair = view.findViewById(R.id.ll_company_contair);
        View view_focus = view.findViewById(R.id.focus_view);
        ImageView iv_arrow_company = view.findViewById(R.id.iv_arrow_company);
        ImageView iv_state_company = view.findViewById(R.id.iv_state_company);
        TextView tv_title_company = view.findViewById(R.id.tv_title_company);
        tv_title_company.setText(orgList.getOrganizationName());
        rootCompany.setNextFocusUpId(rootCompany.getId());
        rootCompany.setNextFocusLeftId(R.id.tv_title_item_company);
        rootCompany.setNextFocusRightId(R.id.iv_arrow_company);
        view_focus.setNextFocusRightId(R.id.focus_view);
        ll_contanir.addView(view);
        if (fromType == Constract.INVITE_FROMTYPE_CONTRACT){
            ll_contanir.requestFocus();
        }
        if(currentChooseAllState.containsKey(orgList.getOrgId())) {
            orgList.setChoose(currentChooseAllState.get(orgList.getOrgId()));
            updataItemChooseState(orgList.getOrganizationList(),currentChooseAllState.get(orgList.getOrgId()));
        }
        iv_state_company.setImageResource(orgList.isChoose()?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);

        rootCompany.setOnClickListener(v-> {
            //全选 1.获取该item下的所有设备，2.选中
            if (orgList.isChoose()) {
                currentChooseAllState.put(orgList.getOrgId(),false);
                orgList.setChoose(false);
            } else {        //取消全选
                currentChooseAllState.put(orgList.getOrgId(),true);
                orgList.setChoose(true);
            }
            iv_state_company.setImageResource(currentChooseAllState.get(orgList.getOrgId())?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);
            updataItemChooseState(orgList.getOrganizationList(),currentChooseAllState.get(orgList.getOrgId()));
            currentClickOrgId = orgList.getOrgId();
            isClickAllChoose = true;
            RoomClient.get().getSubDevOrUser(String.valueOf(orgList.getOrgId()));
        });

        //下拉
        view_focus.setOnClickListener(v -> {
            if (orgList.getOrganizationList().size() == 0) {
                return;
            }

            if (orgList.isExpand()) {
                orgList.setExpand(false);
                ll_company_contair.removeAllViews();
                clearExpandState(orgList.getOrganizationList());
            } else {
                orgList.setExpand(true);
                addSubTreeNode(ll_company_contair,orgList.getOrganizationList());
            }
            iv_arrow_company.setImageResource(orgList.isExpand()?R.drawable.icon_arrow_0212_down:R.drawable.icon_arrow_0212_right);
        });


        RoomClient.get().getSubDevOrUser(String.valueOf(orgList.getOrgId()));
    }

    public void addSubTreeNode(ViewGroup parentView, List<OrgList> list) {
        for (OrgList org : list) {
            View view = View.inflate(mContext, R.layout.item_contact_tree_part, null);
            LinearLayout ll_part_contair = view.findViewById(R.id.ll_part_contair);
            ConstraintLayout constraint_part_root = view.findViewById(R.id.constraint_part_root);
            View view_focus = view.findViewById(R.id.focus_view);
            constraint_part_root.setFocusable(true);
            constraint_part_root.setNextFocusLeftId(R.id.tv_title_item_company);
            ImageView iv_arrow = view.findViewById(R.id.iv_arrow_part);
            view_focus.setNextFocusRightId(R.id.focus_view);
            ImageView iv_state_company = view.findViewById(R.id.iv_state_company);
            if(currentChooseAllState.containsKey(org.getOrgId())) {
                org.setAllChoose(currentChooseAllState.get(org.getOrgId()));
                updataItemChooseState(org.getOrganizationList(),currentChooseAllState.get(org.getOrgId()));
            }
            iv_state_company.setImageResource(org.isAllChoose()?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);
            view_focus.setOnClickListener(v -> {
                //todo
                if(org.getOrganizationList().size() == 0) {     //没有子部门，获取设备
                    org.setLastData(true);
                } else {
                    org.setLastData(false);
                }

                if (org.isExpand()) {
                    org.setExpand(false);
                    ll_part_contair.removeAllViews();
                    clearExpandState(org.getOrganizationList());
                    if (mapOfTempLinear.containsKey(org.getOrgId())) {
                        mapOfTempLinear.remove(org.getOrgId());
                    }
                } else {
                    org.setExpand(true);
                    if(org.isLastData()) {
                        if(!mapOfTempLinear.containsKey(org.getOrgId())) {
                            mapOfTempLinear.put(org.getOrgId(), ll_part_contair);
                        }
                        currentClickOrgId = org.getOrgId();
                        RoomClient.get().getSubDevOrUser(String.valueOf(org.getOrgId()));
                        isClickAllChoose = false;
                    } else {
                        addSubTreeNode(ll_part_contair, org.getOrganizationList());
                    }
                }

                iv_arrow.setImageResource(org.isExpand()?R.drawable.icon_arrow_0212_down:R.drawable.icon_arrow_0212_right);
            });


            ImageView iv_icon_part = view.findViewById(R.id.iv_icon_part);
            iv_icon_part.setImageResource(R.drawable.icon_item_erji);
            TextView tv_title_part = view.findViewById(R.id.tv_title_part);
            tv_title_part.setText(org.getOrganizationName());

            constraint_part_root.setOnClickListener(view1-> {
                //全选 1.获取该item下的所有设备，2.选中
                if (org.isAllChoose()) {
                    currentChooseAllState.put(org.getOrgId(),false);
                    org.setAllChoose(false);
                } else {        //取消全选
                    currentChooseAllState.put(org.getOrgId(),true);
                    org.setAllChoose(true);
                }
                iv_state_company.setImageResource(currentChooseAllState.get(org.getOrgId())?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);
                updataItemChooseState(org.getOrganizationList(),currentChooseAllState.get(org.getOrgId()));
                currentClickOrgId = org.getOrgId();
                isClickAllChoose = true;
                RoomClient.get().getSubDevOrUser(String.valueOf(org.getOrgId()));
            });

            parentView.addView(view);
            if (currentExpandCompanyItem.containsKey(org.getOrgId())) {
                currentExpandCompanyItem.remove(org.getOrgId());
            }
            currentExpandCompanyItem.put(org.getOrgId(),view);

//            if (org.getOrganizationList().size() == 0) {
//                return;
//            }
        }
    }

    private void clearExpandState(List<OrgList> orgList) {
        if (orgList.size() != 0) {
            for (OrgList org:orgList) {
                if (org.isExpand()) {
                    org.setExpand(false);
                    clearExpandState(org.getOrganizationList());
                }
            }
        }
    }

    //更新当前item及子item 全选状态
    private void updataItemChooseState(List<OrgList> orgList, boolean isAllChoose) {
        if (orgList.size() != 0) {
            for (OrgList org:orgList) {
                org.setAllChoose(isAllChoose);
                if (currentExpandCompanyItem.containsKey(org.getOrgId())) {
                    View view = currentExpandCompanyItem.get(org.getOrgId());
                    ImageView iv = view.findViewById(R.id.iv_state_company);
                    iv.setImageResource(isAllChoose?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);
                }
                updataItemChooseState(org.getOrganizationList(),isAllChoose);
            }
        }
    }

    public void notifyOrgList(CompanyModel orgList) {

        ll_contanir.removeAllViews();
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2px(30));
        View view = View.inflate(mContext, R.layout.item_contact_company, null);
        ConstraintLayout rootCompany = view.findViewById(R.id.constaint_company_root);
        ImageView iv_icon_company = view.findViewById(R.id.iv_icon_company);
        TextView tv_title_company = view.findViewById(R.id.tv_title_company);
        tv_title_company.setText(orgList.getOrganizationName());
        rootCompany.setNextFocusUpId(rootCompany.getId());
        rootCompany.setNextFocusLeftId(rootCompany.getId());
            rootCompany.setNextFocusRightId(R.id.rcv_setting_org_list);
//        rootCompany.setNextFocusRightId(tv_start_meet.getId());
        view.setLayoutParams(params);
        ll_contanir.addView(view);
        if (fromType == Constract.INVITE_FROMTYPE_CONTRACT){
            ll_contanir.requestFocus();
        }
        rootCompany.setOnClickListener(v -> {
            isCheck = false;
//            cb_second_head.setChecked(isCheck);
            RoomClient.get().getSubDevOrUser(String.valueOf(orgList.getOrgId()));
        });

        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CallUtil.asyncCall(DELAY_TIME,()-> {
                        isCheck = false;
//                        cb_second_head.setChecked(isCheck);
                        RoomClient.get().getSubDevOrUser(String.valueOf(orgList.getOrgId()));
                    });
                }
            }
        });

        if (orgList.getOrganizationList() != null && orgList.getOrganizationList().size() > 0) {
            setSubItem(orgList.getOrganizationList());
        }
        //获取群组
//        RoomClient.get().getGroupList();

//        if (orgList.getOrganizationList() != null & orgList.getOrganizationList().size() > 0) {
            RoomClient.get().getSubDevOrUser(String.valueOf(orgList.getOrgId()));
//        }
    }

    @Override
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        super.setOnDismissListener(onDismissListener);
        currentChooseAllState.clear();
        currentExpandCompanyItem.clear();
    }

    //设置群组
    public void setSubGroupItem(List<GroupModel> groupModelList) {
        ll_contanir.removeAllViews();
        for (GroupModel model : groupModelList) {
            View view = View.inflate(mContext, R.layout.item_contact_tree_part, null);
            LinearLayout ll_part_contair = view.findViewById(R.id.ll_part_contair);
            ConstraintLayout constraint_part_root = view.findViewById(R.id.constraint_part_root);
            View view_focus = view.findViewById(R.id.focus_view);
            constraint_part_root.setFocusable(true);
            constraint_part_root.setNextFocusRightId(R.id.iv_arrow_part);
            constraint_part_root.setNextFocusLeftId(R.id.tv_title_item_group);
            ImageView iv_arrow = view.findViewById(R.id.iv_arrow_part);
            view_focus.setNextFocusRightId(R.id.focus_view);
            ImageView iv_state_company = view.findViewById(R.id.iv_state_company);
            iv_state_company.setImageResource(model.isAllChooseGroup()?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);
            view_focus.setOnClickListener(v -> {
//                //todo
//                if(org.getOrganizationList().size() == 0) {     //没有子部门，获取设备
//                    org.setLastData(true);
//                } else {
//                    org.setLastData(false);
//                }

                if (model.isExpandGroup()) {
                    model.setExpandGroup(false);
                    ll_part_contair.removeAllViews();
                    if (mapOfTempGroup.containsKey(model.getGroupId())) {
                        mapOfTempGroup.remove(model.getGroupId());
                    }
                } else {
                    model.setExpandGroup(true);
                        if(!mapOfTempGroup.containsKey(model.getGroupId())) {
                            mapOfTempGroup.put(model.getGroupId(), ll_part_contair);
                        }
                        currentClickGroupId = model.getGroupId();
                        RoomClient.get().getGroupInfo(model.getGroupId());
                        isClickAllChoose = false;
                }

                iv_arrow.setImageResource(model.isExpandGroup()?R.drawable.icon_arrow_0212_down:R.drawable.icon_arrow_0212_right);
            });


            ImageView iv_icon_part = view.findViewById(R.id.iv_icon_part);
            iv_icon_part.setImageResource(R.drawable.icon_item_erji);
            TextView tv_title_part = view.findViewById(R.id.tv_title_part);
            tv_title_part.setText(model.getGroupName());

            constraint_part_root.setOnClickListener(view1-> {
                //全选 1.获取该item下的所有设备，2.选中
                if (model.isAllChooseGroup()) {
//                    currentChooseAllState.put(org.getOrgId(),false);
                    currentChooseAllStateGroup.put(model.getGroupId(),false);
                    model.setAllChooseGroup(false);
                } else {        //取消全选
//                    currentChooseAllState.put(org.getOrgId(),true);
                    currentChooseAllStateGroup.put(model.getGroupId(),true);
                    model.setAllChooseGroup(true);
                }
                iv_state_company.setImageResource(model.isAllChooseGroup()?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);

                currentClickGroupId = model.getGroupId();
                isClickAllChoose = true;
                RoomClient.get().getGroupInfo(model.getGroupId());
            });

            ll_contanir.addView(view);
//            if (currentExpandCompanyItem.containsKey(org.getOrgId())) {
//                currentExpandCompanyItem.remove(org.getOrgId());
//            }
//            currentExpandCompanyItem.put(org.getOrgId(),view);

        }
    }

    //添加子部门
    public void setSubItem(List<OrgList> orgList) {
        for (OrgList org : orgList) {
            View view = View.inflate(mContext, R.layout.item_contact_part, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2px(30));
            ConstraintLayout constraint_part_root = view.findViewById(R.id.constraint_part_root);
            constraint_part_root.setFocusable(true);
            view.setNextFocusRightId(R.id.rcv_setting_org_list);
            constraint_part_root.setOnClickListener(v -> {
                isCheck = false;
//                cb_second_head.setChecked(isCheck);
                RoomClient.get().getSubDevOrUser(String.valueOf(org.getOrgId()));
            });
            ImageView iv_icon_part = view.findViewById(R.id.iv_icon_part);
            iv_icon_part.setImageResource(R.drawable.icon_item_part);
            TextView tv_title_part = view.findViewById(R.id.tv_title_part);
            tv_title_part.setText(org.getOrganizationName());
            view.setLayoutParams(params);
            ll_contanir.addView(view);
            if (org.getOrganizationList() != null && org.getOrganizationList().size() > 0) {
                setSubItem(org.getOrganizationList());
            }

            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        CallUtil.asyncCall(DELAY_TIME,() -> {
                            isCheck = false;
//                            cb_second_head.setChecked(isCheck);
                            RoomClient.get().getSubDevOrUser(String.valueOf(org.getOrgId()));
                        });
                    }
                }
            });
        }
    }

//    private void addLastData() {
//        for (UserDeviceModel userDeviceModel : modelMap.values()) {
//            if (Constract.online.equals(userDeviceModel.getDeviceStatus())) {
//                setCacheData(userDeviceModel);
//            }
//        }
//        tv_third_head.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
//    }

    //更新设备数据
    public void notifyDeviceData(List<UserDeviceModel> listOfDevice) {
        String userId = SPEditor.instance().getUserId();
        if (listOfDevice.size() != 0) {
            if (isClickAllChoose) {

                for (int i=0;i<listOfDevice.size();i++) {
                    if (list.size()>DeviceSettingManager.getInstance().getFromSp().getRoomCapacity()) {
                        return;
                    }
                    UserDeviceModel model = listOfDevice.get(i);
                    if(userId.equals(model.getUserId())) {
                        continue;
                    }

                    if (!currentChooseAllState.containsKey(currentClickOrgId)) {
                        return;
                    }
                    if (currentChooseAllState.get(currentClickOrgId)) {     //当前item 点击了全选
                        if (modelMap.containsKey(model.getUserId())) {      //已选
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {     //在线
                                model.setChecked(true);
                            } else {
                                model.setChecked(true);
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());
                            }
                        } else {
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                                model.setChecked(true);
                                modelMap.put(model.getUserId(),model);
                                list.add(modelMap.get(model.getUserId()));
                            }
                        }
                    } else {                                                //当前item 点击了取消全选
                        if (modelMap.containsKey(model.getUserId())) {      //已选
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {     //在线
                                model.setChecked(false);
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());
                            } else {
                                model.setChecked(false);
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());

                            }
                        } else {
                            model.setChecked(false);
                        }
                    }
                    if (currentExpandItem.containsKey(model.getUserId())) {
                        View view = currentExpandItem.get(model.getUserId());
                        ImageView imageView = view.findViewById(R.id.iv_check_status);
                        if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                            imageView.setImageResource(currentChooseAllState.get(currentClickOrgId)?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);
                        } else {
                            imageView.setImageResource(R.drawable.icon_item_square_state_unchecked);
                        }
                    }

                    if (tempModelMap.containsKey(model.getUserId())) {
                        tempModelMap.remove(model.getUserId());
                    }
                    tempModelMap.put(model.getUserId(),model);
                }
                tv_has_choose.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
                adapter.notifyDataSetChanged();
            } else {                //点击查看子设备
                if (mapOfTempLinear.containsKey(currentClickOrgId)) {
                    LinearLayout contair = (LinearLayout) mapOfTempLinear.get(currentClickOrgId);
                    for (int i=0;i<listOfDevice.size();i++) {
                        UserDeviceModel model = listOfDevice.get(i);
                        if (userId.equals(model.getUserId())) {
                            continue;
                        }
                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2px(40));
                        View view = View.inflate(mContext, R.layout.item_contact_item_last, null);
                        ConstraintLayout constaint_item_root =view.findViewById(R.id.constaint_item_root);
                        TextView title = view.findViewById(R.id.tv_title_device);
                        title.setText(model.getDeviceName());
                        ImageView iv_check_status = view.findViewById(R.id.iv_check_status);
                        view.setLayoutParams(params);

                        //已经在已选中了
                        if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                           if (modelMap.containsKey(model.getUserId())) {
                               model.setChecked(true);
                           } else {
                               model.setChecked(false);
                           }
                        } else {
                            if (modelMap.containsKey(model.getUserId())) {
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());
                                model.setChecked(false);
                            } else {
                                model.setChecked(false);
                            }
                        }

                        ImageView iv_tip_state = view.findViewById(R.id.iv_tip_state);
                        setIconStatus(model.getDeviceStatus(),iv_tip_state);
                        if(Constants.ONLINE.equals(model.getDeviceStatus()) && model.isChecked()) {
                            iv_check_status.setImageResource(R.drawable.icon_item_square_state_checked);
                        } else {
                            iv_check_status.setImageResource(R.drawable.icon_item_square_state_unchecked);
                        }

                        constaint_item_root.setOnClickListener(v->{
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                                if (tempModelMap.get(model.getUserId()).isChecked()) {
                                    tempModelMap.get(model.getUserId()).setChecked(false);
                                    iv_check_status.setImageResource(R.drawable.icon_item_square_state_unchecked);
                                    if(modelMap .containsKey(model.getUserId())) {
                                        list.remove(modelMap.get(model.getUserId()));
                                        modelMap.remove(model.getUserId());
                                    }
                                } else {
                                    tempModelMap.get(model.getUserId()).setChecked(true);
                                    iv_check_status.setImageResource(R.drawable.icon_item_square_state_checked);
                                    if(!modelMap .containsKey(model.getUserId())) {
                                        modelMap.put(model.getUserId(),model);
                                        list.add(modelMap.get(model.getUserId()));
                                    }

                                }
                                adapter.notifyDataSetChanged();
                                tv_has_choose.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
                            }
                        });

                        contair.addView(view);

                        if (currentExpandItem.containsKey(model.getUserId())) {
                            currentExpandItem.remove(model.getUserId());
                        }
                        currentExpandItem.put(model.getUserId(),view);
                        if (tempModelMap.containsKey(model.getUserId())) {
                            tempModelMap.remove(model.getUserId());
                        }
                        tempModelMap.put(model.getUserId(),model);
                    }
                }
            }
        }

    }

    public void notifyGroupDeviceData(List<UserDeviceModel> listOfDevice) {
        String userId = SPEditor.instance().getUserId();
        if (listOfDevice.size() != 0) {
            if (isClickAllChoose) {

                for (int i=0;i<listOfDevice.size();i++) {
                    if (list.size()>DeviceSettingManager.getInstance().getFromSp().getRoomCapacity()) {
                        return;
                    }
                    UserDeviceModel model = listOfDevice.get(i);
                    if(userId.equals(model.getUserId())) {
                        continue;
                    }
                    if (!currentChooseAllStateGroup.containsKey(currentClickGroupId)) {
                        return;
                    }
                    if (currentChooseAllStateGroup.get(currentClickGroupId)) {     //当前item 点击了全选
                        if (modelMap.containsKey(model.getUserId())) {      //已选
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {     //在线
                                model.setChecked(true);
                            } else {
                                model.setChecked(true);
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());
                            }
                        } else {
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                                model.setChecked(true);
                                modelMap.put(model.getUserId(),model);
                                list.add(modelMap.get(model.getUserId()));
                            }
                        }
                    } else {                                                //当前item 点击了取消全选
                        if (modelMap.containsKey(model.getUserId())) {      //已选
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {     //在线
                                model.setChecked(false);
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());
                            } else {
                                model.setChecked(false);
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());

                            }
                        } else {
                            model.setChecked(false);
                        }
                    }
                    if (currentExpandItem.containsKey(model.getUserId())) {
                        View view = currentExpandItem.get(model.getUserId());
                        ImageView imageView = view.findViewById(R.id.iv_check_status);
                        if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                            imageView.setImageResource(currentChooseAllStateGroup.get(currentClickGroupId)?R.drawable.icon_item_square_state_checked:R.drawable.icon_item_square_state_unchecked);
                        } else {
                            imageView.setImageResource(R.drawable.icon_item_square_state_unchecked);
                        }
                    }

                    if (tempModelMap.containsKey(model.getUserId())) {
                        tempModelMap.remove(model.getUserId());
                    }
                    tempModelMap.put(model.getUserId(),model);
                }
                tv_has_choose.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
                adapter.notifyDataSetChanged();
            } else {                //点击查看子设备
                    LinearLayout contair = (LinearLayout) mapOfTempGroup.get(currentClickGroupId);
                    for (int i=0;i<listOfDevice.size();i++) {
                        UserDeviceModel model = listOfDevice.get(i);
                        if (userId.equals(model.getUserId())) {
                            continue;
                        }
                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2px(40));
                        View view = View.inflate(mContext, R.layout.item_contact_item_last, null);
                        ConstraintLayout constaint_item_root =view.findViewById(R.id.constaint_item_root);
                        TextView title = view.findViewById(R.id.tv_title_device);
                        title.setText(model.getDeviceName());
                        ImageView iv_check_status = view.findViewById(R.id.iv_check_status);
                        view.setLayoutParams(params);

                        //已经在已选中了
                        if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                            if (modelMap.containsKey(model.getUserId())) {
                                model.setChecked(true);
                            } else {
                                model.setChecked(false);
                            }
                        } else {
                            if (modelMap.containsKey(model.getUserId())) {
                                list.remove(modelMap.get(model.getUserId()));
                                modelMap.remove(model.getUserId());
                                model.setChecked(false);
                            } else {
                                model.setChecked(false);
                            }
                        }

                        ImageView iv_tip_state = view.findViewById(R.id.iv_tip_state);
                        setIconStatus(model.getDeviceStatus(),iv_tip_state);
                        if(Constants.ONLINE.equals(model.getDeviceStatus()) && model.isChecked()) {
                            iv_check_status.setImageResource(R.drawable.icon_item_square_state_checked);
                        } else {
                            iv_check_status.setImageResource(R.drawable.icon_item_square_state_unchecked);
                        }

                        constaint_item_root.setOnClickListener(v->{
                            if (Constants.ONLINE.equals(model.getDeviceStatus())) {
                                if (tempModelMap.get(model.getUserId()).isChecked()) {
                                    tempModelMap.get(model.getUserId()).setChecked(false);
                                    iv_check_status.setImageResource(R.drawable.icon_item_square_state_unchecked);
                                    if(modelMap .containsKey(model.getUserId())) {
                                        list.remove(modelMap.get(model.getUserId()));
                                        modelMap.remove(model.getUserId());
                                    }
                                } else {
                                    tempModelMap.get(model.getUserId()).setChecked(true);
                                    iv_check_status.setImageResource(R.drawable.icon_item_square_state_checked);
                                    if(!modelMap .containsKey(model.getUserId())) {
                                        modelMap.put(model.getUserId(),model);
                                        list.add(modelMap.get(model.getUserId()));
                                    }

                                }
                                adapter.notifyDataSetChanged();
                                tv_has_choose.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
                            }
                        });

                        contair.addView(view);

                        if (currentExpandItem.containsKey(model.getUserId())) {
                            currentExpandItem.remove(model.getUserId());
                        }
                        currentExpandItem.put(model.getUserId(),view);
                        if (tempModelMap.containsKey(model.getUserId())) {
                            tempModelMap.remove(model.getUserId());
                        }
                        tempModelMap.put(model.getUserId(),model);
                    }

            }
        }
    }

    //更新RecyclerView 数据
    public void notifyRcvData(List<UserDeviceModel> listOfDevice) {
        boolean isAllChoose = true;
        int index = -1;
//        list.clear();
        list = listOfDevice;
        String userId = SPEditor.instance().getUserId();
        if (list == null || list.size() == 0) {
            isAllChoose = false;
        } else {
//            cb_second_head.setNextFocusDownId(R.id.rcv_setting_org_list);
        }

        for (int i=0;i<list.size();i++) {
            UserDeviceModel userDeviceModel = listOfDevice.get(i);
            userDeviceModel.setChecked(false);
//            userDeviceModel.setGroupData(isModelFromGroup);
            if(userId.equals(userDeviceModel.getUserId()) ) {
                index = i;
                continue;
            }

            if (serialMap.containsKey(userDeviceModel.getSerialNumber())) {
                //清除离线的item
                if (!Constants.ONLINE.equals(userDeviceModel.getDeviceStatus())) {
                    modelMap.remove(serialMap.get(userDeviceModel.getSerialNumber()));
                    removeCacheDataBySerial(userDeviceModel);
                    tv_third_head.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
                } else {
                    //在线，但登录的账户不同,清除缓存的item
                    if(userDeviceModel.getUserId() != null && !serialMap.get(userDeviceModel.getSerialNumber()).equals(userDeviceModel.getUserId())) {
                        modelMap.remove(serialMap.get(userDeviceModel.getSerialNumber()));
                        removeCacheDataBySerial(userDeviceModel);
                        tv_third_head.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
                    }
                }
            }

            if (modelMap.containsKey(userDeviceModel.getUserId())) {
                if (Constract.online.equals(userDeviceModel.getDeviceStatus())) {                   //如果上次会议
                    userDeviceModel.setChecked(true);
                    isAllChoose = isAllChoose && true;
                } else {
                    removeCacheData(userDeviceModel);
                }

            } else {
                isAllChoose = isAllChoose && false;
            }

        }
        if (index != -1) {
            list.remove(index);
        }
        adapter.setData(list);
//        if (!isModelFromGroup) {
//            isCheck = isAllChoose;
//        } else {
//            isCheck = true;
//            isAllChoose = true;
//            clickCheckAll(true);
//        }
//        cb_second_head.setChecked(isAllChoose);
    }



    //添加选中的人
    public void addCacheData(UserDeviceModel userDeviceModel) {
        setCacheData(userDeviceModel);
        modelMap.put(userDeviceModel.getUserId(), userDeviceModel);
        if (!serialMap.containsKey(userDeviceModel.getSerialNumber())) {
            serialMap.put(userDeviceModel.getSerialNumber(), userDeviceModel.getUserId());
        }
        tv_third_head.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
    }

    public void setCacheData(UserDeviceModel userDeviceModel) {
        if (scroll_cache.getVisibility() == View.GONE) {
            scroll_cache.setVisibility(View.VISIBLE);
        }
        View personView = View.inflate(mContext, R.layout.item_contact_cache, null);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2px(30));
        TextView tv_title_device = personView.findViewById(R.id.tv_title_device);
        TextView tv_state = personView.findViewById(R.id.tv_state);
        tv_title_device.setText(userDeviceModel.getDeviceName());
        personView.setTag(userDeviceModel.getSerialNumber());
        personView.setLayoutParams(params);
//        setIconStatus(userDeviceModel.getDeviceStatus(), tv_state);
//        tv_start_meet.setNextFocusDownId(R.id.ll_cache_contair);
    }

    public void removeCacheDataBySerial(UserDeviceModel userDeviceModel) {
        ll_cache_contair.removeView(ll_cache_contair.findViewWithTag(userDeviceModel.getSerialNumber()));
        if (ll_cache_contair.getChildCount() == 0) {
            scroll_cache.setVisibility(View.GONE);
//            tv_start_meet.setNextFocusDownId(R.id.tv_start_meet);
        }

    }

    //移除选中的人
    public void removeCacheData(UserDeviceModel userDeviceModel) {
        removeCacheDataBySerial(userDeviceModel);
        modelMap.remove(userDeviceModel.getUserId());
        serialMap.remove(userDeviceModel.getSerialNumber());
        tv_third_head.setText(String.format("已选 %s",String.valueOf(modelMap.values().size())));
    }

    //勾选item
    @Override
    public void onItemClick(UserDeviceModel model) {
        if (model.isChecked()) {
            if (!modelMap.containsKey(model.getUserId())) {
                addCacheData(model);
            }
        } else {
            if (modelMap.containsKey(model.getUserId())) {
                removeCacheData(model);
                if (isCheck) {
                    isCheck = false;

//                    cb_second_head.setChecked(isCheck);
                }
            }
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_start_meet) {
            if (modelMap.values().size() == 0) {
                return;
            }

            if (DeviceSettingManager.getInstance().getFromSp().getRoomCapacity() - 1 < modelMap.size()) {
                UCToast.show(mContext, "当前会议最多支持" + (DeviceSettingManager.getInstance().getFromSp().getRoomCapacity() - 1) + "人");
                return;
            }

            if (ViewUtils.isFastClick()) {
                return;
            }
            List<String> availableList = new ArrayList<>(modelMap.keySet());

            if (fromType == Constract.INVITE_FROMTYPE_MEET) {
                if (availableList.size() != 0) {
                    RoomClient.get().inviteParticipant(channel, RoomManager.get().getUserId(), availableList);
                }
                dismiss();
                return;
            }
            RoomManager.get().setListOfInvited(availableList);
            DeviceSettingManager.DeviceModel deviceModel = DeviceSettingManager.getInstance().getFromSp();
            deviceModel.setModelMap(modelMap);
            DeviceSettingManager.getInstance().setModel(deviceModel);
            DeviceSettingManager.getInstance().saveSp();
            String roomPwd = "";
            boolean isUsePwd = DeviceSettingManager.getInstance().getFromSp().isUserPsd();
            if (isUsePwd) {
                roomPwd = DeviceSettingManager.getInstance().getFromSp().getPassword();
            } else {
                roomPwd = "";
            }
            RoomClient.get().createRoom(DeviceSettingManager.getInstance().getFromSp().getMeetId(), roomPwd);
            dismiss();
        } else if (v.getId() == R.id.tv_ignore) {
            if (ViewUtils.isFastClick()) {
                return;
            }
            dismiss();
            RoomManager.get().setListOfInvited(new ArrayList<>());
            String roomPwd = "";
            boolean isUsePwd = DeviceSettingManager.getInstance().getFromSp().isUserPsd();
            if (isUsePwd) {
                roomPwd = DeviceSettingManager.getInstance().getFromSp().getPassword();
            } else {
                roomPwd = "";
            }
            RoomClient.get().createRoom(DeviceSettingManager.getInstance().getFromSp().getMeetId(), roomPwd);
        }
    }

    //点击全选cb
    private void clickCheckAll(boolean isChecked) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            UserDeviceModel userDeviceModel = list.get(i);
            if (Constract.online.equals(userDeviceModel.getDeviceStatus())) {
                if (SPEditor.instance().getUserId().equals(userDeviceModel.getUserId())) {
                    return;
                }
                userDeviceModel.setChecked(isCheck);
                if (isCheck) {
                    if (!modelMap.containsKey(userDeviceModel.getUserId())) {
                        addCacheData(userDeviceModel);
                    }
                } else {
                    if (modelMap.containsKey(userDeviceModel.getUserId())) {
                        removeCacheData(userDeviceModel);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setIconStatus(String status, ImageView iv) {
        if (Constants.ONLINE.equals(status)) {
            iv.setImageResource(R.drawable.icon_tip_online);
        } else if (Constants.OFFLINE.equals(status)) {
            iv.setImageResource(R.drawable.icon_tip_offline);
        } else if (Constants.MEETING.equals(status)) {
            iv.setImageResource(R.drawable.icon_tip_meeting);
        } else if (Constants.UPGRADING.equals(status)) {
            iv.setImageResource(R.drawable.icon_tip_update);
        }
    }

    public void showChooseToast() {
        if (modelMap.values().size() > 0) {
            UCToast.show(mContext, "邀请" + modelMap.values().size() + "人成功!");
        }
    }

    @Override
    public void onFocusChange(View view, boolean isFocus) {
        if (view.getId() == R.id.tv_title_item_company) {
            if (isFocus) {
                controlItemBg(true,false);
                if (isModelFromGroup) {
                    ll_contanir.removeAllViews();
                    RoomClient.get().getDepartmentTree();
                    isModelFromGroup = false;
                    mapOfTempLinear.clear();
                }
            }
        } else if (view.getId() == R.id.tv_title_item_group) {
            if (isFocus) {
                controlItemBg(false,true);
                if (!isModelFromGroup) {
                    ll_contanir.removeAllViews();
                    RoomClient.get().getGroupList();
                    isModelFromGroup = true;
                    mapOfTempLinear.clear();
                }
            }
        } else if (view.getId() == R.id.tv_start_meet) {
            controlItemBg(false,false);
        } else if (view.getId() == R.id.tv_ignore) {
            controlItemBg(false,false);
        }
    }

    private void controlItemBg(boolean isCompanyChoose, boolean isGroupChoose) {
        tv_title_item_company.setBackgroundColor(isCompanyChoose?mContext.getResources().getColor(R.color.color_575e76):mContext.getResources().getColor(R.color.color_353748));
        tv_title_item_group.setBackgroundColor(isGroupChoose?mContext.getResources().getColor(R.color.color_575e76):mContext.getResources().getColor(R.color.color_353748));
    }

}
