package cn.closeli.rtc.widget;

import android.app.Presentation;
import android.content.Context;
import android.util.Log;
import android.view.Display;

public class UCPresentation extends Presentation {
    /**
     * Creates a new presentation that is attached to the specified display
     * using the default theme.
     *
     * @param outerContext The context of the application that is showing the presentation.
     *                     The presentation will create its own context (see {@link #getContext()}) based
     *                     on this context and information about the associated display.
     * @param display      The display to which the presentation should be attached.
     */
    public UCPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    /**
     * Creates a new presentation that is attached to the specified display
     * using the optionally specified theme.
     *
     * @param outerContext The context of the application that is showing the presentation.
     *                     The presentation will create its own context (see {@link #getContext()}) based
     *                     on this context and information about the associated display.
     * @param display      The display to which the presentation should be attached.
     * @param theme        A style resource describing the theme to use for the window.
     *                     See <a href="{@docRoot}guide/topics/resources/available-resources.html#stylesandthemes">
     *                     Style and Theme Resources</a> for more information about defining and using
     *                     styles.  This theme is applied on top of the current theme in
     */
    public UCPresentation(Context outerContext, Display display, int theme) {
        super(outerContext, display, theme);
    }

    @Override
    public void cancel() {
        try {
            super.cancel();
        } catch (Exception e) {
            Log.e("UCPresentation", e.toString());
        }
    }
}
