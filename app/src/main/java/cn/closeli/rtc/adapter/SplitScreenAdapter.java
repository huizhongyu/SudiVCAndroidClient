package cn.closeli.rtc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.ParticipantModel;
import cn.closeli.rtc.sdk.CLRtcSinaling;
import cn.closeli.rtc.utils.RoomManager;

public class SplitScreenAdapter extends RecyclerView.Adapter<SplitScreenAdapter.ListViewHolder> {

    private List<FrameLayout> itemList;
    private LayoutInflater mLayoutInflater;
    private String channel;

    public SplitScreenAdapter(Context context, List<FrameLayout> itemList, String channel) {

        this.itemList = itemList;
        this.channel = channel;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<FrameLayout> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SplitScreenAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SplitScreenAdapter.ListViewHolder(mLayoutInflater.inflate(R.layout.item_split, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SplitScreenAdapter.ListViewHolder holder, int position) {
        holder.frameLayout.addView(itemList.get(position).getChildAt(0));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout frameLayout;


        public ListViewHolder(View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.fl_main);

        }

    }
}


