package cn.closeli.rtc.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.webrtc.SurfaceViewRenderer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.model.RoomLayoutModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.sdk.ProxyVideoSink;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.UtilsKt;

/**
 * @author Administrator
 * <p>
 */
@Deprecated
public class DynamicVideoLayout extends FrameLayout {

    public static final int MODE_1 = 1;
    public static final int MODE_2 = 2;
    public static final int MODE_3 = 3;
    public static final int MODE_4 = 4;
    public static final int MODE_5 = 5;
    public static final int MODE_6 = 6;
    public static final int MODE_7 = 7;
    public static final int MODE_8 = 8;
    public static final int MODE_9 = 9;
    private static final int GAP = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_1, MODE_2, MODE_3, MODE_4, MODE_5, MODE_6, MODE_7, MODE_8, MODE_9})
    @interface LayoutMode {
    }

    @LayoutMode
    private int layoutMode = MODE_1;
    /**
     * 是否是默认布局
     */
    private boolean isDefaultLayout = true;
    private List<ParticipantInfoModel> infoModels = new ArrayList<>();
//    private OnItemVideoClickListener onItemVideoClickListener;
    private List<Rect> rects = new ArrayList<>();
    private List<FrameLayout> noAddedView = new ArrayList<>();
    private int width = getResources().getDisplayMetrics().widthPixels;
    private int height = getResources().getDisplayMetrics().heightPixels;

    public DynamicVideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public DynamicVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public List<ParticipantInfoModel> getInfoModels() {
        return infoModels;
    }

//    public void setInfoModels(List<ParticipantInfoModel> infoModels, @LayoutMode int mode) {
//        this.infoModels = infoModels;
//        this.layoutMode = mode;
//        buildRect(layoutMode);
//        buildInnerViews();
//        renderData();
//    }
//
//    private void renderData() {
//        for (ParticipantInfoModel infoModel : infoModels) {
//            notifyItemChanged(infoModel);
//        }
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        if (rects.isEmpty()) {
//            return;
//        }
//        for (int i = 0; i < getChildCount(); i++) {
//            if (rects.size() < i + 1) {
//                return;
//            }
//            Rect rect = rects.get(i);
//            View container = getChildAt(i);
//            container.layout(rect.left, rect.top, rect.right, rect.bottom);
//        }
//
//
//    }
//
//    private void addChildView() {
//        if (infoModels.size() > rects.size()){
//            //添加完子view后将多余的添加到缓存中
//            for (int i = 0; i < rects.size(); i++) {
//                Rect rect = rects.get(i);
//                FrameLayout frameLayout = buildFrameLayoutWithRenderView(rect, infoModels.get(i));
//                addView(frameLayout);
//                frameLayout.layout(rect.left, rect.top, rect.right, rect.bottom);
//            }
//            for (int i = rects.size(); i < infoModels.size(); i++) {
//
//            }
//        }
//        for (int i = 0; i < rects.size(); i++) {
//            Rect rect = rects.get(i);
//            if (infoModels.size() >= i + 1) {
//            } else {
//                noAddedView.add();
//            }
//        }
//    }
//
//    /**
//     * 组装视图
//     *
//     * @param layoutMode 布局模式
//     */
//    private void buildRect(int layoutMode) {
//        if (!isOneOfMode(layoutMode)) {
//            L.e("layout mode is invalid");
//            return;
//        }
//        rects.clear();
//        this.layoutMode = layoutMode;
//        switch (layoutMode) {
//            //全屏
//            case MODE_1: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                rects.add(new Rect(0, 0, width, height));
//                break;
//            }
//            //1 + 1
//            case MODE_2: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int halfW = (int) (width / 2.0f);
//                rects.add(new Rect(0, 0, halfW, height));
//                rects.add(new Rect(halfW, 0, width, height));
//                break;
//            }
////            1 + 2
//            case MODE_3: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int halfW = (int) (width / 2.0f);
//                int halfH = (int) (height / 2.0f);
//                rects.add(new Rect(0, 0, halfW, height));
//                rects.add(new Rect(halfW, 0, width, halfH));
//                rects.add(new Rect(halfW, halfH, width, height));
//                break;
//            }
//            //1 + 3
//            case MODE_4: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int itemW = (int) (width / 3.0f);
//                int itemH = (int) (height / 3.0f);
//                rects.add(new Rect(0, 0, width, itemH * 2));
//
//                for (int i = 1; i < MODE_4; i++) {
//                    rects.add(new Rect((i - 1) * itemW, itemH * 2, itemW * i, height));
//                }
//            }
//            break;
//            // 1 + 4
//            case MODE_5: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int itemW = (int) (width / 4.0f);
//                int itemH = (int) (height / 4.0f);
//                rects.add(new Rect(0, 0, width, itemH * 3));
//
//                for (int i = 1; i < MODE_5; i++) {
//                    rects.add(new Rect((i - 1) * itemW, itemH * 3, itemW * i, height));
//                }
//            }
//            break;
//            //1 + 5
//            case MODE_6: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int itemW = (int) (width / 3.0f);
//                int itemH = (int) (height / 3.0f);
//                rects.add(new Rect(0, 0, itemW * 2, itemH * 2));
//
//                for (int i = 1; i < 4; i++) {
//                    rects.add(new Rect((i - 1) * itemW, itemH * 2, itemW * i, height));
//                }
//                for (int i = 4; i < MODE_6; i++) {
//                    rects.add(new Rect(itemW * 2, itemH * (i - 4), width, itemH * (i - 3)));
//                }
//            }
//            break;
//            //1 + 6
//            case MODE_7: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int itemW = (int) (width / 8.0f);
//                int itemH = (int) (height / 4.0f);
//                rects.add(new Rect(0, 0, itemW * 5, itemH * 3));
//
//                for (int i = 1; i < 5; i++) {
//                    rects.add(new Rect((i - 1) * itemW * 2, itemH * 3, itemW * 2 * i, height));
//                }
//                for (int i = 5; i < MODE_7; i++) {
//                    rects.add(new Rect(itemW * 5, (int) (itemH * (i - 5) * 1.5f), width, (int) (itemH * 1.5 * (i - 4))));
//                }
//            }
//            break;
//            //1 + 7
//            case MODE_8: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int itemW = (int) (width / 4.0f);
//                int itemH = (int) (height / 4.0f);
//                rects.add(new Rect(0, 0, itemW * 3, itemH * 3));
//
//                for (int i = 1; i < 5; i++) {
//                    rects.add(new Rect((i - 1) * itemW, itemH * 3, itemW * i, height));
//                }
//                for (int i = 5; i < MODE_8; i++) {
//                    rects.add(new Rect(itemW * 3, itemH * (i - 5), width, itemH * (i - 4)));
//                }
//            }
//            break;
//            //1 + 8
//            case MODE_9: {
//                if (infoModels.isEmpty()) {
//                    return;
//                }
//                int itemW = (int) (width / 4.0f);
//                int itemH = (int) (height / 4.0f);
//                rects.add(new Rect(0, 0, itemW * 2, height));
//
//                for (int i = 1; i < MODE_9; i++) {
//
//                    int column = (i - 1) % 2;
//                    int row = (i - 1) / 2;
//                    rects.add(new Rect(itemW * (column + 2), row * itemH, itemW * (column + 3), (row + 1) * itemH));
//                }
//            }
//            break;
//            default:
//        }
//        for (Rect rect : rects) {
//            rect.inset(GAP, GAP);
//        }
//    }
//
//    private boolean isOneOfMode(int layoutMode) {
//        return layoutMode == MODE_1 ||
//                layoutMode == MODE_2 ||
//                layoutMode == MODE_3 ||
//                layoutMode == MODE_4 ||
//                layoutMode == MODE_5 ||
//                layoutMode == MODE_6 ||
//                layoutMode == MODE_7 ||
//                layoutMode == MODE_8 ||
//                layoutMode == MODE_9;
//    }
//
//    private void buildInnerViews() {
//        if (rects.isEmpty()) {
//            return;
//        }
//        removeAllViews();
//        for (int i = 0; i < rects.size(); i++) {
//            Rect rect = rects.get(i);
//            FrameLayout frameLayout = buildFrameLayoutWithRenderView(rect, infoModels.get(i));
//            addView(frameLayout);
//        }
//        requestLayout();
//    }
//
//    private FrameLayout buildFrameLayoutWithRenderView(Rect rect, ParticipantInfoModel infoModel) {
//        FrameLayout fl = new FrameLayout(getContext());
//        StateView stateView = new StateView(getContext());
//        ParticipantInfoModel infoModel1 = infoModel;
//        if (infoModel == null) {
//            infoModel1 = new ParticipantInfoModel();
//        }
//        stateView.setInfoData(infoModel1);
//        if (infoModel1.getVideoSink() != null) {
//            SurfaceViewRenderer rv = WebRTCManager.get().createRendererView(getContext());
//            infoModel1.getVideoSink().setTarget(rv);
//            fl.addView(rv, getRightParams(rect.width(), rect.height()));
//        }
//        fl.addView(stateView, new LayoutParams(rect.width(), rect.height()));
//        fl.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_item_selector_white_line));
//        fl.setFocusable(true);
//        return fl;
//    }
//
//    public static LayoutParams getRightParams(double itemW, double itemH) {
//        float ratio = (float) (itemW / itemH);
//        float standard = 16.0f / 9;
//        if (ratio > standard) {
//            itemW = itemH * standard;
//        } else if (ratio < standard) {
//            itemH = itemW / standard;
//        }
//        return new FrameLayout.LayoutParams((int) itemW, (int) itemH, Gravity.CENTER);
//    }
//
//    /**
//     * 重新为所有child分配尺寸
//     */
//    private void resizeChildren() {
//        if (rects.isEmpty()) {
//            return;
//        }
//        for (int i = 0; i < getChildCount(); i++) {
//            ViewGroup video = (ViewGroup) getChildAt(i);
//            Rect rect = rects.get(i);
//            if (video.getChildCount() > 1) {
//                SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) video.getChildAt(0);
//                surfaceViewRenderer.setLayoutParams(getRightParams(rect.width(), rect.height()));
//                StateView stateView = (StateView) video.getChildAt(1);
//                stateView.setFocusable(true);
//                stateView.setLayoutParams(new LayoutParams(rect.width(), rect.height()));
//                stateView.setOnClickListener(v -> {
//                    ParticipantInfoModel model = stateView.getModel();
//                    if (onItemVideoClickListener != null && !WebRTCManager.SHARING.equals(model.getStreamType()) && model.isVideoActive()) {
//                        model.setLayoutX(rect.left);
//                        model.setLayoutY(rect.top);
//                        model.setLayoutWidth(rect.width());
//                        model.setLayoutHeight(rect.height());
//                        onItemVideoClickListener.OnVideoClick(model);
//                    }
//                });
//            } else {
//                StateView stateView = (StateView) video.getChildAt(0);
//                stateView.setLayoutParams(new LayoutParams(rect.width(), rect.height()));
//                stateView.setOnClickListener(v -> {
//                    ParticipantInfoModel model = stateView.getModel();
//                    if (onItemVideoClickListener != null && !WebRTCManager.SHARING.equals(model.getStreamType()) && model.isVideoActive()) {
//                        model.setLayoutX(rect.left);
//                        model.setLayoutY(rect.top);
//                        model.setLayoutWidth(rect.width());
//                        model.setLayoutHeight(rect.height());
//                        onItemVideoClickListener.OnVideoClick(model);
//                    }
//                });
//            }
//        }
//        requestLayout();
//    }
//
//    @LayoutMode
//    public int getMode() {
//        return layoutMode;
//    }
//
//    /**
//     * 设置布局模式
//     *
//     * @param layoutMode 布局模式
//     */
//    public void setMode(@LayoutMode int layoutMode) {
//        isDefaultLayout = false;
//        if (this.layoutMode == layoutMode) {
//            return;
//        }
//        buildRect(layoutMode);
//        if (this.layoutMode > layoutMode) {
//            int deleteCount = this.layoutMode - layoutMode;
//            for (int i = 0; i < deleteCount; i++) {
//                int childCount = getChildCount();
//                removeViewAt(childCount - 1);
//            }
//        } else {
//            int addCount = layoutMode - this.layoutMode;
//            for (int i = 0; i < addCount; i++) {
//                addView(buildFrameLayoutWithRenderView(new Rect()));
//            }
//            for (int i = this.layoutMode; i < layoutMode && infoModels.size() > i; i++) {
//                notifyItemChanged(infoModels.get(i));
//            }
//        }
//        this.layoutMode = layoutMode;
//        resizeChildren();
//    }
//
//    public void addItem(ParticipantInfoModel infoModel) {
//        if (infoModels.size() > layoutMode) {
//            return;
//        }
//        notifyItemChanged(infoModel);
//    }
//
//    /**
//     * 自动布局
//     */
//    public void autoLayout() {
//        if (isDefaultLayout) {
//            int layoutMode = Math.min(MODE_9, infoModels.size());
//            setMode(layoutMode);
//        }
//    }
//
//    /**
//     * 移除通知
//     */
//    public void notifyItemRemoved(ParticipantInfoModel infoModel) {
//        if (infoModel == null) {
//            return;
//        }
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            if (child instanceof ViewGroup) {
//                if (((ViewGroup) child).getChildCount() < 2) {
//                    continue;
//                }
//                View stateView = ((ViewGroup) child).getChildAt(1);
//                if (stateView instanceof StateView) {
//                    ParticipantInfoModel pre = ((StateView) stateView).getModel();
//                    if (pre.getConnectionId().equals(infoModel.getConnectionId())) {
//                        //目标
//                        removeView(child);
//                        //后面添加新的
//                        addView(buildFrameLayoutWithRenderView(new Rect()));
//                        break;
//                    }
//                }
//            }
//        }
//        resizeChildren();
//    }
//
//    /**
//     * 通知单个更新
//     */
//    public void notifyItemChanged(ParticipantInfoModel model) {
//        if (model == null) {
//            return;
//        }
//        for (int i = 0; i < getChildCount(); i++) {
//            FrameLayout video = (FrameLayout) getChildAt(i);
//            if (video.getChildCount() == 0) {
//                continue;
//            }
//            StateView stateView;
//            if (video.getChildCount() < 2) {
//                stateView = (StateView) video.getChildAt(0);
//                ParticipantInfoModel preModel = stateView.getModel();
//                if (model.getVideoSink() != null && (TextUtils.isEmpty(preModel.getConnectionId()) || preModel.getVideoSink() == null)) {
//                    SurfaceViewRenderer rv = WebRTCManager.get().createRendererView(getContext());
//                    model.getVideoSink().setTarget(rv);
//                    rv.setLayoutParams(getRightParams(0, 0));
//                    video.addView(rv, 0);
//                    stateView.setInfoData(model);
//                    break;
//                }
//            } else {
//                stateView = (StateView) video.getChildAt(1);
//                ParticipantInfoModel preModel = stateView.getModel();
//                if (preModel.getConnectionId().equals(model.getConnectionId())) {
//                    stateView.setInfoData(model);
//                    break;
//                }
//            }
//        }
//        resizeChildren();
//    }
//
//    /**
//     * 交换制定位置的两个窗口
//     *
//     * @param index1
//     * @param index2
//     */
//    public void swap(int index1, int index2) {
//        onlySwap(index1, index2);
//        resizeChildren();
//    }
//
//    private void onlySwap(int index1, int index2) {
//        L.e("childCount = %d", infoModels.size());
//        View video1 = getChildAt(index1);
//        View video2 = getChildAt(index2);
//        removeViewAt(index1);
//        addView(video1, index2);
//        removeView(video2);
//        addView(video2, index1);
//    }
//
//    /**
//     * 交换两个实体类
//     *
//     * @param infoModel1
//     * @param infoModel2
//     */
//    public void swap(ParticipantInfoModel infoModel1, ParticipantInfoModel infoModel2) {
//        int index1 = findIndeX(infoModel1);
//        int index2 = findIndeX(infoModel2);
//        if (isIndexValid(index1) && isIndexValid(index2)) {
//            swap(index1, index2);
//        }
//    }
//
//    /**
//     * 检查索引是否合法
//     *
//     * @param index
//     * @return
//     */
//    private boolean isIndexValid(int index) {
//        return index >= 0 && index < getChildCount();
//    }
//
//    /**
//     * 将窗口交换到主窗口
//     *
//     * @param model model
//     */
//    public void swap2Main(ParticipantInfoModel model) {
//        int index = findIndeX(model);
//        if (isIndexValid(index)) {
//            swap(0, index);
//        }
//    }
//
//    /**
//     * 获取索引
//     *
//     * @param infoModel
//     * @return
//     */
//    private int findIndeX(ParticipantInfoModel infoModel) {
//        for (int i = 0; i < getChildCount(); i++) {
//            ViewGroup video = (ViewGroup) getChildAt(i);
//            StateView stateView;
//            if (video.getChildCount() > 1) {
//                stateView = (StateView) video.getChildAt(1);
//            } else {
//                stateView = (StateView) video.getChildAt(0);
//            }
//            ParticipantInfoModel model = stateView.getModel();
//            String connectionId = model.getConnectionId();
//            if (!TextUtils.isEmpty(connectionId) && connectionId.equals(infoModel.getConnectionId())) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * 辅流
//     */
//    public void shareScreen(ParticipantInfoModel model) {
//        if (model == null) {
//            return;
//        }
//        FrameLayout frameLayout = buildFrameLayoutWithRenderView(new Rect());
//        SurfaceViewRenderer rv = WebRTCManager.get().createRendererView(getContext());
//        rv.setLayoutParams(getRightParams(0, 0));
//        StateView stateView = (StateView) frameLayout.getChildAt(0);
//        ProxyVideoSink videoSink = model.getVideoSink();
//        if (videoSink != null) {
//            videoSink.setTarget(rv);
//        }
//        frameLayout.addView(rv, 0);
//        stateView.setInfoData(model);
//        removeViewAt(getChildCount() - 1);
//        addView(frameLayout, 0);
//        WebRTCManager.get().shareHdmi(rv);
//        resizeChildren();
//    }
//
//    public SurfaceViewRenderer getSurfaceViewRender(String connectId) {
//        for (int i = 0; i < getChildCount(); i++) {
//            ViewGroup video = (ViewGroup) getChildAt(i);
//            if (video.getChildCount() < 2) {
//                continue;
//            }
//            StateView stateView = (StateView) video.getChildAt(1);
//            ParticipantInfoModel model = stateView.getModel();
//            if (model.getConnectionId().equals(connectId)) {
//                SurfaceViewRenderer surfaceView = (SurfaceViewRenderer) video.getChildAt(0);
//                video.removeView(surfaceView);
//                return surfaceView;
//            }
//        }
//        return null;
//    }
//
//    public void resumeSurfaceViewRender(String connectId, SurfaceViewRenderer surfaceViewRenderer) {
//        for (int i = 0; i < getChildCount(); i++) {
//            ViewGroup video = (ViewGroup) getChildAt(i);
//            if (video.getChildCount() != 1) {
//                continue;
//            }
//            StateView stateView = (StateView) video.getChildAt(0);
//            ParticipantInfoModel model = stateView.getModel();
//            if (connectId.equals(model.getConnectionId())) {
//                video.addView(surfaceViewRenderer, 0);
//                break;
//            }
//        }
//        resizeChildren();
//    }
//
//    /**
//     * 与会者接收到布局改变的通知，修改布局变更
//     *
//     * @param layoutModel
//     */
//    public void acceptRoomLayoutMode(@NotNull RoomLayoutModel layoutModel) {
//        int mode = layoutModel.getMode();
//        ArrayList<String> connectIds = layoutModel.getLayout();
//        setMode(connectIds.size());
//        Outer:
//        for (int i = 0; i < connectIds.size(); i++) {
//            String connectId = connectIds.get(i);
//            for (int j = 0; j < getChildCount(); j++) {
//                ViewGroup video = (ViewGroup) getChildAt(j);
//                StateView stateView;
//                if (video.getChildCount() < 2) {
//                    for (ParticipantInfoModel model : infoModels) {
//                        if (model.getConnectionId().equals(connectId)) {
//                            notifyItemChanged(model);
//                            continue Outer;
//                        }
//                    }
//                } else {
//                    stateView = (StateView) video.getChildAt(1);
//                    ParticipantInfoModel model = stateView.getModel();
//                    if (connectId.equals(model.getConnectionId())) {
//                        if (i != j) {
//                            onlySwap(i, j);
//                        }
//                        continue Outer;
//                    }
//                }
//            }
//        }
//        setMode(mode);
//        resizeChildren();
//    }
//
//    /**
//     * 停止共享流
//     *
//     * @param infoModel
//     */
//    public void stopShare(ParticipantInfoModel infoModel) {
//        notifyItemRemoved(infoModel);
//    }
//
//    /**
//     * 当前是否是默认布局
//     *
//     * @return true 默认布局 false 手动选择
//     */
//    public boolean isDefaultLayout() {
//        return isDefaultLayout;
//    }
//
//    public void setDefaultLayout(boolean defaultLayout) {
//        isDefaultLayout = defaultLayout;
//    }
//
//    public void setOnItemVideoClickListener(OnItemVideoClickListener videoClickListener) {
//        this.onItemVideoClickListener = videoClickListener;
//    }
//
//    public interface OnItemVideoClickListener {
//        void OnVideoClick(ParticipantInfoModel infoModel);
//    }
}
