package com.mixedtek.elastics.player.decoder;

import android.media.MediaCodec;
import android.util.Log;

import java.nio.ByteBuffer;

class AudioElement {
    public byte mData[];
    public int mOffset;
    public int mLength;
    public long mTimestamp;
    public AudioElement(int size) {
        mData = new byte[size];
        mOffset = 0;
        mLength = 0;
        mTimestamp = 0;
    }
}

public class AudioBuffer {
    private static final String TAG = "AudioBuffer";
    private AudioElement mBuffer[];
    private int mWidth;
    private int mHeight;
    private int mSize;
    private int mCapacity;
    private int mHead;
    private int mTail;

    public AudioBuffer(int capacity, int size) {
        mBuffer = new AudioElement[capacity];
        for(int i=0; i<capacity; i++) {
            mBuffer[i] = new AudioElement(size);
        }
        mSize = size;
        mCapacity = capacity;
        mHead = -1;
        mTail = -1;
    }

    public synchronized void empty() {
        mHead = -1;
        mTail = -1;
    }

    public synchronized boolean isEmpty() {
        return mHead == mTail;
    }

    public synchronized boolean isFull() {
        int tmp = (mTail  + 1) % mCapacity;
        return (tmp == mHead);
    }

    public synchronized boolean push(byte[] data, int offset, int length, long ts) {
        if(isFull()) {
            mHead = -1;
            mTail = -1;
        }

        mTail = (++mTail % mCapacity);
        mBuffer[mTail].mLength = length;
        System.arraycopy(data, 0, mBuffer[mTail].mData, 0, data.length);
        mBuffer[mTail].mOffset = offset;
        mBuffer[mTail].mTimestamp = ts;
        return true;
    }

    public synchronized AudioElement pop() {
        if( mHead == mTail) {
            return null;
        }
        mHead = (++mHead % mCapacity);

        AudioElement ae = new AudioElement(mSize);
        ae.mLength = mBuffer[mHead].mLength;
        System.arraycopy(mBuffer[mHead].mData, 0, ae.mData, 0, ae.mData.length);
        ae.mOffset = mBuffer[mHead].mOffset;
        ae.mTimestamp = mBuffer[mHead].mTimestamp;
        return ae;
    }
    /*
    public AudioElement front() {
        if( mHead == mTail) {
            return null;
        }
        int tmp = mHead + 1;
        tmp = (tmp % mCapacity);
        return mBuffer[tmp];
    }

    public void pop() {
        if( mHead == mTail) {
            return;
        }
        mHead = (++mHead % mCapacity);
    }
    */
}
