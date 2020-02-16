package cn.closeli.rtc.sdk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import org.webrtc.SurfaceViewRenderer;

public class TestSurface7View extends SurfaceViewRenderer {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public TestSurface7View(Context context) {
        super(context);
        paint.setStrokeWidth(100);
        paint.setColor(Color.RED);


    }

    public TestSurface7View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPoint(getWidth() / 2,getHeight() / 2,paint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        setWillNotDraw(false);
    }

}
