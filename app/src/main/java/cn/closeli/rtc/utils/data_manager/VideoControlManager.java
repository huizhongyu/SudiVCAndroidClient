package cn.closeli.rtc.utils.data_manager;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import cn.closeli.rtc.BaseActivity;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.ext.Act0;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;
import cn.closeli.rtc.widget.popwindow.NetPopupWindow;


/**
 * 会控弹窗管理
 * 1.网络异常弹窗 netPopupWindow
 * 2.延长时间dialog
 */
public class VideoControlManager {
    private NetPopupWindow netPopupWindow;
    private InviteJoinDialog dialog;

    private VideoControlManager() {
    }
    //网络异常弹窗
    public void showNetErrorPopup(Context context, View view) {
        showNetErrorPopup(context, view, Gravity.CENTER, 0, 0);
    }

    public void showNetErrorPopup(Context context, View view, int gravity, int x, int y) {
        if (netPopupWindow == null) {
            netPopupWindow = new NetPopupWindow(context);
        }
        if (netPopupWindow.isShowing()) {
            return;
        }
        netPopupWindow.showAtLocation(view, gravity, x, y);
        netPopupWindow.setOnDismissListener(() -> {
            dismissNetErrorPopup();
        });
    }

    public void dismissNetErrorPopup() {
        if (netPopupWindow != null && netPopupWindow.isShowing()) {
            netPopupWindow.dismiss();
            netPopupWindow = null;
        }
    }

    //1m 延长时间
    public void show1mDialog(BaseActivity activity) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = InviteJoinDialog.newInstance();
        dialog.setTitle("您还剩60秒,是否要延长会议?");
        dialog.setStrCancel("准时结束会议");
        dialog.setStrComfirm("延长20分钟");
        dialog.setOnDialogClick(new InviteJoinDialog.OnDialogClick() {
            @Override
            public void onClick(InviteJoinDialog orderCancelDialog, int position) {
                if (position == 1) {
                    RoomClient.get().roomDelay(RoomManager.get().getRoomId());
                }
                orderCancelDialog.dismiss();
            }
        });
        dialog.show(activity.getSupportFragmentManager(),
                "CloseDialog");
    }

    //退出弹窗
    public void finishAndReleaseDialog(BaseActivity activity, Act0 act0) {
        InviteJoinDialog dialog = InviteJoinDialog.newInstance();
        dialog.setTitle("是否退出？");
        dialog.setStrComfirm("确定");
        dialog.setStrCancel("取消");
        dialog.setOnDialogClick(new InviteJoinDialog.OnDialogClick() {
            @Override
            public void onClick(InviteJoinDialog orderCancelDialog, int position) {
                if (position == 1) {
                    act0.run();
                }
                orderCancelDialog.dismiss();
            }
        });
        dialog.show(activity.getSupportFragmentManager(),
                "CloseDialog");
    }


    public static final VideoControlManager getInstance() {
        return Inner.inner;
    }

    static class Inner {
        private static final VideoControlManager inner = new VideoControlManager();
    }
}
