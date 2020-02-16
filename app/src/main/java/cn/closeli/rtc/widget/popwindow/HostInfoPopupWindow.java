package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.BasePopupWindow;
import top.androidman.SuperButton;

/**
 * @author Administrator
 */
public class HostInfoPopupWindow extends BasePopupWindow {
    private SuperButton superButton;
    private TextView meetingName, meetingId;

    public HostInfoPopupWindow(Context context) {
        super(context);
    }


    @Override
    public int thisLayout() {
        return R.layout.pop_host_info;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        meetingName = fv(R.id.meeting_name);
        meetingId = fv(R.id.meeting_id);
        superButton = fv(R.id.sb_top);
    }


    @Override
    public void doInitData() {

    }

    public void setParticipant(ParticipantInfoModel participant) {
        if (participant != null) {
            meetingName.setText(String.format(Locale.getDefault(), "会议会场: %s", participant.getDeviceName()));
            meetingId.setText(String.format(Locale.getDefault(), "会议ID: %s", participant.getAccount()));
            if (SPEditor.instance().getUserId().equals(RoomManager.get().getHostId())) {
                superButton.setText("主持人");
                superButton.setColorNormal(mContext.getResources().getColor(R.color.color_FF9800));
            } else {
                superButton.setText("参会者");
                superButton.setColorNormal(mContext.getResources().getColor(R.color.color_687DF4));
            }
        }
    }
}


