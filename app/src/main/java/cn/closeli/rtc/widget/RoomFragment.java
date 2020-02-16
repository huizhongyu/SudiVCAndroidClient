package cn.closeli.rtc.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class RoomFragment extends FrameLayout {

    private ImageView iv_item_center, iv_item_flag;
    private TextView iv_item_center_text, tv_item_name;
    private View view;
    private int seatIndex;
    private boolean isPlay;

    public RoomFragment(Context context) {
        this(context, null);
    }

    public RoomFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
//        view = LayoutInflater.from(BaseConfig.getContext()).inflate(R.layout.item_chat_wheat, this);
//        iv_item_center = view.findViewById(R.id.iv_item_center);
//        uc_view_item = view.findViewById(R.id.uc_view_item);
//        iv_item_flag = view.findViewById(R.id.iv_item_flag);
//        iv_item_center_text = view.findViewById(R.id.iv_item_center_text);
//        tv_item_name = view.findViewById(R.id.tv_item_name);
    }


}

