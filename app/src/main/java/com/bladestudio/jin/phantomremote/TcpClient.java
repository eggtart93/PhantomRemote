package com.bladestudio.jin.phantomremote;

import android.util.Log;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.IllegalBlockingModeException;

/**
 * Created by @author J.K. Zhou <zhoujk@mcmaster.com> on Date 2016/1/5.
 */
public class TcpClient {
    /*
        This is a singleton class which encapsulates an instance of Socket class,
        thus network connection can be shared among threads or activities in this
        application.
     */

    private static final String TAG = "TcpClient";
    private static final int TIMEOUT = 5000; // Default timeout 5 sec
    private Socket mSocket;
    private MessageReceivedHandler mMessageHandler;
    private ConnectionLostHandler mConnectionLostHandler;
    private ConnectionFailureHandler mConnectionFailureHandler;
    private boolean mRun;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;


    // Private constructor prevents initialization from other classes
    private TcpClient() {
        // Creates an unconnected socket object
        mSocket = null;
        mMessageHandler = null;
        mConnectionLostHandler = null;
        mConnectionFailureHandler = null;
        mRun = false;
        mBufferOut = null;
        mBufferIn = null;
    }

    private static class ConnectionHolder {
        /*
            Initialization-On-Demand Holder

            {@link ConnectionHolder} is loaded on the first execution of
         */
        private static final TcpClient mConnection = new TcpClient();
    }

    public static TcpClient getInstance(){
        return ConnectionHolder.mConnection;
    }

    public void setMessageReceivedHandler(MessageReceivedHandler handler){
        mMessageHandler = handler;
    }

    public void setConnectionLostHandler(ConnectionLostHandler handler){
        mConnectionLostHandler = handler;
    }

    public void setConnectionFailureHandler(ConnectionFailureHandler handler){
        mConnectionFailureHandler = handler;
    }

    public ConnectionFailureHandler getConnectionFailureHandler() {
        return mConnectionFailureHandler;
    }

    public ConnectionLostHandler getConnectionLostHandler() {
        return mConnectionLostHandler;
    }

    public MessageReceivedHandler getMessageHandler() {
        return mMessageHandler;
    }

    public boolean connect(String dstHost, int dstPort){
        Log.d(TAG, "Connects to <" + dstHost + "> <" + dstPort + ">");

        boolean connected = true;
        try {
            mSocket = new Socket();
            // set socket read timeout
            // mSocket.setSoTimeout(TIMEOUT);
            // connect to server with specified timeout
            mSocket.connect(new InetSocketAddress(dstHost, dstPort), TIMEOUT);
            printLocalInfo();
        }catch(SocketTimeoutException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            mConnectionFailureHandler.onConnectionFailure("Socket Timeout");
            connected = false;
        }catch(IllegalBlockingModeException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            mConnectionFailureHandler.onConnectionFailure("Illegal Blocking Mode");
            connected = false;
        }catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            mConnectionFailureHandler.onConnectionFailure("Unknown Host");
            connected = false;
        }catch (ConnectException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            mConnectionFailureHandler.onConnectionFailure("No route to host");
            connected = false;
        }catch (IOException e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            mConnectionFailureHandler.onConnectionFailure("Unknown Error");
            connected = false;
        }
        return connected;
    }

    public void sendMessage(String message){
        Log.d(TAG, "sendMessage(" + message + ")");

        Assert.assertNotNull(mBufferOut);
        if (!mBufferOut.checkError()) {
            mBufferOut.print(message);
            mBufferOut.flush();
        }else {
            Log.e(TAG, "mBufferOut.checkError() returns true");
            mConnectionLostHandler.onConnectionLost("Lost connection to server");
        }
    }

    public void send(){

    }

    public void run() {
        Log.d(TAG, "run()");

        Assert.assertNotNull("Socket Object is null, make sure the connection "
                        + "to server is established before invoking run()", mSocket);

        Assert.assertNotNull("MessageReceivedHandler is null", mMessageHandler);

        try {
            mBufferOut = new PrintWriter(new BufferedWriter( new OutputStreamWriter(
                    mSocket.getOutputStream())), true);
            mBufferIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mRun = true;

            while (mRun) {
                if (isConnected()) {
                    String msgReceived = mBufferIn.readLine();
                    if (msgReceived != null) {
                        Log.i(TAG, "[Server]: " + msgReceived);
                        mMessageHandler.onMessageReceived(msgReceived);
                    }else {
                        Log.d(TAG, "msgReceived = null");
                    }
                } else {
                    mConnectionLostHandler.onConnectionLost("socket is not connected");
                }
            }
        } catch(SocketException e){
            Log.d(TAG, e.getMessage());
            if (isConnected()) { close(); }
        } catch (IOException e){
            e.printStackTrace();
            if (isConnected()) { close(); }
        }
    }

    public void close() {
        Log.d(TAG, "close()");

        /*
            TO-DO:
            Notify the server that this client is closing
         */

        mRun = false;
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        if (mBufferOut != null){
            mBufferOut.flush();
            mBufferOut.close();
        }

        mSocket = null;
        mBufferIn = null;
        mBufferOut = null;
        mConnectionFailureHandler = null;
        mConnectionLostHandler = null;
        mMessageHandler = null;
    }

    public boolean isConnected(){
        return mSocket != null && mSocket.isConnected();
    }

    public Socket getSocket() {
        return mSocket;
    }


    /*
        For debug purpose only, remove this later
    */
    private void printLocalInfo() {

/*            if (mSocket.getLocalSocketAddress() == null){
            Log.d(TAG, "mSocket.getLocalSocketAddress() == null");
        }*/
        /*Log.d(TAG, "InetAddress.getLocalHost().getHostName()" + InetAddress.getLocalHost().getHostName());
        Log.d(TAG, "InetAddress.getLocalHost().getHostAddress()" + InetAddress.getLocalHost().getHostAddress());
        Log.d(TAG, "Inet4Address.getLocalHost().getHostName()" + Inet4Address.getLocalHost().getHostName());
        Log.d(TAG, "Inet4Address.getLocalHost().getHostAddress()" + Inet4Address.getLocalHost().getHostAddress());*/
        if (mSocket != null) {
            Log.d(TAG, "mSocket.getLocalAddress().getHostAddress()" + mSocket.getLocalAddress().getHostAddress());
            Log.d(TAG, "mSocket.getLocalAddress().getHostName()" + mSocket.getLocalAddress().getHostName());
            //Log.d(TAG, "mSocket.getLocalSocketAddress()" + mSocket.getLocalSocketAddress());
        }

    }

    public interface MessageReceivedHandler {
        void onMessageReceived(String message);
    }

    public interface ConnectionLostHandler {
        void onConnectionLost(String info);
    }

    public interface ConnectionFailureHandler{
        void onConnectionFailure(String info);
    }
}
