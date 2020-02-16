package cn.closeli.rtc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.App;
import cn.closeli.rtc.R;

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.ListViewHolder> {
    private OnItemClickListener onItemClickListener;
    private List<String> itemList;
    private LayoutInflater mLayoutInflater;
    private String channel;
    private Context context;

    public AccountListAdapter(Context context, List<String> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.mLayoutInflater = LayoutInflater.from(context);
    }



    public void setDatas(List<String> itemList) {
        this.itemList.clear();
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountListAdapter.ListViewHolder(mLayoutInflater.inflate(R.layout.item_account_choose, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountListAdapter.ListViewHolder holder, int position) {
        holder.tv_ac.setText(itemList.get(position));
        holder.tv_ac.setOnClickListener(v->{
            if(onItemClickListener != null){
                onItemClickListener.onItemClick(holder.tv_ac.getText().toString().trim());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_ac;

        public ListViewHolder(View itemView) {
            super(itemView);
            tv_ac = itemView.findViewById(R.id.tv_ac);

        }

    }
   public interface OnItemClickListener{
        void onItemClick(String str);
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}

