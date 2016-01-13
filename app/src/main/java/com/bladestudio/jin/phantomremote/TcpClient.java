package com.bladestudio.jin.phantomremote;

import android.util.Log;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    private MessageReceivedHandler<Packet> mMessageHandler;
    private ConnectionLostHandler mConnectionLostHandler;
    private ConnectionFailureHandler mConnectionFailureHandler;
    private boolean mRun;
    private DataInputStream mIn;
    private DataOutputStream mOut;

    /* Private constructor prevents initialization from other classes */
    private TcpClient() {
        // Creates an unconnected socket object
        mSocket = null;
        mMessageHandler = null;
        mConnectionLostHandler = null;
        mConnectionFailureHandler = null;
        mRun = false;
        mIn = null;
        mOut = null;
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

    public void sendMessage(Packet message) throws IOException{
        Log.d(TAG, "sendMessage(Packet)");

        Assert.assertNotNull("mOut is null", mOut);
        Assert.assertNotNull("Packet is null", message);
        Assert.assertTrue("Negative packet data length", message.mDataLength >= 0);

        mOut.writeShort(message.mDataLength);
        mOut.writeByte(message.mType);
        mOut.write(message.mData);
        mOut.flush();
    }


    public Packet receive() throws IOException{
        Log.d(TAG, "receive()");
        Assert.assertNotNull("mIn (DataInputStream) is null", mIn);

        Packet packet = new Packet(mIn.readShort());
        Log.d(TAG, "Data Length = " + packet.mDataLength);
        packet.mType = mIn.readByte();
        Log.d(TAG, "Packet Type = " + packet.mType);

        if (packet.mDataLength > 0) {
            int nBytes = mIn.read(packet.mData);
            Log.d(TAG, "read " + nBytes + " bytes");
        }
        return packet;
    }

    public void run() {
        Log.d(TAG, "run()");

        Assert.assertNotNull("Socket Object is null, make sure the connection "
                + "to server is established before invoking run()", mSocket);

        Assert.assertNotNull("MessageReceivedHandler is null", mMessageHandler);

        try {
            mIn = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
            mOut = new DataOutputStream((new BufferedOutputStream(mSocket.getOutputStream())));
            mRun = true;

            while (mRun) {
                if (isConnected()) {
                    Packet packet = receive();
                    mMessageHandler.onMessageReceived(packet);
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

        if (mIn != null) {
            try {
                mIn.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        if (mOut != null) {
            try{
                mOut.flush();
                mOut.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        mSocket = null;
        mIn = null;
        mOut = null;
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
        if (mSocket != null) {
            Log.d(TAG, "mSocket.getLocalAddress().getHostAddress()" + mSocket.getLocalAddress().getHostAddress());
            Log.d(TAG, "mSocket.getLocalAddress().getHostName()" + mSocket.getLocalAddress().getHostName());
            //Log.d(TAG, "mSocket.getLocalSocketAddress()" + mSocket.getLocalSocketAddress());
        }
    }

    public interface MessageReceivedHandler <MessageType> {
        void onMessageReceived(MessageType message);
    }

    public interface ConnectionLostHandler {
        void onConnectionLost(String info);
    }

    public interface ConnectionFailureHandler{
        void onConnectionFailure(String info);
    }
}
