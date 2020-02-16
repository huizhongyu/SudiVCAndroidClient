package cn.closeli.rtc.constract;

public interface Constract {
    String ETHERNET_SERVICE = "ethernet";
    int AUDIO_STAND = 0;//麦克风-标准
    int AUDIO_MAX =1;   //麦克风-最大
    int AUDIO_MIN =2;   //麦克风-最小

    int SETTING_CAMERA = 10;    //设置摄像头
    int SETTING_MICRO =11;      //设置麦克风
    int SETTING_SHARE =12;      //设置共享屏幕
    int SETTING_RECORD = 13;    //设置录制权限
    int SETTING_HOST_CHANGE = 9;
    int SETTING_NONE =8;    //

    String SPKEY_DEVICE_CONFIG = "spkey_device_config";

    String ROLLCALL_STATUS_UP = "up";                   //与会者列表 - 举手
    String ROLLCALL_STATUS_SPEAKER = "speaker";         //与会者列表 - 发言中
    String ROLLCALL_STATUS_DOWN ="down";                //与会者列表 - 未举手

    String VALUE_STATUS_ON = "on";  //开
    String VALUE_STATUS_OFF = "off";//关


    String JOIN_ACTIVE = "active";  //开
    String JOIN_INVITED = "invited";//关

    String online = "online";  //开
    String offline = "offline";//关

    String replace = "replace";//关
    String change = "change";//关

    /**
     * 码流模式，枚举值如下
     * MIX_MAJOR_AND_SHARING：主辅混叠
     * MIX_MAJOR：主流混叠
     * SFU_SHARING：辅码流
     */
    String MIX_MAJOR_AND_SHARING ="MIX_MAJOR_AND_SHARING";
    String MIX_MAJOR = "MIX_MAJOR";
    String SFU_SHARING = "SFU_SHARING";


    /**
     * 组织关系
     */
    int TYPE_ADAPTER_COMPANY = 1;               //公司
    int TYPE_ADAPTER_DEPARTMENT = 2;           //部门
    int TYPE_ADAPTER_GROUP = 3;                 //小组
    int TYPE_ADAPTER_PERSON = 0;                //个人

    int ERROR_12001 = 12001;

    int MSG_KEY_UP = 100;
    int MSG_KEY_DOWN = 101;
    int MSG_KEY_LEFT = 102;
    int MSG_KEY_RIGHT = 103;
    int MSG_KEY_ZOOMIN = 104;
    int MSG_KEY_ZOOMOUT = 105;
    int MSG_DO = 10;
    int MSG_STOP = 11;

    String BUNDLE_KEY_TIME = "key_time";

    //mcu
    int VIDEO_MODE_MCU = 1000;
    //sfu
    int VIDEO_MODE_SFU = 1001;

    //mcu 最大会议人数
    int MCU_CAPACITY = 12;
    //sfu 最大会议人数
    int SFU_CAPACITY = 8;

    //发起会议
    int INVITE_FROMTYPE_CREATE = 10;
    //通讯录
    int INVITE_FROMTYPE_CONTRACT = 11;
    //会议界面
    int INVITE_FROMTYPE_MEET = 12;


    int MSG_FOCUS = 1000;
    int MSG_FOCUS_GROUP = 1001;
    String BUNDLE_FOCUS = "bundle_focus";
}
