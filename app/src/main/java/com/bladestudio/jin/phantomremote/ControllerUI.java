package com.bladestudio.jin.phantomremote;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import java.io.IOException;

/**
 * Created by Jin on 2016/1/5.
 */
public class ControllerUI extends Activity {

    private static final String TAG = "ControllerUI";
    static final String INVALID_RESPONSE = "Invalid Response";
    static final String CONNECTION_ERROR = "Connection Error";
    static final String DEBUG_MESSAGE = "Debug Message";

    private StringBuilder mStrBuilder;
    private TextView mStatusTextView, mResponseTextView, mMotionTextView;
    private Switch mSensorEnable;
    private TcpClient mTcpClient;
    private Controller mController;
    private MotionDetector mMotionDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setContentView(R.layout.controller_ui);

        Button forwardButton = (Button) findViewById(R.id.forward_button);
        Button reverseButton = (Button) findViewById(R.id.reverse_button);
        Button leftButton = (Button) findViewById(R.id.left_button);
        Button rightButton = (Button) findViewById(R.id.right_button);
        Button stopButton = (Button) findViewById(R.id.stop_button);

        mStatusTextView = (TextView) findViewById(R.id.status_textview);
        mResponseTextView = (TextView) findViewById(R.id.response_textview);
        mMotionTextView = (TextView) findViewById(R.id.motion_textview);

        mSensorEnable = (Switch) findViewById(R.id.sensor_enable);
        mStrBuilder = new StringBuilder();

        mSensorEnable.setChecked(false);
        mSensorEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mMotionDetector != null) {
                    if (isChecked) mMotionDetector.register();
                    else mMotionDetector.unregister();
                }
            }
        });

        mMotionDetector = new MotionDetector(this);

        mMotionDetector.setHandler(new MotionChangeHandler() {
            @Override
            public void onMotionChanged(int motion) {
                byte signal;
                String text;

                switch (motion) {
                    case MotionDetector.MOTION_FORWARD:
                        signal = Controller.FORWARD_SIGNAL;
                        text = "Forward";
                        break;
                    case MotionDetector.MOTION_BACKWARD:
                        signal = Controller.REVERSE_SIGNAL;
                        text = "Backward";
                        break;
                    case MotionDetector.MOTION_STOP:
                        signal = Controller.STOP_SIGNAL;
                        text = "Stop";
                        break;
                    default:
                        signal = Controller.DEBUG_INT;
                        text = "";
                        break;
                }

                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(signal));
                        mMotionTextView.setText(text);
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.FORWARD_SIGNAL));
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.REVERSE_SIGNAL));
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.LEFT_SIGNAL));
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.RIGHT_SIGNAL));
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null && mController != null) {
                    try {
                        mTcpClient.sendMessage(mController.generatePacket(Controller.STOP_SIGNAL));
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        mController = new Controller();
        new ServerMonitorTask().execute("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        Log.d(TAG, "Thread.activeCount(): " + Thread.activeCount());
        if (mSensorEnable.isChecked()) mMotionDetector.register();
        mStatusTextView.setText(getResources().getString(R.string.ctr_ui_text_status_connected));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        if (mSensorEnable.isChecked()) {
            mMotionDetector.unregister();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mTcpClient != null) {
            mTcpClient.close();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private class ServerMonitorTask extends AsyncTask<String, String, TcpClient> {
        private static final String TAG = "ServerMonitorTask";

        @Override
        protected TcpClient doInBackground(String... messages) {
            Log.d(TAG, "doInBackground()");

            mTcpClient = TcpClient.getInstance();
            mTcpClient.setMessageReceivedHandler(new TcpClient.MessageReceivedHandler<Packet>() {
                @Override
                public void onMessageReceived(Packet message) {
                    String[] progress = mController.processPacket(message);
                    publishProgress(progress);
                }
            });

            mTcpClient.setConnectionLostHandler(new TcpClient.ConnectionLostHandler() {
                @Override
                public void onConnectionLost(String info) {
                    String[] progress = {CONNECTION_ERROR, info};
                    publishProgress(progress);
                    mTcpClient.close();
                }
            });

            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.d(TAG, "onProgressUpdate()");

            Assert.assertTrue(progress.length == 2);

            switch (progress[0]) {
                case DEBUG_MESSAGE:
                    Assert.assertNotNull("mStrBuilder is null", mStrBuilder);

                    mStrBuilder.setLength(0);
                    mStrBuilder.append("Received");
                    mStrBuilder.append(": ");
                    mStrBuilder.append(progress[1]);
                    mResponseTextView.setText(mStrBuilder.toString());
                    break;

                case INVALID_RESPONSE:
                case CONNECTION_ERROR:
                    //mStatusTextView.setText(progress[1]);
                    //mStatusTextView.setTextColor(Color.RED);
                    Toast.makeText(getApplicationContext(), progress[1], Toast.LENGTH_LONG).show();
                    break;
                default:
                    Log.e(TAG, "Error progress type");
            }

        }

        @Override
        protected void onPostExecute(TcpClient result) {
            Log.d(TAG, "onPostExecute");
            ControllerUI.this.finish();
        }
    }
}

