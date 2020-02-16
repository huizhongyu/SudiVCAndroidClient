package cn.closeli.rtc.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.PartLinkedListBean;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.RoomManager;

public class BoundShadowLayout extends FrameLayout {
    //视频宽
    private static final int VIDEO_WIDTH = 1920;
    //
    private static final int VIDEO_HEIGHT = 1080;
    //视频显示宽
    private int videoDisplayWidth = -1;
    //视频显示高
    private int videoDisplayHeight = -1;
    //比例
    private float factorX;
    private float factorY;
    //重新测量
    private boolean reSize = false;
    //填充位置
    private SurfaceView surfaceView;
    //靠近右边界的id
    private List<Integer> rightBoundIds = new ArrayList<>();
    //靠近下边界的id
    private List<Integer> bottomBoundIds = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    //    private Map<String, Boolean/*true 退出 false 在线*/> offLineState = new HashMap<>();
    private OnClickListener onClickListener = v -> {
        if (v instanceof StateView) {
            ParticipantInfoModel model = ((StateView) v).getModel();
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(model);
            }
        }
    };
    private OnKeyListener onKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (onItemClickListener == null) {
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == MotionEvent.ACTION_DOWN && rightBoundIds.contains(v.getId())) {
                onItemClickListener.onRightBoundClick();
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == MotionEvent.ACTION_DOWN && bottomBoundIds.contains(v.getId())) {
                onItemClickListener.onBottomBoundClick();
                return false;
            }
            return BoundShadowLayout.super.onKeyDown(keyCode, event);
        }
    };
    private int heightPixels;

    public BoundShadowLayout(@NonNull Context context) {
        this(context, null);
    }

    public BoundShadowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoundShadowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        setId(generateViewId());
    }

    private void init() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthPixels = metrics.widthPixels;
        heightPixels = metrics.heightPixels;
        factorX = (float) (widthPixels * 1.0 / VIDEO_WIDTH);
        factorY = (float) (heightPixels * 1.0 / VIDEO_HEIGHT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (reSize) {
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(videoDisplayWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(videoDisplayHeight, MeasureSpec.EXACTLY));
        }
        videoDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
        videoDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        rightBoundIds.clear();
        bottomBoundIds.clear();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof StateView) {
                ParticipantInfoModel item = ((StateView) view).getModel();
                int b = item.getLayoutY() + item.getLayoutHeight();
                int r = item.getLayoutX() + item.getLayoutWidth();
                if (r >= videoDisplayWidth && !rightBoundIds.contains(view.getId())) {
                    rightBoundIds.add(view.getId());
                }
                if (b >= videoDisplayHeight && !bottomBoundIds.contains(view.getId())) {
                    bottomBoundIds.add(view.getId());
                }
                view.layout(item.getLayoutX(), item.getLayoutY(), r, b);
            }
        }
    }

    /**
     * 设置位置数据
     *
     * @param items 数据集{@link PartLinkedListBean}
     */
    public void setData(List<ParticipantInfoModel> items) {
        if (surfaceView == null) {
            L.e("not bind SurfaceView");
            return;
        }
        removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            ParticipantInfoModel rectItem = items.get(i);
            if (rectItem == null) {
                UCToast.showNormal(getContext(), R.string.str_error_data_error);
                continue;
            }
            if (Constants.STREAM_SHARING.equals(rectItem.getStreamType())) {
                continue;
            }
            L.e("x = %d y = %d width = %d height = %d", rectItem.getLeft(), rectItem.getTop(), rectItem.getWidth(), rectItem.getHeight());
            rectItem.setLayoutX(rectItem.getLeft());
            rectItem.setLayoutY(rectItem.getTop());
            rectItem.setLayoutWidth(rectItem.getWidth());
            rectItem.setLayoutHeight(Math.min(heightPixels, rectItem.getTop() + rectItem.getHeight()) - rectItem.getTop());
            View temp = buildItemView(rectItem);
            if (rectItem.isOnline()) {
                temp.setOnClickListener(onClickListener);
            }
            temp.setOnKeyListener(onKeyListener);
            addView(temp, rectItem.getLayoutWidth(), rectItem.getLayoutHeight());
        }
        requestLayout();
    }

    /**
     * 构建子view
     *
     * @param rectItem 数据源
     * @return view
     */
    private View buildItemView(ParticipantInfoModel rectItem) {
        StateView stateView = new StateView(getContext());
        stateView.setInfoData(rectItem);
        return stateView;
    }

    /**
     * 绑定surfaceview
     *
     * @param surfaceView surfaceview
     */
    public void bindSurfaceView(SurfaceView surfaceView) {
        if (this.surfaceView != null) {
            return;
        }
        this.surfaceView = surfaceView;
        if (this.surfaceView == null) {
            return;
        }
        this.surfaceView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            reSize = true;
            videoDisplayWidth = right - left;
            videoDisplayHeight = bottom - top;
            requestLayout();
        });
    }

    /**
     * 添加自己的流数据 （延迟问题）
     * @param connectId
     * @param surfaceViewRenderer
     */
    public void addSelfStream(String connectId, SurfaceViewRenderer surfaceViewRenderer) {
        Log.i("addSelfStream","addSelfStream");
        for (int i = 0; i < getChildCount(); i++) {
            StateView stateView = (StateView) getChildAt(i);
            ParticipantInfoModel model = stateView.getModel();
            if (connectId.equals(model.getConnectionId())) {
                surfaceViewRenderer.setLayoutParams(DynamicVideoLayout_.getRightParams(model.getWidth(), model.getHeight()));
                stateView.addExtraContent(surfaceViewRenderer);
                break;
            }
        }
    }

    /**
     * 更新单个辅流遮罩的状态为离线
     *
     * @param offlineUserId 辅流对应的用户id
     */
    public void updateSharingState2Offline(String offlineUserId) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof StateView) {
                ParticipantInfoModel model = ((StateView) view).getModel();
                String userId = model.getUserId();
                if (offlineUserId.equals(userId)) {
                    model.setOnlineStatus(Constants.OFFLINE);
                    ((StateView) view).setInfoData(model);
                    break;
                }
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ParticipantInfoModel item);

        //
        void onBottomBoundClick();

        //
        void onRightBoundClick();

    }

}
