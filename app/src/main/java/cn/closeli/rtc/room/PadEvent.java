package cn.closeli.rtc.room;

import java.util.ArrayList;

import cn.closeli.rtc.model.InviteModel;
import cn.closeli.rtc.model.PadModel;
import cn.closeli.rtc.model.ReJoinModel;
import cn.closeli.rtc.model.RoomTimeModel;
import cn.closeli.rtc.model.SingleLoginModel;
import cn.closeli.rtc.model.SlingModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.UserDeviceList;
import cn.closeli.rtc.model.ws.AudioStstusResp;
import cn.closeli.rtc.model.ws.HandsStatusResp;
import cn.closeli.rtc.model.ws.PresetInfoResp;
import cn.closeli.rtc.model.ws.VideoStatusResp;
import cn.closeli.rtc.model.ws.WsError;

public interface PadEvent {
    void accessPadIn(PadModel padModel);

    void setAudio();

    void setVideo();

    void setSpeak();

    void leavePadRoom();

    void shareHDMI();

}