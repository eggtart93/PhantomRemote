package com.bladestudio.jin.phantomremote;

import junit.framework.Assert;

import java.nio.ByteBuffer;

/**
 * Created by @author J.K. Zhou <zhoujk@mcmaster.com> on Date 2016/1/10.
 */
public class Packet {

    /*
        PACKET FORMAT:
            | Data Length  | Type   | Data        |
            | 2 bytes      | 1 byte | 1-255 bytes |
     e.g
    */

    private static final short DEFAULT_DATA_LENGTH = 1024;

    short mDataLength;
    byte mType;
    byte[] mData;

    public Packet() {
        mType = 0;
        mDataLength = 0;
        mData = new byte[DEFAULT_DATA_LENGTH];
    }

    public Packet(short size){
        mType = 0;
        mDataLength = size;
        mData = new byte[size];
    }

    public Packet(byte type, int data) {
        mType = type;
        mData = Util.toBytes(data);
        mDataLength = (short) mData.length;
    }

    public Packet(byte type, String data) {
        mType = type;
        mData = Util.toBytes(data);
        mDataLength = (short) mData.length;
    }
}
