package com.bladestudio.jin.phantomremote;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by @author J.K. Zhou <zhoujk@mcmaster.com> on Date 2016/2/28.
 */
public class MotionDetector {
    private static final String TAG = "MotionDetector";
    public static final int MOTION_STOP = 0xf0;
    public static final int MOTION_FORWARD = 0xf1;
    public static final int MOTION_BACKWARD = 0xf2;
    public static final int MOTION_LEFT = 0xf3;
    public static final int MOTION_RIGHT = 0xf4;

    private SensorManager mSensorMgr;
    private Sensor mSensor;
    private AccelerometerListener mSensorListener;
    private MotionChangeHandler mHandler;
    private int mLastMotion;
    private float mDataBuffer[];
    private final float THRESHOLD = 2.0f;

    public MotionDetector(Context app){
        mSensorMgr = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorListener = new AccelerometerListener();
        mDataBuffer = new float[3];
        mLastMotion = MOTION_STOP;
    }

    public void setHandler(MotionChangeHandler handler){
        mHandler = handler;
    }

    public void register(){
        Log.d(TAG, "register()");
        mSensorMgr.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister(){
        Log.d(TAG, "unregister()");
        mSensorMgr.unregisterListener(mSensorListener);
    }

    private boolean isMotionChanged(float[] vec3){
        boolean changed = false;

        for(int i = 0; i < 3; ++i){
            if (Math.abs(vec3[i] - mDataBuffer[i]) > THRESHOLD){
                mDataBuffer[i] = vec3[i];
                changed = true;
                break;
            }
        }
        return changed;
    }


    private class AccelerometerListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy){}

       @Override
        public void onSensorChanged(SensorEvent event){

           if (isMotionChanged(event.values)) {
               float x = event.values[0];
               float y = event.values[1];
               float z = event.values[2];

               if (Math.abs(y) < 2.5f && x > -3.0f && x < 3.0f && z > 9.0f
                       && mLastMotion != MOTION_FORWARD){
                   mHandler.onMotionChanged(MOTION_FORWARD);
                   mLastMotion = MOTION_FORWARD;
               } else if (Math.abs(y) < 2.5f && x >= 3.0f && x <= 8.5f && z >= 4.0f && z <= 9.0f
                       && mLastMotion != MOTION_STOP){
                   mHandler.onMotionChanged(MOTION_STOP);
                   mLastMotion = MOTION_STOP;
               } else if (Math.abs(y) < 2.5f && x > 8.5f && z > -3.0f && z < 4.0f
                       && mLastMotion != MOTION_BACKWARD) {
                   mHandler.onMotionChanged(MOTION_BACKWARD);
                   mLastMotion = MOTION_BACKWARD;
               } else if (y <= -2.5f && mLastMotion != MOTION_LEFT) {
                   mHandler.onMotionChanged(MOTION_LEFT);
                   mLastMotion = MOTION_LEFT;
               } else if (y >= 2.5f && mLastMotion != MOTION_RIGHT) {
                   mHandler.onMotionChanged(MOTION_RIGHT);
                   mLastMotion = MOTION_RIGHT;
               }
           }

           //  |y| < 2.8
           //Forward -3.0 < x < 3.0,   9.0 < z
           // stop   3.0 <= x <= 8.5  4.0 <= z <= 9.0;
           // back    8.5 < x < 10    -3.0 < z < 4.0
           // left    y < -2.5
       }
    }
}

interface MotionChangeHandler {
    void onMotionChanged(int motion);
}

interface MotionChangeHandler2 {
    void onMotionChanged(String motion);
}


class MotionDetectorTest {

    private static final String TAG = "MotionDetectorTest";
    private SensorManager mSensorManager;
    private Sensor mGyroSensor;
    private Sensor mAccSensor;
    private SensorListener mGyroListener, mAccListener;
    private static final float DEFAULT_SENSITIVITY = 0.1f;


    public MotionDetectorTest(Context sys){
        mSensorManager = (SensorManager) sys.getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (mGyroSensor == null){
            Log.e(TAG, "GyroSensor is null");
        }
    }

    public void registerGyroscope(MotionChangeHandler2 handler){
        mGyroListener = new SensorListener("Gyroscope", handler);
        mSensorManager.registerListener(mGyroListener, mGyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void registerAccelerometer(MotionChangeHandler2 handler){
        mAccListener = new SensorListener("Accelerometer", handler);
        mSensorManager.registerListener(mAccListener, mAccSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister(){
        if (mAccListener != null) mSensorManager.unregisterListener(mAccListener);
        if (mGyroListener != null) mSensorManager.unregisterListener(mGyroListener);
    }

    private class SensorListener implements SensorEventListener {
        private MotionChangeHandler2 mHandler;
        private String mSensorName;
        private float[] mDataBuffer;
        private StringBuilder mStrBuilder;

        SensorListener(String sensorName, MotionChangeHandler2 handler){
            mSensorName = sensorName;
            mHandler = handler;
            mDataBuffer = new float[3];
            mStrBuilder = new StringBuilder();
        }

        @Override
        public void onSensorChanged(SensorEvent event){
            boolean update = false;

            for(int i = 0; i < 3; i++){
                if (Math.abs(event.values[i] - mDataBuffer[i]) > DEFAULT_SENSITIVITY) {
                    mDataBuffer[i] = event.values[i];
                    update = true;
                }
            }

            if (update) {
                mStrBuilder.setLength(0);
                mStrBuilder.append(mSensorName);
                mStrBuilder.append(":");
                mStrBuilder.append("\n\tx: ");
                mStrBuilder.append(event.values[0]);
                mStrBuilder.append("\n\ty: ");
                mStrBuilder.append(event.values[1]);
                mStrBuilder.append("\n\tz: ");
                mStrBuilder.append(event.values[2]);
                mHandler.onMotionChanged(mStrBuilder.toString());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}
