package cn.closeli.rtc.controller.fragment;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.App;
import cn.closeli.rtc.R;
import cn.closeli.rtc.VideoConferenceActivity;
import cn.closeli.rtc.constract.BundleKey;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.controller.PreviewActivity;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.widget.UCToast;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 会控 - 设置 - 我的设置
 */
public class MineSettingFragment extends Fragment implements CustomAdapt {
    private LinearLayout ll_host_change;
    private TextView tv_first_content;
    private ImageView iv_first_icon;
    private boolean isHost;     //是否是主持人
    private boolean isSwitchOff;
    public MineSettingFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_minesetting,container,false);
        if (getArguments() != null) {
            isHost = getArguments().getBoolean(BundleKey.USER_IS_HOST,false);
        }
        ll_host_change = view.findViewById(R.id.ll_host_change);
        tv_first_content = view.findViewById(R.id.tv_first_content);
        iv_first_icon = view.findViewById(R.id.iv_first_icon);

        if (isHost) {
            tv_first_content.setText(R.string.str_setting_host_change);
            iv_first_icon.setImageResource(R.drawable.icon_right_arrow);
        } else {
            tv_first_content.setText(R.string.str_setting_mine_allow);
            iv_first_icon.setImageResource(R.drawable.icon_switch_on);
        }

        ll_host_change.setOnClickListener(v-> {
            if (isHost) {
                if(getActivity() instanceof  VideoConferenceActivity) {
                    RoomClient.get().getParticipants(RoomManager.get().getRoomId());
                }
            } else {
//                UCToast.show(App.getInstance().getApplicationContext(),"切换");
                iv_first_icon.setImageResource(isSwitchOff?R.drawable.icon_switch_off:R.drawable.icon_switch_on);
                isSwitchOff = !isSwitchOff;
            }
        });
        return view;
    }

    /**
     * 是否按照宽度进行等比例适配 (为了保证在高宽比不同的屏幕上也能正常适配, 所以只能在宽度和高度之中选一个作为基准进行适配)
     *
     * @return {@code true} 为按照宽度适配, {@code false} 为按照高度适配
     */
    @Override
    public boolean isBaseOnWidth() {
        return false;
    }

    /**
     * 返回设计图上的设计尺寸, 单位 dp
     * {@link #getSizeInDp} 须配合 {@link #isBaseOnWidth()} 使用, 规则如下:
     * 如果 {@link #isBaseOnWidth()} 返回 {@code true}, {@link #getSizeInDp} 则应该返回设计图的总宽度
     * 如果 {@link #isBaseOnWidth()} 返回 {@code false}, {@link #getSizeInDp} 则应该返回设计图的总高度
     * 如果您不需要自定义设计图上的设计尺寸, 想继续使用在 AndroidManifest 中填写的设计图尺寸, {@link #getSizeInDp} 则返回 {@code 0}
     *
     * @return 设计图上的设计尺寸, 单位 dp
     */
    @Override
    public float getSizeInDp() {
        return 0;
    }
}
