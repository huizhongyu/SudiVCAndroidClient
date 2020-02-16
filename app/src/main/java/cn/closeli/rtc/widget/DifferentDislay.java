package cn.closeli.rtc.widget;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.webrtc.SurfaceViewRenderer;

import cn.closeli.rtc.R;

public class DifferentDislay extends UCPresentation {
    private OnclickForDifferent onclickForDifferent;
    private FrameLayout frameLayout;
    private LinearLayout linearLayout;
    private ImageView imageView;
    private final String PRESENT_TAG = "present_tag";
    public DifferentDislay(Context outerContext, Display display) {

        super(outerContext, display);

        //TODOAuto-generated constructor stub

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_out_display);
        frameLayout = findViewById(R.id.fl_layout);
        linearLayout = findViewById(R.id.ll_layout);
        frameLayout.setTag(PRESENT_TAG);
        imageView = findViewById(R.id.imageView);



    }

    public interface OnclickForDifferent {
        void onclickDiff();
    }

    public OnclickForDifferent getOnclickForDifferent() {
        return onclickForDifferent;
    }

    public void setOnclickForDifferent(OnclickForDifferent onclickForDifferent) {
        this.onclickForDifferent = onclickForDifferent;
    }

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    public void setFrameLayout(FrameLayout frameLayout) {
        this.frameLayout = frameLayout;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public void setLinearLayout(LinearLayout linearLayout) {
        this.linearLayout = linearLayout;
    }
}
