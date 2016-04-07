package pl.m4.wirelesswheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class SpeedMeter extends View {
    public static final String TAG = "SpeedMeter";
    private Paint paint, recPaint;
    private float x, y;
    private float circleX, circleY;

    public SpeedMeter(Context activity) {
        super(activity);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        recPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setShadowLayer(8, 0, 0, Color.DKGRAY);
        setLayerType(LAYER_TYPE_SOFTWARE, paint);

        recPaint.setColor(Color.BLUE);
        recPaint.setStyle(Style.STROKE);

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        x = width/2;
        y = height/2;
        circleX = x;
        circleY = y;
    }

    public void changeMovingCircleColor() {
        int color;
        if (getX() == getCircleX() && getY() == getCircleY())
            color = Color.rgb(0, 255, 0);
        else color = Color.RED;
        paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(circleX, circleY, 80f, recPaint);
        canvas.drawCircle(x, y, 20f, paint);
        changeMovingCircleColor();
        this.invalidate();
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getX() {
        return this.x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public float getY() {
        return this.y;
    }

    public float getCircleX(){
        return circleX;
    }

    public float getCircleY(){
        return circleY;
    }
}
