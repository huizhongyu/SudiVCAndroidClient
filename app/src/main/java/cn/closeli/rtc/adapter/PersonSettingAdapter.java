package cn.closeli.rtc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.utils.ImgUtils;

public class PersonSettingAdapter extends RecyclerView.Adapter<PersonSettingAdapter.ViewHolder> {
    private List<ParticipantInfoModel> list;
    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;
    private boolean manageMicro = true;

    public PersonSettingAdapter(Context context, List<ParticipantInfoModel> list, boolean manageMicro) {
        this.context = context;
        this.list = list;
        this.manageMicro = manageMicro;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.item_person_setting, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        ParticipantInfoModel model = list.get(i);
        if (model == null) {
            return;
        }
        ImgUtils.setCircle(list.get(i).getAvatar(), holder.iv_avatar);
        holder.tv_nick.setText(!TextUtils.isEmpty(list.get(i).getDeviceName()) ? list.get(i).getDeviceName() : list.get(i).getAppShowName());
        holder.tv_info.setText(list.get(i).getAppShowDesc());
        boolean on = "on".equals(model.getShareStatus());
        holder.iv_status.setImageResource(manageMicro ? (model.isAudioActive() ? R.drawable.maininterface_tab_icon_microphone_video_set : R.drawable.maininterface_tab_icon_microphone_chose_micro_unuse)
                : ((on ? R.drawable.maininterface_tab_icon_microphone_video_share : R.drawable.maininterface_tab_icon_microphone_chose_share_unuse)));
        holder.iv_status.setVisibility(((manageMicro) || (model.isShareable())) ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            boolean on1 = model.isShareable();
            if (onItemClickListener != null && (manageMicro) || (model.isShareable())) {
                onItemClickListener.onItemClick(i, manageMicro ? model.isAudioActive() : on1);
            }
        });
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

    public void refreshData(boolean manageMicro) {
        this.manageMicro = manageMicro;
        notifyDataSetChanged();
    }

    public List<ParticipantInfoModel> getList() {
        return list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout constraint_root;
        private ImageView iv_avatar;
        private TextView tv_nick;
        private TextView tv_info;
        private ImageView iv_status;
        private View line;

        public ViewHolder(@NonNull View v) {
            super(v);
            constraint_root = v.findViewById(R.id.constraint_root);
            iv_avatar = v.findViewById(R.id.iv_avatar);
            tv_nick = v.findViewById(R.id.tv_nick);
            tv_info = v.findViewById(R.id.tv_info);
            iv_status = v.findViewById(R.id.iv_status);
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
        void onItemClick(int position, boolean isChecked);
    }

}
