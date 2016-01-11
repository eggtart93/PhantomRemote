package com.bladestudio.jin.phantomremote;

import junit.framework.Assert;

import java.nio.ByteBuffer;

/**
 * Created by @author J.K. Zhou <zhoujk@mcmaster.com> on Date 2016/1/10.
 */
public class Util {
    /*
    Below are some helper methods for converting byte array
    to int and short and vice versa
 */
    public static int bytesToInt(byte[] bytes) {
        Assert.assertEquals(bytes.length, 4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static short bytesToShort(byte[] bytes) {
        Assert.assertEquals(bytes.length, 2);
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static byte[] toBytes(int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    public static byte[] toBytes(short num) {
        return ByteBuffer.allocate(2).putShort(num).array();
    }

    public static byte[] toBytes(String str) {
        return str.getBytes();
    }
}
