package cn.closeli.rtc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.ImgUtils;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;

import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;


public class PartListAdapter extends RecyclerView.Adapter<PartListAdapter.ListViewHolder> {

    private List<ParticipantInfoModel> itemList;
    private LayoutInflater mLayoutInflater;
    private String channel;
    private Context context;

    public PartListAdapter(Context context, List<ParticipantInfoModel> itemList, String channel) {
        this.context = context;
        this.itemList = itemList;
        this.channel = channel;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<ParticipantInfoModel> itemList) {
        this.itemList.clear();
        this.itemList.addAll(itemList);
        L.d("do this ----->>>>>"+itemList.size()+" ...");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PartListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PartListAdapter.ListViewHolder(mLayoutInflater.inflate(R.layout.item_room_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PartListAdapter.ListViewHolder holder, int position) {
        ImgUtils.set(context.getDrawable(R.drawable.maininterface_menu_icon_nobody_small), holder.iv_head);
        holder.item_name.setText(!TextUtils.isEmpty(itemList.get(position).getDeviceName())?itemList.get(position).getDeviceName():itemList.get(position).getAppShowName());
        holder.tv_post.setText(itemList.get(position).getAppShowDesc());
        holder.iv_mute.setBackgroundResource(itemList.get(position).isAudioActive() ? R.mipmap.room_micon : R.mipmap.room_micoff);
        if (!TextUtils.isEmpty(itemList.get(position).getHandStatus())) {
//            holder.tv_hand.setVisibility(itemList.get(position).getHandStatus().equals(CLRtcSinaling.VALUE_STATUS_UP) ? View.VISIBLE : View.INVISIBLE);
        }

//        if (RoomManager.get().getRole().equals(ROLE_PUBLISHER_SUBSCRIBER)) {
//            holder.tv_evicted.setVisibility(itemList.get(position).getUserId().equals(RoomManager.get().getHostId()) ? View.INVISIBLE : View.VISIBLE);
//        } else {
//            holder.tv_evicted.setVisibility(View.INVISIBLE);
//        }
        holder.tv_host.setVisibility(ROLE_PUBLISHER_SUBSCRIBER.equals(itemList.get(position).getRole()) ? View.VISIBLE : View.GONE);
        holder.tv_self.setVisibility(itemList.get(position).getUserId().equals(RoomManager.get().getUserId())?View.VISIBLE:View.GONE);

        holder.iv_mute.setOnClickListener((v -> {
            if (RoomManager.get().getRole().equals(ROLE_PUBLISHER_SUBSCRIBER)) {
//                RoomClient.get().setAudioStatus(channel, RoomManager.get().getUserId(), itemList.get(position).getUserId(), itemList.get(position).isAudioActive() ? false : true);
            }
        }));
        holder.tv_hand.setOnClickListener((v -> {
            if (RoomManager.get().getRole().equals(ROLE_PUBLISHER_SUBSCRIBER)) {
//                RoomClient.get().putDownHand(channel, RoomManager.get().getUserId(), itemList.get(position).getUserId());
            }
        }));

        holder.tv_hand.setOnClickListener((v -> {
            if (RoomManager.get().getRole().equals(ROLE_PUBLISHER_SUBSCRIBER)) {
//                RoomClient.get().putDownHand(channel, RoomManager.get().getUserId(), itemList.get(position).getUserId());
            }
        }));

        holder.tv_evicted.setOnClickListener((v -> {
            String streamId = null;
            String connectId = null;
            for (ParticipantInfoModel participant : RoomClient.get().getRemoteParticipants()) {
                if (participant.getUserId().equals(itemList.get(position).getUserId())) {
                    streamId = participant.getStreamId();
                    connectId = participant.getConnectionId();
                }
            }
            RoomClient.get().forceUnpublish(streamId);
            RoomClient.get().forceDisconnect(connectId);
        }));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_head;
        private TextView item_name;
        private TextView tv_hand;
        private TextView tv_evicted;
        private ImageView iv_mute;
        private TextView tv_post;
        private TextView tv_host;
        private TextView tv_self;

        public ListViewHolder(View itemView) {
            super(itemView);
            iv_head = itemView.findViewById(R.id.image_head);
            item_name = itemView.findViewById(R.id.tv_title);
            tv_hand = itemView.findViewById(R.id.tv_hand);
            tv_evicted = itemView.findViewById(R.id.tv_evicted);
            iv_mute = itemView.findViewById(R.id.iv_mute);
            tv_post = itemView.findViewById(R.id.tv_post);
            tv_host = itemView.findViewById(R.id.tv_host);
            tv_self = itemView.findViewById(R.id.tv_self);

        }

    }
}

