package cn.closeli.rtc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.utils.ImgUtils;

public class ChangeHostAdapter extends RecyclerView.Adapter<ChangeHostAdapter.ViewHolder> {
    private List<ParticipantInfoModel> list;
    private Context context;
    private LayoutInflater inflater;
    private OnItemCheckedListener OnItemCheckedListener;
    public ChangeHostAdapter(Context context, List<ParticipantInfoModel> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.item_host_change,viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        ImgUtils.setCircle(list.get(i).getAvatar(),holder.iv_avatar);
        holder.tv_nick.setText(list.get(i).getAccount());
        holder.tv_post.setText(list.get(i).getPost());
        holder.tv_id.setText(list.get(i).getUserId());
        if (list.get(i).isChecked()) {
            holder.iv_check_status.setVisibility(View.VISIBLE);
        } else {
            holder.iv_check_status.setVisibility(View.GONE);
        }
        holder.constaint_root.setOnClickListener(v-> {
            if (OnItemCheckedListener !=null) {
                OnItemCheckedListener.onItemChecked(i,list.get(i).isChecked());
            }
        });
    }

    public void changeData(List<ParticipantInfoModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout constaint_root;
        private ImageView iv_avatar;
        private TextView tv_nick;
        private TextView tv_post;
        private ImageView iv_check_status;
        private TextView tv_id;
        private View line;
        public ViewHolder(@NonNull View v) {
            super(v);
            constaint_root =v.findViewById(R.id.constaint_root);
            iv_avatar = v.findViewById(R.id.iv_avatar);
            tv_nick = v.findViewById(R.id.tv_nick);
            tv_post = v.findViewById(R.id.tv_post);
            iv_check_status = v.findViewById(R.id.iv_check_status);
            tv_id = v.findViewById(R.id.tv_id);
            line = v.findViewById(R.id.line);
        }
    }

    public ChangeHostAdapter.OnItemCheckedListener getOnItemCheckedListener() {
        return OnItemCheckedListener;
    }

    public void setOnItemCheckedListener(ChangeHostAdapter.OnItemCheckedListener onItemCheckedListener) {
        OnItemCheckedListener = onItemCheckedListener;
    }

    public interface OnItemCheckedListener {
        void onItemChecked(int position,boolean isChecked);
    }
}
