package cn.closeli.rtc.sdk;

import android.view.SurfaceView;

public class VideoCanvas {
    public static final int RENDER_MODE_HIDDEN = 1;
    public static final int RENDER_MODE_FIT = 2;

    public SurfaceView view;
    public int renderMode;
    public String uid;

    public VideoCanvas(SurfaceView view) {
        this.view = view;
        this.renderMode = 1;
    }

    public VideoCanvas(SurfaceView view, int renderMode, String uid) {
        this.view = view;
        this.renderMode = renderMode;
        this.uid = uid;
    }
}

