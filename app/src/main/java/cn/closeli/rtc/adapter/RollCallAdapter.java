package cn.closeli.rtc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.utils.ImgUtils;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.StringUtils;

public class RollCallAdapter extends RecyclerView.Adapter<RollCallAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private List<ParticipantInfoModel> list;
    private OnItemClickListener onItemClickListener;
    public RollCallAdapter(Context context, List<ParticipantInfoModel> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.item_rollcall,viewGroup,false);
        return new ViewHolder(view);
    }
    public void setDatas(List<ParticipantInfoModel> partList){
        list = partList;
        L.d("do this --->>>>size:"+partList.size());
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
//        ImgUtils.setCircle(list.get(i).getAvatar(),h.iv_avatar);
        h.tv_nameinfo.setText(!TextUtils.isEmpty(list.get(i).getDeviceName())?list.get(i).getDeviceName():list.get(i).getAppShowName());
        h.tv_post.setText(list.get(i).getAppShowDesc());

        if (list.get(i).isChecked()) {          //当前item选中
            if (!Constract.ROLLCALL_STATUS_SPEAKER.equals(list.get(i).getHandStatus())) {               //非发言中
                h.tv_post.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                h.tv_post.setTextColor(context.getResources().getColor(R.color.white));
            }
        } else {
//            if (!Constract.ROLLCALL_STATUS_SPEAKER.equals(list.get(i).getHandStatus())) {
                h.tv_post.setTextColor(Color.WHITE);
//            }
//            else {
//                h.tv_post.setTextColor(context.getResources().getColor(R.color.color_D48806));
//            }

        }

        if (Constract.ROLLCALL_STATUS_UP.equals(list.get(i).getHandStatus()) && !list.get(i).isChecked()) {   //举手
            h.tv_nameinfo.setTextColor(context.getResources().getColor(R.color.white));
            h.iv_status.setImageResource(R.drawable.icon_item_handup);
        } else if (Constract.ROLLCALL_STATUS_UP.equals(list.get(i).getHandStatus()) && list.get(i).isChecked()) {    //举手选中
            h.tv_nameinfo.setTextColor(context.getResources().getColor(R.color.white));
            h.iv_status.setImageResource(R.drawable.icon_item_choose);
        } else if (Constract.ROLLCALL_STATUS_SPEAKER.equals(list.get(i).getHandStatus()) && !list.get(i).isChecked()) {    //发言中
            h.tv_nameinfo.setTextColor(context.getResources().getColor(R.color.white));
            h.iv_status.setImageResource(R.drawable.icon_item_speak);
        } else if (Constract.ROLLCALL_STATUS_SPEAKER.equals(list.get(i).getHandStatus()) && list.get(i).isChecked()) {     //发言选中
            h.tv_nameinfo.setTextColor(context.getResources().getColor(R.color.white));
            h.iv_status.setImageResource(R.drawable.icon_item_choose);
        } else {                        //其他情况
            h.tv_nameinfo.setTextColor(context.getResources().getColor(R.color.white));
            h.iv_status.setImageResource(R.drawable.icon_item_handup);
        }

//        if (i == list.size()-1) {
//            h.divider.setVisibility(View.INVISIBLE);
//        } else {
            h.divider.setVisibility(View.VISIBLE);
//        }

        h.constraint_root.setOnClickListener(v-> {
            if (onItemClickListener != null) {
                onItemClickListener.OnItemClick(i);
            }
        });

    }

    private void setInfoTextColor() {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout constraint_root;
        private ImageView iv_avatar;    //头像
        private TextView tv_nameinfo;   //姓名
        private TextView tv_post;       //职位
        private ImageView iv_status;    //状态
        private View divider;

        public ViewHolder(@NonNull View v) {
            super(v);
            constraint_root = v.findViewById(R.id.constraint_root);
            iv_avatar = v.findViewById(R.id.iv_avatar);
            tv_nameinfo = v.findViewById(R.id.tv_nameinfo);
            tv_post = v.findViewById(R.id.tv_post);
            iv_status = v.findViewById(R.id.iv_status);
            divider = v.findViewById(R.id.divider);
        }
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }
}
