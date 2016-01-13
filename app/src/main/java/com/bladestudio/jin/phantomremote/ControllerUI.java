package com.bladestudio.jin.phantomremote;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import junit.framework.Assert;

import java.io.IOException;

/**
 * Created by Jin on 2016/1/5.
 */
public class ControllerUI extends Activity {

    private static final String TAG = "ControllerUI";
    static final String INVALID_RESPONSE = "Invalid Response";
    static final String DEBUG_MESSAGE = "Debug Message";


    private StringBuilder mStrBuilder;
    private TextView mStatusTextView, mResponseTextView;
    private Button mForwardButton, mReverseButton, mLeftButton, mRightButton, mStopButton;
    private SensorManager mSensorManager;
    private AccelerometerListener mAccListener;
    private TcpClient mTcpClient;
    private Controller mController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");


        setContentView(R.layout.controller_ui);
        mStatusTextView = (TextView) findViewById(R.id.status_textview);
        mResponseTextView = (TextView) findViewById(R.id.response_textview);
        mForwardButton = (Button) findViewById(R.id.forward_button);
        mReverseButton = (Button) findViewById(R.id.reverse_button);
        mLeftButton = (Button) findViewById(R.id.left_button);
        mRightButton = (Button) findViewById(R.id.right_button);
        mStopButton = (Button) findViewById(R.id.stop_button);

        mStrBuilder = new StringBuilder();

        //mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //mAccListener = new AccelerometerListener();

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.FORWARD_SIGNAL));
                    }catch (IOException e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        mReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.REVERSE_SIGNAL));
                    }catch (IOException e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.LEFT_SIGNAL));
                    }catch (IOException e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.RIGHT_SIGNAL));
                    }catch (IOException e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.STOP_SIGNAL));
                    }catch (IOException e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        //mStatusTextView.setText("Status: Connected To Server");
        mController = new Controller();
        new ServerMonitorTask().execute("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        Log.d(TAG, "Thread.activeCount(): " + Thread.activeCount());
       /* mSensorManager.registerListener(mAccListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);*/
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onPause()");
        //mSensorManager.unregisterListener(mAccListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mTcpClient != null) {
            mTcpClient.close();
        }
    }

    private class AccelerometerListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (mStrBuilder != null) {
                mStrBuilder.setLength(0);
                mStrBuilder.append("\n\tx: ");
                mStrBuilder.append(event.values[0]);
                mStrBuilder.append(",\n\ty: ");
                mStrBuilder.append(event.values[1]);
                mStrBuilder.append(",\n\tz: ");
                mStrBuilder.append(event.values[2]);
                //mTextView.setText(mStrBuilder.toString());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class ServerMonitorTask extends AsyncTask<String, String, TcpClient> {
        private static final String TAG = "ServerMonitorTask";

        @Override
        protected TcpClient doInBackground(String... messages){
            Log.d(TAG, "doInBackground()");

            mTcpClient = TcpClient.getInstance();
            mTcpClient.setMessageReceivedHandler(new TcpClient.MessageReceivedHandler<Packet>() {
                @Override
                public void onMessageReceived(Packet message) {
                    String[] progress = mController.processPacket(message);
                    //String[] progress = {"onMessageReceived", message};
                    publishProgress(progress);
                }
            });

            mTcpClient.setConnectionLostHandler(new TcpClient.ConnectionLostHandler() {
                @Override
                public void onConnectionLost(String info) {
                    String[] progress = {"onConnectionLost", info};
                    publishProgress(progress);
                    mTcpClient.close();
                }
            });

            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress){
            Log.d(TAG, "onProgressUpdate()");

            Assert.assertTrue(progress.length == 2);

            switch (progress[0]){
                case DEBUG_MESSAGE:
                    Assert.assertNotNull("mStrBuilder is null", mStrBuilder);

                    mStrBuilder.setLength(0);
                    mStrBuilder.append("Message Received");
                    mStrBuilder.append(": ");
                    mStrBuilder.append(progress[1]);
                    mResponseTextView.setText(mStrBuilder.toString());
                    break;

                case INVALID_RESPONSE:
                    mStatusTextView.setText(progress[1]);
                    mStatusTextView.setTextColor(Color.RED);
                    break;
                default:
                    Log.e(TAG, "Error progress type");
            }

        }

        @Override
        protected void onPostExecute(TcpClient result){
            Log.d(TAG, "onPostExecute");
        }
    }
}
