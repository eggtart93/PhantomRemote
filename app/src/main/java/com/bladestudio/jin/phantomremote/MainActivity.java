package com.bladestudio.jin.phantomremote;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PhantomRemote";
    private TextView mDisplayMsg, mGyroDataView, mAccDataView;
    private EditText mEditAddress, mEditPort;
    private MotionDetectorTest mMotionDetectorTest;
    private boolean isDetectorEnabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mEditAddress = (EditText) findViewById(R.id.edit_address);
        mEditPort = (EditText) findViewById(R.id.edit_port);
        mDisplayMsg = (TextView) findViewById(R.id.message_textview);

        mGyroDataView = (TextView) findViewById(R.id.accel_data_view);
        mAccDataView = (TextView) findViewById(R.id.gyro_data_view);

        isDetectorEnabled = false;

        Button connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayMsg.setText(getResources().getString(R.string.main_ui_text_status_connecting));
                mDisplayMsg.setTextColor(Color.BLUE);
                MainActivity.this.findViewById(android.R.id.content).invalidate();

                String host = mEditAddress.getText().toString();
                int port = Integer.parseInt(mEditPort.getText().toString());

                if (host.length() == 0 || port <= 0) {
                    mDisplayMsg.setText(getResources().getString(R.string.main_ui_text_error_ip_port_empty));
                    mDisplayMsg.setTextColor(Color.RED);
                    return;
                }

                //String host = TcpClient.getInstance().getSocket().getLocalSocketAddress()
                if (TcpClient.getInstance().getConnectionFailureHandler() == null) {
                    TcpClient.getInstance().setConnectionFailureHandler(new TcpClient.ConnectionFailureHandler() {
                        @Override
                        public void onConnectionFailure(String info) {
                            mDisplayMsg.setText(info);
                            mDisplayMsg.setTextColor(Color.RED);
                        }
                    });
                }
                if (TcpClient.getInstance().isConnected() || TcpClient.getInstance().connect(host, port) ) {
                    startActivity(new Intent(MainActivity.this, ControllerUI.class));
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isDetectorEnabled) {
                    if (mMotionDetectorTest == null) {
                        mMotionDetectorTest = new MotionDetectorTest(MainActivity.this);
                    }
                    mMotionDetectorTest.registerAccelerometer(new MotionChangeHandler2() {
                        @Override
                        public void onMotionChanged(String data) {
                            mAccDataView.setText(data);
                        }
                    });

                    mMotionDetectorTest.registerGyroscope(new MotionChangeHandler2() {
                        @Override
                        public void onMotionChanged(String data) {
                            mGyroDataView.setText(data);
                        }
                    });
                } else {
                    mMotionDetectorTest.unregister();
                    mAccDataView.setText("");
                    mGyroDataView.setText("");
                }

                isDetectorEnabled = !isDetectorEnabled;
                /*
                mDisplayMsg.setText("");
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "onResume()");
        Log.d(TAG, "Thread.activeCount(): " + Thread.activeCount());
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onPause()");
        if (isDetectorEnabled && mMotionDetectorTest != null) {
            mMotionDetectorTest.unregister();
            isDetectorEnabled = false;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
        mDisplayMsg.setText("");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}

