package com.bladestudio.jin.phantomremote;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/*
import android.view.Menu;
import android.view.MenuItem;
*/

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PhantomRemote";
    private TextView mDisplayMsg;
    private EditText mEditAddress, mEditPort;
    private Button mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        mEditAddress = (EditText) findViewById(R.id.edit_address);
        mEditPort = (EditText) findViewById(R.id.edit_port);
        mDisplayMsg = (TextView) findViewById(R.id.message_textview);
        mConnectButton = (Button) findViewById(R.id.connect_button);

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayMsg.setText("Connecting...");
                mDisplayMsg.setTextColor(Color.BLUE);

                String host = mEditAddress.getText().toString();
                int port = Integer.parseInt(mEditPort.getText().toString());

                if (host.length() == 0 || port <= 0) {
                    mDisplayMsg.setText("Host name/IP or Port number cannot be empty");
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
                mDisplayMsg.setText("");
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_phantom_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
