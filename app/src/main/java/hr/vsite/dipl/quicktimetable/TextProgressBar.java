package hr.vsite.dipl.quicktimetable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by Jure on 13.6.2016..
 * Thanks to http://colintmiller.com/how-to-add-text-over-a-progress-bar-on-android/
 */
public class TextProgressBar extends ProgressBar {
    private String progressBarText;
    private Paint progressBarTextPaint;
    private Rect bounds;

    private void initialize() {
        progressBarText = "";
        progressBarTextPaint = new Paint();
        progressBarTextPaint.setTextSize(30);
        bounds = new Rect();
        this.setVisibility(View.INVISIBLE);
    }

    public TextProgressBar(Context context) {
        super(context);
        initialize();
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //determine center of text
        progressBarTextPaint.getTextBounds(progressBarText, 0, progressBarText.length(), bounds);

        float x = canvas.getWidth() / 2 - bounds.centerX();
        float y = canvas.getHeight() / 2 -bounds.centerY();
        canvas.drawText(progressBarText, x, y, progressBarTextPaint);
    }

    /**
     *
     * @param text the text to display over the progress bar.
     */
    public synchronized void setText(String text) {
        this.progressBarText = text;
        drawableStateChanged();
    }

    /**
     *
     * @param color color for the progress bar text.
     */
    public void setTextColor(int color) {
        progressBarTextPaint.setColor(color);
        drawableStateChanged();
    }

    /**
     *
     * @param size
     */
    public void setTextSize(float size) {
        progressBarTextPaint.setTextSize(size);
        drawableStateChanged();
    }


    public String getText() {
        return this.progressBarText;
    }
}
