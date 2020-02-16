package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.adapter.MemberSettingAdapter;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.widget.BasePopupWindow;

public class JoinMemberPopWindow extends BasePopupWindow {

    private TextView meetingId, currentMeeting, meetingTime, meetingName;

    private RecyclerView members;
    private MemberSettingAdapter adapter;
    private List<ParticipantInfoModel> models;

    public JoinMemberPopWindow(Context context, List<ParticipantInfoModel> models) {
        super(context);
        this.models.clear();
        this.models.addAll(models);
    }

    @Override
    public int thisLayout() {
        return R.layout.pop_join_member;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        currentMeeting = fv(R.id.currentMeeting);
        meetingId = fv(R.id.meetingId);
        meetingTime = fv(R.id.meetingTime);
        meetingName = fv(R.id.meetingName);
        members = fv(R.id.members);
        this.models = new ArrayList<>();
        adapter = new MemberSettingAdapter(mContext,models);
        members.setLayoutManager(new LinearLayoutManager(mContext));
        members.setAdapter(adapter);

    }

    @Override
    public void doInitData() {
        currentMeeting.setText("当前会议：" + "-----------");
        meetingId.setText("会议ID：" + "-----------");
        meetingTime.setText("会议时长："+"-----------");
        meetingName.setText("会议会场："+"-----------");
    }

    public void newData(List<ParticipantInfoModel> models){
        this.models.clear();
        this.models.addAll(models);
        adapter.notifyDataSetChanged();
    }

}
