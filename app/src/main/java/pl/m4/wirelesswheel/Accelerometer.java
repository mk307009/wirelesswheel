package pl.m4.wirelesswheel;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class Accelerometer implements SensorEventListener {
    public static final String TAG = "Accelerometer";
    public static final int MAX_VALUE = 255;
    public static final int MULTIPLIER = 100;
    public static final float RANGE_TOLERANCE = 0.4f;
    public SensorManager mSensorManager;
    private Sensor sensor;
    private float sensitiveYaw;
    private float sensitivePitch;
    private TextView coordinates;
    private ImageButton aroundButton;
    private Activity activity;
    private java.text.DecimalFormat df;
    private TcpClient tcp;
    private boolean isAroundMove;
    private SpeedMeter speed;
    private BroadcastReceiver bReceiver;

    public Accelerometer(Activity activity, TcpClient client){
        this.tcp = client;
        this.activity = activity;
        isAroundMove = false;
        coordinates = (TextView) activity.findViewById(R.id.axis);
        aroundButton = (ImageButton) activity.findViewById(R.id.around);
        sensitiveYaw = 0.15f;
        sensitivePitch = 0.2f;
        df = new java.text.DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(1);
        registerSensor();
        aroundButton.setOnTouchListener(aroundButtonListener());
        speed = new SpeedMeter(activity);
        activity.addContentView(speed, new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        speed.bringToFront();
        speed.invalidate();
    }

    public void registerSensor(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshSensor();
            }
        };
        activity.registerReceiver(bReceiver, filter);
        activity.unregisterReceiver(bReceiver);
        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void refreshSensor(){
        mSensorManager.unregisterListener(this);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor(){
        mSensorManager.unregisterListener(this);
    }

    /**
     * print supported sensor list in logcat.
     * TAG: AccelerometerSensorList.
     */
    public void printSensorList() {
        for (Sensor sens : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.i(TAG + "SensorList", sens.getName());
        }
    }

    private float[] lastPos = {0,0,0};
    private float[] calibratedPos = {0,0,0};
    private float[] toCalibrate = {0,0,0};
    private String msg = null;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            modifyYPositionBySensitivity(event.values[1]);
            modifyXPositionBySensitivity(event);
            for (int i = 0; i < 3; i++){
                toCalibrate[i] = event.values[i];
            }
            setMessageToSendAndCirclePosition();
        }
    }

    private void setMessageToSendAndCirclePosition() {
        if (isAroundTurn())
            msg = tcp.generateMessage(convertTo4BytesString(getTransformedY()), "0   ", "0   ");
        else
            msg = tcp.generateMessage("0   ", convertTo4BytesString(-getTransformedZ()), convertTo4BytesString(getTransformedY()));
        Log.i(TAG, msg);
        tcp.setMessage(msg);
        speed.setX(speed.getCircleX()+(getTransformedY()/5));
        speed.setY(speed.getCircleY()+(-getTransformedZ()/5));
        coordinates.setText("Y: " + convertTo4BytesString(getTransformedY()) + "; Z: " + convertTo4BytesString(-getTransformedZ()));
    }

    private void modifyXPositionBySensitivity(SensorEvent event) {
        if (sensitivePitch(event.values[2])) {
            lastPos[2] = event.values[2] + calibratedPos[2];
            if (lastPos[2] < RANGE_TOLERANCE && lastPos[2] > -RANGE_TOLERANCE){
                lastPos[2] = 0;
            }
            lastPos[0] = event.values[0] + calibratedPos[0];
        }
    }

    private void modifyYPositionBySensitivity(float value) {
        if (sensitiveYaw(value)) {
            lastPos[1] = value + calibratedPos[1];
            if (lastPos[1] < RANGE_TOLERANCE && lastPos[1] > -RANGE_TOLERANCE)
                lastPos[1] = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean sensitivePitch(float value){
        value += calibratedPos[2];
        float result = Math.abs(value - lastPos[2]);
        if (result > sensitivePitch) {
            return true;
        }
        return false;
    }

    private boolean sensitiveYaw(float value){
        value += calibratedPos[1];
        float result = Math.abs(value - lastPos[1]);
        if (result > sensitiveYaw) {
            return true;
        }
        return false;
    }

    public void calibrate(){
        for (int i = 0; i < 3; i++){
            calibratedPos[i] = reverseValue(toCalibrate[i]);
            lastPos[i] = toCalibrate[i] + calibratedPos[i];
        }
    }

    private float reverseValue(float pos){
        return -pos;
    }

    public float getX(){
        return lastPos[0];
    }

    public float getY(){
        return lastPos[1];
    }

    public float getZ(){
        return lastPos[2];
    }

    public boolean isAroundTurn(){
        return isAroundMove;
    }

    public void setAroundTurn(boolean isAround){
        isAroundMove = isAround;
    }

    private int fixLimitsValue(float pos){
        pos = pos * MULTIPLIER;
        if (pos > MAX_VALUE)
            return MAX_VALUE;
        else if(pos < -MAX_VALUE)
            return -MAX_VALUE;
        return Math.round(pos);
    }

    public int getTransformedX(){
        return fixLimitsValue(lastPos[0]);
    }

    public int getTransformedY(){
        return fixLimitsValue(lastPos[1]);
    }

    public int getTransformedZ(){
        return fixLimitsValue(lastPos[2]);
    }

    /**
     * @param value int value to convert.
     * @return String value (4bytes) with spaces.
     */
    public String convertTo4BytesString(int value){
        return String.format("%-4s", value);
    }

    private View.OnTouchListener aroundButtonListener(){
        return new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setAroundTurn(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    setAroundTurn(false);
                }
                return false;
            }
        };
    }
}