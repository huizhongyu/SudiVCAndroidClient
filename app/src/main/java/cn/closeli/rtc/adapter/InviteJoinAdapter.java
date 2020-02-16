package cn.closeli.rtc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
import cn.closeli.rtc.utils.ImgUtils;

//邀请入会
public class InviteJoinAdapter extends RecyclerView.Adapter<InviteJoinAdapter.ViewHolder> {
    private Context context;
    private List<UserDeviceModel> list;
    private LayoutInflater mInflater;
    private OnItemCheckedListener listener;
    private boolean isChecked;
    public InviteJoinAdapter(Context context,List<UserDeviceModel> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.item_invitejoin,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        isChecked = list.get(i).isChecked();
        holder.checkBox.setImageResource(isChecked?R.drawable.icon_cb_square_checked:R.drawable.icon_cb_square_unchecked);
        if (Constract.online.equals(list.get(i).getStatus())) {                 //
            holder.tv_line_state.setVisibility(View.GONE);
        } else {
            holder.tv_line_state.setVisibility(View.VISIBLE);
        }
//        holder.checkBox.setOnClickListener(v-> {
//            list.get(i).setChecked(!list.get(i).isChecked());
//            holder.checkBox.setChecked(!holder.checkBox.isChecked());
//            if (listener != null) {
//                listener.onItemChoose(i,holder.checkBox.isChecked());
//            }
//        });
        holder.cl_root.setOnClickListener(v-> {
            isChecked = list.get(i).isChecked();
            isChecked = !isChecked;
            list.get(i).setChecked(isChecked);
            holder.checkBox.setImageResource(isChecked?R.drawable.icon_cb_square_checked:R.drawable.icon_cb_square_unchecked);
            if (listener != null) {
                listener.onItemChoose(i,isChecked);
            }
        });
        holder.tv_nameInfo.setText(list.get(i).getAppShowName());
        holder.tv_idInfo.setText(list.get(i).getAppShowDesc());
    }
    public void setDatas(ArrayList<UserDeviceModel> userDeviceModels){
        this.list = userDeviceModels;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout cl_root;
        private ImageView checkBox;
        private ImageView iv_avatar;
        private TextView tv_nameInfo;
        private TextView tv_idInfo;
        private TextView tv_line_state;

        public ViewHolder(@NonNull View view) {
            super(view);
            cl_root = view.findViewById(R.id.cl_root);
            checkBox = view.findViewById(R.id.checkbox);
            iv_avatar = view.findViewById(R.id.iv_avatar);
            tv_nameInfo = view.findViewById(R.id.tv_nameinfo);
            tv_idInfo = view.findViewById(R.id.tv_id);
            tv_line_state = view.findViewById(R.id.tv_line_state);
        }
    }

    public OnItemCheckedListener getListener() {
        return listener;
    }

    public void setListener(OnItemCheckedListener listener) {
        this.listener = listener;
    }

    public interface OnItemCheckedListener {
        void onItemChoose(int position,boolean isChecked);
    }
}
