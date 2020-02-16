package cn.closeli.rtc.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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

/**
 * @author Administrator
 * <p>
 */
public class DynamicVideoLayout_ extends FrameLayout {

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
    private static final int AUDIO = 1;
    private int width;
    private int height;

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
    private OnItemVideoClickListener onItemVideoClickListener;
    private List<Rect> rects = new ArrayList<>();
    /**
     * 保存所有的子view
     */
    private List<FrameLayout> frameLayouts = new ArrayList<>();

    public DynamicVideoLayout_(@NonNull Context context) {
        this(context, null);
    }

    public DynamicVideoLayout_(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicVideoLayout_(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;
    }

    public List<ParticipantInfoModel> getInfoModels() {
        return infoModels;
    }

    public void setInfoModels(List<ParticipantInfoModel> infoModels) {
        this.infoModels = infoModels;
        this.layoutMode = infoModels.size();
        buildRect(layoutMode);
        buildInnerViews();
        L.e("refresh when setInfoModels");
        addChildView();
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (rects.isEmpty()) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            if (rects.size() < i + 1) {
                return;
            }
            Rect rect = rects.get(i);
            View container = getChildAt(i);
            container.layout(rect.left, rect.top, rect.right, rect.bottom);
        }
    }


    /**
     * 组装视图
     *
     * @param layoutMode 布局模式
     */
    private void buildRect(int layoutMode) {
        if (!isOneOfMode(layoutMode)) {
            L.e("layout mode is invalid");
            return;
        }
        rects.clear();
        this.layoutMode = layoutMode;
        switch (layoutMode) {
            //全屏
            case MODE_1: {
                if (infoModels.isEmpty()) {
                    return;
                }
                rects.add(new Rect(0, 0, width, height));
                break;
            }
            //1 + 1
            case MODE_2: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int halfW = (int) (width / 2.0f);
                rects.add(new Rect(0, 0, halfW, height));
                rects.add(new Rect(halfW, 0, width, height));
                break;
            }
//            1 + 2
            case MODE_3: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int halfW = (int) (width / 2.0f);
                int halfH = (int) (height / 2.0f);
                rects.add(new Rect(0, 0, halfW, height));
                rects.add(new Rect(halfW, 0, width, halfH));
                rects.add(new Rect(halfW, halfH, width, height));
                break;
            }
            //1 + 3
            case MODE_4: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int itemW = (int) (width / 3.0f);
                int itemH = (int) (height / 3.0f);
                rects.add(new Rect(0, 0, width, itemH * 2));

                for (int i = 1; i < MODE_4; i++) {
                    rects.add(new Rect((i - 1) * itemW, itemH * 2, itemW * i, height));
                }
            }
            break;
            // 1 + 4
            case MODE_5: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int itemW = (int) (width / 4.0f);
                int itemH = (int) (height / 4.0f);
                rects.add(new Rect(0, 0, width, itemH * 3));

                for (int i = 1; i < MODE_5; i++) {
                    rects.add(new Rect((i - 1) * itemW, itemH * 3, itemW * i, height));
                }
            }
            break;
            //1 + 5
            case MODE_6: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int itemW = (int) (width / 3.0f);
                int itemH = (int) (height / 3.0f);
                rects.add(new Rect(0, 0, itemW * 2, itemH * 2));

                for (int i = 1; i < 4; i++) {
                    rects.add(new Rect((i - 1) * itemW, itemH * 2, itemW * i, height));
                }
                for (int i = 4; i < MODE_6; i++) {
                    rects.add(new Rect(itemW * 2, itemH * (i - 4), width, itemH * (i - 3)));
                }
            }
            break;
            //1 + 6
            case MODE_7: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int itemW = (int) (width / 8.0f);
                int itemH = (int) (height / 4.0f);
                rects.add(new Rect(0, 0, itemW * 5, itemH * 3));

                for (int i = 1; i < 5; i++) {
                    rects.add(new Rect((i - 1) * itemW * 2, itemH * 3, itemW * 2 * i, height));
                }
                for (int i = 5; i < MODE_7; i++) {
                    rects.add(new Rect(itemW * 5, (int) (itemH * (i - 5) * 1.5f), width, (int) (itemH * 1.5 * (i - 4))));
                }
            }
            break;
            //1 + 7
            case MODE_8: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int itemW = (int) (width / 4.0f);
                int itemH = (int) (height / 4.0f);
                rects.add(new Rect(0, 0, itemW * 3, itemH * 3));

                for (int i = 1; i < 5; i++) {
                    rects.add(new Rect((i - 1) * itemW, itemH * 3, itemW * i, height));
                }
                for (int i = 5; i < MODE_8; i++) {
                    rects.add(new Rect(itemW * 3, itemH * (i - 5), width, itemH * (i - 4)));
                }
            }
            break;
            //1 + 8
            case MODE_9: {
                if (infoModels.isEmpty()) {
                    return;
                }
                int itemW = (int) (width / 4.0f);
                int itemH = (int) (height / 4.0f);
                rects.add(new Rect(0, 0, itemW * 2, height));

                for (int i = 1; i < MODE_9; i++) {

                    int column = (i - 1) % 2;
                    int row = (i - 1) / 2;
                    rects.add(new Rect(itemW * (column + 2), row * itemH, itemW * (column + 3), (row + 1) * itemH));
                }
            }
            break;
            default:
        }
        for (Rect rect : rects) {
            rect.inset(GAP, GAP);
        }
    }

    private boolean isOneOfMode(int layoutMode) {
        return layoutMode == MODE_1 ||
                layoutMode == MODE_2 ||
                layoutMode == MODE_3 ||
                layoutMode == MODE_4 ||
                layoutMode == MODE_5 ||
                layoutMode == MODE_6 ||
                layoutMode == MODE_7 ||
                layoutMode == MODE_8 ||
                layoutMode == MODE_9;
    }

    /**
     * 只有首次会调用
     */
    private void buildInnerViews() {
        if (infoModels.isEmpty()) {
            return;
        }
        frameLayouts.clear();
        for (int i = 0; i < infoModels.size(); i++) {
            Rect rect;
            if (rects.size() > i) {
                rect = rects.get(i);
            } else {
                rect = new Rect();
            }
            ParticipantInfoModel model = infoModels.get(i);
            FrameLayout frameLayout = buildFrameLayoutWithRenderView(rect, model);
//            addView(frameLayout);
            frameLayouts.add(frameLayout);
            L.e("add in buildInnerViews %s", model.getConnectionId());
        }
//        requestLayout();
    }

    private void addChildView() {
        removeAllViews();
        for (int i = 0; i < Math.min(rects.size(), frameLayouts.size()); i++) {
            Rect rect = rects.get(i);
//            removeParent(fl);
            addView(frameLayouts.get(i), new LayoutParams(rect.width(), rect.height()));
        }
        if (rects.size() > frameLayouts.size()) {
            for (int i = frameLayouts.size(); i < rects.size(); i++) {
                addView(buildFrameLayoutWithRenderView(rects.get(i), new ParticipantInfoModel()));
            }
        }
        resizeChildren();
        requestLayout();
    }

    private FrameLayout buildFrameLayoutWithRenderView(Rect rect, ParticipantInfoModel model) {
        FrameLayout fl = new FrameLayout(getContext());
        fl.setFocusable(true);
        if (model.getVideoSink() != null) {
            LayoutParams rightParams = getRightParams(rect.width(), rect.height());
            SurfaceViewRenderer surfaceViewRenderer = WebRTCManager.get().createRendererView(getContext());
            ProxyVideoSink videoSink = model.getVideoSink();
            videoSink.setTarget(surfaceViewRenderer);
            fl.addView(surfaceViewRenderer, rightParams);
        }
        StateView stateView = new StateView(getContext());
        stateView.setInfoData(model);
        fl.addView(stateView, new LayoutParams(rect.width(), rect.height()));
        stateView.setRect(rect);
        fl.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_item_selector_white_line));
        fl.setFocusable(true);
        return fl;
    }

    public static LayoutParams getRightParams(double itemW, double itemH) {
        float ratio = (float) (itemW / itemH);
        float standard = 16.0f / 9;
        if (ratio > standard) {
            itemW = itemH * standard;
        } else if (ratio < standard) {
            itemH = itemW / standard;
        }
        return new LayoutParams((int) itemW, (int) itemH, Gravity.CENTER);
    }

    /**
     * 一定是在addChildView之后调用的
     * 重新为所有child分配尺寸
     */
    private void resizeChildren() {
        if (rects.isEmpty()) {
            return;
        }
        for (int i = 0; i < rects.size(); i++) {
            Rect rect = rects.get(i);
            FrameLayout frameLayout = (FrameLayout) getChildAt(i);
            int childCount = frameLayout.getChildCount();
            StateView stateView = (StateView) frameLayout.getChildAt(childCount > 1 ? 1 : 0);
            stateView.setLayoutParams(new LayoutParams(rect.width(), rect.height()));
            if (childCount > 1) {
                SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) frameLayout.getChildAt(0);
                surfaceViewRenderer.setLayoutParams(getRightParams(rect.width(), rect.height()));
            }
            stateView.setOnClickListener(v -> {
                ParticipantInfoModel model = stateView.getModel();
                if (onItemVideoClickListener != null && !WebRTCManager.SHARING.equals(model.getStreamType()) && model.isVideoActive()) {
                    model.setLayoutX(rect.left);
                    model.setLayoutY(rect.top);
                    model.setLayoutWidth(rect.width());
                    model.setLayoutHeight(rect.height());
                    onItemVideoClickListener.OnVideoClick(model);
                }
            });
        }
    }

    @LayoutMode
    public int getMode() {
        return layoutMode;
    }

    /**
     * 设置布局模式
     *
     * @param layoutMode 布局模式
     */
    public void setMode(@LayoutMode int layoutMode) {
        isDefaultLayout = false;
        if (this.layoutMode == layoutMode) {
            return;
        }
        this.layoutMode = layoutMode;
        buildRect(layoutMode);
        L.e("refresh when setMode");
        addChildView();
    }

    /**
     * 自动布局(默认布局)
     */
    public void autoLayout() {
        isDefaultLayout = true;
        int layoutMode = Math.min(MODE_9, infoModels.size());
        setMode(layoutMode);
    }

    /**
     * 移除通知
     */
    public void notifyItemRemoved(ParticipantInfoModel infoModel) {
        if (infoModel == null) {
            return;
        }
        for (int i = frameLayouts.size() - 1; i >= 0; i--) {
            FrameLayout frameLayout = frameLayouts.get(i);
            int childCount = frameLayout.getChildCount();
            StateView stateView = (StateView) frameLayout.getChildAt(childCount > 1 ? 1 : 0);
            ParticipantInfoModel model = stateView.getModel();
            if (model == null) {
                return;
            }
            if (model.getConnectionId().equals(infoModel.getConnectionId())) {
                frameLayouts.remove(i);
                break;
            }
        }
        if (isDefaultLayout && frameLayouts.size() != layoutMode) {
            buildRect(frameLayouts.size());
            this.layoutMode = frameLayouts.size();
        }
        L.e("refresh when notifyItemRemoved");
        addChildView();
    }

    /**
     * 通知单个更新
     */
    public void notifyItemChanged(ParticipantInfoModel model) {
        if (model == null) {
            return;
        }

        for (FrameLayout frameLayout : frameLayouts) {
            int childCount = frameLayout.getChildCount();
            StateView stateView = (StateView) frameLayout.getChildAt(childCount > 1 ? 1 : 0);
            ParticipantInfoModel temp = stateView.getModel();
            if (temp == null) {
                return;
            }
            //需要更新
            if (model.getUserId().equals(temp.getUserId()) && model.getStreamType().equals(temp.getStreamType())) {
                stateView.setInfoData(model);
                if (frameLayout.getChildCount() < 2 && model.getVideoSink() != null) {
                    Rect rect = stateView.getRect();
                    LayoutParams rightParams = getRightParams(rect.width(), rect.height());
                    SurfaceViewRenderer surfaceViewRenderer = WebRTCManager.get().createRendererView(getContext());
                    model.getVideoSink().setTarget(surfaceViewRenderer);
                    frameLayout.addView(surfaceViewRenderer, 0, rightParams);
                    L.e("refresh when notifyItemChanged");
                }
                return;
            }
        }
        //需要新增
        FrameLayout frameLayout = buildFrameLayoutWithRenderView(new Rect(), model);
        if (!WebRTCManager.SHARING.equals(model.getStreamType())) {
            frameLayouts.add(frameLayout);
        } else {
            frameLayouts.add(0, frameLayout);
        }
        L.e("add in notifyItemChanged %s", model.getConnectionId());
        if (isDefaultLayout && frameLayouts.size() != layoutMode) {
            buildRect(frameLayouts.size());
            //自动模式，修改布局mode
            this.layoutMode = frameLayouts.size();
        }
        L.e("refresh when notifyItemChanged count = %d infomodels count = %d", frameLayouts.size(), infoModels.size());
        addChildView();
    }

    /**
     * 更新视频状态
     *
     * @param infoModel
     */
    public void notifyVideoStateChanged(ParticipantInfoModel infoModel) {
        for (FrameLayout frameLayout : frameLayouts) {
            StateView stateView = (StateView) frameLayout.getChildAt(frameLayout.getChildCount() > 1 ? 1 : 0);
            ParticipantInfoModel model = stateView.getModel();
            if (infoModel.getConnectionId().equals(model.getConnectionId())) {
                stateView.setInfoData(infoModel);
                break;
            }
        }
    }

    /**
     * 交换制定位置的两个窗口
     *
     * @param index1
     * @param index2
     */
    public void swap(int index1, int index2) {
        onlySwap(index1, index2);
        addChildView();
    }

    private void onlySwap(int index1, int index2) {
        L.e("childCount = %d", infoModels.size());
        FrameLayout video1 = frameLayouts.get(index1);
        FrameLayout video2 = frameLayouts.get(index2);
        frameLayouts.remove(index1);
        removeView(video1);
        frameLayouts.add(index2, video1);
        frameLayouts.remove(video2);
        removeView(video2);
        frameLayouts.add(index1, video2);
//        addChildView();
    }

    /**
     * 交换两个实体类
     *
     * @param infoModel1
     * @param infoModel2
     */
    public void swap(ParticipantInfoModel infoModel1, ParticipantInfoModel infoModel2) {
        int index1 = findIndeX(infoModel1);
        int index2 = findIndeX(infoModel2);
        if (isIndexValid(index1) && isIndexValid(index2)) {
            L.e("refresh when swap(ParticipantInfoModel infoModel1, ParticipantInfoModel infoModel2)");
            swap(index1, index2);
        }
    }

    /**
     * 检查索引是否合法
     *
     * @param index
     * @return
     */
    private boolean isIndexValid(int index) {
        return index >= 0 && index < frameLayouts.size();
    }

    /**
     * 将窗口交换到主窗口
     *
     * @param model model
     */
    public void swap2Main(ParticipantInfoModel model) {
        int index = findIndeX(model);
        if (isIndexValid(index)) {
            L.e("refresh when swap2Main");
            swap(0, index);
        }
    }

    /**
     * 获取索引
     *
     * @param infoModel
     * @return
     */
    private int findIndeX(ParticipantInfoModel infoModel) {
        for (int i = 0; i < frameLayouts.size(); i++) {
            ViewGroup video = frameLayouts.get(i);
            StateView stateView = (StateView) video.getChildAt(video.getChildCount() > 1 ? 1 : 0);
            ParticipantInfoModel model = stateView.getModel();
            String connectionId = model.getConnectionId();
            if (!TextUtils.isEmpty(connectionId) && connectionId.equals(infoModel.getConnectionId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 辅流
     */
    public void shareScreen(ParticipantInfoModel model) {
        if (model == null) {
            return;
        }
        FrameLayout fl = new FrameLayout(getContext());
        fl.setFocusable(true);
        LayoutParams rightParams = getRightParams(0, 0);
        SurfaceViewRenderer surfaceViewRenderer = WebRTCManager.get().createRendererView(getContext());
        fl.addView(surfaceViewRenderer, rightParams);
        if (model.getVideoSink() != null) {
            model.getVideoSink().setTarget(surfaceViewRenderer);
        }
        WebRTCManager.get().shareHdmi(surfaceViewRenderer);
        StateView stateView = new StateView(getContext());
        stateView.setInfoData(model);
        fl.addView(stateView, new LayoutParams(0, 0));
        fl.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_item_selector_white_line));
        frameLayouts.add(0, fl);
        if (isDefaultLayout && frameLayouts.size() != layoutMode) {
            buildRect(frameLayouts.size());
            //自动模式，修改布局mode
            this.layoutMode = frameLayouts.size();
        }
        L.e("refresh when shareScreen");
        addChildView();
    }

    public SurfaceViewRenderer getSurfaceViewRender(String connectId) {
        for (int i = 0; i < frameLayouts.size(); i++) {
            ViewGroup video = frameLayouts.get(i);
            if (video.getChildCount() < 2) {
                continue;
            }
            StateView stateView = (StateView) video.getChildAt(1);
            ParticipantInfoModel model = stateView.getModel();
            if (model.getConnectionId().equals(connectId)) {
                SurfaceViewRenderer surfaceView = (SurfaceViewRenderer) video.getChildAt(0);
                video.removeView(surfaceView);
                return surfaceView;
            }
        }
        return null;
    }

    public void resumeSurfaceViewRender(String connectId, SurfaceViewRenderer surfaceViewRenderer) {
        for (int i = 0; i < frameLayouts.size(); i++) {
            ViewGroup video = frameLayouts.get(i);
            if (video.getChildCount() != 1) {
                continue;
            }
            StateView stateView = (StateView) video.getChildAt(0);
            ParticipantInfoModel model = stateView.getModel();
            if (connectId.equals(model.getConnectionId())) {
                LayoutParams rightParams = getRightParams(model.getLayoutWidth(), model.getLayoutHeight());
                video.addView(surfaceViewRenderer, 0, rightParams);
                break;
            }
        }
        resizeChildren();
    }

    /**
     * 与会者接收到布局改变的通知，修改布局变更
     *
     * @param layoutModel
     */
    public void acceptRoomLayoutMode(@NotNull RoomLayoutModel layoutModel) {


        int mode = layoutModel.getMode();
        ArrayList<String> connectIds = layoutModel.getLayout();
        if (connectIds.size() != frameLayouts.size() || connectIds.size() != infoModels.size()) {
            L.e("acceptRoomLayoutMode connectId count = %d  infoModels count = %d  framelayout count = %d", connectIds.size(), infoModels.size(), frameLayouts.size());
            return;
        }

//        boolean isSame = true;
//        for (int i = 0; i < frameLayouts.size(); i++) {
//            String connectId = connectIds.get(i);
//            FrameLayout frameLayout = frameLayouts.get(i);
//            StateView stateView = (StateView) frameLayout.getChildAt(frameLayout.getChildCount() > 1 ? 1 : 0);
//            if (!connectId.equals(stateView.getModel().getConnectionId())) {
//                isSame = false;
//                break;
//            }
//        }
//        if (isSame) {
//            L.e("acceptRoomLayoutMode", "same layout");
//            return;
//        }
        for (int i = 0; i < frameLayouts.size(); i++) {
            String connectId = connectIds.get(i);
            for (int i1 = 0; i1 < frameLayouts.size(); i1++) {
                FrameLayout frameLayout = frameLayouts.get(i1);
                StateView stateView = (StateView) frameLayout.getChildAt(frameLayout.getChildCount() > 1 ? 1 : 0);
                if (connectId.equals(stateView.getModel().getConnectionId()) && i != i1) {
                    onlySwap(i, i1);
                    break;
                }
            }
        }
        buildRect(mode);
        L.e("refresh when acceptRoomLayoutMode");
        addChildView();
    }

    /**
     * 停止共享流
     *
     * @param infoModel
     */
    public void stopShare(ParticipantInfoModel infoModel) {
        notifyItemRemoved(infoModel);
    }

    /**
     * 当前是否是默认布局
     *
     * @return true 默认布局 false 手动选择
     */
    public boolean isDefaultLayout() {
        return isDefaultLayout;
    }

    public void setDefaultLayout(boolean defaultLayout) {
        isDefaultLayout = defaultLayout;
    }

    public void setOnItemVideoClickListener(OnItemVideoClickListener videoClickListener) {
        this.onItemVideoClickListener = videoClickListener;
    }

    public interface OnItemVideoClickListener {
        void OnVideoClick(ParticipantInfoModel infoModel);
    }
}
