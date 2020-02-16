package cn.closeli.rtc.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.SPEditor;
import top.androidman.SuperButton;

/**
 * @author Administrator
 */
public class StateView extends FrameLayout {
    private TextView meetingName;
    private ImageView micState;
    private ImageView cameraState;
    private LinearLayout infoContainer;
    private ImageView stateNotConnect;
    private ParticipantInfoModel model;
    private SuperButton role;
    private SuperButton me;
    private FrameLayout extraConbtainer;
    private View bound;
    private FrameLayout info;
    private Rect rect;

    public StateView(@NonNull Context context) {
        this(context, null);
    }

    public StateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.item_video_view, this, true);
        setId(View.generateViewId());
        setFocusable(true);
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_item_selector_white_line));
        initView();
    }

    private void initView() {
        meetingName = findViewById(R.id.meeting_name);
        micState = findViewById(R.id.mic);
        cameraState = findViewById(R.id.camera);
        infoContainer = findViewById(R.id.info_container);
        stateNotConnect = findViewById(R.id.state_not_connect);
        role = findViewById(R.id.role);
        me = findViewById(R.id.me);
        bound = findViewById(R.id.bound);
        extraConbtainer = findViewById(R.id.extra_container);
        info = findViewById(R.id.info);
    }

    public void setInfoData(ParticipantInfoModel model) {
        this.model = model;
        micState.setImageResource(model.isAudioActive() ? R.drawable.meet_tab_menu_icon_mai : R.drawable.meet_tab_menu_icon_mai_unuse);
        cameraState.setImageResource(model.isShareable() ? R.mipmap.meet_tab_icon_screen : R.mipmap.meet_tab_icon_screen_unuse);
        cameraState.setVisibility(model.isShareable() ? VISIBLE : GONE);
        if (!meetingName.getText().toString().equals(model.getAppShowName())) {
            meetingName.setText(model.getAppShowName());
        }
        String userId = SPEditor.instance().getUserId();
        role.setVisibility(model.isHandSpeaker() ? VISIBLE : GONE);
        me.setVisibility(userId.equals(model.getUserId()) ? VISIBLE : GONE);
        info.setVisibility(Constants.STREAM_SHARING.equals(model.getStreamType()) ? GONE : VISIBLE);
        updateOfflineState();
    }

    private void updateOfflineState() {
        if (model.isPlaceholder()) {
            stateNotConnect.setVisibility(GONE);
            infoContainer.setVisibility(GONE);
            bound.setVisibility(VISIBLE);
            return;
        }
        bound.setVisibility(GONE);
        if (model.isOnline()) {
            infoContainer.setVisibility(VISIBLE);
            if (model.isVideoActive()) {
                stateNotConnect.setVisibility(GONE);
            } else {
                stateNotConnect.setVisibility(VISIBLE);
                stateNotConnect.setImageResource(R.mipmap.maininterface_screen_notunicom_big);
            }
        } else {
            stateNotConnect.setVisibility(VISIBLE);
            stateNotConnect.setImageResource(R.mipmap.maininterface_screen_quit_big);
            infoContainer.setVisibility(GONE);
        }
    }

    public ParticipantInfoModel getModel() {
        return model;
    }

    /**
     * 添加额外的内容
     *
     * @param view
     */
    public void addExtraContent(View view) {
        if (extraConbtainer.getChildCount() > 0 || view == null) {
            return;
        }
        extraConbtainer.setVisibility(VISIBLE);
        extraConbtainer.removeAllViews();
        removeParent(view);
        extraConbtainer.addView(view);
    }

    public void removeParent(View view) {
        if (view != null) {
            ViewGroup parentViewGroup = (ViewGroup) view.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeView(view);
            }
        }
    }

    @Nullable
    public View getExtraContent() {
        View extra = null;
        if (extraConbtainer.getChildCount() > 0) {
            extra = extraConbtainer.getChildAt(0);
            extraConbtainer.removeView(extra);
        }
        return extra;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}
