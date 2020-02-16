package cn.closeli.rtc.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.UCToast;

/**
 * 新的邀请入会adatpter
 */
public class InvitePersonAdapter extends RecyclerView.Adapter<InvitePersonAdapter.InviteViewHolder> {
    private Context context;
    private List<UserDeviceModel> list;
    private LayoutInflater mInflater;
    private OnInviteItemClickListener onInviteItemClickListener;
    private boolean isShowCheckbox;

    public InvitePersonAdapter(Context context, List<UserDeviceModel> list, boolean isShowCheckbox) {
        this.context = context;
        this.list = list;
        this.isShowCheckbox = isShowCheckbox;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<UserDeviceModel> listOfDevice) {
        this.list.clear();
        this.list = listOfDevice;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.item_contact_device, viewGroup, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int i) {
//        if (i == list.size()-1) {
//            holder.constraintLayout.setNextFocusDownId(holder.constraintLayout.getId());
//        } else {
//            holder.constraintLayout.setNextFocusDownId(0);
//        }
//        holder.constraintLayout.setNextFocusLeftId(R.id.ll_title_contanir);
        holder.tv_title_device.setText(list.get(i).getDeviceName());
//        holder.cb_device_item.setVisibility(isShowCheckbox ? View.VISIBLE : View.GONE);
        setIconStatus(list.get(i).getDeviceStatus(),holder.iv_tip_state);
        if (isShowCheckbox) {
//            holder.cb_device_item.setChecked(list.get(i).isChecked() && Constract.online.equals(list.get(i).getDeviceStatus()));
            if (Constract.online.equals(list.get(i).getDeviceStatus())) {
//                if (SPEditor.instance().getUserId().equals(list.get(i).getUserId())) {
//                    return;
//                }
//                holder.cb_device_item.setEnabled(true);
//                holder.cb_device_item.setClickable(true);
//                holder.cb_device_item.setOnClickListener(v -> {
//                    if (onInviteItemClickListener != null) {
//                        list.get(i).setChecked(!list.get(i).isChecked());
////                        holder.cb_device_item.setChecked(list.get(i).isChecked());
//                        onInviteItemClickListener.onItemClick(list.get(i));
//                    }
//                });
            } else {
//                holder.cb_device_item.setEnabled(false);
//                holder.cb_device_item.setClickable(false);
            }

            holder.constraintLayout.setOnClickListener(v -> {
                if (!Constract.online.equals(list.get(i).getDeviceStatus())) {
                    UCToast.show(context, getStateStr(list.get(i)));
                    return;
                }

//                if (SPEditor.instance().getUserId().equals(list.get(i).getUserId())) {
//                    return;
//                }
//                if (onInviteItemClickListener != null) {
//                    list.get(i).setChecked(!list.get(i).isChecked());
//                    onInviteItemClickListener.onItemClick(list.get(i));
//                }
            });
        }
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

    @Override
    public int getItemCount() {
        return list.size();
    }

    public OnInviteItemClickListener getOnInviteItemClickListener() {
        return onInviteItemClickListener;
    }

    public void setOnInviteItemClickListener(OnInviteItemClickListener onInviteItemClickListener) {
        this.onInviteItemClickListener = onInviteItemClickListener;
    }

    class InviteViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout constraintLayout;
        private TextView tv_title_device;
        private ImageView iv_tip_state;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.constaint_item_device);
            tv_title_device = itemView.findViewById(R.id.tv_title_device);
            iv_tip_state = itemView.findViewById(R.id.iv_tip_state);
        }
    }

    public interface OnInviteItemClickListener {
        void onItemClick(UserDeviceModel model);
    }

    public String getStateStr(UserDeviceModel userDeviceModel) {
        String str = "当前与会者空闲中";
        if (Constants.ONLINE.equals(userDeviceModel.getDeviceStatus())) {
            str = "当前与会者空闲中";
        } else if (Constants.OFFLINE.equals(userDeviceModel.getDeviceStatus())) {
            str = "当前与会者离线";
        } else if (Constants.MEETING.equals(userDeviceModel.getDeviceStatus())) {
            str = "当前与会者正在会议中";
        } else if (Constants.UPGRADING.equals(userDeviceModel.getDeviceStatus())) {
            str = "当前与会者正在升级中";
        }

        return str;
    }
}
