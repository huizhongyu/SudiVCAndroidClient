package cn.closeli.rtc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.RoomManager;
import top.androidman.SuperButton;

public class MemberSettingAdapter extends RecyclerView.Adapter<MemberSettingAdapter.ViewHolder> {
    private List<ParticipantInfoModel> list;
    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;

    public MemberSettingAdapter(Context context, List<ParticipantInfoModel> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.item_member_setting, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        ParticipantInfoModel model = list.get(i);
        if (model == null) {
            return;
        }
        holder.tv_nick.setText(TextUtils.isEmpty(list.get(i).getAccount()) ? list.get(i).getAppShowName() : list.get(i).getAccount());
        holder.tv_info.setText(list.get(i).getPost());
        holder.iv_role.setVisibility(model.getUserId().equals(RoomManager.get().getHostId()) ? View.VISIBLE : View.GONE);
    }

    //更改数据
    public void changeData(List<ParticipantInfoModel> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout constraint_root;
        private ImageView iv_avatar;
        private TextView tv_nick;
        private TextView tv_info;
        private SuperButton iv_role;
        private View line;

        public ViewHolder(@NonNull View v) {
            super(v);
            constraint_root = v.findViewById(R.id.constraint_root);
            iv_avatar = v.findViewById(R.id.iv_avatar);
            tv_nick = v.findViewById(R.id.tv_nick);
            tv_info = v.findViewById(R.id.tv_info);
            iv_role = v.findViewById(R.id.iv_role);
            line = v.findViewById(R.id.line);
        }
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, int type, boolean isChecked);
    }

}
