package com.bladestudio.jin.phantomremote;

import android.util.Log;

/**
 * Created by @author J.K. Zhou <zhoujk@mcmaster.com> on Date 2016/1/9.
 */
public class Controller {

    private static final String TAG = "Controller";

    public static final byte FORWARD_SIGNAL = (byte) 0xF0;
    public static final byte REVERSE_SIGNAL = (byte) 0xF1;
    public static final byte LEFT_SIGNAL = (byte) 0xF2;
    public static final byte RIGHT_SIGNAL = (byte) 0xF3;
    public static final byte SHUTDOWN_SIGNAL = (byte) 0xFF;

    public static final byte DEBUG_TEXT = (byte) 0x10;
    public static final byte DEBUG_INT = (byte) 0x11;

    public Packet generatePacket(byte signalType) {
        Packet packet = null;

        switch (signalType){
            case FORWARD_SIGNAL:
                packet = new Packet(FORWARD_SIGNAL, "Forward");
                break;
            case REVERSE_SIGNAL:
                packet = new Packet(REVERSE_SIGNAL, "Reverse");
                break;
            case LEFT_SIGNAL:
                packet = new Packet(LEFT_SIGNAL, "Left");
                break;
            case RIGHT_SIGNAL:
                packet = new Packet(RIGHT_SIGNAL, "Right");
                break;
            case SHUTDOWN_SIGNAL:
                packet = new Packet(SHUTDOWN_SIGNAL, "Shutdown");
                break;
            default:
                Log.e(TAG, "Unknown signal type");
        }
        return packet;
    }

    public String[] processPacket(Packet packet) {
        String[] uiResponse = new String[2];
        if (packet == null){
            uiResponse[0] = ControllerUI.INVALID_RESPONSE;
            uiResponse[1] = "Invalid Packet (null)";
        }
        switch (packet.mType) {
            case DEBUG_TEXT:
                uiResponse[0] = ControllerUI.DEBUG_MESSAGE;
                uiResponse[1] = new String(packet.mData);
                break;
            case DEBUG_INT:
                uiResponse[0] = ControllerUI.DEBUG_MESSAGE;
                uiResponse[1] = String.valueOf(Util.bytesToInt(packet.mData));
                break;
            default:
                Log.e(TAG, "Unknown Packet Type");
                uiResponse[0] = ControllerUI.INVALID_RESPONSE;
                uiResponse[1] = "Unknown Packet Type";
                break;
        }
        return uiResponse;
    }
}
