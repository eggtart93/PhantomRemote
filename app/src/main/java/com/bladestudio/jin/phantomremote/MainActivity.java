package com.bladestudio.jin.phantomremote;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

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
                int port;
                try{
                    port = Integer.parseInt(mEditPort.getText().toString());
                } catch (NumberFormatException e){
                    Log.e(TAG, "Entered port number has Invalid format");
                    mDisplayMsg.setText(getResources().getString(R.string.main_ui_text_error_invalid_port_format));
                    mDisplayMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorLightPink));
                    return;
                }
                new ConnectionTask(mEditAddress.getText().toString(), port)
                        .execute(5000);
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

    private class ConnectionTask extends AsyncTask<Integer, Void, Boolean> {
        private String mHost, mErrorStr;
        private int mPort;

        ConnectionTask(String ip, int port){
            mHost = ip;
            mPort = port;
        }

        @Override
        protected void onPreExecute(){
            if (mHost.length() == 0 || mPort <= 0) {
                mDisplayMsg.setText(getResources().getString(R.string.main_ui_text_error_ip_port_empty));
                mDisplayMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorLightPink));
                cancel(true);
            } else {
                mDisplayMsg.setText(getResources().getString(R.string.main_ui_text_status_connecting));
                mDisplayMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorLightBlue));
            }
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            TcpClient.getInstance().setConnectionFailureHandler(new TcpClient.ConnectionFailureHandler() {
                @Override
                public void onConnectionFailure(String info) {
                    mErrorStr = info;
                }
            });

            return TcpClient.getInstance().connect(mHost, mPort, params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result){
            if (result){
                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_success_connect_completed),
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 100);
                toast.show();
                startActivity(new Intent(MainActivity.this, ControllerUI.class));
            } else {
                mDisplayMsg.setText(mErrorStr);
                mDisplayMsg.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorLightPink));
            }
        }
    }
}

